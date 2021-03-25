package com.tenth.space.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

public class PermissionUtil {

    private static String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    public static boolean isCameraPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(PERMISSION_CAMERA) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{PERMISSION_CAMERA}, requestCode);
                return false;
            }
        }
        return true;
    }

    public static boolean isWriteExternalStoragePermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(PERMISSION_WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{PERMISSION_WRITE_EXTERNAL_STORAGE}, requestCode);
                return false;
            }
        }
        return true;
    }

}
