package com.gdu.gdusocketdemo;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.gdu.gdudecoder.GduDecoder;
import com.gdu.gdudecoder.SpsPpsUtils;
import com.gdu.gdusocket.GduSocketManager;
import com.gdu.gdusocket.SocketCallBack;
import com.gdu.gdusocket.ce.IGduSocket;
import com.gdu.gdusocketmodel.GduFrame;
import com.gdu.gdusocketmodel.GduSocketConfig;
import com.gdu.gdusocketmodel.GlobalVariable;

import java.util.List;

/**
 * Created by zhangzhilai on 2018/1/29.
 * 预览界面
 */

public class PreViewActivity extends Activity implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;
    private GduSocketManager mGduSocketManager;
    private GduDecoder mGduDecoder;

    private TextView mInfoTextView;
    private TextView mPPSTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        initView();
        intiData();
        initListener();
    }

    private void intiData() {
        mGduSocketManager = GduSocketManager.getInstance(this);
        mGduDecoder = new GduDecoder();
        mGduDecoder.begin();
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mInfoTextView = (TextView) findViewById(R.id.info_textview);
        mPPSTextView = (TextView) findViewById(R.id.pps_textview);
        initSurfaceView();
    }

    private void initListener() {
        mGduSocketManager.setDataReceivedListener(new IGduSocket.OnDataReceivedListener() {
            @Override
            public void onDataReceived(GduFrame gduFrame) {
                dataReceived(gduFrame);
            }
        });
    }

    public void dataReceived(GduFrame gduFrame) {
        if (gduFrame == null) {
            return;
        }
        switch (gduFrame.frame_CMD) {
            case GduSocketConfig.CycleACK_Heart:
                pps(gduFrame);
                break;
            case GduSocketConfig.CycleACK_WIFIQuality:
                getWifiQuality(gduFrame);
                break;
        }
    }

    private void pps(GduFrame gduFrame) {
        if (GlobalVariable.ppsspsIndex == -1 && gduFrame.frame_content[9] == 2) {
            if (gduFrame.frame_content[4] != 0)
                GlobalVariable.ppsspsIndex = gduFrame.frame_content[4];
        }
        showText(mPPSTextView, GlobalVariable.ppsspsIndex + "");
    }

    /*****************************************
     * wifi的质量----ron
     * @param gduFrame
     */
    public void getWifiQuality(final GduFrame gduFrame) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("信号强度:").append(gduFrame.frame_content[0]).append("\n");
        stringBuilder.append("信噪比:").append(gduFrame.frame_content[1]).append("\n");
        stringBuilder.append("信噪比:").append(gduFrame.frame_content[1]).append("\n");
        showText(mInfoTextView, stringBuilder.toString());
    }

    private void showText(final TextView view, final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setText(content);
            }
        });
    }

    private void initSurfaceView() {
        //设置画布  背景透明
        mSurfaceView.setZOrderOnTop(true);
        //覆盖在其他的媒体上边（置顶）
        mSurfaceView.setZOrderMediaOverlay(true);
        //设置surface 为半透明。TRANSPARENT 为全透明
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceView.getHolder().addCallback(this);
    }


    public void start_preview(View view) {
        System.out.println("test 11 " + Thread.currentThread().getId());
        mGduSocketManager.getCommunication().beginOrPausePreCamera(false, new SocketCallBack() {
            @Override
            public void callBack(byte code, GduFrame bean) {
                String content = bean != null ? bean.getString() : "null";
                System.out.println("test code " + code + " " + content);
                System.out.println("test 22 " + Thread.currentThread().getId());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startDecoder();
                    }
                });
            }
        });

    }

    public void startDecoder() {
        mGduDecoder.init();
        getIndex();
    }

    private void getIndex() {
        mGduSocketManager.getCommunication().getCameraArgs(new SocketCallBack() {
            @Override
            public void callBack(byte code, GduFrame bean) {
                System.out.println("test spsAndppsIndex code: " + code);
                if (code == 0) {
                    //bean.frame_content[5]; 代表的是那一组分辨率和哪一组sps，pps 头
                    final byte spsAndppsIndex = bean.frame_content[5];  //为何是数组的第 5
                    GlobalVariable.ppsspsIndex = spsAndppsIndex;
                    System.out.println("test spsAndppsIndex : " + spsAndppsIndex);
                    final byte index = spsAndppsIndex;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SpsPpsUtils spsPpsUtils = new SpsPpsUtils();
                            //集合中第一个为sps 第二个为 pps
                            List<byte[]> sp = spsPpsUtils.getSpsAndPPS(index);
                            mGduDecoder.initSurfaceView(spsPpsUtils.getVideoW(index), spsPpsUtils.getVideoH(index), sp.get(0),
                                    sp.get(1), mSurfaceView.getHolder().getSurface());
                        }
                    });
                } else {
//                    RonLog.LogE("开始视频流---获取PPS失败");
//                    Message message = handler.obtainMessage(BEGINPREVIEW,false);
//                    handler.sendMessageDelayed(message,200);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
