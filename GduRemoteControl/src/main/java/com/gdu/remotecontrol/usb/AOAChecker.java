package com.gdu.remotecontrol.usb;

import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;

/**
 * Created by zhangzhilai on 2018/1/24.
 * AOA检查类
 */

public class AOAChecker {

    private static final int ACCESSORY_GOT = 0x1;
    private static final int ACCESSORY_CHECK_PERIOD = 1000;

    private UsbManager mUsbManager;
    private Thread mAOACheckThread;
    private boolean isCheckBegin;

    private Handler mHandler;
    private OnAOACheckListener mOnAOACheckListener;

    public AOAChecker(UsbManager usbManager){
        mUsbManager = usbManager;
        initHandler();
    }

    public void setOnAOACheckListener(OnAOACheckListener onAOACheckListener){
        mOnAOACheckListener = onAOACheckListener;
    }

    private void initHandler() {
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case ACCESSORY_GOT:
                        mOnAOACheckListener.onAccessoryGot(msg.obj.toString());
                        break;
                }
            }
        };
    }


    /************************
     * 开始检查AoA的协议----ron
     ************************/
    public void checkAOA(){
        if (mAOACheckThread == null || mAOACheckThread.isAlive()) {
            isCheckBegin = true;
            mAOACheckThread = new Thread(aoaConnRun);
            mAOACheckThread.start();
        }
    }

    public void stopCheckAOA(){
        isCheckBegin = false;
        mAOACheckThread = null;
    }

    private Runnable aoaConnRun = new Runnable() {
        @Override
        public void run() {
            try {
                while (isCheckBegin) {
                    UsbAccessory[] accessoryList = mUsbManager.getAccessoryList();
                    if (accessoryList != null && accessoryList.length > 0) {
                        UsbAccessory mAccessory = accessoryList[0];
                        if (mHandler != null && mAccessory != null)
                            mHandler.obtainMessage(ACCESSORY_GOT, mAccessory.getModel()).sendToTarget();
                    }
                    Thread.sleep(ACCESSORY_CHECK_PERIOD);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public interface OnAOACheckListener{
        void onAccessoryGot(String model);
    }
}
