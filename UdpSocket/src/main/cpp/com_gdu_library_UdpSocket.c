//
// Created by zhangzhilai on 2017/12/13.
//
#include "com_gdu_library_UdpSocket.h"

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_gdu_library_UdpSocket
 * Method:    start
 * Signature: (Lcom/gdu/library/CBUdpSocket;)V
 */
JNIEXPORT void JNICALL Java_com_gdu_library_UdpSocket_start
        (JNIEnv *env, jobject job, jobject jcb, jint port) {
    isStop = 0;
    connSocket(env, jcb, port);
}

JNIEXPORT void JNICALL Java_com_gdu_library_UdpSocket_showLog
        (JNIEnv *env, jobject job, jbyte isShowLog) {
//    showLog = isShowLog;
    LOGE("setShowLog");
}

/*
 * Class:     com_gdu_library_UdpSocket
 * Method:    stop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_gdu_library_UdpSocket_stop
        (JNIEnv *env, jobject jobj) {
    isStop = 1;
    /*if(sock != NULL )
        shutdown(sock,SHUT_WR);*/
}


void connSocket1(JNIEnv *env, jobject gObj, int port) {
    LOGI("go to connSocket");
    struct sockaddr_in addr;

    if ((sock = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        LOGE("create socket");
        exit(1);
    }
    struct timeval tv_out;
    tv_out.tv_sec = 2;//等待3秒
    tv_out.tv_usec = 0;
    setsockopt(sock, SOL_SOCKET, SO_RCVTIMEO, &tv_out, sizeof(tv_out));
    memset(&addr, 0, sizeof(addr));
    LOGI("create socket success00000002");
    addr.sin_family = AF_INET;
    LOGI("use add 0001");
    addr.sin_port = htons(port);
    LOGI("use add 0002");
    addr.sin_addr.s_addr = htonl(INADDR_ANY);//inet_addr("127.0.0.1");//
    LOGI("use add 0003");
    if (addr.sin_addr.s_addr == INADDR_NONE) {
        LOGE("Incorrect ip address!");
        close(sock);
        return;
    }
    LOGI("begin bind ip ");
    int len = sizeof(addr);
    //绑定地址---必须要绑定
    n = bind(sock, (struct sockaddr *) &addr, len);
    if (n < 0) {
        LOGE("bind fail %d,%s", errno, strerror(errno));
        close(sock);
        return;
    }
    //存放数据的buffer
    char buff[2048];
    char lastRtpBuff[4096];
    int lastRtpPosition = 0;
    int mark = 0;
    //从哪里接收到数据
    cacheBuff = (char *) malloc(BUFFLENGTH);
    isPause = 0;
    createFile();
    while (1) {
        n = recvfrom(sock, buff, 2048, 0, (struct sockaddr *) &addr, &len);
        if (n > 0) {
            //不需要自己组包的情况
            receiverAllPckNum++;
            disposeData(buff, 0, n, env, gObj);
            //==========================需要组包的
            if (lastRtpPosition + n < 4096) {
                memcpy(lastRtpBuff + lastRtpPosition, buff, n);
                lastRtpPosition += n;
                mark = 0;
                for (int i = 5; i < lastRtpPosition - 2; ++i) {
                    if (*(lastRtpBuff + i) == 0x80 &&
                        (*(lastRtpBuff + i + 1) == 0x60 || *(lastRtpBuff + i + 1) == 224)) {
                        mark = i;
                        break;
                    }
                }

                if (mark > 5) {
                    receiverAllPckNum++;
//                    writeData(lastRtpBuff,mark);
//                    writeData("\n\n",2);
                    disposeData(lastRtpBuff, 0, mark, env, gObj);
//                    memcpy(lastRtpBuff,lastRtpBuff + mark,lastRtpPosition- mark );
                    for (int i = 0; i < lastRtpPosition - mark; i++) {
                        lastRtpBuff[i] = lastRtpBuff[i + mark];
                    }
                    lastRtpPosition = lastRtpPosition - mark;
                }
            } else {
                lastRtpPosition = 0;
                LOGE("receiver data and merge err");
            }

        } else if (n == 0) {
            LOGE("server closed\n");
        } else if (n == -1) {
            LOGE("recvfrom length = -1");
        }
        if (isStop) {
            LOGE("is Stop:%d", isStop);
            break;
        }
    }
    if (sock != NULL) {
        close(sock);
        sock = NULL;
    }
    destroyData(env);
}

/*********设置当前飞机的类型********/
JNIEXPORT void JNICALL Java_com_gdu_library_UdpSocket_droneType
        (JNIEnv *env, jobject job, jbyte droneType) {
    LOGE("Java_com_gdu_library_UdpSocket_droneType:%d", droneType);
    if (droneType == 3) {
        isGeekVersion = 1;
    }
}

/*************************
 * 连接socket ----ron
 ************************/
void connSocket(JNIEnv *env, jobject gObj, int port) {
    LOGI("go to connSocket");
    struct sockaddr_in addr;

    if ((sock = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        LOGE("create socket");
        exit(1);
    }
    struct timeval tv_out;
    tv_out.tv_sec = 2;//等待3秒
    tv_out.tv_usec = 0;
    setsockopt(sock, SOL_SOCKET, SO_RCVTIMEO, &tv_out, sizeof(tv_out));
    memset(&addr, 0, sizeof(addr));
    LOGI("create socket success00000002");
    addr.sin_family = AF_INET;
    LOGI("use add 0001");
    addr.sin_port = htons(port);
    LOGI("use add 0002");
    addr.sin_addr.s_addr = htonl(INADDR_ANY);//inet_addr("127.0.0.1");//
    LOGI("use add 0003");
    if (addr.sin_addr.s_addr == INADDR_NONE) {
        LOGE("Incorrect ip address!");
        close(sock);
        return;
    }
    LOGI("begin bind ip ");
    int len = sizeof(addr);
    //绑定地址---必须要绑定
    n = bind(sock, (struct sockaddr *) &addr, len);
    if (n < 0) {
        LOGE("bind fail %d,%s", errno, strerror(errno));
        close(sock);
        return;
    }
    //存放数据的buffer
    char buff[204800];
    int buffPosition = 0;//尾部的索引号
    int begin = 0; //头部起始的索引号
    int mark = 0;
    //从哪里接收到数据
    cacheBuff = (char *) malloc(BUFFLENGTH);
    isPause = 0;
    createFile();
    while (1) {
//        n = recvfrom(sock, buff , 2048, 0, (struct sockaddr *)&addr, &len);
        n = recvfrom(sock, buff + buffPosition, 4096, 0, (struct sockaddr *) &addr, &len);
        if (n > 0) {
//            writeStr("recvFrom",n);
            //不需要自己组包的情况
            if (!isGeekVersion) {
                receiverAllPckNum++;
                disposeData(buff, 0, n, env, gObj);
            }
                //==========================需要组包的
            else {
                buffPosition += n;
                for (int i = 0; i < 15; i++) {
                    mark = 0;
                    for (int i = begin + 2; i < buffPosition - 4; ++i) {
                        if (*(buff + i + 0) == 0 &&
                            *(buff + i + 1) == 0 &&
                            *(buff + i + 2) == 0 &&
                            *(buff + i + 3) == 2) {
                            mark = i;
                            break;
                        }
                    }
                    if (mark > 5) {
                        receiverAllPckNum++;
//                    writeData(lastRtpBuff,mark);
//                    writeData("\n\n",2);
                        mark += 4;
                        LOGE("disposeData:begin:%d,mark:%d,buffPosition:%d", begin, mark,
                             buffPosition);
//                        writeData(buff + begin,mark-begin);
//                        writeData("\n\n",2);
                        disposeData(buff + begin, 0, mark - begin - 4, env, gObj);
                        begin = mark;
                        if (buffPosition + 4096 > 204800) {
                            for (int i = 0; i < buffPosition - begin; i++) {
                                buff[i] = buff[i + begin];
                            }
                            buffPosition -= begin;
                            begin = 0;
                        }
                    } else {
                        break;
                    }
                }
                //防止出现超出界限的bug
                if ((buffPosition + 4096) > 204800) {
//                    writeStr("buff positon > 204800:",buffPosition);
                    buffPosition = 0;
                }
            }
        } else if (n == 0) {
            LOGE("server closed\n");
        } else if (n == -1) {
            LOGE("recvfrom length = -1");
        }
        if (isStop) {
            LOGE("is Stop:%d", isStop);
            break;
        }
    }
    if (sock != NULL) {
        close(sock);
        sock = NULL;
    }
    destroyData(env);
}

void destroyData(JNIEnv *env) {
    if (oneFrameDataJ != NULL) {
        LOGE("destory onframeData");
        (*env)->DeleteLocalRef(env, oneFrameDataJ);
        oneFrameDataJ = NULL;
        LOGE("destory callData");
//    (*env)->DeleteLocalRef(env, callData);
//    callData = NULL;
//    (*env)->DeleteLocalRef(env, cbClazz);
//    cbClazz = NULL;
    }

    if (cacheBuff != NULL) {
        free(cacheBuff);
        cacheBuff = NULL;
    }

}

void sendData2Java(JNIEnv *env, jobject gObj) {
    if (isPause) {
        if (showLog)
            LOGI("progress is Pause");
        return;
    }

    if (callData == NULL) {
        cbClazz = (*env)->GetObjectClass(env, gObj);
        callData = (*env)->GetMethodID(env, cbClazz, "dataCB", "([BI)V");
        LOGI("find java method");
    }

    //如果 oneFrameDataJ 为null的情况
    if (oneFrameDataJ == NULL) {
        oneFrameDataJ = (*env)->NewByteArray(env, position);
        jArrayLength = position;
        LOGI("oneFrameDataJ == NULL");
    } else if (jArrayLength < position)//oneFrameDataJ的长度小于当前需要发送的长度
    {
        (*env)->DeleteLocalRef(env, oneFrameDataJ);
        oneFrameDataJ = NULL;
        oneFrameDataJ = (*env)->NewByteArray(env, position);
        jArrayLength = position;
        LOGI(" oneFrameDataJ ReCreate");
    }
    if (oneFrameDataJ == NULL) {
        LOGE("oneFrameDataJ is null,err====");
    }
    if (showLog)
        LOGI("beigin set oneFrameDataJ positon:%d", position);
//    writeData2(cacheBuff, position);
    (*env)->SetByteArrayRegion(env, oneFrameDataJ, 0, position, cacheBuff);
    (*env)->CallVoidMethod(env, gObj, callData, oneFrameDataJ, position);
}

/******************************
 * 处理每次收到的数据---ron
 */
void disposeData(char *data, int offset, int length, JNIEnv *env, jobject gObj) {
    if (length < 14) return;
    //先拿序列号
    int currentSerial = (*(data + 2) << 8) + *(data + 3);
//  LOGI("recvframe====length:%d,serail:%d,serialNum:%d",n,currentSerial,serialNum);
    if (currentSerial - 1 != serialNum && position > 0) {
//        LOGE("lost one package:%d,%d",currentSerial,serialNum);
        int num = currentSerial - 1 - serialNum;
        if (num > 0) {
            lostCutNum += num;
            //计算丢包率的问题
            lostAllPckNum += num;
        }
        serialNum = currentSerial;
        return;
    }
    serialNum = currentSerial;
    char step = (*(data + 13) & 0xc0) >> 6;
    char type = (*(data + 12) & 0x1f);
    if (showLog)
        LOGI("type:%d,step:%d,currentSerial:%d,serialNum:%d", type, step, currentSerial, serialNum);
    //没有丢序列号
    if (type == 28)//分片的
    {
        if (step == 2)//开始
        {
            position = 0;
            //开始收到第一包的数据，初始化状态
            receiveCutNum = 1;
            lostCutNum = 0;
            *(cacheBuff + 0) = 0;
            *(cacheBuff + 1) = 0;
            *(cacheBuff + 2) = 0;
            *(cacheBuff + 3) = 1;
            *(cacheBuff + 4) = (char) (*(data + 12) & 224 | *(data + 13) & 31);
            position += 5;
            memcpy(cacheBuff + position, data + 14, length - 14);
            position += length - 14;
        } else if (step == 1)//一帧的结束
        {
            if (showLog)
                LOGI("receive frame is end");
            if (position < 14) {
                LOGI("position < 14");
                return;
            }
            if (position + length - 14 > BUFFLENGTH) {
                LOGE("receive data length too big");
                position = 0;
                return;
            }
            receiveCutNum++;
            if (showLog)
                LOGI("TYPE:%d,lostCutNum:%d,receiveCutNum:%d", *(cacheBuff + 4), lostCutNum,
                     receiveCutNum);
            //计算丢包的多少
            if (lostCutNum * 2 > receiveCutNum) {
                LOGE("lost data > 25%");
                return;
            }
            memcpy(cacheBuff + position, data + 14, length - 14);
            position += length - 14;
            sendData2Java(env, gObj);
        } else {
            if (position < 14) {
                LOGI("position < 14");
                return;
            }
            if (position + length - 14 > BUFFLENGTH) {
                LOGE("receive data length too big");
                position = 0;
                return;
            }
            memcpy(cacheBuff + position, data + 14, length - 14);
            position += length - 14;
            receiveCutNum++;
        }
    } else {//不是分片的
        position = 0;
        *(cacheBuff + 0) = 0;
        *(cacheBuff + 1) = 0;
        *(cacheBuff + 2) = 0;
        *(cacheBuff + 3) = 1;
        position += 4;
        memcpy(cacheBuff + position, data + 12, length - 12);
        position += length - 12;
        sendData2Java(env, gObj);
    }
}


/******************************
 * 处理每次收到的数据---ron
 */
void disposeData1(char *data, int offset, int length, JNIEnv *env, jobject gObj) {
    if (length < 14) return;
    //先拿序列号
    int currentSerial = (*(data + 2) << 8) + *(data + 3);
//  LOGI("recvframe====length:%d,serail:%d,serialNum:%d",n,currentSerial,serialNum);
    if (currentSerial - 1 != serialNum && position > 0) {
        position = 0;
        LOGE("lost one package");
        return;
    }
    serialNum = currentSerial;
    char step = (*(data + 13) & 0xc0) >> 6;
    char type = (*(data + 12) & 0x1f);
    if (showLog)
        LOGI("type:%d,step:%d", type, step);
    //没有丢序列号
    if (type == 28)//分片的
    {
        if (step == 2)//开始
        {
            position = 0;
            *(cacheBuff + 0) = 0;
            *(cacheBuff + 1) = 0;
            *(cacheBuff + 2) = 0;
            *(cacheBuff + 3) = 1;
            *(cacheBuff + 4) = (char) (*(data + 12) & 224 | *(data + 13) & 31);
            position += 5;
            memcpy(cacheBuff + position, data + 14, length - 14);
            position += length - 14;
        } else if (step == 1)//一帧的结束
        {
            if (showLog)
                LOGI("receive frame is end");
            if (position < 14) {
                LOGI("position < 14");
                return;
            }
            if (position + length - 14 > BUFFLENGTH) {
                LOGE("receive data length too big");
                position = 0;
                return;
            }
            memcpy(cacheBuff + position, data + 14, length - 14);
            position += length - 14;
            sendData2Java(env, gObj);
        } else {
            if (position < 14) {
                LOGI("position < 14");
                return;
            }
            if (position + length - 14 > BUFFLENGTH) {
                LOGE("receive data length too big");
                position = 0;
                return;
            }
            memcpy(cacheBuff + position, data + 14, length - 14);
            position += length - 14;
        }
    } else {//不是分片的
        position = 0;
        *(cacheBuff + 0) = 0;
        *(cacheBuff + 1) = 0;
        *(cacheBuff + 2) = 0;
        *(cacheBuff + 3) = 1;
        position += 4;
        memcpy(cacheBuff + position, data + 12, length - 12);
        position += length - 12;
        sendData2Java(env, gObj);
    }
}

JNIEXPORT jint JNICALL Java_com_gdu_library_UdpSocket_getReceiverData
        (JNIEnv *env, jobject jobject1) {
//    LOGE("lostAllPckNum:%d;receiverAllPckNum:%d;"
//                 "lastLoastAllPckNum:%d;lastReceiverAllPckNum:%d",lostAllPckNum,receiverAllPckNum,lastReceiverAllPckNum,lastLoastAllPckNum);
    int b = ((lostAllPckNum - lastLoastAllPckNum) + (receiverAllPckNum - lastReceiverAllPckNum));
    int a = 100;
    if (b > 0) {
        a = (lostAllPckNum - lastLoastAllPckNum) * 100 / b;

    } else {
        a = 1000;
    }
    lastReceiverAllPckNum = receiverAllPckNum;
    lastLoastAllPckNum = lostAllPckNum;
    return a;
}


JNIEXPORT void JNICALL Java_com_gdu_library_UdpSocket_onResume
        (JNIEnv *env, jobject job) {
    isPause = 0;
}

JNIEXPORT void JNICALL Java_com_gdu_library_UdpSocket_onPause
        (JNIEnv *env, jobject job) {
    isPause = 1;
}


void createFile() {
    pFile = fopen("/sdcard/logData.txt", "w");
    pStrFile = fopen("/sdcard/log.txt", "w");
}

void writeData(char *str, int length) {
    fwrite(str, 1, length, pFile);
    fflush(pFile);
}

void writeData2(char *str, int data) {
//    int n = 100;
//    char msg[100];// = malloc(n);
//    n = sprintf(msg,"%s:%d \n",str,data);
    fwrite(str, 1, data, pStrFile);
    fflush(pStrFile);
//    writeData(msg,n);
}

void writeStr(char *str, int length) {
    int n = 100;
    char msg[100];// = malloc(n);
    n = sprintf(msg, "%s:%d \n", str, length);
    fwrite(msg, 1, n, pStrFile);
    fflush(pStrFile);
//    writeData(msg,n);
}

#ifdef __cplusplus
}
#endif

