package org.example;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.InstallReceiver;
import com.android.ddmlib.Log.LogLevel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

  private static final int ADB_CONNECT_TIMEOUT_MS = 5000;
  final static String DEVICE_SERIAL = "emulator-5554";

  public static void main(String[] args) throws InstallException {
    System.out.println("Args: " + List.of(args));
    File apkFile = checkFileExists(args[0]);
    File adbFile = checkFileExists(args[1]);

    com.android.ddmlib.Log.setLevel(LogLevel.VERBOSE);

    AndroidDebugBridge.init(false /* clientSupport */);

    AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(adbFile.getAbsolutePath(), true,
        ADB_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    if (bridge == null) {
      throw new RuntimeException("Failed to initialize ADB bridge.");
    }

    waitForDevice(bridge);

    IDevice targetDevice = Arrays.stream(bridge.getDevices())
        .filter(device -> device.getSerialNumber().equals(DEVICE_SERIAL)).findFirst()
        .orElseThrow();

    // when installing an APK on the emulator (e.g. the emulator started from an android studio)
    // the installation process hangs on the 'V/ddms: execute: running rm "/data/local/tmp/app.apk"' step.
    // when invoking the same shell command from the terminal, (adb shell rm /data/local/tmp/app.apk), it works fine.
    // the hanged process also ignores the timeout passed to the installPackage method.

    // when installing an APK on a real device, the 'device.installPackage' method works fine.
    installApk(targetDevice, apkFile.getAbsolutePath());

    // This also works fine.
    // String installResult = executeShellCommandAndReadOutput(adbFile.getAbsolutePath(), List.of("install", apkFile.getAbsolutePath()));
    // System.out.println("Adb install cmd result: " + installResult);
    AndroidDebugBridge.terminate();
  }

  private static void installApk(IDevice device, String apkPath)
      throws InstallException {
    System.out.println("Installing APK: " + apkPath + " to device: " + device.getSerialNumber());
    InstallReceiver installReceiver = new InstallReceiver();
    device.installPackage(apkPath, false, installReceiver, 5, 5, TimeUnit.SECONDS);
    if (installReceiver.isSuccessfullyCompleted()) {
      System.out.println("APK installed successfully: " + installReceiver.getSuccessMessage());
    } else {
      System.out.println("APK not installed: " + installReceiver.getErrorMessage());
    }
  }


  private static String executeShellCommandAndReadOutput(String executable, List<String> args)
      throws IOException, InterruptedException {
    ArrayList<String> command = new ArrayList<>();
    command.add(executable);
    command.addAll(args);
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.redirectErrorStream(true);
    Process p = pb.start();
    p.waitFor();
    return new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
  }

  private static File checkFileExists(String path) {
    File file = new File(path);
    if (!file.exists()) {
      throw new IllegalArgumentException("File does not exist: " + path);
    }
    return file;
  }

  private static void waitForDevice(AndroidDebugBridge bridge) {
    System.out.println("Waiting for device to connect...");
    int timeoutMs = ADB_CONNECT_TIMEOUT_MS;
    int sleepMs = 1000;
    while (timeoutMs > 0) {
      if (bridge.hasInitialDeviceList()) {
        IDevice[] devices = AndroidDebugBridge.getBridge().getDevices();
        if (devices.length > 0) {
          System.out.println("Device(s) connected.");
          return;
        }
      }

      try {
        Thread.sleep(sleepMs);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      timeoutMs -= sleepMs;
    }

    System.err.println("Timeout waiting for device.");
  }
}