#include <jni.h>
#include <iostream>
#include <string>

using namespace std;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_security_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_security_MainActivity_factorial(
        JNIEnv *env,
        jobject /* this */,
        jint n) {
    int factorial = 1;
    for (int i = 1; i <= n; ++i) {
        factorial *= i;
    }

    return env->NewStringUTF(to_string(factorial).c_str());
}
