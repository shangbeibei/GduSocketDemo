//
// Created by zhangzhilai on 2017/12/13.
//
#include <jni.h>
#include "com_gdu_library_DroneWallUtil.h"

#ifdef __cplusplus
extern "C" {
#endif

float MAX(float a, float b) {
    return (a < b ? b : a);
}

float MIN(float a, float b) {
    return (a < b ? a : b);
}

const double eps = 1e-10;

#define MAXPOINTCOUNT 20

/*
 * Class:     com_gdu_library_DroneWallUtil
 * Method:    wallIsOk
 * Signature: ([I[I)B
 */

JNIEXPORT jbyte JNICALL Java_com_gdu_library_DroneWallUtil_wallIsOk
        (JNIEnv *env, jobject job, jintArray lats, jintArray longs) {
    uint size = env->GetArrayLength(lats);
    jint *_lats;
    _lats = env->GetIntArrayElements(lats, 0);
    jint *_longs = env->GetIntArrayElements(longs, 0);
    CGPoint points[size];
    for (int i = 0; i < size; i++) {
        points[i].x = *(_longs + i) / 10000000.0;
        points[i].y = *(_lats + i) / 10000000.0;
        LOGE("verson001=i:%d,x:%f,y:%f", i, points[i].x, points[i].y);
    }
    fenceResult result = wallIsValid(points, size);
    jbyte jbyte1 = 0;
    switch (result) {
        case fenceErrorA:
            jbyte1 = 1;
            break;
        case fenceErrorB:
            jbyte1 = 2;
            break;
        case fenceErrorC:
            jbyte1 = 3;
            break;
        case pointError:
            jbyte1 = 4;
            break;
        case fenceOK:
            jbyte1 = 0;
            break;
    }
    env->ReleaseIntArrayElements(lats, _lats, 0);
    env->ReleaseIntArrayElements(longs, _longs, 0);
    return jbyte1;
}


float fabs(float src) {
    if (src > 0) {
        return src;
    } else {
        return src * -1;
    }
}

char *stringFromFenceResult(fenceResult result) {
    char *str;
    switch (result) {
        case fenceErrorA:
            str = "suoyouyitiaozhixiang";
            break;
        case fenceErrorB:
            str = "nottuduobianxing";
            break;
        case fenceErrorC:
            str = "duobianxingneiyouxiangjiao";
            break;
        case pointError:
            str = "dianbuzaiweilannei";
            break;
        case fenceOK:
            str = "ok";
            break;
        default:
            str = "fencenotok";
            break;
    }
    return str;
}

double JudgeSide(CGPoint A, CGPoint B, CGPoint C) {

    float f1 = (A.x - C.x) * (B.y - C.y);

    float f2 = (A.y - C.y) * (B.x - C.x);
    return f1 - f2;
}

bool isTowLineIntersect(CGPoint A, CGPoint B, CGPoint C, CGPoint D) {
    if (MAX(A.x, B.x) < MIN(C.x, D.x)) {
        return false;
    }
    if (MAX(A.y, B.y) < MIN(C.y, D.y)) {
        return false;
    }
    if (MAX(C.x, D.x) < MIN(A.x, B.x)) {
        return false;
    }
    if (MAX(C.y, D.y) < MIN(A.y, B.y)) {
        return false;
    }
    if (JudgeSide(C, B, A) * JudgeSide(B, D, A) < 0) {
        return false;
    }
    if (JudgeSide(A, D, C) * JudgeSide(D, B, C) < 0) {
        return false;
    }
    return true;
}

bool pointsOnOneLine(CGPoint *p, int n) {
    if (n < 3) { return true; }
    double result = 0;
    do {
        result = JudgeSide(p[0], p[1], p[2]);
        if (fabs(result) < eps) { return true; }
        p++;
        n--;
    } while (n > 3);
    return false;
}

bool isConvexPolygon(CGPoint *p, int n) {
    if (n < 3) { return false; }
    double a, b;
    CGPoint *pp = p;
    int num = n;
    int iter = 0;
    do {
        a = JudgeSide(p[0], p[1], p[2]);
        int i = 2;
        while (i < num) {
            b = JudgeSide(p[0], p[1], pp[(iter + i) % num]);
            if (a * b < 0) { return false; }
            i++;
        }
        p++;
        n--;
        a = b;
        iter++;
    } while (n > 2);
    return true;
}

bool isPolygonIntersect(CGPoint *p, int n) {
    if (n < 3) { return false; }
    bool a;
    CGPoint start, end;
    start.x = p[0].x;
    start.y = p[0].y;
    end.x = p[n - 1].x;
    end.y = p[n - 1].y;
    while (n > 2) {
        if (fabs(p[0].x - start.x) > eps || fabs(p[0].y - start.y) > eps) {
            a = isTowLineIntersect(p[0], p[1], end, start);
            if (a) { return true; }
        }
        int i = 2;
        while (fabs(p[i].x - end.x) > eps || fabs(p[i].y - end.y) > eps) {
            a = isTowLineIntersect(p[0], p[1], p[i], p[i + 1]);

            if (a) { return true; }
            i = i + 1;
        }
        p = p + 1;
        n = n - 1;
    }
    return false;
}

bool isPointInPolygon(float x, float y, CGPoint p[], int num) {

    int i, j = num - 1;
    bool oddNodes = false;
    float polyX[MAXPOINTCOUNT];
    float polyY[MAXPOINTCOUNT];
    for (int i = 0; i < num; i++) {
        polyX[i] = p[i].x;
        polyY[i] = p[i].y;
    }

    for (i = 0; i < num; i++) {
        if (((polyY[i] < y && polyY[j] >= y)
             || (polyY[j] < y && polyY[i] >= y))
            && (polyX[i] <= x || polyX[j] <= x)) {
            oddNodes ^= (polyX[i] + (y - polyY[i]) / (polyY[j] - polyY[i]) * (polyX[j] - polyX[i]) <
                         x);
        }
        j = i;
    }
    return oddNodes;

}

fenceResult wallIsValid(CGPoint points[], int num) {
    //所有点是否都在一条直线
    //如果所有点都在一条直线上，不能构成多边形。
    bool a = pointsOnOneLine(points, num - 1);
    if (a == true) { return fenceErrorA; }

    //判断多边形内是否有相交
    bool c = isPolygonIntersect(points, num - 1);
    if (c == true) { return fenceErrorC; }

    //判断是否为凸多边形
    bool b = isConvexPolygon(points, num - 1);
    if (b == false) { return fenceErrorB; }

    //判断点是否在多边形内
    CGPoint dronePoint = points[num - 1];
    bool d = isPointInPolygon(dronePoint.x, dronePoint.y, points, num - 1);
    if (d == false) { return pointError; }

    return fenceOK;
}

/*void main()
{
int size=4;
CGPoint points[5];*/
//可以围住
/*points[0].x=22.955553;
points[0].y=114.041309;
points[1].x=22.955804;
points[1].y=114.052298;
points[2].x=22.95011;
points[2].y=114.051107;
points[3].x=22.948629;
points[3].y=114.04022;
points[4].x=22.952473;
points[4].y=114.046238;
fenceResult result = wallIsValid(points,5);*/
//一条线
/*points[0].x=22.95215;
points[0].y=114.04181;
points[1].x=22.95215;
points[1].y=114.050291;
points[2].x=22.95215;
points[2].y=114.056894;
points[3].x=22.952473;
points[3].y=114.046238;
fenceResult result = wallIsValid(points,4);*/
//凸多边形
//points[0].x=22.952072;
//points[0].y=114.043564;
//points[1].x=22.952821;
//points[1].y=114.036857;
//points[2].x=22.956362;
//points[2].y=114.053467;
//points[3].x=22.945838;
//points[3].y=114.042186;
//points[4].x=22.952473;
//points[4].y=114.046238;
//fenceResult result = wallIsValid(points,5);

//相交
/*points[0].x=22.954053;
points[0].y=114.042373;
points[1].x=22.952879;
points[1].y=114.051315;
points[2].x=22.948089;
points[2].y=114.043878;
points[3].x=22.956169;
points[3].y=114.046092;
points[4].x=22.952473;
points[4].y=114.046238;
fenceResult result = wallIsValid(points,5);*/

//不在围栏内
//points[0].x=22.949629;
//points[0].y=114.042164;
//points[1].x=22.951244;
//points[1].y=114.054219;
//points[2].x=22.94834;
//points[2].y=114.054533;
//points[3].x=22.944395;
//points[3].y=114.05071;
//points[4].x=22.952473;
//points[4].y=114.046238;
//fenceResult result = wallIsValid(points,5);
//}

/*void readPointsFromFile(char *filename,CGPoint points[] ,int *pointCount)
{
    FILE *fp = fopen(filename, "r");
    float lon = 0, lat = 0;
    while (!feof(fp)) {
        fscanf(fp, "%f,%f", &lon, &lat);
        points[*pointCount].x = lon;
        points[*pointCount].y = lat;
        (*pointCount)++;
    }
    fclose(fp);
}

//计算结果：https://www.desmos.com/calculator/te0evyj1n0
//A:  i =  464    ron=1    The fence meets the requirements but the plane is not in the enclosure
//B:  i =  464    围栏满足要求,飞机在围栏内

int main(int argc, char * argv[]) {

    if (argc < 2) {
        printf("Usage:\n");
        printf("\tmain [filename] [resultFile]\n");
        printf("\t\t - filename   待处理文件\n");
        printf("\t\t - resultFile 输出结果文件 \n");
        return 0;
    }

    //输出文件
    char *resultFileName;
    if (argc == 3) {
        resultFileName = argv[2];
    }else{
        resultFileName = "./zx_result.txt";
    }
    FILE* resultFile = fopen(resultFileName, "wb+");

    //输入：文件，所有点，点数
    char* fileToTest = argv[1];
    CGPoint points[MAXPOINTCOUNT];
    int pointCount = 0;
    {
        //get points
        readPointsFromFile(fileToTest, points, &pointCount);

        //judge points
        fenceResult result = wallIsValid(points, pointCount);

        //log results
        fprintf(resultFile, "%s\n", stringFromFenceResult(result));
        printf("%s\t%s\n",fileToTest, stringFromFenceResult(result));

        memset(&points,0,sizeof(points));
        pointCount = 0;
    }

    fclose(resultFile);
}*/



#ifdef __cplusplus
}
#endif
