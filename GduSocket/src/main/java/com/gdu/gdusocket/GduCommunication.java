package com.gdu.gdusocket;

import com.gdu.gdusocket.ce.GduCESocket;
import com.gdu.gdusocketmodel.ConnType;
import com.gdu.gdusocketmodel.GduSocketConfig;
import com.gdu.gdusocketmodel.GlobalVariable;
import com.gdu.util.DataUtil;

import java.util.Calendar;

import static com.gdu.gdusocketmodel.GduSocketConfig.AllControlModel;
import static com.gdu.gdusocketmodel.GduSocketConfig.FlyControlModel;


/**
 * Created by ron on 2017/5/25.
 */
public class GduCommunication {
    /***
     * 数传socket
     */
    private GduCESocket gduSocket;

    private final byte ZERO = 0x00;

    private final byte ONE = 0x01;

    private final byte TWO = 0x02;

    /**
     * <p>云台控制帮助类</p>
     */
//    private HolderControl holderControl;

    /**
     * <p>连接的分辨率 --- ron</p>
     */
    private String ip;

    /**
     * <p>连接的端口 --- ron</p>
     */
    private int port;

    /**********************
     * 升级指令的端口
     */
    private int update_port = 9000;

    public GduCommunication(GduCESocket gduSocket) {
        this.gduSocket = gduSocket;
    }


    /**
     * <p>断开Socket连接 --ron</p>
     */
    public void disconnSocket() {
        if (gduSocket != null)
            gduSocket.closeConnSocket();
    }

//    public GduSocket getGduSocket()
//    {
//        return gduSocket;
//    }

    /**
     * <p>获取唯一标识码 ---ron</p>
     */
    public void getUnique(SocketCallBack socketCallBack) {
        gduSocket.sendMsg(socketCallBack, (byte) 0x03, FlyControlModel, null);
    }

    /**
     * <p>发送心跳(测试使用) ---ron</p>
     */
    public void sendHeart() {
        gduSocket.sendMsg(null, (byte) 0xE0, AllControlModel, null);
    }

    /**
     * <p>解锁或者 上锁指令 -- ron</p>
     *
     * @param isUnLock       是否是解锁
     * @param socketCallBack
     */
    public void lockOrUnLockDrone(boolean isUnLock, SocketCallBack socketCallBack) {
//        RonLog.LogE("lockAndlock", "isUnlock" + isUnLock);
//        gduSocket.sendMsg(socketCallBack, (byte) 0x04, FlyControlModel, isUnLock ? ONE : ZERO);
    }


    /***
     * <p>重新设置IP的地址 和 port</p>
     *
     * @param ip   重新设置的地址
     * @param port 重新设置的端口
     */
    public void reSetInetAdd(String ip, int port) {
        this.ip = ip;
        this.port = port;
        if (gduSocket != null) {
            gduSocket.setIpAndPort(ip, port, update_port);
        }
    }

    /***
     * <p>遥控器对码   ---ron</p>
     *
     * @param socketCallBack
     */
    public void matchRC(SocketCallBack socketCallBack) {
        gduSocket.sendMsg(null, (byte) 0x80, (byte) 0x0a, null);
    }

