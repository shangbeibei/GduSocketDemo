package com.gdu.gdusocket.ce;


import com.gdu.gdusocketmodel.GduFrame;

/**
 * Created by zhangzhilai on 2017/12/10.
 */

public abstract class GduBaseUdp {
    /**
     * <p>添加重试Frame  -- ron</p>
     * @param frame
     */
    public abstract void addRetryMsg(GduFrame frame);

}
