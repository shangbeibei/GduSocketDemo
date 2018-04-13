package com.gdu.gdusocket;


import com.gdu.gdusocketmodel.GduFrame;

/**
 * Created by zhangzhilai on 2017/12/10.
 * 长连接的回调函数
 */

public interface SocketCallBack {
    void callBack(byte code, GduFrame bean);
}