    /****
     * <p>获取飞机home点</p>
     *
     * @param socketCallBack
     */
    public void getDroneHome(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x05, FlyControlModel, null);
    }

    /***
     * <p>一键起飞 ---ron</p>
     *
     * @param socketCallBack
     */
    public void oneKeyFly(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x07, FlyControlModel, null);
    }

    /**
     * <p>一键返航</p>
     *
     * @param socketCallBack
     */
    public void oneKeyBack(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x08, FlyControlModel, null);
    }

    /**
     * <p>垂直降落</p>
     */
    public void verticalLanding(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x09, FlyControlModel, null);
    }


    /**
     * <p>控制飞机的 ---ron</p>
     * <p>控制油门，旋转，方位</p>
     */
    public void controlDrone(short rollValue, short rotateValue, short orientionXValue, short orientionYValue) {
        byte[] rollData = DataUtil.short2byte(rollValue);
        byte[] rotateData = DataUtil.short2byte(rotateValue);
        byte[] orientXData = DataUtil.short2byte(orientionXValue);
        byte[] orientYData = DataUtil.short2byte(orientionYValue);
        gduSocket.sendMsg(null, (byte) 0x0a, FlyControlModel, rollData[0], rollData[1],
                rotateData[0], rotateData[1], orientYData[0], orientYData[1], orientXData[0], orientXData[1], (byte) 0, (byte) 0, (byte) 0, (byte) 0);
    }

    /**
     * Smart模式下，飞机的控制
     */
    public void controlSmartDrone() {
        //TODO 需要讨论，Smart模式下，飞机的控制
    }

    /**
     * <p>设置飞机的限高限距的开关 -- ron</p>
     *
     * @param isOn
     * @param socketCallBack
     * @deprecated
     */
    public void setDroneLimitSwitch(boolean isOn, SocketCallBack socketCallBack) {
//        if (isOn)
//            gduSocket.sendMsg(socketCallBack, (byte) 0x0c, FlyControlModel, ONE, ONE);
//        else
//            gduSocket.sendMsg(socketCallBack, (byte) 0x0c, FlyControlModel, ZERO, ZERO);
    }

    /**
     * <p>设置飞机的限制高度和限制距离的值  ---ron</p>
     *
     * @param limitHeight    高度设置值，单位（m*100,为了保留2位小数)
     * @param limitDistans   距离设置值，单位（m*100,为了保留2位小数)
     * @param socketCallBack
     */
    public void setDroneLimitValue(int limitHeight, int limitDistans, SocketCallBack socketCallBack) {
        byte[] data_value = new byte[8];
//        System.arraycopy(ByteUtilsLowBefore.int2byte(limitHeight), 0, data_value, 0, 4);
//        System.arraycopy(ByteUtilsLowBefore.int2byte(limitDistans), 0, data_value, 4, 4);
//        gduSocket.sendMsg(socketCallBack, (byte) 0x0d, FlyControlModel, data_value);
    }

    /***
     * <p>获取限制距离 和 限制高度的参数</p>
     *
     * @param socketCallBack
     */
    public void getDroneLimitArgs(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x0e, FlyControlModel, null);
    }

    /**
     * <p>设置飞机的返航高度</p>
     *
     * @param socketCallBack 返航高度（单位m*100）
     */
    public void setDroneBackHeight(short backHeight, SocketCallBack socketCallBack) {
//        byte[] data_value = ByteUtilsLowBefore.short2byte(backHeight);
//        gduSocket.sendMsg(socketCallBack, (byte) 0x0f, FlyControlModel, data_value[0], data_value[1]);
    }

    /**
     * <p>发送校磁指令</p>
     *
     * @param cmd            0x00停止校磁，0x01 XY校磁，0x02 Z轴校磁
     * @param socketCallBack
     */
    public void sendCheckNorth(byte cmd, SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x10, FlyControlModel, cmd);
    }

    /**
     * <p>一键炸机</p>
     *
     * @param socketCallBack
     */
    public void oneKeyLockDrone(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x14, FlyControlModel, null);
    }

    /**
     * IMU校准
     *
     * @param socketCallBack
     */
    public void checkIMU(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x87, FlyControlModel, (byte) 0x00);
    }

    /**
     * 开始或者 暂停 视频预览流 --ron
     *
     * @param isPause
     * @param socketCallBack
     */
    public void beginOrPausePreCamera(boolean isPause, SocketCallBack socketCallBack)
    {
        byte[] data = new byte[3];
        data[0] = isPause ? TWO : ONE;
        if (GlobalVariable.connType == ConnType.MGP03_RC_USB) {
            System.arraycopy(DataUtil.short2byte((short) 7078), 0, data, 1, 2);
        }else {
            System.arraycopy(DataUtil.short2byte((short) GlobalVariable.UDPSocketIMGPort), 0, data, 1, 2);
        }
        gduSocket.sendMsg(socketCallBack, (byte) 0x15, GduSocketConfig.CameraModel, data);
    }

    /***
     * 获取视频流的pps,sps --ron
     *
     * @param socketCallBack
     */
    public void getSpsPPS(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x16, GduSocketConfig.CameraModel);
    }

    /**
     * 开始云台控制 --ron
     *
     * @param roll  横滚  ---roll轴不用，参数无效，随便传
     * @param pitch 俯仰
     * @param or    方位
     */
    public void beginControlHolder(byte roll, byte pitch, byte or) {
//        if (holderControl == null || !holderControl.isAlive()) {
//            holderControl = new HolderControl(gduSocket);
//            holderControl.startThread();
//        }
//        holderControl.update(roll, pitch, or);
    }

    /**
     * <p>停止控制云台 -- ron</p>
     */
    public void stopControlHolder() {
//        if (holderControl != null)
//            holderControl.stopThread();
    }

    /**
     * <p>切换录像模式和 照相模式 --ron</p>
     *
     * @param isCameraModel
     * @param socketCallBack
     */
    public void switchCameraRecordModel(boolean isCameraModel, SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x22, GduSocketConfig.CameraModel, isCameraModel ? ZERO : ONE);
    }

    /***
     * <p>拍照，连拍，停止拍照 --ron</p>
     *
     * @param type           0x00 正常拍照，0x01连拍，0xFF停止拍照
     * @param arg2           如果是连拍，此参数表示连拍的张数
     * @param socketCallBack
     */
    public void
    takePhoto(byte type, byte arg2, SocketCallBack socketCallBack) {
        gduSocket.sendMsg(socketCallBack, (byte) 0x23, GduSocketConfig.CameraModel, type, arg2);
    }

    /**
     * <p>录像操作  --ron</p>
     *
     * @param type           0x00开始录像，0x01停止录像，0x02:慢动作录像，0x03:快动作录像
     * @param scaleIndex     录像时间比例索引
     * @param socketCallBack
     */
    public void recordVideo(byte type, byte scaleIndex, SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x25, GduSocketConfig.CameraModel, type, scaleIndex);
    }

    /**
     * <p>获取相机参数 --ron</p>
     *
     * @param socketCallBack
     */
    public void getCameraArgs(SocketCallBack socketCallBack) {
        gduSocket.sendMsg(socketCallBack, (byte) 0x27, GduSocketConfig.CameraModel, null);
    }

    /**
     * <p>设置相机的EV,WB,ISO --ron</p>
     *
     * @param type           设置类型0x00:恢复到默认（EV,,WB,ISO都恢复到默认），0x01EV, 0x02WB,0x03ISO
     * @param valueIndex     值得索引
     * @param socketCallBack
     */
    public void setCamera_EVWBISO(byte type, byte valueIndex, SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x28, GduSocketConfig.CameraModel, type, valueIndex);
    }

    /**
     * <p>设置录像的尺寸 --ron</p>
     *
     * @param recordSize     录像尺寸的索引
     * @param socketCallBack
     */
    public void setRecordSize(byte recordSize, SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x29, GduSocketConfig.CameraModel, recordSize);
    }

    /**
     * <p>设置预览视频的尺寸 -- ron</p>
     *
     * @param preVideoSize   预览视频的尺寸索引
     * @param socketCallBack
     */
    public void setPreVideoSize(byte preVideoSize, SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x2a, GduSocketConfig.CameraModel, preVideoSize);
    }

    /**
     * <p>设置照片的尺寸 -- ron</p>
     *
     * @param photoSize      设置照片尺寸的索引
     * @param socketCallBack
     */
    public void setPhotoSize(byte photoSize, SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x2b, GduSocketConfig.CameraModel, photoSize);
    }

    /**
     * <p>一键清空储存内存 -- ron</p>
     *
     * @param socketCallBack
     */
    public void clearMedia(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x2c, GduSocketConfig.CameraModel, null);
    }

    /**
     * <p>添加周期反馈的回调函数 --ron</p>
     *
     * @param cmd            周期反馈的命令字
     * @param socketCallBack 回调函数
     */
    public void addCycleACKCB(byte cmd, SocketCallBack socketCallBack) {
        gduSocket.addCycleAck(cmd, socketCallBack);
    }

    /**
     * <p>清除周期回调的反馈  --- ron</p>
     *
     * @param cmd
     */
    public void removeCycleACKCB(byte cmd) {
        gduSocket.removeCycleAck(cmd);
    }
    /*=========================视觉算法BEGIN==========================*/


    /**
     * <p>视觉算法 --跟踪 和 环绕 --ron</p>
     *
     * @param type           0x01进入跟随模式; 0x02退出跟随模式;0x03开始跟踪; 0x04结束跟踪;
     *                       0x05 进入环绕模式; 0x06 退出环绕模式；0x07 开始环绕; 0x08 结束环绕
     * @param leftX
     * @param leftY
     * @param width
     * @param height
     * @param socketCallBack
     */
    public void videoTrackOrSurrondALG(byte type, short leftX, short leftY,
                                       short width, short height, SocketCallBack socketCallBack) {
//        byte[] leftXdata = ByteUtilsLowBefore.short2byte(leftX);
//        byte[] leftYdata = ByteUtilsLowBefore.short2byte(leftY);
//        byte[] widthData = ByteUtilsLowBefore.short2byte(width);
//        byte[] heightData = ByteUtilsLowBefore.short2byte(height);
//        gduSocket.sendMsg(socketCallBack, (byte) 0x31, GduSocketConfig.SmartModel,
//                type, leftXdata[0], leftXdata[1],
//                leftYdata[0], leftYdata[1],
//                widthData[0], widthData[1],
//                heightData[0], heightData[1]
//        );
    }

    /**
     * <p>手势算法库 --ron</p>
     *
     * @param isOpen         是否开启手势算法库
     * @param socketCallBack
     */
    public void gestureALG(boolean isOpen, SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x35, GduSocketConfig.SmartModel, isOpen ? ZERO : ONE);
    }

    /**
     * <p>避障算法开始 关闭 --ron</p>
     */
