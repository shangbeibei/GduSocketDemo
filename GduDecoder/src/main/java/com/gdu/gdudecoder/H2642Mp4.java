package com.gdu.gdudecoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;

import com.gdu.gdusocketmodel.GlobalVariable;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;

/**
 * Created by zhangzhilai on 2018/1/29.
 * //一秒30帧 固定的
 */

public class H2642Mp4 implements Runnable {

    public static String BaseDirectory = Environment
            .getExternalStorageDirectory() + "/gdu/phoneDrone";

    public static String VideoLocalCache = "video/LocalCache";
    /*****************
     * 视频流的索引号---ron
     */
    private String fileName;


    public H2642Mp4() {
        waitQueue = new LinkedBlockingQueue<>(30);
    }


    MediaMuxer mediaMuxer;

    /*******************
     * 需要保存到mp4里面的数据包的队列
     */
    private BlockingQueue<DecoderPkgBean> waitQueue;

    /**********************
     * 该次录像是否结束
     */
    private boolean isFinish;

    private final int BUFFERSIZE = 40960 * 8;

    /*************************
     * 已经添加了多少帧---ron
     */
    private int putVideoCount = 0;

    /*****************************
     * 添加数据到列表中
     * @param data
     */
    public void addData(DecoderPkgBean data) {
        if (!isFinish) {
            waitQueue.add(data);
        }
    }

    private final String OUTPATH = BaseDirectory + "/" + VideoLocalCache + "/";//本地副本的保存路径

    private Thread thread;

    /********************
     * 开始启动副本保存
     */
    public void start() {
//        RonLog.LogE("begin Start");
        isFinish = false;
        putVideoCount = 0;
        waitQueue.clear();
        if (thread != null) return;
//        RonLog.LogE("begin Start------------");
        thread = new Thread(this);
        thread.start();
    }

    /*********************
     * 停止保存副本---ron
     */
    public void stop() {
//        RonLog.LogE("begin stop------------");
        isFinish = true;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /*****************************
     *  保存副本的主要方法---ron
     * @param videoIndex
     * @throws IOException
     * @throws InterruptedException
     */
    private void runMethod(byte videoIndex) throws IOException {
//        RonLog.LogE("开始保存副本");
        mediaMuxer = new MediaMuxer(OUTPATH + fileName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        MediaFormat videoFormat = createVideoFormat(videoIndex);
        int videoTrackIndex = mediaMuxer.addTrack(videoFormat);
//        int radioTrackIndex = mediaMuxer.addTrack(MediaFormat.createAudioFormat(MIMETYPE_AUDIO_AAC,2500,2));
        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFERSIZE);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        mediaMuxer.start();
        try {
            while (!isFinish) {
                DecoderPkgBean framData = waitQueue.poll(1000, TimeUnit.MILLISECONDS);
                if (framData != null && framData.data != null) {
                    byteBuffer.clear();
                    byteBuffer.put(framData.data, 0, framData.position);
                    //偏移量到底是啥意思，功能是干嘛用的
                    bufferInfo.offset = 0;
                    if (framData.isI) {
                        bufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                    } else
                        bufferInfo.flags = 0;
                    bufferInfo.size = framData.position;
                    //为了是的播放每一帧是均匀播放的，每一帧的在播放的时侯是根据 presentationTimeUs 来确定
                    // 每增加一帧，就x 33333 ,一秒30帧
                    bufferInfo.presentationTimeUs = (++putVideoCount) * 33333;
                    mediaMuxer.writeSampleData(videoTrackIndex, byteBuffer, bufferInfo);
                } else {
//                    RonLog.LogW("mp4 数据的DAta为NULL");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //发现快速点击录像 此处会崩溃，try掉----ron
        try {
            mediaMuxer.stop();
            mediaMuxer.release();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            mediaMuxer = null;
        }
//        RonLog.LogE("保存副本完成==================================================333333");

        //副本保存，只保存4s以上的，4s以下的删除----ron
        //一秒30帧 固定的
        if (putVideoCount < 120) {
            File file = new File(OUTPATH + fileName);
            if (file.exists())
                file.delete();
        }
//        EventBus.getDefault().post(new FiterVideoEvent((byte) 3,OUTPATH+fileName));
    }

    /*******************************
     * 创建VideoFormat ,给视频通道配置参数
     * @param videoIndex
     * @return
     */
    private MediaFormat createVideoFormat(byte videoIndex) {
        SpsPpsUtils spsPpsUtils = new SpsPpsUtils();
        /*if(videoIndex == 1)
            videoIndex = 4;*/
        int width = spsPpsUtils.getVideoW(videoIndex);
        int height = spsPpsUtils.getVideoH(videoIndex);
        List<byte[]> spspps = spsPpsUtils.getSpsAndPPS(videoIndex);
        MediaFormat videoFormat = MediaFormat.createVideoFormat(
                MIMETYPE_VIDEO_AVC, width, height);
//        byte[] header_sps = {0, 0, 0, 1, 103, 100, 0, 31, -84, -76, 2, -128, 45, -56};
//        byte[] header_pps = {0, 0, 0, 1, 104, -18, 60, 97, 15, -1, -16, -121, -1, -8, 67, -1, -4, 33, -1, -2, 16, -1, -1, 8, 127, -1, -64};
        videoFormat.setByteBuffer("csd-0", ByteBuffer.wrap(spspps.get(0)));
        videoFormat.setByteBuffer("csd-1", ByteBuffer.wrap(spspps.get(1)));
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
        videoFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
        videoFormat.setInteger(MediaFormat.KEY_CAPTURE_RATE, 30);
        return videoFormat;
    }

    @Override
    public void run() {
        try {
            runMethod(GlobalVariable.heartPpsSps);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
