package com.gdu.remotecontrol.model;

/**
 * Created by zhangzhilai on 2017/12/9.
 */

public class GDUConstants {

    public static final int BUFFER_HEADER_LENGTH = 8;  //零度头长度

    public static final String O2PLAN_NORMAL = "demo";  //O2 wifi版

    public static final String O2PLAN_PLUS = "AccessoryPassthrough";  // O2 plus版



    /**
     * 本地IP
     */
    public static final String LOCAL_IP = "127.0.0.1";

    /**
     * 缓存大小
     */
    public static final int BUFFER_SIZE_4104 = 4104;
    public static final int BUFFER_SIZE_1024 = 1024;

    /**
     * 接收USB数据 client本地端口
     */
    public static final int LOCAL_PORT_PROXY_CLIENT = 7048;

    /**
     *  server本地端口,向USB发送和接受数据
     */
    public static final int LOCAL_PORT_PROXY_SERVER = 7058;

    /**
     * TCP server本地端口
     */
    public static final int LOCAL_PORT_TCP = 7068;

    /**
     * TCP 远程端口(飞机)
     */
    public static final int REMOTE_PORT_TCP = 80;

    /**
     * UDP server本地端口
     */
    public static final int LOCAL_PORT_UDP = 7088;

    /**
     * UDP 远程端口
     */
    public static final int REMOTE_PORT_UDP = 7088;

    /**
     * UDP 远程端口 固件升级
     */
    public static final int REMOTE_PORT_UDP_UPGRADE = 9000;

    /**
     * 通过usb连接遥控器 获取遥控器和飞机的wifi信息
     */
    public static final int LOCAL_PORT_RC_USB = 8002;

    /**
     * FTP 本地端口
     */
    public static final int LOCAL_PORT_FTP_CMD = 7008;
    public static final int LOCAL_PORT_FTP_DATA = 13331;

    /**
     * FTP 远程端口(发送给飞机的端口)
     */
    public static final int REMOTE_PORT_FTP_DATA_3412 = 13331;
    public static final int REMOTE_PORT_FTP_CMD_23 = 24;


    /**
     * 数据类型 TCP(0)、UDP(1)
     */
    public static final int TCP = 0;
    public static final int UDP = 1;

    /**
     * 连接状态 CONNECTING(0)、CONNECTED(1)、DISCONNECTED(2)、DATA(3)
     */
    public static final int CONNECTING = 0;
    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 2;
    public static final int DATA = 3;


    public static final String USB_HOST_HTTP = "http://127.0.0.1:7068";
    /**
     * 照片的请求URL
     */
    public static final String URL_PIC =
            USB_HOST_HTTP + "/uav.cgi?op=select&type=pic&startPos=1";
    /**
     * 照片请求的拼接URl
     */
    public static final String URL_PIC_PAGE =
            USB_HOST_HTTP + "/uav.cgi?op=select&type=pic&startPos=";
    /**
     * 照片删除
     */
    public static final String PIC_DELETE =
            USB_HOST_HTTP + "/uav.cgi?op=delete&type=pic&name=";
    /**
     * 视频删除
     */
    public static final String VIDEO_DELETE =
            USB_HOST_HTTP + "/uav.cgi?op=delete&type=video&name=";
    /**
     * 视频的请求URL
     */
    public static final String URL_VIDEO =
            USB_HOST_HTTP + "/uav.cgi?op=select&type=video&startPos=1";
    /**
     * 视频请求的拼接URl
     */
    public static final String URL_VIDEO_PAGE =
            USB_HOST_HTTP + "/uav.cgi?op=select&type=video&startPos=";


    //___________________________________________wifi_________
    /**
     * IP地址
     */
    public static final String WIFI_UAV_HOST_IP = "192.168.1.1";
    /**
     * Http请求地址
     */
    public static final String WIFI_HOST_HTTP = "http://192.168.1.1";
    /**
     * 照片的请求URL
     */
    public static final String WIFI_URL_PIC = WIFI_HOST_HTTP + "/uav.cgi?op=select&type=pic&startPos=1";
    /**
     * 视频的请求URL
     */
    public static final String WIFI_URL_VIDEO = WIFI_HOST_HTTP + "/uav.cgi?op=select&type=video&startPos=1";
    /**
     * 飞控的端口
     */
    public static final int WIFI_UDP_FLY_CONTROL_PORT = 7088;
    /**
     * ftp  端口
     */
    public static final int WIFI_FTP_PORT = 21;
    /**
     * 照片请求的拼接URl
     */
    public static String WIFI_URL_PIC_PAGE = WIFI_HOST_HTTP + "/uav.cgi?op=select&type=pic&startPos=";
    /**
     * 照片删除
     */
    public static String WIFI_PIC_DELETE = WIFI_HOST_HTTP + "/uav.cgi?op=delete&type=pic&name=";
    /**
     * 视频删除
     */
    public static String WIFI_VIDEO_DELETE = WIFI_HOST_HTTP + "/uav.cgi?op=delete&type=video&name=";
    /**
     * 视频请求的拼接URl
     */
    public static String WIFI_URL_VIDEO_PAGE = WIFI_HOST_HTTP + "/uav.cgi?op=select&type=video&startPos=";
    //___________________________________________end wifi_______
}
