# ddmlib-install-package-repro

This sample reproduces the bug in 'device.installPackage'. The method hangs indefinitly when installing an APK to the emulator.

Run it using `bazelisk run //src/main/org/example:Main`

when installing an APK on the emulator (e.g. the emulator started from an android studio)

the installation process hangs on the 'V/ddms: execute: running rm "/data/local/tmp/app.apk"' step.

when invoking the same shell command from the terminal, (adb shell rm /data/local/tmp/app.apk), it works fine.

the hanged process also ignores the timeout passed to the installPackage method.

when installing an APK on a real device, the 'device.installPackage' method works fine.

I suspect, the process may hand because on the emulator sometimes the shell asks if I really want to delete the read only file (when doing adb shell rm) and waits for the `(Y/n)` confirmation.

This happens to me once while I was playing with the ddmlib API.
The weird thing is, however, that the API hangs even when the manual invocation of `adb shell rm ...` works fine

Log for the emulator:
```
INFO: Running command line: bazel-bin/src/main/org/example/Main src/android/app/app.apk ../rules_android++android_sdk_repository_extension+androidsdk/platform-tools/adb
Args: [src/android/app/app.apk, ../rules_android++android_sdk_repository_extension+androidsdk/platform-tools/adb]
07:05:50 D/ddms: Monitor is up
07:05:50 D/ddms: Launching '/usr/local/google/home/ktimofeev/.cache/bazel/_bazel_ktimofeev/005815a26d14c806e76984ac3447652e/execroot/_main/bazel-out/k8-fastbuild/bin/src/main/org/example/Main.runfiles/_main/../rules_android++android_sdk_repository_extension+androidsdk/platform-tools/adb start-server' to ensure ADB is running.
07:05:50 D/ddms: '/usr/local/google/home/ktimofeev/.cache/bazel/_bazel_ktimofeev/005815a26d14c806e76984ac3447652e/execroot/_main/bazel-out/k8-fastbuild/bin/src/main/org/example/Main.runfiles/_main/../rules_android++android_sdk_repository_extension+androidsdk/platform-tools/adb start-server' succeeded
Waiting for device to connect...
07:05:50 D/DeviceMonitor: Opening adb connection
07:05:50 I/DeviceMonitor: Connected to adb for device monitoring
07:05:50 V/EmulatorConsole: Creating emulator console for 5554
07:05:50 V/EmulatorConsole: Removing emulator console for 5554
07:05:50 V/ddms: execute: running getprop
07:05:50 V/ddms: execute 'getprop' on 'emulator-5554' : EOF hit. Read: -1
07:05:50 V/ddms: execute: returning
Device(s) connected.
Installing APK: /usr/local/google/home/ktimofeev/.cache/bazel/_bazel_ktimofeev/005815a26d14c806e76984ac3447652e/execroot/_main/bazel-out/k8-fastbuild/bin/src/main/org/example/Main.runfiles/_main/src/android/app/app.apk to device: emulator-5554
07:05:51 D/app.apk: Uploading app.apk onto device 'emulator-5554'
07:05:51 D/Device: Uploading file onto device 'emulator-5554'
07:05:51 D/ddms: Reading file permission of /usr/local/google/home/ktimofeev/.cache/bazel/_bazel_ktimofeev/005815a26d14c806e76984ac3447652e/execroot/_main/bazel-out/k8-fastbuild/bin/src/main/org/example/Main.runfiles/_main/src/android/app/app.apk as: r-xr-xr-x
07:05:51 V/ddms: execute: running pm install  "/data/local/tmp/app.apk"
07:05:51 V/ddms: execute 'pm install  "/data/local/tmp/app.apk"' on 'emulator-5554' : EOF hit. Read: -1
07:05:51 V/ddms: execute: returning
07:05:51 V/ddms: execute: running rm "/data/local/tmp/app.apk"
^C%           
```

Log for the real device:
```
Args: [src/android/app/app.apk, ../rules_android++android_sdk_repository_extension+androidsdk/platform-tools/adb]
07:14:03 D/ddms: Monitor is up
07:14:03 D/ddms: Launching '/usr/local/google/home/ktimofeev/.cache/bazel/_bazel_ktimofeev/005815a26d14c806e76984ac3447652e/execroot/_main/bazel-out/k8-fastbuild/bin/src/main/org/example/Main.runfiles/_main/../rules_android++android_sdk_repository_extension+androidsdk/platform-tools/adb start-server' to ensure ADB is running.
07:14:03 D/ddms: '/usr/local/google/home/ktimofeev/.cache/bazel/_bazel_ktimofeev/005815a26d14c806e76984ac3447652e/execroot/_main/bazel-out/k8-fastbuild/bin/src/main/org/example/Main.runfiles/_main/../rules_android++android_sdk_repository_extension+androidsdk/platform-tools/adb start-server' succeeded
Waiting for device to connect...
07:14:03 D/DeviceMonitor: Opening adb connection
07:14:03 I/DeviceMonitor: Connected to adb for device monitoring
07:14:03 V/EmulatorConsole: Creating emulator console for 5554
07:14:03 V/EmulatorConsole: Removing emulator console for 5554
07:14:03 V/ddms: execute: running getprop
07:14:03 V/ddms: execute: running getprop
07:14:03 V/ddms: execute 'getprop' on 'localhost:27209' : EOF hit. Read: -1
07:14:03 V/ddms: execute: returning
07:14:03 V/ddms: execute 'getprop' on 'emulator-5554' : EOF hit. Read: -1
07:14:03 V/ddms: execute: returning
Device(s) connected.
Installing APK: /usr/local/google/home/ktimofeev/.cache/bazel/_bazel_ktimofeev/005815a26d14c806e76984ac3447652e/execroot/_main/bazel-out/k8-fastbuild/bin/src/main/org/example/Main.runfiles/_main/src/android/app/app.apk to device: localhost:27209
07:14:04 D/app.apk: Uploading app.apk onto device 'localhost:27209'
07:14:04 D/Device: Uploading file onto device 'localhost:27209'
07:14:04 D/ddms: Reading file permission of /usr/local/google/home/ktimofeev/.cache/bazel/_bazel_ktimofeev/005815a26d14c806e76984ac3447652e/execroot/_main/bazel-out/k8-fastbuild/bin/src/main/org/example/Main.runfiles/_main/src/android/app/app.apk as: r-xr-xr-x
07:14:04 V/ddms: execute: running pm install  "/data/local/tmp/app.apk"
07:14:05 V/ddms: execute 'pm install  "/data/local/tmp/app.apk"' on 'localhost:27209' : EOF hit. Read: -1
07:14:05 V/ddms: execute: returning
07:14:05 V/ddms: execute: running rm "/data/local/tmp/app.apk"
07:14:05 V/ddms: execute 'rm "/data/local/tmp/app.apk"' on 'localhost:27209' : EOF hit. Read: -1
07:14:05 V/ddms: execute: returning
APK installed successfully: Success
07:14:05 D/ddms: Waiting for Monitor thread

```
