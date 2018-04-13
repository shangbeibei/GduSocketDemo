package com.gdu.gdusocket.ce;


import com.gdu.gdusocketmodel.GduFrame;

/**
 * Created by zhangzhilai on 2018/1/23.
 */

public interface IGduSocket {
    /**
     * 连接的监听回调
     */
    interface OnConnectListener{
        void onConnect();
        void onDisConnect();
        void onConnectDelay(boolean isDelay);
    }

    interface OnDataReceivedListener{
        void onDataReceived(GduFrame gduFrame);
    }
}
