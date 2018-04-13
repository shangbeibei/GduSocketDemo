package com.gdu.gdusocket.ce;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.gdu.gdusocket.SocketCallBack;
import com.gdu.gdusocket.util.CacheCommUtils;
import com.gdu.gdusocket.util.FrameParseUtil;
import com.gdu.gdusocket.util.GduFrameUtil;
import com.gdu.gdusocket.util.XOR;
import com.gdu.gdusocketmodel.ConnStateEnum;
import com.gdu.gdusocketmodel.GduFrame;
import com.gdu.gdusocketmodel.GduSocketConfig;
import com.gdu.gdusocketmodel.GlobalVariable;
import com.gdu.util.DataUtils;
import com.gdu.util.NetUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangzhilai on 2017/12/10.
 * 主要功能
 * 1.数传数据用UDP的传输方式进行数据的传输和接收
 */

public class GduCESocket extends GduBaseUdp implements IGduSocket {

    private Context mContext;

    private static final int DISCONNECT_TIME = 8;

    private static final int MAX_FRAME_SEND_SIZE = 100;

    private DatagramSocket mDatagramSocket;

    private DatagramPacket mDatagramPacket;

    private byte[] mDataBuffer = new byte[1024];

    private SocketCallBack mSocketCallBack;

    private OnConnectListener mOnConnectListener;

    private OnDataReceivedListener mOnDataReceivedListener;
    /***
     *
     * frame的序列号 0-(-5)
     */
    private byte mSocketSerial;

    /**
     * 发送数据帧队列
     */
    private BlockingQueue<GduFrame> mSendFrameQueue;

    /**
     * 解析工具类
     */
    private FrameParseUtil mFrameParseUtil;

    /**
     * 缓存工具类
     */
    private CacheCommUtils mCacheCommUtils;


    /**
     * <p>GduFrame的工具类 --ron</p>
     */
    private GduFrameUtil mGduFrameUtil;

    private String mWebIp;

    private int mWebPort;

    /***
     * <p>上一次收到数据的时间</p>
     */
    private int mLastReceiverDataTime;

    /*****************
     * 普通指令的地址 ---ron
     */
    private InetAddress mInetAddress;

    /***********************
     * 升级指令的端口号
     */
    private int mUpdatePort;

    /**
     * 读线程
     */
    private Thread mWriteThread;

    /**
     * 写线程
     */
    private Thread mReadThread;


    /**
     * 检查连接线程
     */
    private Thread mCheckConnThread;

    /**
     * <p>标示 读写线程，是否可以使用 -- ron</p>
     */
    private boolean isWriteAndReadAble;

    /**
     * 标识是否连接server
     */
    private boolean isServerConnected;

    private boolean hadGetData;


    /**
     * <p>保存每个指令最后一次接受到的Serial</p>
     */
    private Map<Byte, Byte> mCacheSerialMap;  //TODO ?????

    public GduCESocket(Context context) {
        mContext = context;
        mSendFrameQueue = new LinkedBlockingDeque<>(MAX_FRAME_SEND_SIZE);
        mFrameParseUtil = new FrameParseUtil();
        mCacheCommUtils = new CacheCommUtils(this);
        mCacheSerialMap = new HashMap<>();
        mGduFrameUtil = new GduFrameUtil();
        //TODO
    }

