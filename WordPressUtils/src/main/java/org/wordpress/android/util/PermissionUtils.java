package org.wordpress.android.util;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    /**
     * Check for permissions, request them if they're not granted.
     *
     * @return true if permissions are already granted, else request them and return false.
     */
    public static boolean checkAndRequestPermissions(Activity activity, int requestCode, String[] permissionList) {
        List<String> toRequest = new ArrayList<>();
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                toRequest.add(permission);
            }
        }
        if (toRequest.size() > 0) {
            String[] requestedPermissions = toRequest.toArray(new String[toRequest.size()]);
            ActivityCompat.requestPermissions(activity, requestedPermissions, requestCode);
            return false;
        }
        return true;
    }

    /**
     * Check for permissions, request them if they're not granted.
     *
     * @return true if permissions are already granted, else request them and return false.
     */
    private static boolean checkAndRequestPermissions(Fragment fragment, int requestCode, String[] permissionList) {
        List<String> toRequest = new ArrayList<>();
        for (String permission : permissionList) {
            Context context = fragment.getActivity();
            if (context != null && ContextCompat.checkSelfPermission(context, permission) != PackageManager
                    .PERMISSION_GRANTED) {
                toRequest.add(permission);
            }
        }
        if (toRequest.size() > 0) {
            String[] requestedPermissions = toRequest.toArray(new String[toRequest.size()]);
            fragment.requestPermissions(requestedPermissions, requestCode);
            return false;
        }
        return true;
    }

    /**
     * Check for permissions without requesting them
     *
     * @return true if all permissions are granted
     */
    public static boolean checkPermissions(Context context, String[] permissionList) {
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check for permissions without requesting them
     *
     * @return true if all permissions are granted
     */
    public static boolean checkPermissions(Activity activity, String[] permissionList) {
        return checkPermissions((Context) activity, permissionList);
    }

    public static boolean checkCameraAndStoragePermissions(Context context) {
        return checkPermissions(context, getCameraAndStoragePermissions());
    }

    public static boolean checkCameraAndStoragePermissions(Activity activity) {
        return checkPermissions(activity, getCameraAndStoragePermissions());
    }

    public static boolean checkNotificationsPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkPermissions(activity, new String[]{permission.POST_NOTIFICATIONS});
        } else {
            return true;
        }
    }

    public static boolean checkAndRequestCameraAndStoragePermissions(Fragment fragment, int requestCode) {
        return checkAndRequestPermissions(fragment, requestCode, getCameraAndStoragePermissions());
    }

    public static boolean checkAndRequestCameraAndStoragePermissions(Activity activity, int requestCode) {
        return checkAndRequestPermissions(activity, requestCode, getCameraAndStoragePermissions());
    }

    public static boolean checkAndRequestStoragePermission(Activity activity, int requestCode) {
        return checkAndRequestPermissions(activity, requestCode, getStoragePermissions());
    }

    public static boolean checkAndRequestStoragePermission(Fragment fragment, int requestCode) {
        return checkAndRequestPermissions(fragment, requestCode, getStoragePermissions());
    }

    public static String[] getCameraAndStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{permission.CAMERA};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return new String[]{permission.CAMERA, permission.READ_EXTERNAL_STORAGE};
        } else {
            return new String[]{permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE};
        }
    }

    private static String[] getStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{permission.READ_MEDIA_IMAGES, permission.READ_MEDIA_VIDEO};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return new String[]{permission.READ_EXTERNAL_STORAGE};
        } else {
            return new String[]{permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE};
        }
    }
}
