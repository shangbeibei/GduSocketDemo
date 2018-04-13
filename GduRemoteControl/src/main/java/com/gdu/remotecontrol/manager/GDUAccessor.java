package com.gdu.remotecontrol.manager;

import android.content.Context;
import android.hardware.usb.UsbManager;

import com.gdu.remotecontrol.listener.OnGDUUsbListener;

/**
 * Created by zhangzhilai on 2017/12/9.
 * 附件管理类，功能：
 * usb附件打开，关闭
 */

public class GDUAccessor extends AbAccessorManager {

    private OnGDUUsbListener mOnUsbListener;

    public GDUAccessor(Context context, UsbManager usbManager){
        mContext = context;
        init();
        mUsbManager = usbManager;
    }

    public void setOnUsbListener(OnGDUUsbListener onUsbListener){
        mOnUsbListener = onUsbListener;
    }


    @Override
    void openUsbModel() {
        if (mOnUsbListener != null) {
            mOnUsbListener.openUsbModel();
        }
    }

    @Override
    void closeUsbModel() {
        close();
        if (mOnUsbListener != null) {
            mOnUsbListener.closeUsbModel();
        }
    }

}