    /**
     * <p>设置webip 和 web端口</p>
     *
     * @param webIp
     * @param webPort
     * @return
     */
    public boolean setIpAndPort(String webIp, int webPort, int portUpdate) {
        try {
            mWebIp = webIp;
            mWebPort = webPort;
            mUpdatePort = portUpdate;
            mInetAddress = InetAddress.getByName(webIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 因为UDP发送时，每个包都会带上目的ip地址和端口号，所以可以动态的改变地址和端口
     * 重置UDP的目的ip地址和端口
     *
     * @param webIp
     * @param webPort
     * @return
     */
    public boolean resetIpAndPort(String webIp, int webPort) {
        mWebIp = webIp;
        mWebPort = webPort;
        try {
            mInetAddress = InetAddress.getByName(webIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public void setConnCallBack(SocketCallBack socketCallBack) {
        mSocketCallBack = socketCallBack;
    }


    public void setOnConnectListener(OnConnectListener onConnectListener) {
        mOnConnectListener = onConnectListener;
    }

    public void setOnDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
        mOnDataReceivedListener = onDataReceivedListener;
    }

    /**
     * 命令字做key，添加监听回调
     *
     * @param cmd
     * @param socketCallBack
     */
    public void addCycleAck(byte cmd, SocketCallBack socketCallBack) {
        mFrameParseUtil.addCycleACK(cmd, socketCallBack);
    }

    /**
     * 移除当前命令字的监听回调
     *
     * @param cmd
     */
    public void removeCycleAck(byte cmd) {
        mFrameParseUtil.removeCycleACK(cmd);
    }

    /**
     * <p>添加发送 帧 -- ron</p>
     * <p>帧的序列号 再此处生成</p>  //TODO 序列号？？？
     */
    public boolean addSendFrame(GduFrame frame, SocketCallBack socketCallBack) {
        if (!isServerConnected) {
            if (socketCallBack != null) {
                socketCallBack.callBack(GduSocketConfig.CONNERR_CODE, null);
            }
            return false;
        }
        if (mSendFrameQueue.offer(frame)) {
            if (socketCallBack != null) {
                mCacheCommUtils.putCache(frame.frame_Serial, frame.frame_CMD, (byte) 0, socketCallBack, frame);
            }
            return true;
        } else {
            if (socketCallBack != null) {
                socketCallBack.callBack(GduSocketConfig.SENDERR_CODE, null);
            }
            return false;
        }
    }

    /**
     * 创建可以读写的UDP socket
     *
     * @param webIp
     * @param webPort
     * @param portUpdate
     * @return
     */
    public boolean createSocket(String webIp, int webPort, int portUpdate) {
        if (mReadThread != null && mReadThread.isAlive()) {
            return false;
        }
        mWebIp = webIp;
        mWebPort = webPort;
        mUpdatePort = portUpdate;

        isServerConnected = false;

        if (TextUtils.isEmpty(webIp)) {
            return false;
        }

        if (mDatagramSocket != null) {
            if (mDatagramSocket.isConnected()) {
                mDatagramSocket.close();
            }
        }
        mDatagramPacket = new DatagramPacket(mDataBuffer, mDataBuffer.length);
        try {
            mInetAddress = InetAddress.getByName(webIp);
            mDatagramSocket = new DatagramSocket(GlobalVariable.UDP_SOCKET_PORT);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        isWriteAndReadAble = true;

        /**移出缓存指令线程**/
        mCacheCommUtils.startWork();
        /**读取线程**/
        mReadThread = new Thread(readRun);
        mReadThread.start();
        /**写入线程**/
        mWriteThread = new Thread(writeRun);
        mWriteThread.start();

        mLastReceiverDataTime = (int) (System.currentTimeMillis() / 1000);
        /**检查连接线程**/
        mCheckConnThread = new Thread(checkConnRun);
        mCheckConnThread.start();

        isServerConnected = true;

        return true;
    }

    /**
     * 写线程
     * 将app要发送的指令，从发送队列取出以UDP的方式发送出去
     */
    private Runnable writeRun = new Runnable() {
        @Override
        public void run() {
            try {
                while (isWriteAndReadAble) {
                    GduFrame frame = mSendFrameQueue.poll(GduSocketConfig.WriteWaitTimeOut, TimeUnit.SECONDS);
                    if (frame != null) {
                        if (GlobalVariable.RC_usb_hadConn == 1 || NetUtils.isNetworkConnected(mContext)) { //TODO 添加网络判断
                            byte[] data = GduFrameUtil.gduFrame2ByteArray(frame);
                            DatagramPacket pck = null;
                            if (frame.isUpdate) {
                                pck = new DatagramPacket(data, 0, data.length,
                                        mInetAddress, mUpdatePort);
                            } else {  /**mInetAddress : 目标主机地址 mWebPort: 目标主机端口 **/
                                pck = new DatagramPacket(data, 0, data.length, mInetAddress, mWebPort);
                            }
                            try {
                                mDatagramSocket.send(pck);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {

            }
        }
    };

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
                    /**是否匹配指令长度**/
                    int le = mGduFrameUtil.checkFrameHead(mDatagramPacket.getData(), 0) + 4;
                    if (le > 30 || le < 3) {
                        continue;
                    }
                    /**校验帧尾**/
                    if (mDatagramPacket.getData()[le - 1] == (byte) 0xf0) {
                        if (mGduFrameUtil.checkCode(mDatagramPacket.getData(), mDatagramPacket.getOffset(), le)) {
                            parseFrame(mDatagramPacket.getData(), mDatagramPacket.getOffset(), le);
                        } else {
                            continue;
                        }
                    } else {
                        //TODO 帧尾匹配失败
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 发送心跳包，进行连接的检查
     * 每隔1秒，发送一次心跳包
     */
    private Runnable checkConnRun = new Runnable() {
        @Override
        public void run() {
            try {
                while (isWriteAndReadAble) {
                    connectCheck();
                    // 发送时间
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * <P>检查和飞机连接的状态</P>
     * <P>20180105-看到当前</P>
     */
    private void connectCheck() {
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        /**这里相当于连接成功的情况走这里【判断是否断连】【延迟是否过大】**/
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_None) {
            if (mLastReceiverDataTime > 1000 && currentTime - mLastReceiverDataTime > DISCONNECT_TIME) {
                GlobalVariable.connStateEnum = ConnStateEnum.Conn_None;
                mOnConnectListener.onDisConnect();
//                        if (getDroneInfoHelper != null)  //TODO 解耦到回调
                GlobalVariable.ppsspsIndex = -1;
//                        EventBus.getDefault().post(new EventConnState(GlobalVariable.connStateEnum));
                //连接失败
            }

            if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess &&
                    mLastReceiverDataTime > 1000 && currentTime - mLastReceiverDataTime > 2) {
                GlobalVariable.wifiDelay = true;
                mOnConnectListener.onConnectDelay(true);
                //wifi延迟过大"
            }
        }

        byte[] data = new byte[21];
        System.arraycopy(DataUtils.int2byte(GlobalVariable.LatPhone), 0, data, 0, 4);
        System.arraycopy(DataUtils.int2byte(GlobalVariable.LonPhone), 0, data, 4, 4);
//                   RonLog.LogE("GPS_Phone:"+GlobalVariable.LatPhone + ","+ GlobalVariable.LonPhone);
        //发送心跳包
        GduFrame gduFrame = createFrame(GduSocketConfig.CycleACK_Heart, GduSocketConfig.AllControlModel, data);

        boolean isSuccess = mSendFrameQueue.offer(gduFrame);
        /**一旦从阻塞队列再也取不出数据的话，则表示数据传输中断，则清除阻塞队列**/
        if (!isSuccess) {
            mSendFrameQueue.clear();
        }
    }

    /**
     * 解析一帧的数据，已经做过校验和数据验证
     *
     * @param buffer 包含完整一帧数据
     * @param index  数据开始的位置   都是 0
     * @param le     数据的长度
     */
    private void parseFrame(byte[] buffer, int index, int le) {
        GduFrame gduFrame = GduFrameUtil.byteArray2GduFrame(buffer, index, le);
        switch (gduFrame.frame_CMD) {
            case GduSocketConfig.CycleACK_DroneWifi:
                hadGetData = true;
                GlobalVariable.wifiDelay = false;
                break;
            case GduSocketConfig.CycleACK_Heart:
                dealHeartData(gduFrame);
                break;
            case 97:
                dealHeartData(gduFrame);
                break;
            default:
                if (gduFrame.frame_Serial != GduSocketConfig.InvalidateSerial) {
                    Byte result = mCacheSerialMap.get(gduFrame.frame_CMD);
                    if (result == null || result != gduFrame.frame_Serial) {
                        mCacheSerialMap.put(gduFrame.frame_CMD, gduFrame.frame_Serial);
                    } else {
                        return;
                    }
                }
                mFrameParseUtil.parse(gduFrame, mCacheCommUtils);
        }
    }

    /**
     * 处理心跳包
     */
    private void dealHeartData(GduFrame gduFrame) {
        hadGetData = true;
        mLastReceiverDataTime = (int) (System.currentTimeMillis() / 1000);
        mOnConnectListener.onConnectDelay(false);
        mOnConnectListener.onConnect();
        if (mOnDataReceivedListener != null) {
            mOnDataReceivedListener.onDataReceived(gduFrame);
        }
    }

    /**
     * 关闭长连接Socket
     */
    public void closeConnSocket() {
        isWriteAndReadAble = false;
        if (mReadThread != null && mReadThread.isAlive()) {
            mReadThread.interrupt();
        }

        if (mWriteThread != null && mWriteThread.isAlive()) {
            mWriteThread.interrupt();
        }

        if (mCacheCommUtils != null) {
            mCacheCommUtils.finishCache();
        }
    }

    /**
     * <p>发生一条指令 -- ron</p>
     *
     * @param socketCallBack 反馈对象
     * @param cmd            命令字
     * @param to             发生目的地
     * @param data           发生的数据
     */
    public void sendMsg(SocketCallBack socketCallBack, byte cmd, byte to, byte... data) {
        sendMsg(false, socketCallBack, cmd, to, data);
    }

    public void sendMsg(boolean isUpdate, SocketCallBack socketCallBack, byte cmd, byte to, byte... data) {
        GduFrame gduFrame = createFrame(cmd, to, data);
        gduFrame.isUpdate = isUpdate;
        addSendFrame(gduFrame, socketCallBack);
    }

    public GduFrame createFrame(byte cmd, byte to, byte... data) {
        GduFrame gduFrame = new GduFrame();
        gduFrame.frame_CMD = cmd;
        gduFrame.frame_content = data;

        mSocketSerial++;
        if (mSocketSerial < -5) {
            //不明白为何要小于 -5 的时候
            mSocketSerial = 0;
        }
       /* Log.e("createFrame",mSocketSerial+"");*/
        gduFrame.frame_Serial = mSocketSerial;
        if (cmd == 97) {
            gduFrame.frame_To = 114;
            gduFrame.frame_Serial = 123;
        } else {
            gduFrame.frame_To = to;
        }

        gduFrame.getFrameLength();

        gduFrame.frame_CheckCode = XOR.xorCmd(GduFrameUtil.gduFrame2ByteArray(gduFrame));
        return gduFrame;
    }

    @Override
    public void addRetryMsg(GduFrame frame) {

    }
}
