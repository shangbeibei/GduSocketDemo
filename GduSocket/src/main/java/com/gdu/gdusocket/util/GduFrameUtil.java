package com.gdu.gdusocket.util;

/**
 * Created by zhangzhilai on 2017/12/10.
 */

import com.gdu.gdusocketmodel.GduFrame;
import com.gdu.gdusocketmodel.GduSocketConfig;

/**
 * <p>GduFrame的工具类</p>
 * <ul>
 * <li>GduFrame->byte[]</li>
 * <li>byte[] -> GduFrame</li>
 * </ul>
 */
public class GduFrameUtil {

    /**
     * gduFrame对象 转化为 ByteArray -- ron
     *
     * @param frame
     * @return
     */
    public static byte[] gduFrame2ByteArray(GduFrame frame) {
        int frameIndex = 0;
        byte[] data = new byte[frame.getFrameLength() + 4];
        data[frameIndex++] = frame.frame_Header;
        data[frameIndex++] = frame.getFrameLength();
        data[frameIndex++] = frame.frame_To;
        data[frameIndex++] = frame.frame_CMD;
        data[frameIndex++] = frame.frame_Serial;
        if (frame.frame_content != null) {
            System.arraycopy(frame.frame_content, 0, data, frameIndex, frame.frame_content.length);
            frameIndex += frame.frame_content.length;
        }
        data[frameIndex++] = frame.frame_CheckCode;
        data[frameIndex++] = frame.frame_end;
        return data;
    }

    /**
     * byte数组转化为GduFrame -- ron
     *
     * @param data   byte数组
     * @param index  起始位置 0
     * @param length 需要的数据的长度
     * @return
     */
    public static GduFrame byteArray2GduFrame(byte[] data, int index, int length) {
        GduFrame frame = new GduFrame();
        frame.frame_Header = data[index++];
        frame.frame_length = data[index++];
        frame.frame_To = data[index++];
        frame.frame_CMD = data[index++];
        frame.frame_Serial = data[index++];
        /** shang 不明白为哈-7 **/
        if (length > 7) {
            byte[] content = new byte[length - 7];
            for (int i = 0; i < content.length; i++) {
                content[i] = data[index + i];
            }
            index += content.length;
            frame.frame_content = content;
        }
        frame.frame_CheckCode = data[index++];
        frame.frame_end = data[index];
        return frame;
    }


    /**
     * <p>检验帧头 -- ron</p>
     *
     * @param data
     * @param index 开始找帧头的位置
     * @return 0代表匹配失败，非0成功(且是该帧的长度)
     */
    public int checkFrameHead(byte[] data, int index) {
        //长度不满足
        if (data.length < index + GduSocketConfig.Frame_Head_length)
            return 0;

        //帧头部满足
        if (data[index] != GduSocketConfig.Frame_Head) {
            return 0;
        }

        //来源满足
        if ((data[index + 2] & 0x0f) != GduSocketConfig.To_App) {
            return 0;
        }

        return data[index + 1];
    }

    /**
     * <p>校验码检验 --ron</p>
     *
     * @param data   包含完整一帧数据
     * @param index  数据开始的位置
     * @param length 数据的长度
     * @return 返回校验是否正确
     */
    public boolean checkCode(byte[] data, int index, int length) {
        byte xor = XOR.xorCmd(data, index + 1, length - 3);
        if (xor == data[index + length - 2]) {
            return true;
        } else {
            return false;
        }
    }

}
