package com.gdu.gdusocket.util;

/**
 * Created by zhangzhilai on 2017/12/10.
 */

public class XOR {
    /**
     * <p>异或校验 -- ron</p>
     *
     * @param data   数据的数组
     * @param offset 从哪一个索引开始
     * @param num    总共要异或多少个字节
     * @return 异或校验的值
     */
    public static byte xorCmd(byte[] data, int offset, int num) {
        int xorData = 0;
        int tag = num + offset;

        for (int i = offset; i < tag; i++) {
            xorData ^= data[i];
        }
        return (byte) xorData;
    }

    /**
     * <p>异或校验 -- ron</p>
     *
     * @param data 是一帧的数据
     * @return 异或校验的值
     */
    public static byte xorCmd(byte[] data) {
        return xorCmd(data, 1, data.length - 3);
    }
}

