package com.gdu.gdusocketdemo.cs;

/**
 * Created by zhangzhilai on 2018/2/8.
 */

public interface OnCSReceiveListener {
    void onCSDataReceived(byte[] data, int length);
}
