package com.gdu.library;

/**
 * Created by zhangzhilai on 2017/12/12.
 */

/******************
 * 底层的Socket通讯---ron
 */
public class UdpSocket {

    static {
        System.loadLibrary("CRtp");
    }

    /***************
     * 开始启用Socket ---ron
     * @param socketCB
     */
    public native void start(CBUdpSocket socketCB,int port);

    /****************
     * 停止Socket的启用---ron
     */
    public native void stop();

    /****************************
     * 重新开始解码------ron
     */
    public native void onResume();

    /****************************
     * 暂停解码--------ron
     */
    public native void onPause();

    /****************************
     * 暂停解码--------ron
     */
    public native void showLog(byte pause);

    /*********************************
     * 获取收包率----ron
     * @return
     */
    public native int  getReceiverData();

    public native void droneType(byte type);
}