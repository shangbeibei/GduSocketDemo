package com.gdu.gdusocketdemo.cs;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by zhangzhilai on 2018/2/8.
 */

public class GduCSSocket implements IGduCSSocket {

    private final int BUFFLENGTH = 307200;

    //停止
    int isStop = 0;
    //暂停
    int isPause = 0;

    //序列号---ron
    int serialNum = 0;

    byte[] cacheBuff = new byte[BUFFLENGTH];

    /**
     * 当前收到的长度
     */
    int position = 0;

    /**
     * 接收一帧数据的分片包
     */
    int receiveCutNum;

    /**
     * 丢掉的一帧数据的分片包
     */
    int lostCutNum;

    /**
     * 总共收到的包数---ron
     */
    int receiverAllPckNum = 0;

    /**
     * 换成的数据，用来标识上次计算码流的数据
     */
    int cachePckNum = 0;

    /**
     * 总共丢包数---ron
     */
    int lostAllPckNum = 0;


    /**
     * 上一次的丢包总数---ron
     */
    int lastLostAllPckNum = 0;

    /**
     * 上一次收到的包数---ron
     */
    int lastReceiverAllPckNum = 0;

    /**
     * 一帧数据的java 数组
     */
    byte[] oneFrameDataJ;

    /**
     * java数组的长度*
     */
    int jArrayLength;


    private boolean isGeekVersion;

    /**
     * 读线程
     */
    private Thread mReadThread;

    private DatagramSocket mDatagramSocket;

    private DatagramPacket mDatagramPacket;
    /**
     * <p>标示 读写线程，是否可以使用 -- ron</p>
     */
    private boolean isWriteAndReadAble;

    private OnCSReceiveListener mOnCSReceiveListener;

    private byte[] mDataBuffer = new byte[204800];

    public GduCSSocket(OnCSReceiveListener onCSReceiveListener) {
        mOnCSReceiveListener = onCSReceiveListener;
        createSocket();
    }

