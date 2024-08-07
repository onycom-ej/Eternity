#include <jni.h>
#include <stdio.h>

JNIEXPORT void JNICALL
Java_eu_toldi_infinityforlemmy_activities_SearchActivity_triggerNativeError(JNIEnv *env, jobject instance) {
    // 예외를 던지기 위해 JNI API 사용
    jclass exceptionClass = (*env)->FindClass(env, "java/lang/Error");
    if (exceptionClass == NULL) {
        // 예외 클래스를 찾지 못했을 경우
        return; // 예외가 발생하여 JNI 호출이 종료됩니다.
    }
    (*env)->ThrowNew(env, exceptionClass, "의도적으로 발생시킨 JNI 에러입니다.");
}


//
//
//extern "C" JNIEXPORT jstring JNICALL
//eu_toldi_infinityforlemmy_activities_MainActivity_stringFromJNI(JNIEnv* env, jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
//}
//
//extern "C" JNIEXPORT void JNICALL
//eu_toldi_infinityforlemmy_activities_MainActivity_triggerJNIFailure(JNIEnv* env, jobject /* this */) {
//    // 임의의 JNI 오류를 발생시키기 위해 NULL 포인터를 사용하는 예
//    jclass cls = NULL;
//    jmethodID mid = env->GetMethodID(cls, "methodThatDoesNotExist", "()V");
//    env->CallVoidMethod(cls, mid);
//}