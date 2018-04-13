package com.gdu.gdusocketmodel;

/**
 * Created by zhangzhilai on 2017/12/10.
 */

public class GlobalVariable {

    /***************************
     * 接受图传的端口号-----ron
     */
    public static int UDPSocketIMGPort = 7078;

    /*********************************
     *  gps跟踪已经收到反馈了
     *  -2 未接受的反馈数据
     *  -1 失败
     *  0 成功
     */
    public static byte GPSTrackHadBack = -2;

    /**********************************
     * 接受数传的端口号-----ron
     */
    public static  int UDP_SOCKET_PORT = 0;

    /**********************************
     * 接受图传的端口号-----ron
     */
    public static  int UDP_SOCKET_IMG_PORT = 0;

    /**
     * <p>当前飞机的连接状态 --- ron</p>
     */
    public static ConnStateEnum connStateEnum = ConnStateEnum.Conn_None;

    /*******************************
     * 心跳发送的PPS和 sps,发现视频流的分辨率和 发送来的分辨率 不一样，所以
     * 副本保存的 PPS都直接用心跳的
     */
    public static byte heartPpsSps = 0;

    /*****************************
     * 获取的PPS和sps的索引号----ron
     */
    public static byte ppsspsIndex = -1;

    /********************
     * wifi的延迟是否过大----ron
     */
    public static boolean wifiDelay;

    /*********ron**********
     * 遥控器是否已经连接 0：未连接 1：连接
     **********************/
    public static byte RC_usb_hadConn = 0;

    /**************************
     * 手机端的经度-----ron
     **************************/
    public static int LonPhone;

    /**************************
     * 手机端的纬度-----ron
     **************************/
    public static int LatPhone;

    /**********ron***********
     * 飞机连接的状态
     ************************/
    public static ConnType connType = ConnType.MGP03_WIFI;
}