    public boolean createSocket() {
        if (mReadThread != null && mReadThread.isAlive()) {
            return false;
        }
        if (mDatagramSocket != null) {
            if (mDatagramSocket.isConnected()) {
                mDatagramSocket.close();
            }
        }
        mDatagramPacket = new DatagramPacket(mDataBuffer, mDataBuffer.length);

        try {
            mDatagramSocket = new DatagramSocket(7078);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        mReadThread = new Thread(readRun);
        mReadThread.start();
        isWriteAndReadAble = true;
        return true;
    }


    /**
     * 读线程
     * 从UDP中读取数传数据
     */
    private Runnable readRun = new Runnable() {
        @Override
        public void run() {
            try {
                while (isWriteAndReadAble) {
                    mDatagramSocket.receive(mDatagramPacket);
                    int length = mDatagramPacket.getLength();
                    if (length == 0) {
                        continue;
                    }
                    byte[] data = mDatagramPacket.getData();
//                    printData(data);
                    dealData(data, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private int[] changeData(byte[] data) {
        if (data == null) {
            return null;
        }
        int[] rData = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            if (data[i] < 0) {
                rData[i] = data[i] & 0xff;
            }
        }
        return rData;
    }

    private byte[] changeToBytes(int[] data) {
        if (data == null) {
            return null;
        }
        byte[] rData = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            if (data[i] < 0) {
                rData[i] = (byte) data[i];
            }
        }
        return rData;
    }

    private void printData(byte[] data) {
        if (data == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int b : data) {
            sb.append((b & 0xff) + " ");
        }
        System.out.println("test" + sb.toString());
    }

    private void dealData(byte[] data, int length) {
        if (!isGeekVersion) {
            receiverAllPckNum++;
            normalData(data,length);
        } else {
            geekData(data, length);
        }
    }

    private void normalData(byte[] data,int length) {
        if (length < 14) {
            return;
        }

        int currentSerial = ((data[2] & 0xff) << 8) + (data[3] & 0xff);
//        System.out.println("test recvframe====length:" + length + "serail:" + currentSerial + "serialNum:%d" + serialNum);
        //如果当前包的序列号减一不等于上一次的序列号，则认为中间有丢包
        if (currentSerial - 1 != serialNum && position > 0) {
//            System.out.println("test lost one package====length:");
            int num = currentSerial - 1 - serialNum;  //计算丢包数量
            if (num > 0) {
                lostCutNum += num;
                //计算丢包率的问题
                lostAllPckNum += num;
            }
            serialNum = currentSerial;
            return;
        }
        serialNum = currentSerial;
        int step = (data[13] & 0xc0) >> 6;
        int type = (data[12] & 0x1f);
//        System.out.println("test type:" + type + " step: " + step + " currentSerial:" + currentSerial + " serialNum: " + serialNum);
        if (type == 28) {  //分片数据，重新组装
            if (step == 2) {  //一帧的开始
                position = 0;
                receiveCutNum = 1;
                lostCutNum = 0;
                cacheBuff[0] = 0;
                cacheBuff[1] = 0;
                cacheBuff[2] = 0;
                cacheBuff[3] = 1;
                cacheBuff[4] = (byte) (data[12] & 224 | data[13] & 31);
                position += 5;
                System.arraycopy(data, 14, cacheBuff, position, length - 14);
                position += length - 14;
            } else if (step == 1) { //一帧的结束
//                System.out.println("test receive frame is end");
                if (position < 14) {
                    System.out.println("test position < 14");
                    return;
                }
                if (position + length - 14 > BUFFLENGTH) {
                    position = 0;
                    System.out.println("receive data length too big");
                    return;
                }
                receiveCutNum++;
                if (lostCutNum * 2 > receiveCutNum) {  //如果接受的包数量小于丢包数量的两倍，则认为失败
                    return;
                }
                System.arraycopy(data, 14, cacheBuff, position, length - 14);
                position += length - 14;
                mOnCSReceiveListener.onCSDataReceived(cacheBuff, position);
            } else {
                if (position < 14) {
                    System.out.println("test position < 14");
                    return;
                }
                if (position + length - 14 > BUFFLENGTH) {
                    position = 0;
                    System.out.println("receive data length too big");
                    return;
                }
                System.arraycopy(data, 14, cacheBuff, position, length - 14);
                position += length - 14;
                receiveCutNum++;
            }
        } else { //不是分片
            position = 0;
            cacheBuff[0] = 0;
            cacheBuff[1] = 0;
            cacheBuff[2] = 0;
            cacheBuff[3] = 1;
            position += 4;
            System.arraycopy(data, 12, cacheBuff, position, length - 12);
            position += length - 12;
            mOnCSReceiveListener.onCSDataReceived(cacheBuff, position);
        }
    }

    //存放数据的buffer
//    byte[] buff = new byte[204800];
    int buffPosition = 0;//尾部的索引号
    int begin = 0; //头部起始的索引号
    int mark = 0;

    private void geekData(byte[] buff, int length) {
        buffPosition += length;
        for (int i = 0; i < 15; i++) {
            mark = 0;
            for (int j = begin + 2; j < buffPosition - 4; ++j) {
                if (buff[j + 0] == 0 &&
                        buff[j + 1] == 0 &&
                        buff[j + 2] == 0 &&
                        buff[j + 3] == 2) {
                    mark = j;
                    break;
                }
            }
            if (mark > 5) {
                receiverAllPckNum++;
//                    writeData(lastRtpBuff,mark);
//                    writeData("\n\n",2);
                mark += 4;
//                System.out.println("disposeData:begin:%d,mark:%d,buffPosition:%d", begin, mark,
//                        buffPosition);
//                        writeData(buff + begin,mark-begin);
//                        writeData("\n\n",2);
                //TODO disposeData(buff + begin, 0, mark - begin - 4, env, gObj);
                byte[] temp = new byte[length - begin];
                int l = 0;
                for (int j = begin; j < length; j++) {
                    temp[l++] = buff[j];
                }
                normalData(temp,  mark - begin - 4);
                begin = mark;
                if (buffPosition + 4096 > 204800) {
                    for (int k = 0; k < buffPosition - begin; k++) {
                        buff[k] = buff[k + begin];
                    }
                    buffPosition -= begin;
                    begin = 0;
                }
            } else {
                break;
            }
        }
        //防止出现超出界限的bug
        if ((buffPosition + 4096) > 204800) {
//                    writeStr("buff positon > 204800:",buffPosition);
            buffPosition = 0;
        }
    }


    @Override
    public void stop() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void showLog(byte pause) {

    }

    @Override
    public int getReceiverData() {
        return 0;
    }

    @Override
    public void droneType(byte type) {

    }
}
