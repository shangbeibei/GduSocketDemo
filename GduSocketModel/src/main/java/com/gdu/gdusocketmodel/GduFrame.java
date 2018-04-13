package com.gdu.gdusocketmodel;

/**
 * Created by zhangzhilai on 2017/12/10.
 */

/**
 * Created by ron on 2016/5/6.
 * <p>Socket通讯的帧类</p>
 * <ul>
 *     <li>考虑到帧经常 创建，销毁，所以就 不设置 set 和 get了</li>
 * </ul>
 */
public class GduFrame
{
    /**
     * <p>帧头--ron</p>
     */
    public byte frame_Header = 0x55;

    /**
     * <p>帧的长度，数据长度 = 字节数求和（地址+命令字+包序号+数据段)--ron</p>
     */
    public byte frame_length;

    /**
     * <p>帧的目的地 -- ron </p>
     */
    public byte frame_To;

    /**
     * <p>帧的命令字 -- ron</p>
     */
    public byte frame_CMD;


    /**
     * <p>帧的序列号 -- ron</p>
     */
    public byte frame_Serial;



    /**
     * <p>具体的帧内容了 -- ron</p>
     */
    public byte[] frame_content;

    /**
     * <p>帧的校验码 --- ron</p>
     */
    public byte frame_CheckCode;

    /**
     * <p>帧的结尾符 ---ron</p>
     */
    public byte frame_end = (byte) 0xf0;

    /**
     * <p>获取帧的长度  --- ron</p>
     * @return
     */
    public byte getFrameLength()
    {
        this.frame_length = (byte)( 3 + (frame_content == null?0:frame_content.length) );
        return this.frame_length;
    }

    public boolean isUpdate;

    public String getString(){
        StringBuilder builder = new StringBuilder();
        builder.append("header: " + frame_Header);
        builder.append(" length: " + frame_length);
        builder.append(" to address: " + frame_To);
        builder.append(" cmd: " + frame_CMD);
        builder.append("Serial: " + frame_Serial);
        builder.append(" content: ");
        if (frame_content != null) {
            for (byte b : frame_content) {
                builder.append("," + b);
            }
        }
        return builder.toString();
    }
}
