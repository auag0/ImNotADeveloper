#pragma once

#include <map>
#include <jni.h>

static std::map<const char *, const char *> propOverrides;

extern "C"
JNIEXPORT void JNICALL
Java_io_github_auag0_imnotadeveloper_xposed_NativeFun_setProps(JNIEnv *env, jobject thiz, jobject props) {
    jclass mapClass = env->FindClass("java/util/Map");
    jmethodID entrySetMethod = env->GetMethodID(mapClass, "entrySet", "()Ljava/util/Set;");
    jobject entrySet = env->CallObjectMethod(props, entrySetMethod);

    jclass setClass = env->FindClass("java/util/Set");
    jmethodID iteratorMethod = env->GetMethodID(setClass, "iterator", "()Ljava/util/Iterator;");
    jobject iterator = env->CallObjectMethod(entrySet, iteratorMethod);

    jclass iteratorClass = env->FindClass("java/util/Iterator");
    jmethodID hasNextMethod = env->GetMethodID(iteratorClass, "hasNext", "()Z");
    jmethodID nextMethod = env->GetMethodID(iteratorClass, "next", "()Ljava/lang/Object;");

    jclass entryClass = env->FindClass("java/util/Map$Entry");
    jmethodID getKeyMethod = env->GetMethodID(entryClass, "getKey", "()Ljava/lang/Object;");
    jmethodID getValueMethod = env->GetMethodID(entryClass, "getValue", "()Ljava/lang/Object;");

    while (env->CallBooleanMethod(iterator, hasNextMethod)) {
        jobject entry = env->CallObjectMethod(iterator, nextMethod);
        jobject keyObj = env->CallObjectMethod(entry, getKeyMethod);
        jstring keyString = (jstring) keyObj;
        jobject valueObj = env->CallObjectMethod(entry, getValueMethod);
        jstring valueString = (jstring) valueObj;
        const char *key = env->GetStringUTFChars(keyString, nullptr);
        const char *value = env->GetStringUTFChars(valueString, nullptr);

        propOverrides[key] = value;

        env->ReleaseStringUTFChars(keyString, key);
        env->ReleaseStringUTFChars(valueString, value);
        env->DeleteLocalRef(entry);
        env->DeleteLocalRef(keyObj);
        env->DeleteLocalRef(valueObj);
    }

    env->DeleteLocalRef(mapClass);
    env->DeleteLocalRef(entrySet);
    env->DeleteLocalRef(setClass);
    env->DeleteLocalRef(iterator);
    env->DeleteLocalRef(iteratorClass);
    env->DeleteLocalRef(entryClass);
}