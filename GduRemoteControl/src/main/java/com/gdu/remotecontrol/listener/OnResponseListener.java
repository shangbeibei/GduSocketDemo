package com.gdu.remotecontrol.listener;

/**
 * Created by zhangzhilai on 2018/1/26.
 * 监听usb发送过来的数据
 */

public interface OnResponseListener {
    void onDataReceived(byte[] data);
}
