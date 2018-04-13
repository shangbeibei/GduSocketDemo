//
// Created by zhangzhilai on 2017/12/13.
//
#include <jni.h>
#include "_log_ron.h"
/* Header for class com_gdu_library_DroneWallUtil */

#ifndef _Included_com_gdu_library_DroneWallUtil
#define _Included_com_gdu_library_DroneWallUtil
#ifdef __cplusplus
extern "C" {
#endif

typedef enum {
    fenceErrorA = 1 << 0,
    fenceErrorB = 1 << 1,
    fenceErrorC = 1 << 2,
    pointError = 1 << 3,
    fenceError = fenceErrorA | fenceErrorB | fenceErrorC | pointError,
    fenceOK = 1 << 7
} fenceResult;


#if !defined(CGPointls)
typedef struct CGPoint {
    float x;
    float y;
} CGPoint;
#endif


double JudgeSide(CGPoint A, CGPoint B, CGPoint C);


bool isTowLineIntersect(CGPoint A, CGPoint B, CGPoint C, CGPoint D);


bool pointsOnOneLine(CGPoint *p, int n);


bool isConvexPolygon(CGPoint *p, int n);

bool isPolygonIntersect(CGPoint *p, int n);

bool isPointInPolygon(float x, float y, CGPoint p[], int num);

fenceResult wallIsValid(CGPoint points[], int num);


/*
 * Class:     com_gdu_library_DroneWallUtil
 * Method:    wallIsOk
 * Signature: ([I[I)B
 */

JNIEXPORT jbyte JNICALL Java_com_gdu_library_DroneWallUtil_wallIsOk
        (JNIEnv *, jobject, jintArray, jintArray);


float fabs(float src);

char *stringFromFenceResult(fenceResult result);

#ifdef __cplusplus
}
#endif
#endif
