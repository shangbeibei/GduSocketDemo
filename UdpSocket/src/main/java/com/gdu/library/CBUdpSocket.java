package com.gdu.library;

/**
 * Created by zhangzhilai on 2017/12/15.
 */

public interface CBUdpSocket {
    void dataCB(byte[] data, int length);
    void stateChange(byte state);
}
