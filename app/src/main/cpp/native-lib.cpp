#include <jni.h>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include <opencv2/opencv.hpp>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <string>

using namespace cv;
using namespace std;


extern "C" {
float resize(Mat img_src, Mat &img_resize, int resize_width){

    float scale = resize_width / (float)img_src.cols ;
    if (img_src.cols > resize_width) {
        int new_height = cvRound(img_src.rows * scale);
        resize(img_src, img_resize, Size(resize_width, new_height));
    }
    else {
        img_resize = img_src;
    }
    return scale;
}



//Mat &img_input = *(Mat *) matAddrInput;
//
//std::vector<Rect> faces;
//Mat img_gray;
//
//cvtColor(img_input, img_gray, COLOR_BGR2GRAY);
//equalizeHist(img_gray, img_gray);
//
//Mat img_resize;
//float resizeRatio = resize(img_gray, img_resize, 160);
//
////-- Detect faces
//((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale( img_resize, faces, 1.1, 2, 0|CASCADE_SCALE_IMAGE, Size(30, 30) );
//
//faces[0].x ;
//faces[0].y ;
//faces[0].width ;
//faces[0].height ;

    JNIEXPORT void JNICALL
    Java_com_example_kimjungwon_opencv2_MainActivity_ConvertRGBtoGray(JNIEnv *env, jobject instance,
                                                                      jlong matAddrInput,
                                                                      jlong matAddrResult) {

        // TODO
        Mat &matInput = *(Mat *)matAddrInput;
        Mat &matResult = *(Mat *)matAddrResult;
        //CV_BGR2YCrCb 불그스름
        //CV_RGBA2GRAY 회색 필터
        //CV_BGR2HSV 초록색

        cvtColor(matInput, matResult, CV_RGBA2GRAY);

    }
    JNIEXPORT jlong JNICALL
    Java_com_example_kimjungwon_opencv2_MainActivity_loadCascade(JNIEnv *env, jclass type,
                                                                 jstring cascadeFileName_) {
        const char *nativeFileNameString = env->GetStringUTFChars(cascadeFileName_, 0);

        string baseDir("/storage/emulated/0/");
        baseDir.append(nativeFileNameString);
        const char *pathDir = baseDir.c_str();

        jlong ret = 0;
        ret = (jlong) new CascadeClassifier(pathDir);
        if (((CascadeClassifier *) ret)->empty()) {
//            __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
//                                "CascadeClassifier로 로딩 실패  %s", nativeFileNameString);
        }
        else
//            __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
//                                "CascadeClassifier로 로딩 성공 %s", nativeFileNameString);


        env->ReleaseStringUTFChars(cascadeFileName_, nativeFileNameString);

        return ret;
    }



    JNIEXPORT void JNICALL
    Java_com_example_kimjungwon_opencv2_MainActivity_detect(JNIEnv *env, jclass type,
                                                            jlong cascadeClassifier_face,
                                                            jlong cascadeClassifier_eye,
                                                            jlong matAddrInput, jlong matAddrResult) {

        // TODO
        Mat &img_input = *(Mat *) matAddrInput;
        Mat &img_result = *(Mat *) matAddrResult;

        img_result = img_input.clone();

        std::vector<Rect> faces;
        Mat img_gray;

        cvtColor(img_input, img_gray, COLOR_BGR2GRAY);
        equalizeHist(img_gray, img_gray);

        Mat img_resize;
        float resizeRatio = resize(img_gray, img_resize, 160);

        //-- Detect faces
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale( img_resize, faces, 1.1, 2, 0|CASCADE_SCALE_IMAGE, Size(30, 30) );


//        __android_log_print(ANDROID_LOG_DEBUG, (char *) "native-lib :: ",
//                            (char *) "face %d found ", faces.size());

        for (int i = 0; i < faces.size(); i++) {
            double real_facesize_x = faces[i].x / resizeRatio;
            double real_facesize_y = faces[i].y / resizeRatio;
            double real_facesize_width = faces[i].width / resizeRatio;
            double real_facesize_height = faces[i].height / resizeRatio;

            Point center( real_facesize_x + real_facesize_width / 2, real_facesize_y + real_facesize_height/2);
            ellipse(img_result, center, Size( real_facesize_width / 2, real_facesize_height / 2), 0, 0, 360,
                    Scalar(255, 0, 255), 30, 8, 0);


            Rect face_area(real_facesize_x, real_facesize_y, real_facesize_width,real_facesize_height);
            Mat faceROI = img_gray( face_area );
            std::vector<Rect> eyes;

            //-- In each face, detect eyes
            ((CascadeClassifier *) cascadeClassifier_eye)->detectMultiScale( faceROI, eyes, 1.1, 2, 0 |CASCADE_SCALE_IMAGE, Size(30, 30) );

            for ( size_t j = 0; j < eyes.size(); j++ )
            {
                Point eye_center( real_facesize_x + eyes[j].x + eyes[j].width/2, real_facesize_y + eyes[j].y + eyes[j].height/2 );
                int radius = cvRound( (eyes[j].width + eyes[j].height)*0.25 );
                circle( img_result, eye_center, radius, Scalar( 255, 0, 0 ), 30, 8, 0 );
            }
        }
    }

JNIEXPORT jlong JNICALL
Java_com_example_kimjungwon_livebusker_Activity_StreamingActivity_loadCascade(JNIEnv *env,
                                                                              jclass type,
                                                                              jstring cascadeFileName_) {
    const char *nativeFileNameString = env->GetStringUTFChars(cascadeFileName_, 0);

    string baseDir("/storage/emulated/0/");
    baseDir.append(nativeFileNameString);
    const char *pathDir = baseDir.c_str();

    jlong ret = 0;
    ret = (jlong) new CascadeClassifier(pathDir);
    if (((CascadeClassifier *) ret)->empty()) {
//            __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
//                                "CascadeClassifier로 로딩 실패  %s", nativeFileNameString);
    }
    else
//            __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
//                                "CascadeClassifier로 로딩 성공 %s", nativeFileNameString);


        env->ReleaseStringUTFChars(cascadeFileName_, nativeFileNameString);

    return ret;
}
JNIEXPORT jintArray JNICALL
Java_com_example_kimjungwon_livebusker_Activity_StreamingActivity_detect(JNIEnv *env, jclass type,
                                                                         jlong cascadeClassifier_face,
                                                                         jlong matAddrInput,
                                                                         jintArray face_) {
jint *face = env->GetIntArrayElements(face_, NULL);

// TODO

env->ReleaseIntArrayElements(face_, face, 0);

//    얼굴인식
Mat &img_input = *(Mat *) matAddrInput;

std::vector<Rect> faces;
Mat img_gray;

cvtColor(img_input, img_gray, COLOR_BGR2GRAY);
equalizeHist(img_gray, img_gray);

Mat img_resize;
float resizeRatio = resize(img_gray, img_resize, 160);

//-- Detect faces
((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale( img_resize, faces, 1.1, 2, 0|CASCADE_SCALE_IMAGE, Size(30, 30) );


//    반환할 인트 배열 만들기
const jsize length = env->GetArrayLength(face_);
jintArray newArray = env->NewIntArray(length);
jint *oarr = env->GetIntArrayElements(face_, NULL);
jint *narr = env->GetIntArrayElements(newArray, NULL);

narr[0] = faces[0].x ;
narr[1] = faces[0].y ;
narr[2] = faces[0].width ;
narr[3] = faces[0].height ;

//    for (int o = 0, n = length - 1; o < length; o++, n--) {
//        narr[n] = oarr[o];
//    }

env->ReleaseIntArrayElements(newArray, narr, NULL);
env->ReleaseIntArrayElements(face_, oarr, NULL);

return newArray;
}


}
