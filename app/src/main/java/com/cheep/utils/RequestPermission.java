package com.cheep.utils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;


public class RequestPermission {
    FragmentActivity mActivity;
    Fragment mFragment;

    public static final int REQUEST_PERMISSION_FOR_VIDEO_CAPTURE = 1001;
    public static final int REQUEST_PERMISSION_FOR_IMAGE_CAPTURE = 1002;
    public static final int REQUEST_PERMISSION_FOR_OPEN_GALLERY_VIDEO = 1003;
    public static final int REQUEST_PERMISSION_FOR_OPEN_GALLERY_IMAGE = 1004;

    public String[] permissionsRequiredForVideoCapture = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};

    public String[] permissionsRequiredForImageCapture = new String[]{Manifest.permission.CAMERA};

    public String[] permissionsRequiredForOpenGallery = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

    OnRequestPermissionResult mOnRequestPermissionResult;

    public RequestPermission(Fragment fragment, OnRequestPermissionResult onRequestPermissionResult) {
        mFragment = fragment;
        mActivity = fragment.getActivity();
        mOnRequestPermissionResult = onRequestPermissionResult;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_FOR_VIDEO_CAPTURE:
                boolean allGranted = false;
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        allGranted = true;
                    } else {
                        allGranted = false;
                        break;
                    }
                }
                if (!allGranted) {
                    Utility.showToast(mActivity, "Some of permission was not granted");
                } else {
                    mOnRequestPermissionResult.onPermissionGranted(requestCode);
                }
                break;
            case REQUEST_PERMISSION_FOR_IMAGE_CAPTURE:
            case REQUEST_PERMISSION_FOR_OPEN_GALLERY_IMAGE:
            case REQUEST_PERMISSION_FOR_OPEN_GALLERY_VIDEO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mOnRequestPermissionResult.onPermissionGranted(requestCode);
                } else {
                    mOnRequestPermissionResult.onPermissionDenied(requestCode);
                }
                break;
        }
    }

    public boolean shouldCheckVideoCapturePermission() {
        return ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    public boolean shouldCheckImageCapturePermission() {
        return ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
    }

    public boolean shouldCheckOpenGalleryPermission() {
        return ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }


    public interface OnRequestPermissionResult {
        void onPermissionGranted(int code);

        void onPermissionDenied(int code);
    }

}
