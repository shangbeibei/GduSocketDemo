package com.gdu.remotecontrol.hotplugging;

import com.gdu.remotecontrol.model.GDUComment;
import com.gdu.remotecontrol.model.GDUConstants;

/**
 * Created by zhangzhilai on 2017/12/9.
 */

public class WifiConnectImp implements IConnectUav {
    @Override
    public void setConnectInfo() {
        setHttpInfo();
        setUdpInfo();
        setFtpInfo();
    }

    @Override
    public void setHttpInfo() {
        GDUComment.httpHost = GDUConstants.WIFI_HOST_HTTP;
        GDUComment.urlPic = GDUConstants.WIFI_URL_PIC;
        GDUComment.urlPicPage = GDUConstants.WIFI_URL_PIC_PAGE;
        GDUComment.picDelete = GDUConstants.WIFI_PIC_DELETE;
        GDUComment.videoDelete = GDUConstants.WIFI_VIDEO_DELETE;
        GDUComment.urlVideo = GDUConstants.WIFI_URL_VIDEO;
        GDUComment.urlVideoPage = GDUConstants.WIFI_URL_VIDEO_PAGE;
    }

    @Override
    public void setUdpInfo() {
        GDUComment.udpIp = GDUConstants.WIFI_UAV_HOST_IP;
        GDUComment.udpPort = GDUConstants.WIFI_UDP_FLY_CONTROL_PORT;
    }

    @Override
    public void setFtpInfo() {
        GDUComment.ftpIp = GDUConstants.WIFI_UAV_HOST_IP;
        GDUComment.ftpPort = GDUConstants.WIFI_FTP_PORT;
    }
}