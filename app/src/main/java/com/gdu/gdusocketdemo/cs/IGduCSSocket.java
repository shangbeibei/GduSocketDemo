package com.gdu.gdusocketdemo.cs;

/**
 * Created by zhangzhilai on 2018/2/8.
 */

public interface IGduCSSocket {
    public  void stop();

    /****************************
     * 重新开始解码------ron
     */
    public  void onResume();

    /****************************
     * 暂停解码--------ron
     */
    public  void onPause();

    /****************************
     * 暂停解码--------ron
     */
    public  void showLog(byte pause);

    /*********************************
     * 获取收包率----ron
     * @return
     */
    public  int  getReceiverData();

    public  void droneType(byte type);
}
