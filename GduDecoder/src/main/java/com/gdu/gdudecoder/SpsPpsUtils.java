package com.gdu.gdudecoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Woo on 2017-7-27.
 * Sps 和 pps的帮助类 ---ron
 */

public class SpsPpsUtils {
    byte[] spsAndpps_240;
    byte[] spsAndpps_480;
    byte[] spsAndpps_720;
    byte[] spsAndpps_1080;
    byte[] spsAndpps_360;

    public SpsPpsUtils() {
        initSpsPPs();
    }

    /****************
     * 初始化所有的pps和sps ----ron
     */
    private void initSpsPPs() {
        spsAndpps_240 = new byte[]{(byte) 0x67, (byte) 0x42, (byte) 0x80, (byte) 0x0d, (byte) 0xda, (byte) 0x05, (byte) 0x07, (byte) 0xe8, (byte) 0x06, (byte) 0xd0,
                (byte) 0xa1, (byte) 0x35, (byte) 0x04, (byte) 0x68, (byte) 0xce, (byte) 0x06, (byte) 0xe2};
        spsAndpps_480 = new byte[]{(byte) 0x67, (byte) 0x42, (byte) 0x80, (byte) 0x1e, (byte) 0xda, (byte) 0x02, (byte) 0x80, (byte) 0xf6
                , (byte) 0x80, (byte) 0x6d, (byte) 0x0a, (byte) 0x13, (byte) 0x50, (byte) 0x04, (byte) 0x68, (byte) 0xce, (byte) 0x06, (byte) 0xe2};
        spsAndpps_720 = new byte[]{(byte) 0x67, (byte) 0x42, (byte) 0x80, (byte) 0x1f, (byte) 0xda, (byte) 0x01, (byte) 0x40, (byte) 0x16, (byte) 0xe8
                , (byte) 0x06, (byte) 0xd0, (byte) 0xa1, (byte) 0x35, (byte) 0x04, (byte) 0x68, (byte) 0xce, (byte) 0x06, (byte) 0xe2};
        spsAndpps_1080 = new byte[]{(byte) 0x67, (byte) 0x42, (byte) 0x80, (byte) 0x28, (byte) 0xda, (byte) 0x01, (byte) 0xe0, (byte) 0x08
                , (byte) 0x9f, (byte) 0x96, (byte) 0x01, (byte) 0xb4, (byte) 0x28, (byte) 0x4d, (byte) 0x40, (byte) 0x04, (byte) 0x68, (byte) 0xce
                , (byte) 0x06, (byte) 0xe2};
        spsAndpps_360 = new byte[]
                {
                        (byte) 0x67, (byte) 0x42, (byte) 0x80, (byte) 0x16, (byte) 0xda, (byte) 0x02, (byte) 0x80,
                        (byte) 0xbf, (byte) 0xe5, (byte) 0x80, 0x6d, 0x0a, 0x13, 0x50, (byte) 0x04, (byte) 0x68, (byte) 0xce, (byte) 0x06, (byte) 0xe2
                };

    }

    /************************************
     * 获取sps和pps ----ron
     * @param index 索引号
     * @return
     */
    public List<byte[]> getSpsAndPPS(byte index) {
        byte[] spspps = null;
        List<byte[]> data = new ArrayList<>(2);
        switch (index) {

            case 0:
                spspps = spsAndpps_240;
                break;
            case 1:
                spspps = spsAndpps_480;
                break;
            default://这特么是啥用法
            case 2:
                spspps = spsAndpps_720;
                break;
            case 3:
                spspps = spsAndpps_1080;
                break;
            case 4:
                spspps = spsAndpps_360;

        }
        int spsLength = spspps.length - 5;
        byte[] spsData = new byte[spsLength + 4];
        spsData[0] = 0;
        spsData[1] = 0;
        spsData[2] = 0;
        spsData[3] = 1;
        System.arraycopy(spspps, 0, spsData, 4, spsLength);

        int ppsLength = 4;
        byte[] ppsData = new byte[ppsLength + 4];
        ppsData[0] = 0;
        ppsData[1] = 0;
        ppsData[2] = 0;
        ppsData[3] = 1;
        System.arraycopy(spspps, spspps.length - 4, ppsData, 4, ppsLength);
        data.add(spsData);
        data.add(ppsData);
        return data;
    }

    /***********************
     * 获取vide的高---ron
     * @param index
     * @return
     */
    public int getVideoH(byte index) {
        switch (index) {
            case 0:
                return 240;
            case 1:
                return 480;
            default:
            case 2:
                return 720;
            case 3:
                return 1080;
            case 4:
                return 360;

        }
    }

    /***********************
     * 获取vide的宽---ron
     * @param index
     * @return
     */
    public int getVideoW(byte index) {
        switch (index) {
            case 0:
                return 320;
            case 1:
                return 640;
            default:
            case 2:
                return 1280;
            case 3:
                return 1920;
            case 4:
                return 640;
        }
    }
}
