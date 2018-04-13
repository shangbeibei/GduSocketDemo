package com.gdu.remotecontrol.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import com.gdu.remotecontrol.manager.AbAccessorManager;
import com.gdu.remotecontrol.manager.GDUAccessor;

/**
 * Created by zhangzhilai on 2018/1/30.
 */

public class RCBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        System.out.println("test action: " + action);
        if (AbAccessorManager.ACTION_USB_PERMISSION.equals(action)) {
        } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
            UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
//            if (accessory != null && accessory.equals(GDUAccessor.getInstance().mUsbAccessory)) {
//                ZOUsbAccessor.getInstance().closeAccessory();
//            }
        } else if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
//            Toast.makeText(context,"有USB接入",Toast.LENGTH_SHORT).show();
        }
    }
}
