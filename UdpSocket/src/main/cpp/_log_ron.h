//
// Created by zhangzhilai on 2017/12/13.
//

#ifndef TESTC_LOG_RON_H
#define TESTC_LOG_RON_H
#ifdef ANDROID
#include <android/log.h>
#include <stdio.h>
#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, "(>_<)", format, ##__VA_ARGS__)
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  "(^_^)", format, ##__VA_ARGS__)
#else
#define LOGE(format, ...)  printf("(>_<) " format "\n", ##__VA_ARGS__)
#define LOGI(format, ...)  printf("(^_^) " format "\n", ##__VA_ARGS__)
#endif
#endif //TESTC_LOG_RON_H
