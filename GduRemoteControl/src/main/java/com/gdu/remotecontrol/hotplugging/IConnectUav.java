package com.gdu.remotecontrol.hotplugging;

/**
 * Created by zhangzhilai on 2017/12/9.
 */

public interface IConnectUav {
    void setConnectInfo();
    void setHttpInfo();
    void setUdpInfo();
    void setFtpInfo();
}
