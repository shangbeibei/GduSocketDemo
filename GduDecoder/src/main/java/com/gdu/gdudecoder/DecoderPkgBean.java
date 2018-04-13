package com.gdu.gdudecoder;

/**
 * Created by zhangzhilai on 2018/1/28.
 */

public class DecoderPkgBean {
    public boolean isI;
    public byte[] data;
    public int position;

    public DecoderPkgBean(){}

    public DecoderPkgBean(boolean isI, byte[] data, int position) {
        this.isI = isI;
        this.data = data;
        this.position = position;
    }
}
