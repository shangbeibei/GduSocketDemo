package com.gdu.gdudecoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import com.gdu.gdusocketmodel.ConnStateEnum;
import com.gdu.gdusocketmodel.GlobalVariable;
import com.gdu.library.CBUdpSocket;
import com.gdu.library.UdpSocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangzhilai on 2018/1/28.
 */

public class GduDecoder {

    private BlockingQueue<DecoderPkgBean> mWaitDecoderQueue;
    private MediaCodec.BufferInfo mMediaBufferInfo;
    private MediaCodec mMediaDecoder;
    private Surface mSurface;
    private H2642Mp4 mH2642Mp4;

    private Thread mReadDataThread;
    private UdpSocket mUdpSocket;
    private int mNum;
    private boolean hadGetFirstFrame;

    private boolean isDecoderPause;
    private boolean isDecoderStop;
    //是否解码有初始化
    private boolean isDecoderHadInit;
    //正在解码中 ---ron
    private boolean isDecoding;


    public GduDecoder() {
        mWaitDecoderQueue = new LinkedBlockingQueue<>(100);
        mMediaBufferInfo = new MediaCodec.BufferInfo();
        mNum = 0;
    }

    public void init() {
        mReadDataThread = new Thread(readRun);
        mReadDataThread.start();
    }

    private Thread thread;

    public void begin(){
        isDecoding = true;
        thread = new Thread(runnable);
        thread.start();
    }

    public void initSurfaceView(int videoW, int videoH, byte[] sps, byte pps[], Surface surfaceView) {
        mSurface = surfaceView;
        try {
            //创建一个编解码的类型
            mMediaDecoder = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        setDecoderConfig(videoW, videoH, sps, pps);
    }

    public boolean changeDecoderConfig(int videoW, int videoH, byte[] sps, byte pps[]) {
        if (mMediaDecoder == null) {
            return false;
        }
        if (mReadDataThread == null || !mReadDataThread.isAlive()) {
            return false;
        }
        mMediaDecoder.stop();
        setDecoderConfig(videoW, videoH, sps, pps);
        return true;
    }

    /**
     * <P>shang</P>
     * <P>20180206  看到当前</P>
     * <P>需要隔天去查看 硬解码格式  设置  的 sps 和 pps</P>
     */
    public void setDecoderConfig(int videoW, int videoH, byte[] sps, byte pps[]) {
        //创建硬解码的格式 ，包括类型和 宽 高
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", videoW, videoH);
        format.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
        format.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
        //设置播放尺寸
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, videoW * videoH);
        //配置硬解码格式 和 surface
        mMediaDecoder.configure(format, mSurface, null, 0);//blind surfaceView
        mMediaDecoder.start(); //start decode thread
        isDecoderStop = false;
        isDecoderHadInit = true;
    }

    public void startVideo(String fileName) {
        if (mH2642Mp4 == null) {
            mH2642Mp4 = new H2642Mp4();
            mH2642Mp4.setFileName(fileName);
            mH2642Mp4.start();
        }
    }

    public void stopVideo() {
        if (mH2642Mp4 != null) {
            mH2642Mp4.stop();
            mH2642Mp4 = null;
        }
    }

    private String fileNamePic;
    private boolean isSavePic;

    public void savePic(String fileNamePic) {
        this.fileNamePic = fileNamePic;
        isSavePic = true;
//        saveFileUtils.setPath(OUTPATH, fileNamePic);
    }

    public void stopDecoder() {
//        RonLog.LogE("调用了stopDecoder方法");
        isDecoderHadInit = false;
        isDecoderStop = true;
        mWaitDecoderQueue.offer(new DecoderPkgBean());
    }

    /***************************
     * 停止-----ron
     ***************************/
    public void stop() {
        isDecoding = false;
//        thread.interrupt();
        if (mUdpSocket != null) {
            mUdpSocket.stop();
            mUdpSocket = null;
        }
    }

    public int getLostPacent() {
        if (mUdpSocket != null)
            return mUdpSocket.getReceiverData();

        return -1;
    }

    public void onResume() {
        isDecoderPause = false;
        mWaitDecoderQueue.clear();
        hadGetFirstFrame = false;
        if (mUdpSocket != null) {
            mUdpSocket.onResume();
        }
    }

    public void onPause() {
        hadGetFirstFrame = false;
        if (mUdpSocket != null) {
            mUdpSocket.onPause();
            mWaitDecoderQueue.clear();
        }
        isDecoderPause = true;
    }

    private Runnable readRun = new Runnable() {
        @Override
        public void run() {
            if (mUdpSocket == null) {
                mUdpSocket = new UdpSocket();
//                if (GlobalVariable.planType == PlanType.O2Plan_Gek)
//                    udpSocket.droneType((byte) 3);
                mUdpSocket.start(mUdpSocketCallBack, GlobalVariable.UDPSocketIMGPort);
            }
        }
    };

