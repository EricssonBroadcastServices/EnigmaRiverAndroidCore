package com.redbeemedia.enigma.core.util.device;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by Joao Coelho on 2017-10-03.
 */

public class CheckRootUtil {
    private static final String TAG = CheckRootUtil.class.getName();

    public static boolean isDeviceRooted() {
        if (checkDeviceDebuggable() || checkSuperuserApk() || checkRootPathSU() || checkAccessRootData() || checkGetRootAuth()) {
            return true;
        }

        return false;
    }

    private static void logd(String tag, String message) {
//        Log.d(tag, message);
    }

    private static void loge(String tag, String message) {
//        Log.e(tag, message);
    }

    // check buildTags
    private static boolean checkDeviceDebuggable() {
        String buildTags = android.os.Build.TAGS;
        if (buildTags != null && buildTags.contains("test-keys")) {
            logd(TAG, "buildTags = " + buildTags);
            return true;
        }
        return false;
    }

    // Superuser.apk
    private static boolean checkSuperuserApk() {
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                logd(TAG , "/system/app/Superuser.apk exist");
                return true;
            }
        } catch (Exception e) {
            loge(TAG,  e.toString());
        }
        return false;
    }

    // find su in some path
    private static boolean checkRootPathSU() {
        final String kSuSearchPaths[] = {"/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/"};
        try {
            for (String path : kSuSearchPaths) {
                if (new File(path + "su").exists()) {
                    logd(TAG,  "find su in : " + path);
                    return true;
                }
            }
        } catch (Exception e) {
            loge(TAG,  e.toString());
        }
        return false;
    }

    // Check /data
    private static synchronized boolean checkAccessRootData() {
        try {
            logd(TAG,  "to write /data");
            String fileContent = "test_ok";
            Boolean writeFlag = writeFile("/data/su_test", fileContent);
            if (writeFlag) {
                logd(TAG,  "write ok");
            } else {
                logd(TAG,  "write failed");
            }

            logd(TAG,  "to read /data");
            String strRead = readFile("/data/su_test");
            logd(TAG,  "strRead=" + strRead);
            return fileContent.equals(strRead);

        } catch (Exception e) {
            loge(TAG,  "Unexpected error - Here is what I know: " + e.getMessage());
            return false;
        }
    }

    private static Boolean writeFile(String fileName, String message) {
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            byte[] bytes = message.getBytes();
            fout.write(bytes);
            fout.close();
            return true;

        } catch (Exception e) {
            loge(TAG,  e.toString());
            return false;
        }
    }

    private static String readFile(String fileName) {
        File file = new File(fileName);
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len;
            while ((len = fis.read(bytes)) > 0) {
                bos.write(bytes, 0, len);
            }
            String result = new String(bos.toByteArray());
            logd(TAG,  result);
            return result;

        } catch (Exception e) {
            loge(TAG,  e.toString());
            return null;
        }
    }

    // exec su
    private static synchronized boolean checkGetRootAuth() {
        Process process = null;
        DataOutputStream os = null;
        try {
            logd(TAG,  "to exec su");
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            logd(TAG,  "exitValue=" + exitValue);
            return exitValue == 0;

        } catch (Exception e) {
            loge(TAG,  "Unexpected error - Here is what I know: " + e.getMessage());
            return false;

        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                loge(TAG,  e.toString());
            }
        }
    }
}