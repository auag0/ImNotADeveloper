# ImNotADeveloper
This is an Xposed module that hides things like developer mode and USB debugging status  
開発者モードやUsbデバッグの状態を隠すXposedモジュールです。

## How to open settings
you can open module settings from LSPosed or open module settings from app info screen  
LSPosedでモジュール設定を押すか、アプリ情報画面の「アプリ内の設定」からモジュールの設定を開く事が出来ます

## hooked methods
- **android.provider.Settings**
  - Secure.getStringForUser()
  - System.getStringForUser()
  - Global.getStringForUser()
  - NameValueCache.getStringForUser()
- **android.os.SystemProperties**
  - native_get()
  - native_get_int()
  - native_get_long()
  - native_get_boolean()
- **java.lang.ProcessManager**
  - exec()
- **java.lang.ProcessImpl**
  - start()
- **native**
  - __system_property_get()
  - __system_property_find()

## banned keys ([latest](/app/src/main/java/io/github/auag0/imnotadeveloper/common/PropKeys.kt))
- **property keys**
  - sys.usb.ffs.ready
  - sys.usb.config
  - persist.sys.usb.config
  - sys.usb.state
  - init.svc.adbd
- **variable keys**
  - development_settings_enabled
  - adb_enabled
  - adb_wifi_enabled

 ## super thanks (reference)
 - [**xfqwdsj/IAmNotADeveloper**](https://github.com/xfqwdsj/IAmNotADeveloper)
