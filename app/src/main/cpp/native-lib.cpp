#include <jni.h>
#include <string>
#include <iostream>
#include <android/log.h>

using namespace std;

#define LOG_TAG "isign"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

string e(const std::string &input, char key) {
    std::string output = input;
    for (size_t i = 0; i < input.size(); ++i) {
        output[i] = input[i] ^ key;
    }
    return output;
}

string d(const std::string &input, char key) {
    return e(input, key);
}

char k() {
    int dummyVar1 = 10;
    float dummyVar2 = 5.5;
    bool dummyVar3 = false;
    char result = '\0';

    int targetValue = 40;
    for (int i = 0; i < 100; i++) {
        if (i == 25) {
            targetValue += 10;
        } else if (i == 50) {
            targetValue += 20;
        } else if (i == 75) {
            targetValue += 10;
        }

        if (i > 75) {
            break;
        }
    }

    if (targetValue >= 80) {
        if (dummyVar1 > 0) {
            result = static_cast<char>(targetValue + 10);
        }
    }

    return result;
}

extern "C" JNIEXPORT jstring JNICALL
b(
        JNIEnv *env,
        jobject /* this */) {
    string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

const string strings[] = {
        "957u?\";7*6?u)?9/(3.#u)?9/(3.#u\u000F)?(\u001E;.;",
        "\u00160;,;u6;4=u\t.(34=a",
        "rs\u00160;,;u6;4=u\t.(34=a",
        "\u001E",
        "4;7?",
        "8;6;49?",
        ".5\t.(34=",
};

extern "C" JNIEXPORT jobject JNICALL
Java_com_example_security_MainActivity_createUser(
        JNIEnv *env,
        jobject /* this */,
        jstring name,
        jdouble balance) {
    // create the object of the class UserData
    jclass userDataClass = env->FindClass(d(strings[0], k()).c_str());
    jobject newUserData = env->AllocObject(userDataClass);

    // Get the UserData fields to be set
    jfieldID nameField = env->GetFieldID(userDataClass, d(strings[4], k()).c_str(),
                                         d(strings[1], k()).c_str());
    jfieldID balanceField = env->GetFieldID(userDataClass, d(strings[5], k()).c_str(),
                                            d(strings[3], k()).c_str());

    env->SetObjectField(newUserData, nameField, name);
    env->SetDoubleField(newUserData, balanceField, balance);

    jobject newGlobalUserData = env->NewGlobalRef(newUserData);
    env->DeleteLocalRef(newUserData);

    return newGlobalUserData;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_security_MainActivity_printUserData(
        JNIEnv *env,
        jobject /* this */,
        jobject userData) {
    // Find the id of the Java method to be called
    jclass userDataClass = env->GetObjectClass(userData);
    jmethodID methodId = env->GetMethodID(userDataClass, d(strings[6], k()).c_str(),
                                          d(strings[2], k()).c_str());

    return (jstring) env->CallObjectMethod(userData, methodId);
}

JNINativeMethod methods[] = {
        {"s", "()Ljava/lang/String;", (void *) b}
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass clazz = env->FindClass("com/example/security/n");
    if (clazz == nullptr) {
        return JNI_ERR;
    }

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) < 0) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}
