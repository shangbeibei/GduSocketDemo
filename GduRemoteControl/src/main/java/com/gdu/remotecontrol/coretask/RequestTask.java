package com.gdu.remotecontrol.coretask;

import com.gdu.remotecontrol.listener.OnResponseListener;
import com.gdu.remotecontrol.manager.GDUThreadManager;
import com.gdu.remotecontrol.model.GDUConstants;
import com.gdu.remotecontrol.util.RCUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by zhangzhilai on 2017/12/9.
 * 建立两个连接
 * 1.通过流的形式从USB读取数据，通过socket形式写入到代理服务器
 * 2.将数据写入代理服务器，然后通过socket以流的形式写入到USB
 * <P>请求TASK</P>
 */

public class RequestTask extends GDUTask{

    /********* 属于USB  ********/
    private FileOutputStream mFileOutputStream;   //写数据到USB
    private FileInputStream mFileInputStream;     //读取USB数据
    /********** 属于USB *******/

    private Socket mServerToUsbSocket;      //和代理服务器建立连接
    private InputStream mServerToUsbStream; //从代理服务器获取数据
    //响应Task
    private ResponseTask mCommandTask;      //读取usb数据写入代理服务器 usb ---> proxy server

    private byte[]  mWriteBuffer;  //从代理服务器读取数据，然后写入到USB的缓冲，数据长度+零度头（8byte）
    //响应监听
    private OnResponseListener mOnResponseListener;
    /**
     *
     * @param inputStream  读取USB数据
     * @param outputStream 写数据到USB
     */
    public RequestTask(FileInputStream inputStream, FileOutputStream outputStream, OnResponseListener onResponseListener){
        mOnResponseListener = onResponseListener;
        mFileInputStream = inputStream;
        mFileOutputStream = outputStream;
        mWriteBuffer = new byte[GDUConstants.BUFFER_SIZE_1024 + GDUConstants.BUFFER_HEADER_LENGTH];
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO); //标准音乐播放使用的线程优先级
        try {
            while (!isClose){
                try {
//                    getServerToUsbStream();
                    if (mCommandTask == null) {
                       getResponseTask();
                    }
                    //捕捉IO异常说明USB断开，跳出最外层while循环，以便线程可被回收。
                    if (mCommandTask != null) {
                        operateData();//管理数据
                    } else {
                        Thread.sleep(100);
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 1.首先建立与代理服务器的socket，获取输入流写入到usb；   proxy server ---> usb
     * @throws IOException
     */
    private void getServerToUsbStream() throws IOException{
        System.out.println("test port: " + GDUConstants.LOCAL_PORT_PROXY_SERVER);
        mServerToUsbSocket = new Socket(InetAddress.getLocalHost(), GDUConstants.LOCAL_PORT_PROXY_SERVER);
        mServerToUsbStream = mServerToUsbSocket.getInputStream();
    }

    /**
     * 2.创建ResponseTask ,从FileInputStream(usb),通过Socket读取usb数据写入代理服务器；
     * usb ---> proxy server
     *
     * @return
     * @throws IOException
     */
    private ResponseTask getResponseTask() throws IOException{
        System.out.println("test getResponseTask");
        mServerToUsbSocket = new Socket(InetAddress.getLocalHost(), GDUConstants.LOCAL_PORT_PROXY_SERVER);
        System.out.println("test getResponseTask 22");
        mServerToUsbStream = mServerToUsbSocket.getInputStream();
        mCommandTask = new ResponseTask(mFileInputStream, mServerToUsbSocket);
        mCommandTask.setOnResponseListener(mOnResponseListener);
        GDUThreadManager.getInstance().addWorkStealingPool(mCommandTask);
        return mCommandTask;
    }

    /**proxy server发送数据到usb*/
    /**读取Client 数据到代理服务器，然后从代理服务器写入usb*/
    private void operateData() throws IOException {
        while (mServerToUsbStream.read(mWriteBuffer) != -1 && !isClose) {
            try {
                if (mFileOutputStream != null ) {
                   mFileOutputStream.write(mWriteBuffer);
                    RCUtil.printSendData(mWriteBuffer, "server发送数据到usb");
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }



    @Override
    public void close() throws IOException {
        isClose = true;
        mWriteBuffer = null;
        close(mServerToUsbStream);
        close(mServerToUsbSocket);
        close(mCommandTask);
        GDUThreadManager.getInstance().removeWorkStealingPool(mCommandTask);
    }
}
