#include <stdlib.h>
#include "LSPosed.h"
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
    HookFunType hookFunc = entries->hook_func;
    hookFunc((void *) __system_property_get, (void *) hooked___system_property_get,
             (void **) &orig___system_property_get);
    hookFunc((void *) __system_property_find, (void *) hooked___system_property_find,
             (void **) &orig___system_property_find);
    return [](const char *name, void *handle) {};
}