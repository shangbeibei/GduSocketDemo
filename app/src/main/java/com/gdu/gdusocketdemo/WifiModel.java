package com.gdu.gdusocketdemo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhangzhilai on 2017/12/28.
 */

public class WifiModel implements Serializable{
    private List<String> wifi;
    private List<String> signal;


    public List<String> getWifi() {
        return wifi;
    }

    public void setWifi(List<String> wifi) {
        this.wifi = wifi;
    }

    public List<String> getSignal() {
        return signal;
    }

    public void setSignal(List<String> signal) {
        this.signal = signal;
    }
}
