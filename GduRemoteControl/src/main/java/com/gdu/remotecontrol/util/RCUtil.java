package com.gdu.remotecontrol.util;

import com.gdu.remotecontrol.model.GDUConstants;

import java.nio.ByteBuffer;

/**
 * Created by zhangzhilai on 2017/12/9.
 */

public class RCUtil {

    public static int csIsLastVersion;

    public static int getUnsignedByte(byte var0){
        int var1 = var0;
        if (var0 < 0) {
            var1 = var0 + 256;
        }
        return var1;
    }

    public static int get2ByteToInt(byte var0, byte var1) {
        int var = var1 << 8 | getUnsignedByte(var0);
        if(var < 0){
            var += 65536;
        }
        return var;
    }

    public static byte[] getUdpBytes(ByteBuffer byteBuffer, int port, int remotePort, int len) {
        byte [] data = new byte[GDUConstants.BUFFER_SIZE_1024 + 8];
        data[0] = (byte) 1 ;//UDP
        data[1] = (byte) 3 ;//请求头
        data[2] = (byte) (port & 0xFF) ;
        data[3] = (byte) (port >>> 8 & 0xFF) ;
        data[4] = (byte) (remotePort  & 0xFF) ;
        data[5] = (byte) (remotePort >>> 8 & 0xFF) ;
        data[6] = (byte) (len & 0xFF) ;
        data[7] =  (byte) (len >>> 8 & 0xFF) ;
        System.arraycopy(byteBuffer.array(), 0, data, 8, GDUConstants.BUFFER_SIZE_1024);
        return data;
    }

    /**获取TCP包含协议头的字节数组*/
    public static byte[] getTcpBytes(int connType, int localPort, int remotePort, int len) {
        byte[] data = new byte[GDUConstants.BUFFER_SIZE_1024 + GDUConstants.BUFFER_HEADER_LENGTH];
        data[0] = (byte) 0;//tcp
        data[1] = (byte) connType;//connecting
        data[2] = (byte) (localPort & 0xFF);
        data[3] = (byte) (localPort >>> 8 & 0xFF);
        data[4] = (byte) (remotePort & 0xFF);
        data[5] = (byte) (remotePort >>> 8 & 0xFF);
        data[6] = (byte) (len & 0xFF);
        data[7] = (byte) (len >>> 8 & 0xFF);
        return data;
    }

    public static void printSendData(byte[] bytes,  String hint){
        StringBuilder builder = new StringBuilder();
        StringBuilder rawSB = new StringBuilder();
        if (bytes != null) {
            builder.append(bytes[0] + ",");
            builder.append(bytes[1] + ",");
            builder.append(get2ByteToInt(bytes[2], bytes[3]) + ",");
            builder.append(get2ByteToInt(bytes[4], bytes[5]) + ",");
            builder.append(get2ByteToInt(bytes[6], bytes[7]) + ",");
            builder.append(bytes[0] + ",");
            for (int i = 8; i < bytes.length; i++) {
                builder.append((char) bytes[i] + ",");
                rawSB.append(bytes[i] + ",");
            }
            if (bytes[0] == 0) {
                System.out.println("test " + hint + builder.toString());
                System.out.println("test " + hint + rawSB.toString());
            }
            System.out.println("test " + hint + builder.toString());
            System.out.println("test " + hint + rawSB.toString());
        }
    }

}
