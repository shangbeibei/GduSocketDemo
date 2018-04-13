package com.gdu.gdusocket.util;

import com.gdu.gdusocket.SocketCallBack;
import com.gdu.gdusocket.ce.GduBaseUdp;
import com.gdu.gdusocketmodel.GduFrame;
import com.gdu.gdusocketmodel.GduSocketConfig;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangzhilai on 2017/12/10.
 * 长连接通信，缓存工具类
 */

public class CacheCommUtils {

    private static final int CACHE_TIME_OUT = 5;  //缓存超时时间

    private static final int RETRY_TIME = 1;     //重试时间



    /**
     * key 是消息序列号
     */
    private Map<Byte, CacheBean> mCacheBeanMap;

    private boolean isActiveRun = false;

    private Thread mRemoveCacheThread;

    private GduBaseUdp mSocket;

    public CacheCommUtils(GduBaseUdp socket){
        mCacheBeanMap = new ConcurrentHashMap<>();
        mSocket = socket;
        isActiveRun = true;
    }

    public void startWork(){
        isActiveRun = true;
        mRemoveCacheThread = new Thread(removeCacheRun);
        mRemoveCacheThread.start();
    }

    /**
     * 结束缓存任务
     */
    public void finishCache(){
        isActiveRun = false;
        if (mRemoveCacheThread != null) {
            try{
                mRemoveCacheThread.interrupt();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        mRemoveCacheThread = null;
        removeAllCache();
    }

    /**
     * <p>查看缓存中 是否 存在对应的序列号 -- ron</p>
     * @param msgSerial 消息的序列号
     * @return
     */
    public boolean cacheIsExit(byte msgSerial)
    {
        return mCacheBeanMap.containsKey(msgSerial);
    }

    /**
     * <h3>清理掉所有的 缓存 -- ron</h3>
     * <ul>
     *   <li></li>
     * </ul>
     */
    private void removeAllCache() {
        mCacheBeanMap.clear();
    }

    /**
     * <P>将指令存入缓存工具类中</P>
     * @param msgSerial 帧序号
     * @param markFirst CMD  帧的命令字
     * @param markSecond
     * @param cb         socket
     * @param frame 内容
     */
    public void putCache(byte msgSerial, byte markFirst, byte markSecond, SocketCallBack cb, GduFrame frame){
        int time = (int) (System.currentTimeMillis() / 1000);
        CacheBean cache = new CacheBean(markFirst, markSecond, time, cb, frame);
        mCacheBeanMap.put(msgSerial, cache);
    }

    /**
     * <ul>
     *   <li>获取缓存的擦车 -- ron</li>
     *   <li>如果缓存存在，拿出保存的缓存，并清除保存的缓存</li>
     * </ul>
     * @param msgSerial
     * @return
     */
    public CacheBean getCache(byte msgSerial)
    {
        if(mCacheBeanMap.containsKey(msgSerial))
        {
            CacheBean cache = mCacheBeanMap.get(msgSerial);
            mCacheBeanMap.remove(msgSerial);
            return cache;
        }
        return null;
    }


    private Runnable removeCacheRun = new Runnable() {
        @Override
        public void run() {
            while (isActiveRun) {
                Iterator iterator = mCacheBeanMap.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry entry = (Map.Entry) iterator.next();
                    Object key = entry.getKey();
                    CacheBean bean = mCacheBeanMap.get(key);
                    int currentTime = (int) (System.currentTimeMillis() / 1000);

                    if (bean != null && currentTime - bean.getHappenTime() > CACHE_TIME_OUT) {
                        mCacheBeanMap.remove(key);
                        if (bean.cb != null) {
                            bean.cb.callBack(GduSocketConfig.SENDTIMEOUT_CODE, null);
                        } else if(bean != null && currentTime - bean.getRetryTime() > RETRY_TIME){
                            bean.setRetryTime(currentTime);
                            if (mSocket != null) {
                                mSocket.addRetryMsg(bean.frame);
                            }
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };

    public static class CacheBean{
        /**
         * <p>发送的指令Frame</p>
         */
        private GduFrame frame;
        /**
         * <p>socket的回调函数 -- ron</p>
         */
        private SocketCallBack cb;

        /**
         * <p>标示1 -- ron</p>
         */
        public byte markFirst;

        /**
         * <p>标示2 -- ron</p>
         */
        public byte markSecond;

        /**
         * <p>发送的时间 -- ron</p>
         */
        private int happenTime;

        private int retryTime;

        public void setRetryTime(int retryTime)
        {
            this.retryTime = retryTime;
        }

        public int getRetryTime()
        {
            return this.retryTime;
        }

        public int getHappenTime() {
            return happenTime;
        }

        public SocketCallBack getCb() {
            return cb;
        }



        public CacheBean(byte markFirst, byte markSecond, int happenTime,
                         SocketCallBack cb, GduFrame gduFrame){
            this.cb = cb;
            this.markFirst = markFirst;
            this.markSecond = markSecond;
            this.happenTime = happenTime;
            this.frame = gduFrame;
        }
    }
}
