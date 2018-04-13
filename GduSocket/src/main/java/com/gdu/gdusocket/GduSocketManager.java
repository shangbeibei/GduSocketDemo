package com.gdu.gdusocket;

import android.content.Context;
import android.util.Log;

import com.gdu.gdusocket.ce.GduCESocket;
import com.gdu.gdusocket.ce.IGduSocket;
import com.gdu.gdusocketmodel.GduFrame;
import com.gdu.gdusocketmodel.GduSocketConfig;
import com.gdu.gdusocketmodel.GlobalVariable;

import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by zhangzhilai on 2017/12/22.
 */

public class GduSocketManager {
    public static final String TAG = "GduSocketManager";
    private Context mContext;
    private String ip;  //连接的ip地址
    private int port;  //连接的端口
    private int updatePort = 9000;  //升级指令端口

    private GduCESocket mGduCESocket;
    private GduCommunication mGduCommunication;

    private static GduSocketManager mGduSocketManager;

    public static GduSocketManager getInstance(Context context){
        if (mGduSocketManager == null) {
            mGduSocketManager = new GduSocketManager(context);
        }
        return mGduSocketManager;
    }

    private GduSocketManager(Context context){
        init(context);
    }

    private void init(Context context){
        mContext = context;
        mGduCESocket = new GduCESocket(context);
        mGduCommunication = new GduCommunication(mGduCESocket);
        initListener();
        initSocketPort();
        initIpAndPort();
    }

    public GduCommunication getCommunication(){
        return mGduCommunication;
    }

    private void initListener() {
        mGduCESocket.setOnConnectListener(new IGduSocket.OnConnectListener() {
            @Override
            public void onConnect() {
                Log.d(TAG, "test connect");
            }

            @Override
            public void onDisConnect() {
                Log.d(TAG, "test DisConnect");
            }

            @Override
            public void onConnectDelay(boolean isDelay) {
                Log.d(TAG, "test onConnectDelay " + isDelay);
            }
        });

    }

    private void initIpAndPort(){
        ip = GduSocketConfig.IP_ADDRESS;
        port = GduSocketConfig.REMOTE_UDP_PORT;
    }

    public void reInitIp(String ip){
        this.ip = ip;
    }

    public void setDataReceivedListener(IGduSocket.OnDataReceivedListener onDataReceivedListener){
        mGduCESocket.setOnDataReceivedListener(onDataReceivedListener);
    }

    /**
     * 添加连接监听回调
     */
    public void setConnectCallBack(IGduSocket.OnConnectListener onConnectListener){
        mGduCESocket.setOnConnectListener(onConnectListener);
    }

    /**
     * 开启连接线程
     * @return
     */
    public boolean startConnect(){
        try {
            mGduCESocket.createSocket(ip, port, updatePort);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 关闭连接
     */
    public void stopConnect(){
        if (mGduCESocket != null)
            mGduCESocket.closeConnSocket();
    }

    /***************************
     * 获取socket的端口号
     */
    private void initSocketPort() {
        //获取数传端口
        for (int a = 3000; a < 9000; a++) {
            try {
                DatagramSocket socket = new DatagramSocket(a);
                socket.close();
                GlobalVariable.UDP_SOCKET_PORT = a;
                break;
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        //获取图传端口
        for (int a = 7078; a < 9000; a++) {
            try {
                DatagramSocket socket = new DatagramSocket(a);
                socket.close();
                GlobalVariable.UDP_SOCKET_IMG_PORT = a;
                break;
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }
}