//    public void obstacleALG(boolean isOpen, VisionSetHelper.SwitchType type, SocketCallBack socketCallBack) {
//        RonLog.LogE("避障算法是否开启:" + isOpen);
//        if (isOpen) {
//            if ((GlobalVariable.CheckSelfResult_C & 0x01) == 1) {
//                if (socketCallBack != null) {
//                    socketCallBack.callBack((byte) -1, null);
//                }
//                return;
//            }
//        }
//        Log.e("zhaijiang", "obstacleALG isOpen-->" + isOpen + "  type--->" + type);
//        switch (type){
//            case OBSTACLE_TYPE_MAIN:
//                gduSocket.sendMsg(socketCallBack, (byte) 0x36, GduSocketConfig.SmartModel, isOpen ? ZERO : ONE);
//                break;
//            case OBSTACLE_TYPE_RETURN:
//                gduSocket.sendMsg(socketCallBack, (byte) 0x36, GduSocketConfig.SmartModel, isOpen ? (byte) 0x02 : (byte) 0x03);
//                break;
//            case OBSTACLE_TYPE_BACK:
//                gduSocket.sendMsg(socketCallBack, (byte) 0x36, GduSocketConfig.SmartModel, isOpen ? (byte) 0x04 : (byte) 0x05);
//                break;
//        }
//    }

    public void obstacleALG(boolean isOpen, SocketCallBack socketCallBack) {
//        obstacleALG(isOpen, OBSTACLE_TYPE_MAIN, socketCallBack);
    }

    /**
     * <p>算法 全景模式的操控  -- ron</p>
     * 0x00进入全景模式，
     * 0x01开始全景拍照，
     * 0x02取消当前全景，
     * 0x03退出全景模式
     *
     * @param type
     * @param socketCallBack 回调函数
     */
    public void panoramaALG(byte type, SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x38, GduSocketConfig.SmartModel, type);
    }

    /**
     * 上传gps的线程
     */
