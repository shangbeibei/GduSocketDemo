package com.gdu.gdusocketmodel;

/**
 * Created by zhangzhilai on 2017/12/10.
 */

public class GduSocketConfig {

    /***************
     * socket ip的地址---ron
     * wifi直连
     ***************/

    public static final String IP_ADDRESS =  "192.168.1.1";//2.4G
    public static final String RC_IP_ADDRESS =  "127.0.0.1";//2.4G
//    public static final String IP_ADDRESS = "192.168.11.124";  //5.8G
//    public static final String IpAdress = "192.168.1.102";

        public static final int REMOTE_UDP_PORT = 7088;  ////2.4G
//    public static final int REMOTE_UDP_PORT = 7089;  ////5.8G


    /**
     * <p>socket的写线程，等待超时1s后，就发送一个 心跳包</p>
     */
    public static final int WriteWaitTimeOut = 1;

    /**
     * <p>帧的头部</p>
     */
    public static final byte Frame_Head = 0x55;

    /**
     * <p>发送目的地：app</p>
     */
    public static final byte To_App = 0x07;

    /**
     * <p>来源：无人机</p>
     */
    public static final byte To_drone = 0x01;

    /**
     * <ul>
     *     <li>帧头的长度</li>
     *     <li>包含，帧头，长度，</li>
     * </ul>
     */
    public static final byte Frame_Head_length = 0x03;

    /***********
     * <ul>
     *     <li>反馈码</li>
     *     <li>发生失败</li>
     * </ul>
     */
    public static final byte SENDERR_CODE = (byte) 0xFE;

    /***********
     * <ul>
     *     <li>反馈码</li>
     *     <li>发送超时</li>
     * </ul>
     */
    public static final byte SENDTIMEOUT_CODE = (byte)0x01;


    /**
     * <ul>
     *     <li>反馈码</li>
     *     <li>ACK成功码</li>
     * </ul>
     */
    public static final byte SUCCESS_CODE = 0x00;


    /***********
     * <ul>
     *     <li>反馈码</li>
     *     <li>连接失败</li>
     * </ul>
     */
    public static final byte CONNERR_CODE = (byte)0xFC;


    /**
     * <p>遥控器模块--ron</p>
     */
    public static final byte RCModel = 0x01;


    /*****
     * <p>飞控模块 --ron</p>
     */
    public static final byte FlyControlModel = 0x02;

    /*****
     * <p>云台模块 --ron</p>
     */
    public static final byte HolderModel = 0x03;

    /*****
     * <p>相机模块包括CS --ron</p>
     */
    public static final byte CameraModel = 0x04;

    /*****
     * <p>综控模块（包含CE） --ron</p>
     */
    public static final byte AllControlModel = 0x05;


    /*=====================周期反馈的命令字==========================*/

    /**
     * <p>一键起飞，垂直降落，一键返航的回掉 --ron</p>
     */
    public static final byte CycleACK_ControlFly = 0x12;

    /**
     * <p>校次状态的周期反馈 -- ron</p>
     */
    public static final byte CycleACK_checkNorth = 0x11;

    /**
     * <p>拍照的周期反馈 -- ron</p>
     */
    public static final byte CycleACK_takePhoto = 0x24;

    /**
     *  录像的周期反馈
     */
    public static final byte CycleACK_record = 0x26;

    /**
     * 手势识别的周期反馈
     */
    public static final byte CycleAck_guster=0x30;

    /**
     * 视频跟踪和视频环绕的反馈 ---ron
     */
    public static final byte CycleACK_VideoTrack = 0x32;


    /**
     * <p>避障的周期反馈命令字 --ron</p>
     */
    public static final byte CycleACK_obscale = 0x37;

    /**
     * <p>全景拍摄的周期反馈 -- ron</p>
     */
    public static final byte CycleACK_panorama = 0x39;

    /**
     * <p>gps跟踪的周期反馈 --ron</p>
     */
    public static final byte CycleACK_gpsTrack = 0x3b;

    /**
     * <p>gps环绕的周期反馈 --ron</p>
     */
    public static final byte CycleACK_gpsSurround = 0x3d;


    /**
     * <p>云台固件升级的反馈 --ron</p>
     */
    public static final byte CycleACK_HolderFMUpdate = 0x65;


    public static final byte CycleAck_BatteryUpdate=0x67;

    /**
     * <p>航迹规划的反馈 --ron</p>
     */
    public static final byte CycleACK_pathPlan = 0x73;

    /**
     * @author 余浩
     * 电子围栏的周期反馈
     */
    public static final byte CycleAck_wall=0x76;

    /**
     * <p>周期反馈心跳反馈  ---ron</p>
     */
    public static final byte CycleACK_Heart =(byte)0x19;

    /***
     * <p>遥控器的周期反馈 ---ron</p>
     */
    public static final byte CycleACK_RC = 0x50;

    /**
     * <p>反馈当前的连接是否有效 ---ron</p>
     */
    public static final byte CycleACK_ConnState = 0x17;

    /*******************************
     *  清理多媒体的反馈 ---ron
     *******************************/
    public static final byte CycleACK_ClearMedia = 0x2E;

    /*************************************
     * 飞机反馈的详情A --- ron
     *************************************/
    public static final byte CycleACK_DroneInfoA = (byte) 0xBA;

    /****************************************
     * 飞机的反馈详情 --- ron
     ****************************************/
    public static final byte CycleACK_DroneInfoB = (byte) 0xBB;

    /****************************************
     * 飞机的反馈详情 --- ronD
     ****************************************/
    public static final byte CycleACK_DroneInfoD = (byte) 0x8C;

    /*****************************************
     * 云台信息周期反馈 ---ron
     ******************************************/
    public static final byte CycleACK_HolderInfo = (byte)0x81;


    /************************************
     * 更新飞机固件 ---------ron
     ************************************/
    public static final byte CycleACK_UpdateDroneFM = (byte)0x63;

    /*************************************
     * 更新飞机的OTA ----ron
     *************************************/
    public static final byte CycleACK_UpdateDroneOTA = (byte)0x6B;

    /*************************
     * AGPS升级 ------ron
     *************************/
    public static final byte CycleACK_UpdateDroneAGPS = (byte)0x69;


    /*********************************
     * 斜飞的周期反馈-----ron
     */
    public static final byte CycleACK_Baisc = (byte)0x41;

    /*********************
     * Ap12主动下发的wifi质量 ----ron
     */
    public static final byte CycleACK_WIFIQuality = (byte)0x91;

    /*******************************
     * RC扫描周围WIFI信道的情况----ron
     */
    public static final byte CycleACK_WIFIChannel = (byte)0x6F;


    /**
     * 获取飞机附近的WIFI信号扫描结果
     */
    public static final byte CycleACK_DroneWifi = (byte)0x46;

    /**
     * <p>机头朝前的反馈 --zhaijiang</p>
     */
    public static final byte CycleACK_HeadOrientation  = (byte)0x77;

    /**
     * <p>U盘备份的反馈 --zhaijiang</p>
     */
    public static final byte CycleACK_USBDiskBackup  = (byte)0x94;

    /**
     * <p>无效的序列号</p>
     */
    public static byte InvalidateSerial = -1;

    /*==============================================================*/
}
