package com.gdu.remotecontrol.hotplugging;

import com.gdu.remotecontrol.model.GDUComment;
import com.gdu.remotecontrol.model.GDUConstants;

/**
 * Created by zhangzhilai on 2017/12/9.
 */

public class UsbConnectImp implements IConnectUav {
    @Override
    public void setConnectInfo() {
        setHttpInfo();
        setUdpInfo();
        setFtpInfo();
    }

    @Override
    public void setHttpInfo() {
        GDUComment.httpHost = GDUConstants.USB_HOST_HTTP;
        GDUComment.urlPic = GDUConstants.URL_PIC;
        GDUComment.urlPicPage = GDUConstants.URL_PIC_PAGE;
        GDUComment.picDelete = GDUConstants.PIC_DELETE;
        GDUComment.videoDelete = GDUConstants.VIDEO_DELETE;
        GDUComment.urlVideo = GDUConstants.URL_VIDEO;
        GDUComment.urlVideoPage = GDUConstants.URL_VIDEO_PAGE;
    }

    @Override
    public void setUdpInfo() {
        GDUComment.udpIp = GDUConstants.LOCAL_IP;
        GDUComment.udpPort = GDUConstants.LOCAL_PORT_UDP;
    }

    @Override
    public void setFtpInfo() {
        GDUComment.ftpIp = GDUConstants.LOCAL_IP;
        GDUComment.ftpPort = GDUConstants.LOCAL_PORT_FTP_CMD;
    }
}