//    private UploadClientGps uploadClientGps;

    /**
     * <p>GPS跟踪 -- ron</p>
     *
     * @param isOpen 是否开启GPS跟踪
     */
//    public void gpsTrackALG(final boolean isOpen, LocationHelper locationHelper) {
//        if (isOpen) {
//            if (uploadClientGps == null || !uploadClientGps.isAlive()) {
//                uploadClientGps = new UploadClientGps(gduSocket, locationHelper);
//                uploadClientGps.startThread();
//            }
//            uploadClientGps.setBeginFollowMe(true);
//        } else {
//            uploadClientGps.setBeginFollowMe(false);
//            locationHelper.onDestory();
//        }
//    }

    /**********************************
     * 关闭GPS跟踪 ----ron
     */
//    public void closeGPSTrack() {
//        if (uploadClientGps != null) {
//            uploadClientGps.stopThread();
//            uploadClientGps = null;
//        }
//    }

    /**
     * <p>GPS环绕 -- ron</p>
     *
     * @param isOpen         是否开启GPS环绕
     * @param surrondRaido   环绕半径，单位是分米
     * @param surrondHeight  环绕高度，单位分米
     * @param socketCallBack
     */
    public void gpsSurrondALG(final boolean isOpen, short surrondRaido, short surrondHeight, final SocketCallBack socketCallBack) {
        byte[] data = new byte[5];
        data[0] = isOpen ? ZERO : ONE;
//        System.arraycopy(ByteUtils.short2byte(surrondRaido), 0, data, 1, 2);
//        System.arraycopy(ByteUtils.short2byte(surrondHeight), 0, data, 3, 2);
//        gduSocket.sendMsg(socketCallBack, (byte) 0x3c, FlyControlModel, data);
    }

    /**
     * <p>变倍连拍 --ron</p>
     *
     * @param socketCallBack
     */
    public void changeScalePhotosALG(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x42, GduSocketConfig.CameraModel);
    }

    /**
     * <p>获取当前飞机所以算法的状态 -- ron</p>
     *
     * @param socketCallBack
     */
    public void getAllALGStatus(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x43, FlyControlModel);
    }

    /*=========================视觉算法END============================*/

    /**
     * <p>获取飞机里面所以得版本号 --ron</p>
     *
     * @param socketCallBack
     */
    public void getVersions(SocketCallBack socketCallBack) {
        gduSocket.sendMsg(true, socketCallBack, (byte) 0x61, FlyControlModel);
    }

    /*************************************
     * <p>发送升级固件指令 ----ron</p>
     *
     * @param versionCodeBig   固件版本号大号
     * @param versionCodeSmall 固件版本号小号
     * @param MD5Code          MD5号
     */
    public void sendUpdateFM(byte versionCodeBig, byte versionCodeSmall, byte[] MD5Code, SocketCallBack socketCallBack) {
        if (MD5Code == null || MD5Code.length != 16) {
            throw new IllegalArgumentException("length of MD5Code must be 16");
        }
        byte[] data = new byte[18];
        data[0] = versionCodeBig;
        data[1] = versionCodeSmall;
        System.arraycopy(MD5Code, 0, data, 2, MD5Code.length);
//        gduSocket.sendMsg(true, socketCallBack, (byte) 0x62, FlyControlModel, data);
    }

    /*************************************
     * <p>发送OTA升级指令 ----ron</p>
     */
    public void sendUpdateOTA(long version, SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(true, socketCallBack, (byte) 0x6a, FlyControlModel, ByteUtilsLowBefore.long2byte(version));
    }


    /*************************************
     * <p>发送OTA升级指令 ----ron</p>
     */
    public void sendUpdateOTA(long version, byte[] md5, SocketCallBack socketCallBack) {
        byte[] datas = new byte[24];
//        byte[] versions = ByteUtilsLowBefore.long2byte(version);
//        System.arraycopy(versions, 0, datas, 0, 8);
//        System.arraycopy(md5, 0, datas, 8, 16);
//        gduSocket.sendMsg(true, socketCallBack, (byte) 0x6a, FlyControlModel, datas);
    }

    /*************************************
     * <p>AGPS升级---ron</p>
     */
    public void sendUpdateAGPS(SocketCallBack socketCallBack) {
        gduSocket.sendMsg(socketCallBack, (byte) 0x68, AllControlModel);
    }

    /**
     * <p>获取遥控器的设置 --ron</p>
     *
     * @param socketCallBack
     */
    public void getRCSet(SocketCallBack socketCallBack) {
        gduSocket.sendMsg(socketCallBack, (byte) 0x51, AllControlModel);
    }

    /**
     * <p>设置遥控器的c1，c2 --ron</p>
     * @param c1Index        c1的索引号
     * @param c2Index        c2的索引号
     * @param socketCallBack
     */
    public void setRCC1AndC2(byte c1Index, byte c2Index, SocketCallBack socketCallBack) {
        gduSocket.sendMsg(socketCallBack, (byte) 0x53, AllControlModel, c1Index, c2Index);
    }

    /**
     * <p>设置遥控器的控制手 --ron</p>
     * @param socketCallBack
     * @param controlHandIndex 控制手的索引号
     */
    public void setRCControlHand(SocketCallBack socketCallBack, byte controlHandIndex) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x52, GduSocketConfig.RCModel, controlHandIndex);
    }

    /**
     * @param socketCallBack
     * @author 余浩
     * 获取ap12的版本号
     */
    public void getAp12Version(SocketCallBack socketCallBack) {
        byte[] bytes = new byte[1];
//        gduSocket.sendMsg(socketCallBack, (byte) 0x54, GduSocketConfig.AP12Model, bytes);
    }

    /**
     * @param socketCallBack
     * @param name
     * @param md5
     * @author 余浩
     */
    public void updateAp12(SocketCallBack socketCallBack, String name, String md5) {
        byte[] byteName = name.getBytes();
        byte[] bytesMd5 = md5.getBytes();
        int length = byteName.length + bytesMd5.length + 2;
        byte[] data = new byte[length];
        data[0] = (byte) byteName.length;
        System.arraycopy(byteName, 0, data, 1, byteName.length);
        data[byteName.length + 1] = (byte) bytesMd5.length;
        System.arraycopy(bytesMd5, 0, data, 2 + byteName.length, 32);
//        gduSocket.sendMsg(socketCallBack, (byte) 0x55, GduSocketConfig.AP12Model, data);
    }


    /**
     * @param socketCallBack 屏蔽拍照，录像指令
     * @param type           0x01 开始拼比   0x00  结束屏蔽
     * @author 余浩
     */
    public void shieldCamera(SocketCallBack socketCallBack, byte type) {
        byte[] bytes = new byte[10];
        bytes[0] = type;
        gduSocket.sendMsg(socketCallBack, (byte) 0x57, AllControlModel, bytes);
    }

    /***
     * 云台回中 --ron
     */
    public void holderBack2Center(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x2d, GduSocketConfig.HolderModel, (byte) 0x10, (byte) 0, (byte) 0);
    }


    /**
     * <p>云台roll调整 --ron</p>
     *
     * @param value -30 至 30
     */
    public void holderRollChange(byte value) {
        if (value > 30) value = 30;
        else if (value < -30) value = -30;
        byte v = (byte) (value + 30);
        gduSocket.sendMsg(null, (byte) 0x21, GduSocketConfig.HolderModel, v);
    }

    /***
     * <p>同步时间</p>
     *
     * @param socketCallBack
     */
    public void synTime(SocketCallBack socketCallBack) {
        Calendar calendar = Calendar.getInstance();
        int time = (int) ((calendar.getTimeInMillis() + calendar.getTimeZone().getRawOffset()) / 1000);
//        byte[] data = ByteUtilsLowBefore.int2byte(time);
//        gduSocket.sendMsg(socketCallBack, (byte) 0x18, AllControlModel, data[0], data[1], data[2], data[3]);
    }

    /***************************
     * 忽略云台稳像功能--ron
     ***************************/
    public void igonoreHolderKeyImg() {
//        gduSocket.sendMsg(null, (byte) 0x86, GduSocketConfig.HolderModel);
    }

    /*********************************
     * 设置当前的分比率的比特率 ---ron
     *********************************/
    public void setCurrentBitValue(byte value, SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x8b, GduSocketConfig.CameraModel, value);
    }

    /**************************************
     * 智能电池升级发送指令-------ron
     *
     * @param socketCallBack
     */
    public void updateBattery(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x66, GduSocketConfig.BatteryModel);
    }

    /**************************************
     * 智能电池升级发送指令-------ron
     *
     * @param socketCallBack
     */
    public void updateBattery(SocketCallBack socketCallBack, byte[] md5Code) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x66, GduSocketConfig.BatteryModel, md5Code);
    }

    /***************************************
     * 升级云台指令-------ron
     *
     * @param socketCallBack
     */
    public void updateHolder(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x64, GduSocketConfig.HolderModel);
    }

    /***************************************
     * 升级云台指令-------ron
     *
     * @param socketCallBack
     */
    public void updateHolder(SocketCallBack socketCallBack, byte[] md5Code) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x64, GduSocketConfig.HolderModel, md5Code);
    }

    /**
     * @param socketCallBack 查询电池的版本号以及厂家信息
     * @nthor yuhao
     */
    public void getBatterInfo(SocketCallBack socketCallBack) {
//        gduSocket.sendMsg(socketCallBack, (byte) 0x6C, GduSocketConfig.BatteryModel);
    }
    /**
     *<P>shang</P>
     *<li>GDU 内部 Debug 使用（测试）</li>
     *<li> APP打开或者关闭ADB 功能</li>
     *<li> 0x00 关闭</li>
     *<li> 0x01 开启</li>
     */
    public void setAPP_ADBFunction(byte value,SocketCallBack socketCallBack) {
        gduSocket.sendMsg(socketCallBack, (byte) 0x59, AllControlModel, value);
    }

    /***************************************
     * 设置飞机的最大速度
     *
     * @param maxTopSpeed       最大向上速度
     * @param maxDownSpeed      最大向下速度
     * @param maxOrientionSpeed 最大水平速度
     * @param socketCallBack
     */
    public void setSpeedLimit(short maxTopSpeed, short maxDownSpeed, short maxOrientionSpeed, SocketCallBack socketCallBack) {
        byte[] data = new byte[6];
        int index = 0;
//        System.arraycopy(ByteUtilsLowBefore.short2byte(maxTopSpeed), 0, data, index, 2);
//        index += 2;
//        System.arraycopy(ByteUtilsLowBefore.short2byte(maxDownSpeed), 0, data, index, 2);
//        index += 2;
//        System.arraycopy(ByteUtilsLowBefore.short2byte(maxOrientionSpeed), 0, data, index, 2);
//        gduSocket.sendMsg(socketCallBack, (byte) 0x8a, FlyControlModel, data);
    }

    /*************************************************
     * 设置视觉环绕的功能参数 ----ron
     * @param isClockWise 是否是顺时针
     * @param speed  速度，cm/s
     */
    public void setSurrondImgFlyArgs(boolean isClockWise, short speed) {
        byte[] data = new byte[7];
//        data[0] = isClockWise ? ZERO : ONE;
//        System.arraycopy(ByteUtilsLowBefore.short2byte(speed), 0, data, 1, 2);
//        RonLog.LogE("setSurrondImgFlyArgs:" + isClockWise + "," + speed);
//        gduSocket.sendMsg(null, (byte) 0x33, GduSocketConfig.SmartModel, data);
    }

    /*************************
     * Smart斜飞自拍 ---ron
     * @param socketCallBack 执行回掉
     * @param cmd  0x00开启，0x01关闭 0x02Ready
     * @param distense 距离(单位DM，Ready阶段和开启阶段都有效，低字节在前)
     * @param speed 速度（单位cm/s，开启阶段有效，低字节在前）
     * @param angle  角度（Ready阶段有效）
     * @param controlModel 控制模式，0x01是斜飞，0x02是倒飞
     * @param type 操作类型 Bit0：返回，执行完成后返回。Bit1:悬停，执行完成后悬停。Bit2:拍照, Bit3:录像
     */
    public void biasFly(SocketCallBack socketCallBack, byte cmd, short distense, short speed, short angle, byte controlModel, byte type) {
        byte[] data = new byte[9];
        int index = 0;
        data[index++] = cmd;
//        System.arraycopy(ByteUtilsLowBefore.short2byte(distense), 0, data, index, 2);//距离
//        index += 2;
//
//        System.arraycopy(ByteUtilsLowBefore.short2byte(speed), 0, data, index, 2);//速度
//        index += 2;
//
//        System.arraycopy(ByteUtilsLowBefore.short2byte(angle), 0, data, index, 2);//角度
//        index += 2;

        data[index++] = controlModel;//操作模式
        data[index++] = type;  //控制类型
        gduSocket.sendMsg(socketCallBack, (byte) 0x40, (byte) 0x0b, data);
    }

    /******************************
     * 视觉定位 ---------ron
     */
    public void videoLocation(boolean isOpen, SocketCallBack socketCallBack, short leftX, short leftY, short width, short height) {
        byte[] data = new byte[9];
        int index = 0;
        data[index++] = isOpen ? (byte) 0 : (byte) 1;
//        System.arraycopy(ByteUtilsLowBefore.short2byte(leftX), 0, data, index, 2);
//        index += 2;
//        System.arraycopy(ByteUtilsLowBefore.short2byte(leftY), 0, data, index, 2);
//        index += 2;
//        System.arraycopy(ByteUtilsLowBefore.short2byte(width), 0, data, index, 2);
//        index += 2;
//        System.arraycopy(ByteUtilsLowBefore.short2byte(height), 0, data, index, 2);
        index += 2;
        gduSocket.sendMsg(socketCallBack, (byte) 0x44, (byte) 0x04, data);
    }

    /**********************************
     * 修改USB连接的图传端口-----ron
     */
    public void changeUSBConnIMGPORT(short port,SocketCallBack socketCallBack)
    {
//        gduSocket.sendMsg(socketCallBack,(byte)0x56,GduSocketConfig.AP12Model,ByteUtils.short2byte(port));
    }

    /************************************
     * 取消返航模式----ron
     * @param socketCallBack
     */
    public void breakBackModel(SocketCallBack socketCallBack) {
        gduSocket.sendMsg(socketCallBack, (byte) 0x89, (byte) 0x02);
    }

    /**
     * 开启或关闭ELOG
     * 关闭 0， 开启 1
     * @param socketCallBack
     * @param openLog
     */
    public void openELog(SocketCallBack socketCallBack, byte openLog){
        gduSocket.sendMsg(socketCallBack, (byte)0x78, AllControlModel, openLog);
    }

    /**
     * 获取wifi名
     * @param socketCallBack
     */
    public void getWifiName(SocketCallBack socketCallBack){
        gduSocket.sendMsg(socketCallBack, (byte)0xA3, AllControlModel, (byte)0x01);
    }

    /*****************
     * 变wifi信道---ron
     * @param channel
     * @param callBack
     */
    public void changeWifiChannel(byte channel,SocketCallBack callBack)
    {
//        RonLog.LogE("changeWifiChannel:"+ channel);
        gduSocket.sendMsg(callBack,(byte)0x8e,AllControlModel, channel);
    }

    /*****************************8G
     * 切换WIFI的名字
     * @param name
     * @param callBack
     */
    public void changeWIFIName(String name,SocketCallBack callBack)
    {
//        RonLog.LogE("已经发送指令----ron");
        gduSocket.sendMsg(callBack,(byte)0x6D,AllControlModel,name.getBytes());
    }

    /*****************************8
     * 切换WIFI的密码
     * @param pwd
     * @param callBack
     */
    public void changeWIFIPWD(String pwd,SocketCallBack callBack)
    {
        gduSocket.sendMsg(callBack,(byte)0x6D,AllControlModel,pwd.getBytes());
    }

    /*********************************
     * 切换网罩模式---ron
     * @param isGauze 00非网罩模式，0x01网罩模式
     * @param socketCallBack
     */
    public void changeGauzeMode(int isGauze,SocketCallBack socketCallBack)
    {
//        RonLog.LogE("开启网罩模式");
//        gduSocket.sendMsg(socketCallBack,(byte)0x8f,FlyControlModel,(byte)isGauze);
    }

    /********************8
     * 获取飞机扫描的当前环境的wifi信息---ron
     */
    public void getWifiScanInfo()
    {
        gduSocket.sendMsg(null,(byte)0x6F,AllControlModel);
    }

    /**
     * <P>shang</P>
     * <P>20171103测试使用，信道扫描指令，3S</P>
     */
    public void getWifiChannelScan(SocketCallBack socketCallBack){
        gduSocket.sendMsg(socketCallBack,(byte)0x1F,AllControlModel,(byte)0xFF);

    }

    public void cancelUSBBackup(SocketCallBack socketCallBack){
        gduSocket.sendMsg(socketCallBack,(byte)0x93,AllControlModel,(byte)0x03);
    }

    public void onDestory() {
        if (gduSocket != null)
            gduSocket.closeConnSocket();
    }

}
