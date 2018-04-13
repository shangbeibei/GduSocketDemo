package com.gdu.remotecontrol.manager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;

import com.gdu.gdusocketmodel.ConnStateEnum;
import com.gdu.gdusocketmodel.GlobalVariable;
import com.gdu.remotecontrol.coretask.ProxyServerTask;
import com.gdu.remotecontrol.coretask.RequestTask;
import com.gdu.remotecontrol.hotplugging.IConnectUav;
import com.gdu.remotecontrol.hotplugging.UsbConnectImp;
import com.gdu.remotecontrol.hotplugging.WifiConnectImp;
import com.gdu.remotecontrol.listener.OnResponseListener;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zhangzhilai on 2017/12/9.
 * 附件操作类，代理服务器，对USB的请求，对USB返回数据的处理
 */

public abstract class AbAccessorManager {

    public static final String ACTION_USB_PERMISSION = "com.gdutech.action.USB_PERMISSION";

    private static final long CONNECT_ACCESSORY_PERIOD = 5000L;

    protected boolean isPermissionRequest; //是否获取到USB权限
    protected UsbAccessory mUsbAccessory;
    private PendingIntent mPermissionIntent;
    protected UsbManager mUsbManager;
    //FileDescriptor它可以被写入Parcel并在读取时返回一个ParcelFileDescriptor
    // 对象用于操作原始的文件描述符。ParcelFileDescriptor是原始描述符的一个复制
    private ParcelFileDescriptor mUsbParcelFileDescriptor;
    private FileOutputStream mUsbFileOutputStream;
    private FileInputStream mUsbFileInputStream;
    // 请求task
    private RequestTask mRequestTask;
    private ProxyServerTask mProxyServerTask;

    protected Context mContext;

    private IConnectUav mConnectUav;

    private long mLastConnAccessoryTime = 0;

    public AbAccessorManager() {
    }

    public void init() {
        mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
    }

    public void onResume() {
        if (System.currentTimeMillis() - mLastConnAccessoryTime < CONNECT_ACCESSORY_PERIOD) {
            return;
        }
        try {
            if (mUsbFileInputStream != null && mUsbFileOutputStream != null) {
                return;
            }
            mLastConnAccessoryTime = System.currentTimeMillis();
            getUsbAccessory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getUsbAccessory() throws Exception {
        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = accessories == null ? null : accessories[0];
        if (accessory != null) {
            if (mUsbManager.hasPermission(accessory)) {
                openAccessory(accessory);
            } else {
                synchronized (AbAccessorManager.class) {
                    if (!isPermissionRequest) {
                        mUsbManager.requestPermission(accessory, mPermissionIntent);
                        isPermissionRequest = true;
                    }
                }
            }
        }
    }

    /**
     * 打开usb配件，进行连接
     *
     * @param accessory
     */
    private void openAccessory(UsbAccessory accessory) {
        mUsbParcelFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mUsbParcelFileDescriptor != null) {
            GlobalVariable.connStateEnum = ConnStateEnum.Conn_Sucess;
            setUsbConnect();
            FileDescriptor fd = mUsbParcelFileDescriptor.getFileDescriptor();
            mUsbFileInputStream = new FileInputStream(fd);
            mUsbFileOutputStream = new FileOutputStream(fd);
            createTask();
            openUsbModel();
        }
    }

    public void closeAccessory() {
        setWifiConnect();
        close();
        removeThread();
        closeUsbModel();
        mUsbFileOutputStream = null;
        mUsbFileOutputStream = null;
        mUsbParcelFileDescriptor = null;
        mUsbAccessory = null;
    }

    /**
     * 关闭各种流，清除数据
     */
    public void close() {
        try {
            if (mUsbFileOutputStream != null) {
                mUsbFileOutputStream.close();
            }
            if (mUsbFileOutputStream != null) {
                mUsbFileOutputStream.close();
            }
            if (mUsbParcelFileDescriptor != null) {
                mUsbParcelFileDescriptor.close();
            }
            if (mRequestTask != null) {
                mRequestTask.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (mProxyServerTask != null) {
                mProxyServerTask.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 清除任务
     */
    private void removeThread() {
        try {
            GDUThreadManager.getInstance().removeWorkStealingPool(mRequestTask);
            GDUThreadManager.getInstance().removeWorkStealingPool(mProxyServerTask);
            mRequestTask = null;
            mProxyServerTask = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建代理服务器，并开启相关的通道监听
     * 开始监听usb的流，对流做出相关处理
     */
    private void createTask() {
        if (mProxyServerTask == null) {
            mProxyServerTask = new ProxyServerTask();
            GDUThreadManager.getInstance().addWorkStealingPool(mProxyServerTask);
        }
        mRequestTask = new RequestTask(mUsbFileInputStream, mUsbFileOutputStream, new OnResponseListener() {
            @Override
            public void onDataReceived(byte[] data) {
                mProxyServerTask.setUsbData(data);
            }
        });
        GDUThreadManager.getInstance().addWorkStealingPool(mRequestTask);
    }

    /**
     * 设置USB的基本信息
     */
    private void setUsbConnect() {
        mConnectUav = new UsbConnectImp();
        mConnectUav.setConnectInfo();
    }

    /**
     * 设置wifi连接
     */
    private void setWifiConnect() {
        mConnectUav = new WifiConnectImp();
        mConnectUav.setConnectInfo();
    }

    abstract void openUsbModel();

    abstract void closeUsbModel();
}
