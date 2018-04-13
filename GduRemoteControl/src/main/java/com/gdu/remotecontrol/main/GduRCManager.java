package com.gdu.remotecontrol.main;

import android.content.Context;
import android.hardware.usb.UsbManager;

//import com.gdu.gdusocket.GduCommunication;
//import com.gdu.gdusocket.GduSocketManager;
//import com.gdu.gdusocket.SocketCallBack;
//import com.gdu.gdusocket.model.ConnStateEnum;
//import com.gdu.gdusocket.model.ConnType;
//import com.gdu.gdusocket.model.GduFrame;
//import com.gdu.gdusocket.model.GlobalVariable;
import com.gdu.gdusocketmodel.ConnStateEnum;
import com.gdu.gdusocketmodel.GlobalVariable;
import com.gdu.remotecontrol.listener.OnGDUUsbListener;
import com.gdu.remotecontrol.manager.GDUAccessor;
import com.gdu.remotecontrol.usb.AOAChecker;

/**
 * Created by zhangzhilai on 2018/1/24.
 * 遥控器管理类
 */

public class GduRCManager implements IRCManager{

    private static GduRCManager mGduRCManager;

    private Context mContext;
    private GDUAccessor mGDUAccessor;  //USB附件管理类
    private AOAChecker mAOAChecker;  //AOA检查类
    private UsbManager mUsbManager;  //USB操作类
//    private GduSocketManager mGduSocketManager;
//    private GduCommunication mGduCommunication;
    private OnRCManagerListener mOnRCManagerListener;


    public static GduRCManager getInstance(Context context){
        if (mGduRCManager == null) {
            mGduRCManager = new GduRCManager(context);
        }
        return mGduRCManager;
    }


    private GduRCManager(Context context){
        mContext = context;
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mGDUAccessor = new GDUAccessor(context, mUsbManager);
        mAOAChecker = new AOAChecker(mUsbManager);
//        mGduSocketManager = GduSocketManager.getInstance();
//        mGduSocketManager.init(context);
//        mGduCommunication = mGduSocketManager.getCommunication();
        initListener();
    }

    public void setOnRCManagerListener(OnRCManagerListener listener){
        mOnRCManagerListener = listener;
    }

    private void initListener() {
        mAOAChecker.setOnAOACheckListener(new AOAChecker.OnAOACheckListener() {
            @Override
            public void onAccessoryGot(String model) { //获取到附件
                mOnRCManagerListener.onAOACheck(model);
                accessoryGot(model);
            }
        });
        mGDUAccessor.setOnUsbListener(new OnGDUUsbListener() {
            @Override
            public void openUsbModel() {
                mOnRCManagerListener.onUsbOpen();
//                GduApplication.getSingleApp().gduCommunication.
//                        reSetInetAdd(ZOComment.udpIp, ZOComment.udpPort);
//                mGduSocketManager.getCommunication().reSetInetAdd("127.0.0.1", 7088);
//                GlobalVariable.RC_usb_hadConn = 1;
//                GlobalVariable.connType = ConnType.MGP03_RC_USB;
                System.out.println("test openUsbModel");
//                if (rcConnListener != null) {
//                    rcConnListener.isConn(true);
//                }
            }

            @Override
            public void closeUsbModel() {
                mOnRCManagerListener.onUsbClose();
                System.out.println("test openUsbModel");
            }
        });
    }

    /**
     * 获取到连接的附件后
     * 开启代理服务器，开启usb数据发送和接收的
     * @param model
     */
    private void accessoryGot(String model){
        System.out.println("test model: " + model);
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
            if (model.equals("demo")) {
                if (mGDUAccessor != null) {
                    mGDUAccessor.onResume();
                }
            }
        }
//        else if (msg.obj.toString().equals("AccessoryPassthrough")) {
//            if (GduApplication.getSingleApp().accessoryEngine == null)
//                GduApplication.getSingleApp().accessoryEngine = new AccessoryEngine
//                        (MainActivity.this, GduApplication.getSingleApp().engineCallBack);
//            GduApplication.getSingleApp().accessoryEngine.onNewIntent(null);
//        }
    }

    /**
     * 开始检查连接usb的附件
     */
    public void startCheck(){
        mAOAChecker.checkAOA();
    }

    /**
     * 关闭连接
     */
    public void closeConnect(){
        mAOAChecker.stopCheckAOA();
        mGDUAccessor.closeAccessory();
    }

}
