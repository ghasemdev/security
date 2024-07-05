#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_security_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_security_k_a(
        JNIEnv *env,
        jobject /* this */,
        jobject b
) {
    // Get the OkHttpClient class
    jclass clientClass = env->GetObjectClass(b);

    // Find the newBuilder method from OkHttpClient class
    jmethodID method_newBuilder = env->GetMethodID(
            clientClass,
            "newBuilder",
            "()Lokhttp3/OkHttpClient$Builder;"
    );

    // Call newBuilder() to get OkHttpClient.Builder
    jobject builder = env->CallObjectMethod(b, method_newBuilder);

    // Get the OkHttpClient$Builder class
    jclass builderClass = env->GetObjectClass(builder);

    // Find the methods we need from OkHttpClient$Builder class
    jmethodID method_setCertificatePinner = env->GetMethodID(
            builderClass,
            "certificatePinner",
            "(Lokhttp3/CertificatePinner;)Lokhttp3/OkHttpClient$Builder;"
    );
    jmethodID method_followRedirects = env->GetMethodID(
            builderClass,
            "followRedirects",
            "(Z)Lokhttp3/OkHttpClient$Builder;"
    );
    jmethodID method_followSslRedirects = env->GetMethodID(
            builderClass,
            "followSslRedirects",
            "(Z)Lokhttp3/OkHttpClient$Builder;"
    );

    // Create CertificatePinner
    jclass certificatePinnerBuilderClass = env->FindClass("okhttp3/CertificatePinner$Builder");
    jmethodID certificatePinnerBuilderConstructor = env->GetMethodID(
            certificatePinnerBuilderClass,
            "<init>", "()V"
    );
    jobject certificatePinnerBuilder = env->NewObject(
            certificatePinnerBuilderClass,
            certificatePinnerBuilderConstructor
    );

    jmethodID addMethod = env->GetMethodID(
            certificatePinnerBuilderClass,
            "add",
            "(Ljava/lang/String;[Ljava/lang/String;)Lokhttp3/CertificatePinner$Builder;"
    );
    jstring pattern = env->NewStringUTF("moviesapi.ir");
    jobjectArray pins = env->NewObjectArray(1, env->FindClass("java/lang/String"), nullptr);
    jstring pin = env->NewStringUTF("sha256/NaML600Zdn8JqRXxynWV4nSQruBcra8o7YeRUM/UD6s=");
    env->SetObjectArrayElement(pins, 0, pin);

    certificatePinnerBuilder = env->CallObjectMethod(
            certificatePinnerBuilder,
            addMethod,
            pattern,
            pins
    );

    env->DeleteLocalRef(pattern);
    env->DeleteLocalRef(pin);
    env->DeleteLocalRef(pins);

    jmethodID buildMethod = env->GetMethodID(
            certificatePinnerBuilderClass,
            "build",
            "()Lokhttp3/CertificatePinner;"
    );
    jobject certificatePinner = env->CallObjectMethod(certificatePinnerBuilder, buildMethod);

    env->DeleteLocalRef(certificatePinnerBuilder);
    env->DeleteLocalRef(certificatePinnerBuilderClass);

    // Set CertificatePinner in OkHttpClient$Builder
    builder = env->CallObjectMethod(builder, method_setCertificatePinner, certificatePinner);

    env->DeleteLocalRef(certificatePinner);

    // Set followRedirects and followSslRedirects
    builder = env->CallObjectMethod(builder, method_followRedirects, JNI_FALSE);
    builder = env->CallObjectMethod(builder, method_followSslRedirects, JNI_FALSE);

    // Build the OkHttpClient from the builder
    jmethodID method_build = env->GetMethodID(builderClass, "build", "()Lokhttp3/OkHttpClient;");
    jobject newClient = env->CallObjectMethod(builder, method_build);

    env->DeleteLocalRef(builderClass);
    env->DeleteLocalRef(builder);
    env->DeleteLocalRef(clientClass);

    return newClient;
}
