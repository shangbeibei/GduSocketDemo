package com.gdu.gdusocketdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gdu.gdudecoder.GduDecoder;
import com.gdu.gdusocket.GduSocketManager;
import com.gdu.gdusocket.SocketCallBack;
import com.gdu.gdusocket.ce.IGduSocket;
import com.gdu.gdusocketmodel.GduFrame;
import com.gdu.util.DataUtil;

/**
 * Created by zhangzhilai on 2017/12/13.
 */

public class WifiConnectActivity extends Activity {

    private GduSocketManager mGduSocketManager;

    private TextView mConnectTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_connect);
        initView();
        initSocket();
    }

    private void initView() {
        mConnectTextView = (TextView) findViewById(R.id.conn_textview);
    }

    private void initSocket() {
        mGduSocketManager = GduSocketManager.getInstance(this);
        mGduSocketManager.setConnectCallBack(new IGduSocket.OnConnectListener() {
            @Override
            public void onConnect() {
                showConnect("连接");
            }

            @Override
            public void onDisConnect() {
                showConnect("未连接");
            }

            @Override
            public void onConnectDelay(boolean isDelay) {
                showConnect("延迟");
            }
        });
    }

    private void showConnect(final String connect){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectTextView.setText(connect);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * <P>WIFI 连接 </P>
     * @param view
     */
    public void connect(View view) {
        mGduSocketManager.startConnect();
    }

    /**
     * <P>关闭连接</P>
     * @param view
     */
    public void disconnect(View view) {
        mGduSocketManager.stopConnect();
    }

    /**
     * <P>获取SN---数传</P>
     * @param view
     */
    public void getSN(View view) {
        mGduSocketManager.getCommunication().getUnique(new SocketCallBack() {
            @Override
            public void callBack(byte code, GduFrame bean) {
                if (code == 0) {
                    System.out.println("test " + bean.getString());
                    StringBuilder stringBuilder = new StringBuilder();
                    byte[] tempFrame = bean.frame_content;
                    if (tempFrame != null) {
                        int length = tempFrame.length;
                        int contentLength = length;
                        for (int i = length - 1; i >= 0; i--) {
                            if (tempFrame[i] != 0) {
                                contentLength = i;
                                break;
                            }
                        }
                        for (int i = 0; i < contentLength + 1; i++) {
                            stringBuilder.append((char) bean.frame_content[i]);
                        }
                        System.out.println("test sn: " + stringBuilder.toString());
                    }
                }
            }
        });
    }

    public void getVersion(View view) {
        mGduSocketManager.getCommunication().getVersions(new SocketCallBack() {
            @Override
            public void callBack(byte code, GduFrame bean) {
                if (code == 0 && bean != null && bean.frame_content[0] == 0 && bean.frame_content.length > 10) {
                int bigVersion = bean.frame_content[9];
                int smallVersion = bean.frame_content[10];
                String planeVersion;
                if (smallVersion < 10) {
                    planeVersion = String.valueOf(bigVersion + (float) smallVersion / 100);  //飞机版本号  补全
                } else {
                    planeVersion = String.valueOf(bigVersion + (float) smallVersion / 100);  //飞机版本号 正常
                }
                long planeOTAVersion = DataUtil.byte2long(bean.frame_content, 1);
                System.out.println("test version: " + planeVersion + " " + planeOTAVersion);
            }
            }
        });
    }

    /**
     * <P>开启预览流</P>
     * @param view
     */
    public void startPre(View view) {
        Intent intent = new Intent(this, PreViewActivity.class);
        startActivity(intent);
//        isStart = !isStart;
//        System.out.println("test 11 " + Thread.currentThread().getId());
//        mGduSocketManager.getCommunication().beginOrPausePreCamera(false, new SocketCallBack() {
//            @Override
//            public void callBack(byte code, GduFrame bean) {
//                String content = bean != null ? bean.getString() : "null";
//                System.out.println("test code " + code + " " + content);
//                System.out.println("test 22 " + Thread.currentThread().getId());
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        statDecoder();
//                    }
//                });
//            }
//        });
    }


    int angle = 10;
    public void cloudChange(View view) {
        angle = -angle;
        mGduSocketManager.getCommunication().holderRollChange((byte) angle);
    }

    public void takePhoto(View view) {
        mGduSocketManager.getCommunication().takePhoto((byte) 1, (byte) 4, new SocketCallBack() {
            @Override
            public void callBack(byte code, GduFrame bean) {

            }
        });
    }


    public void control(View view) {
       short orientationY = 1500;
       short orientationX = 1500;
       short rotateValue= 1500;
       short throttleValue = 1500;
        mGduSocketManager.getCommunication().controlDrone(throttleValue, rotateValue, orientationX, orientationY);
    }
}
