package com.gdu.gdusocketdemo.usb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.gdu.gdudecoder.GduDecoder;
import com.gdu.gdusocket.GduCommunication;
import com.gdu.gdusocket.GduSocketManager;
import com.gdu.gdusocket.SocketCallBack;
import com.gdu.gdusocket.ce.IGduSocket;
import com.gdu.gdusocketdemo.APTestActivity;
import com.gdu.gdusocketdemo.PreViewActivity;
import com.gdu.gdusocketdemo.R;
import com.gdu.gdusocketmodel.ConnStateEnum;
import com.gdu.gdusocketmodel.ConnType;
import com.gdu.gdusocketmodel.GduFrame;
import com.gdu.gdusocketmodel.GduSocketConfig;
import com.gdu.gdusocketmodel.GlobalVariable;
import com.gdu.remotecontrol.main.GduRCManager;
import com.gdu.remotecontrol.main.IRCManager;

/**
 * Created by zhangzhilai on 2018/1/24.
 */

public class USBActivity extends Activity {

    private GduRCManager mGduRCManager;
    private GduSocketManager mGduSocketManager;
    private GduDecoder mGduDecoder;
    private GduCommunication mGduCommunication;

    private TextView mInfoTextView;

    private TextView mConnTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        mGduRCManager.setOnRCManagerListener(new IRCManager.OnRCManagerListener.RCListenerAdapter(){
            @Override
            public void onAOACheck(String model) {
                super.onAOACheck(model);
                showText(mInfoTextView, model);
            }

            @Override
            public void onUsbOpen() {
                mGduCommunication.reSetInetAdd(GduSocketConfig.RC_IP_ADDRESS, GduSocketConfig.REMOTE_UDP_PORT);
                GlobalVariable.RC_usb_hadConn = 1;
                GlobalVariable.connType = ConnType.MGP03_RC_USB;
                showText(mConnTextView, "openUsbModel");
            }

            @Override
            public void onUsbClose() {
                super.onUsbClose();
                mGduCommunication.reSetInetAdd(GduSocketConfig.IP_ADDRESS, GduSocketConfig.REMOTE_UDP_PORT);
                GlobalVariable.RC_usb_hadConn = 1;
                GlobalVariable.connType = ConnType.MGP03_NONE;
            }
        });
        mGduSocketManager.setConnectCallBack(new IGduSocket.OnConnectListener() {
            @Override
            public void onConnect() {
                showText(mConnTextView, "client开启");
            }

            @Override
            public void onDisConnect() {

            }

            @Override
            public void onConnectDelay(boolean isDelay) {

            }
        });
    }

    private void showText(final TextView textView, final String content){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(content);
            }
        });
    }

    private void initData() {
        mGduRCManager = GduRCManager.getInstance(this);
        mGduSocketManager = GduSocketManager.getInstance(this);
        mGduCommunication = mGduSocketManager.getCommunication();
        mGduDecoder = new GduDecoder();
    }

    private void initView() {
        mInfoTextView = (TextView) findViewById(R.id.info_textview);
        mConnTextView = (TextView) findViewById(R.id.conn_textview);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void aoaCheck(View view) {
        mGduRCManager.startCheck();
    }

    public void wifiTest(View view) {
        Intent intent = new Intent(this, APTestActivity.class);
        startActivity(intent);
    }


    public void apTest(View view) {
//        final String getPicList_usb = "http://127.0.0.1:8002/goform/WifiChannel";
        final String getPicList_usb = "http://127.0.0.1:8002/goform/WifiChannel";
        new Thread(new Runnable() {
            @Override
            public void run() {
                String test = APTest.sendGet(getPicList_usb);
                showText(mInfoTextView, test);
                System.out.println("test ::" + test);
            }
        }).start();

    }

    public void closeConnect(View view) {
        mGduRCManager.closeConnect();
        GlobalVariable.connStateEnum = ConnStateEnum.Conn_None;
    }

    public void CSTest(View view) {
        Intent intent = new Intent(this, PreViewActivity.class);
        startActivity(intent);
    }

    public void openClient(View view) {
        mGduSocketManager.reInitIp("127.0.0.1");
        mGduSocketManager.startConnect();
    }
}
