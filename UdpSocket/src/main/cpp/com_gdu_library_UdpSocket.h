//
// Created by zhangzhilai on 2017/12/13.
//
#include <jni.h>

#include <stdio.h>
#include <time.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <netinet/in.h>
#include <unistd.h>
#include <sys/time.h>

#ifdef ANDROID

#include <android/log.h>
#include <stdio.h>

#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, "(>_<)", format, ##__VA_ARGS__)
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  "(^_^)", format, ##__VA_ARGS__)
#else
#define LOGE(format, ...)  printf("(>_<) " format "\n", ##__VA_ARGS__)
#define LOGI(format, ...)  printf("(^_^) " format "\n", ##__VA_ARGS__)
#endif
/* Header for class com_gdu_library_UdpSocket */

/*#ifndef 如果没有被定义 A，#define 则定义 A*/
#ifndef _Included_com_gdu_library_UdpSocket
#define _Included_com_gdu_library_UdpSocket
#ifdef __cplusplus
extern "C" {
#endif
char isStop = 0;
#define BUFFLENGTH 307200
//序列号---ron
int serialNum = 0;

char *cacheBuff;

/************当前是否需要暂停ron***********/
char isPause = 0;

/**********数据回调的方法*******/
jmethodID callData;
jclass cbClazz;

//当前收到的长度----ron
int position = 0;

//接收一帧数据的分片包
int receiveCutNum;

//丢掉的一帧数据的分片包
int lostCutNum;

/***************************
 *  总共收到的包数---ron
 */
int receiverAllPckNum = 0;

/******************************************
 * 换成的数据，用来标识上次计算码流的数据
 */
int cachePckNum = 0;

/***************************
 *  总共丢包数---ron
 */
int lostAllPckNum = 0;

/************************
 * 上一次的丢包总数---ron
 */
int lastLoastAllPckNum = 0;

/***************************
 *  上一次收到的包数---ron
 */
int lastReceiverAllPckNum = 0;

//建立sock的句柄 ---ron
int sock = 0;
int n = 0;

//是否显示日志信息
char showLog = 0;

/*******一帧数据的java 数组******/
jbyteArray oneFrameDataJ;

/*************java数组的长度***************/
int jArrayLength;
/*
 * Class:     com_gdu_library_UdpSocket
 * Method:    start
 * Signature: (Lcom/gdu/library/CBUdpSocket;)V
 */
JNIEXPORT void JNICALL Java_com_gdu_library_UdpSocket_start
        (JNIEnv *, jobject, jobject, jint);

JNIEXPORT void JNICALL Java_com_gdu_library_UdpSocket_showLog
        (JNIEnv *env, jobject job, jbyte showLog);

JNIEXPORT void JNICALL Java_com_gdu_library_UdpSocket_droneType
        (JNIEnv *env, jobject job, jbyte droneType);

JNIEXPORT jint JNICALL Java_com_gdu_library_UdpSocket_getReceiverData
        (JNIEnv *, jobject);

/*
 * Class:     com_gdu_library_UdpSocket
 * Method:    stop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_gdu_library_UdpSocket_stop
        (JNIEnv *, jobject);

JNIEXPORT void JNICALL Java_com_gdu_library_UdpSocket_onResume
        (JNIEnv *, jobject);
/*JNIEXPORT 类似java中的默认方法 标准格式，也就是使用jni申明
 * void     和java中的返回类型一样，可以是void，也可以使 int，只不过在jni中，形象的都要加jint ，jboolean，等
 * JNICALL  我的理解是 java 中的 class  不知道是不是这么理解 ，
 * Java_com_gdu_library_UdpSocket_onPause  命名的规则是 （包名 +类名）
 * JNIEnv *    * 指的就是指针，可以带参数也可以不带
 * */
JNIEXPORT void JNICALL Java_com_gdu_library_UdpSocket_onPause
        (JNIEnv *, jobject);

void connSocket(JNIEnv *env, jobject gObj, int port);

/******************************
 * 处理每次收到的数据---ron
 */
void disposeData(char *data, int offset, int length, JNIEnv *env, jobject gObj);

void sendData2Java(JNIEnv *env, jobject gObj);

void destroyData(JNIEnv *env);

FILE *pFile;
FILE *pStrFile;

void createFile();

void writeData(char *str, int length);

void writeData2(char *str, int data);

void writeStr(char *str, int length);

/*******极客版本********/
char isGeekVersion = 0;

#ifdef __cplusplus
}
#endif
#endif
