package com.gdu.remotecontrol.coretask;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by zhangzhilai on 2017/12/9.
 */

public abstract class GDUTask implements Runnable, Closeable {
    boolean isClose = false;
    public void close(Closeable obj){
        try {
            if(obj!= null){
                obj.close();
                obj = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
