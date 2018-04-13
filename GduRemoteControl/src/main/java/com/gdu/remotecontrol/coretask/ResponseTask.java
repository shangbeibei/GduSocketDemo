package com.gdu.remotecontrol.coretask;

import com.gdu.remotecontrol.listener.OnResponseListener;
import com.gdu.remotecontrol.model.GDUConstants;
import com.gdu.remotecontrol.util.RCUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by zhangzhilai on 2017/12/9.
 * 接受USB的数据，传给代理服务器
 */

public class ResponseTask extends GDUTask {
    private byte[] mBuffer;
    private OutputStream mUsbToServerOutputStream;  //通过socket以流的形式往代理服务器写数据
    private FileInputStream mUsbInputStream;        //从USB读取数据
    private Socket mUsbToServerSocket;              //USB与server的socket
    private OnResponseListener mOnResponseListener;
    /**
     * @param inputStream 遥控器通过USB传递的数据流
     * @param socketUsb
     */
    public ResponseTask(FileInputStream inputStream, Socket socketUsb) {
        mUsbInputStream = inputStream;
        mUsbToServerSocket = socketUsb;
        mBuffer = new byte[GDUConstants.BUFFER_SIZE_4104];
    }

    public void setOnResponseListener(OnResponseListener onResponseListener){
        mOnResponseListener = onResponseListener;
    }

    /**
     * 1.获取往server发送的输出流（与server建立连接）
     * 2.从usb读取数据流
     * 3.将usb获取的数据写入到往server的输出流中（已建立连接）
     * usb---->server
     */
    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        int len = -1;
        if (null != mUsbInputStream) {
            try {
                mUsbToServerOutputStream = mUsbToServerSocket.getOutputStream(); //代理服务器写入流
                while ((len = mUsbInputStream.read(mBuffer)) != -1 && !isClose) {  //从USB读取数据
                    if (len > 0) {
                        if (mUsbToServerOutputStream != null) {
                            RCUtil.printSendData(mBuffer, "usb到往server");//打印
                            //TODO -----------------TCP为什么发送不成功，以后研究， 现在用回调解决
//                            mUsbToServerOutputStream.write(mBuffer);  //往代理服务器写数据
                            mOnResponseListener.onDataReceived(mBuffer);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (mUsbToServerOutputStream != null) {
                    mUsbToServerOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws IOException {
        isClose = true;
        close(mUsbToServerOutputStream);
        close(mUsbInputStream);
    }
}
