#include <stdlib.h>
#include "LSPosed.h"
#include "Dobby/include/dobby.h"
#include <sys/system_properties.h>
#include "PropKeys.h"
#include "Logger.h"
#include <jni.h>

int (*orig___system_property_get)(...);

int hooked___system_property_get(const char *name, char *value) {
    LOGD("__system_property_get: %s", name);
    auto newProp = propOverrides.find(name);
    if (newProp != propOverrides.end()) {
        strcpy(value, newProp->second);
        return strlen(value);
    }
    return orig___system_property_get(name, value);
}

void *(*orig___system_property_find)(...);

void *hooked___system_property_find(const char *name) {
    // idk, it loop and freeze
    //LOGD("__system_property_find: %s", name);
    auto newProp = propOverrides.find(name);
    if (newProp != propOverrides.end()) {
        return nullptr;
    }
    return orig___system_property_find(name);
}

extern "C" [[gnu::visibility("default")]] [[gnu::used]]
NativeOnModuleLoaded native_init(const NativeAPIEntries *entries) {
    LOGD("native_init");
    void *target;
    int fail = 0;
    target = DobbySymbolResolver("libc.so", "__system_property_get");
    fail += DobbyHook(target, (void *) hooked___system_property_get,
                      (void **) &orig___system_property_get);

    target = DobbySymbolResolver("libc.so", "__system_property_find");
    fail += DobbyHook(target, (void *) hooked___system_property_find,
                      (void **) &orig___system_property_find);
    if (fail > 0) {
        LOGE("hook failures: %d", fail);
    }
    return [](const char *name, void *handle) {};
}