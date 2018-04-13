package com.gdu.gdusocket.util;

import com.gdu.gdusocket.SocketCallBack;
import com.gdu.gdusocketmodel.GduFrame;
import com.gdu.gdusocketmodel.GduSocketConfig;
import com.gdu.gdusocketmodel.GlobalVariable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangzhilai on 2017/12/10.
 * 1.管理连接的回调
 */

public class FrameParseUtil {
    /***
     * <p>存储周期反馈，根据命令字来找对应的反馈对象  -- ron</p>
     */
    private Map<Byte, SocketCallBack> mCycleACKMap;

    public FrameParseUtil() {
        mCycleACKMap = new HashMap<>();
    }

    /**
     * 添加持久回调对象
     *
     * @param cmd            命令字
     * @param socketCallBack
     */
    public void addCycleACK(byte cmd, SocketCallBack socketCallBack) {
        if (!mCycleACKMap.containsKey(cmd)) {
            mCycleACKMap.put(cmd, socketCallBack);
        } else {
            removeCycleACK(cmd);
            mCycleACKMap.put(cmd, socketCallBack);
        }
    }

    /**
     * <p>清理保存的持久回调对象 -- ron</p>
     *
     * @param cmd 命令字
     */
    public void removeCycleACK(byte cmd) {
        if (cmd == -1) {
            mCycleACKMap.clear();
        } else {
            mCycleACKMap.remove(cmd);
        }
    }

    public void parse(GduFrame gduFrame, CacheCommUtils cacheCommUtils) {
        switch (gduFrame.frame_CMD) {
            //周期反馈的
            case GduSocketConfig.CycleACK_gpsSurround://gpsrr环绕
            case GduSocketConfig.CycleACK_obscale://双目避障反馈
            case GduSocketConfig.CycleACK_panorama://全景拍摄
            case GduSocketConfig.CycleACK_pathPlan://航迹规划反馈
            case GduSocketConfig.CycleACK_checkNorth://校磁反馈
            case GduSocketConfig.CycleACK_HolderFMUpdate://云台升级反馈
            case GduSocketConfig.CycleACK_Heart://心跳反馈
            case GduSocketConfig.CycleACK_takePhoto://拍照的反馈
            case GduSocketConfig.CycleACK_record: //录像的反馈
            case GduSocketConfig.CycleACK_ClearMedia://清理媒体库的反馈
            case GduSocketConfig.CycleACK_UpdateDroneAGPS://AGPS升级
            case GduSocketConfig.CycleACK_UpdateDroneFM://飞机固件升级
            case GduSocketConfig.CycleACK_UpdateDroneOTA://飞机OTA升级
            case GduSocketConfig.CycleACK_RC:
            case GduSocketConfig.CycleACK_VideoTrack://视频跟踪或者视频环绕
            case GduSocketConfig.CycleAck_BatteryUpdate://电池升级的反馈
            case GduSocketConfig.CycleAck_guster://手势拍照
            case GduSocketConfig.CycleACK_Baisc://倒飞自拍 和 垂飞
            case GduSocketConfig.CycleACK_HeadOrientation://一键对尾
            case GduSocketConfig.CycleACK_USBDiskBackup://U盘备份
                parseCycleAck(gduFrame);
                break;
            case GduSocketConfig.CycleACK_gpsTrack://gps跟踪
                GlobalVariable.GPSTrackHadBack = gduFrame.frame_content[0];
                parseCycleAck(gduFrame);
                break;
            case 0x03: ////SN码的获取 ,由于获取SN码，没有ACK反馈
                if (cacheCommUtils.cacheIsExit(gduFrame.frame_Serial)) {
                    CacheCommUtils.CacheBean cacheBean = cacheCommUtils.getCache(gduFrame.frame_Serial);
                    if (gduFrame != null && gduFrame.frame_content.length > 0) {
                        cacheBean.getCb().callBack((byte) 0, gduFrame);
                    }
                }
                break;
            default:
                if (cacheCommUtils.cacheIsExit(gduFrame.frame_Serial)) {
                    CacheCommUtils.CacheBean cacheBean = cacheCommUtils.getCache(gduFrame.frame_Serial);
                    if (gduFrame != null && gduFrame.frame_content.length > 0) {
                        byte ack = gduFrame.frame_content[0];
                        cacheBean.getCb().callBack(ack, gduFrame);
                    }
                }
                break;
        }
    }

    /**
     * <P>仅限 1.视觉 功能 2.gps 跟踪 3.U盘备份的反馈 </P>
     * <P>只要一条指令的 帧命令不为空 就ACK 0 </P>
     *
     * @param gduFrame
     */
    private void parseCycleAck(GduFrame gduFrame) {
        SocketCallBack socketCallBack = mCycleACKMap.get(gduFrame.frame_CMD);
        if (socketCallBack != null) {
            socketCallBack.callBack(GduSocketConfig.SUCCESS_CODE, gduFrame);
        }
    }
}