    /*****************************
     *  销毁Decoder对象----ron
     */
    private void onDestoryDecoder() {
        if (mMediaDecoder == null) return;
        for (int i = 0; i < 3; i++) {
            try {
                mMediaDecoder.stop();
                mMediaDecoder.release();//建一个空的缓存区
                mMediaDecoder = null;
                isDecoderStop = false;//必须要加上 和 初始化还没有完成的时候，容易被调用，导致出问题
                return;
            } catch (IllegalStateException e)//IllegalStateException
            {
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (mMediaDecoder != null)
            mMediaDecoder.release();
        mMediaDecoder = null;
    }


    /***************************************
     * 解码线程 ---ron
     ***************************************/
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                while (isDecoding) {
                    System.out.println("开始=====================");

                    if (mMediaDecoder != null) {
                        if (isDecoderStop) {
                            onDestoryDecoder();
                            System.out.println("decoderIsStop ==================");
                            continue;
                        }
                        if (isDecoderHadInit && !isDecoderPause) {
                            try {
                                decodeData();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                            System.out.println("decoder 解码完成一帧=============");
                        } else {
                            System.out.println("Decode未初始化:" + isDecoderHadInit + "," + isDecoderPause);
                            Thread.sleep(500);
                        }
                    } else {
                        System.out.println("开decoder== null=====================");
                        Thread.sleep(500);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            onDestoryDecoder();
        }
    };

    private int inIndex;//输入Index
    private int outIndex;//输出index
    private boolean needReleaseAndNoRender;//需要释放和不渲染

    /**
     * <p>译码，解码数据，获取关键帧I帧</p>
     * @throws InterruptedException
     * @throws IllegalStateException
     */
    private void decodeData() throws InterruptedException, IllegalStateException {
        DecoderPkgBean framData = mWaitDecoderQueue.poll(1000, TimeUnit.MILLISECONDS);
        if (framData == null || mMediaDecoder == null) {
//            RonLog.LogI("frameData == null");
            //1s  如果2s种没收到视频流
            mNum++;
            if (mNum % 2 == 0 && GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess) {
                //TODO 开启
//                if (!GlobalVariable.stopPreViewByUser)
//                    GduApplication.getSingleApp().gduCommunication.beginOrPausePreCamera(false, null);
            }
            return;
        } else if (framData.data == null) {
            return;
        }

//        RonLog.LogE("从队列中拿到数据====================="+hadGetFirstFrame + ","+ (framData.data[4] & 31) + ","+ framData.isI);
        if (!hadGetFirstFrame) {
            if ((framData.data[4] & 31) == 5) {//是I帧
                hadGetFirstFrame = true;
            } else {
                return;
            }
        }
        inIndex = mMediaDecoder.dequeueInputBuffer(50);
//        RonLog.LogI("从队列中拿到数据=====================" + inIndex +"," + framData.position);
        if (inIndex >= 0) {
            ByteBuffer byteBuffer = mMediaDecoder.getInputBuffer(inIndex);
            byteBuffer.clear();
            byteBuffer.put(framData.data, 0, framData.position);
            mMediaDecoder.queueInputBuffer(inIndex, 0, framData.position, 10, 0);
        } else {
//            decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            return;
        }
        // 本地副本 喂 数据，在 H2642Mp4 中 分离出来视频流 并生成本地副本
        if (mH2642Mp4 != null) {
            mH2642Mp4.addData(framData);
        } else {
            framData.data = null;
            framData = null;
        }
        //如果为了拿到解码的数据
        outIndex = mMediaDecoder.dequeueOutputBuffer(mMediaBufferInfo, 10);
        needReleaseAndNoRender = false;
        while (outIndex > -1) {
//            RonLog.LogI("outIndex:===========" + outIndex);
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    break;
                default:
                    if (needReleaseAndNoRender)
                        mMediaDecoder.releaseOutputBuffer(outIndex, false);
                    else {
                        try {
                            mMediaDecoder.releaseOutputBuffer(outIndex, true);
                        } catch (IllegalStateException ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;
            }
            outIndex = mMediaDecoder.dequeueOutputBuffer(mMediaBufferInfo, 10);
            needReleaseAndNoRender = false;
            Thread.sleep(20);
        }
    }

    /**
     * <P>shang</P>
     * <P>如何分析   (data[4] & 31) == 5)  这段数据</P>
     * <P>H264 分为三个 起始码 或者 四个起始码 ，起始码之后是 I帧</P>
     * <P>三个起始码： 0x00 ，0x00，0x01 ，I帧</P>
     * <P>四个起始码： 0x00 ，0x00，0x00，0x01 ，I帧</P>
     * <P>就明白了data[4]的意思，而 data[4] & 31</P>
     * <P>一幅图像根据概念来分可以分为两种：
     * IDR 图像和非 IDR图像。一幅图像是否是 IDR 图像是由组成该图像的 NALU决定的，
     * 如果组成该图像的 NALU 为标准“表7-1”中 nal_unit_type 值为 5 的NALU，则该图像为 IDR 图像，
     * 否则为非 IDR图像，是根据 NALU 数据帧的低5位来判断的，也就是说 0001 1111 ，也就是0X1F ,十进制 是 31</P>
     */
    private CBUdpSocket mUdpSocketCallBack = new CBUdpSocket() {
        @Override
        public void dataCB(byte[] data, int length) {
            if (isDecoderPause) return;
            DecoderPkgBean decoder = new DecoderPkgBean();
            decoder.data = new byte[length];
            if (data != null) {
                System.arraycopy(data, 0, decoder.data, 0, length);
            }
            // == 5 就是I帧？
            if ((data[4] & 31) == 5) {
                decoder.isI = true;
            } else {
                decoder.isI = false;
            }
            decoder.position = length;
            boolean addSuccess = mWaitDecoderQueue.offer(decoder);
            System.out.println("test addSuccess: " + addSuccess);
            if (!addSuccess) {
//                RonLog.LogW("添加解码数据到队列中失败:" + waitQueue.size());
                System.out.println("test size" + mWaitDecoderQueue.size());
            } else {
//                GlobalVariableTest.addDataQueueErrAndSize = -1;
            }
        }

        @Override
        public void stateChange(byte state) {

        }
    };
}
