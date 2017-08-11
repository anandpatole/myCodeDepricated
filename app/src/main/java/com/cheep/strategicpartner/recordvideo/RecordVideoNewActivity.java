package com.cheep.strategicpartner.recordvideo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cheep.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Creator Giteeka 31/07/2017
 * Activity for capturing video.
 * Maximum duration is 10 sec and and size 20 MB
 */
public class RecordVideoNewActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RecordVideoNewActivity";

    private static final int MAXIMUM_DURATION = 10000;
    private static final int DURATION_SECOND = 1000;
    private static final int MAX_FILE_SIZE = 20000000;

    @Nullable
    private Camera mCamera;
    private CameraPreview mPreview;
    @Nullable
    private MediaRecorder mediaRecorder;
    private TextView tvCounter;
    private ImageView ivCapture;
    private ImageView ivSwitchCamera;
    private boolean cameraFront = false;
    private boolean recording = false;
    private File output_file;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_record_video);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initialize();
    }

    private void initialize() {

        tvCounter = (TextView) findViewById(R.id.tv_counter);

        // image view for front/back camera selection
        ivSwitchCamera = (ImageView) findViewById(R.id.iv_change_camera);
        ivSwitchCamera.setOnClickListener(this);


        FrameLayout flCameraPreview = (FrameLayout) findViewById(R.id.camera_preview);
        mPreview = new CameraPreview(RecordVideoNewActivity.this, mCamera);
        flCameraPreview.addView(mPreview);

        // image view for start/stop video playing
        ivCapture = (ImageView) findViewById(R.id.ivCapture);
        ivCapture.setOnClickListener(this);
    }


    public void onResume() {
        super.onResume();
        if (!hasCamera(RecordVideoNewActivity.this)) {
            showToast(RecordVideoNewActivity.this, "Sorry, your phone does not have a camera!");
            finish();
        }
        if (mCamera == null) {
            // if the front facing camera does not exist
            if (findFrontFacingCamera() < 0) {
                showToast(RecordVideoNewActivity.this, "No front facing camera found.");
                ivSwitchCamera.setVisibility(View.GONE);
            }
            try {
                mCamera = Camera.open(findBackFacingCamera());
                mCamera.setDisplayOrientation(90);

                Camera.Parameters params = mCamera.getParameters();
                if (params.getSupportedFocusModes().contains(
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
                mCamera.setParameters(params);
                mPreview.refreshCamera(mCamera);
            } catch (RuntimeException exp) {
                Toast.makeText(RecordVideoNewActivity.this, exp.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void showToast(RecordVideoNewActivity RecordVideoNewActivity, String s) {
        Toast.makeText(RecordVideoNewActivity, s, Toast.LENGTH_SHORT).show();
    }


    private void chooseCamera() {
        // if the camera preview is the front
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview

                mCamera = Camera.open(cameraId);
                // mPicture = getPictureCallback();
                mCamera.setDisplayOrientation(90);
                Camera.Parameters params = mCamera.getParameters();
                if (params.getSupportedFocusModes().contains(
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
                mPreview.refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview

                mCamera = Camera.open(cameraId);
                // mPicture = getPictureCallback();
                mCamera.setDisplayOrientation(90);

                Camera.Parameters params = mCamera.getParameters();
                if (params.getSupportedFocusModes().contains(
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }

                mCamera.startPreview();
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // when on Pause, release camera in order to be used from other
        // applications
        releaseCamera();
    }


    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    private boolean hasCamera(Context context) {
        // check if the device has camera
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_change_camera:
                // get the number of cameras
                if (!recording) {
                    int camerasNumber = Camera.getNumberOfCameras();
                    if (camerasNumber > 1) {
                        // release the old camera instance
                        // switch camera, from the front and the back and vice versa
                        releaseCamera();
                        chooseCamera();
                    } else {
                        showToast(RecordVideoNewActivity.this, "Sorry, your phone has only one camera!");
                    }
                }
                break;
            case R.id.ivCapture:
                if (recording) {
                    stopMediaRecorder();
                } else {
                    startMediaRecorder();
                }
                break;
        }
    }

    /**
     * start 10 sec count timer and media recorder
     * set stop button
     */
    private void startMediaRecorder() {
        try {
            if (!prepareMediaRecorder()) {
                Toast.makeText(RecordVideoNewActivity.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
                finish();
            }
            // work on UiThread for better performance
            runOnUiThread(new Runnable() {
                public void run() {
                    // If there are stories, add them to the table
                    try {
                        mediaRecorder.start();
                        mCountDownTimer.start();
                        recording = true;
                        ivCapture.setImageResource(R.drawable.ic_stop);
                    } catch (final Exception ex) {
                        showToast(RecordVideoNewActivity.this, "Recording fail.");
                    }
                }
            });
        } catch (Exception exp) {
            Log.e(TAG, " startMediaRecorder() : " + exp.getMessage());
        }
    }

    /**
     * stop video recording,
     * release media recorder and camera
     * and send file result back to called fragment/activity
     */
    private void stopMediaRecorder() {
        try {
            mCountDownTimer.cancel();
            mediaRecorder.stop(); // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            recording = false;
            ivCapture.setImageResource(R.drawable.ic_play);
            Intent sendData = getIntent();
            sendData.putExtra("path", output_file.getAbsolutePath());
            setResult(RESULT_OK, sendData);
            finish();
        } catch (Exception exp) {
            Log.e(TAG, " stopMediaRecorder() : " + exp.getMessage());
        }
    }

    /**
     * release media recorder and camera and finish activity
     */
    private void cancelMediaRecorder() {
        try {
            if (recording) {
                mCountDownTimer.cancel();
                mediaRecorder.stop(); // stop the recording
                releaseMediaRecorder(); // release the MediaRecorder object
                recording = false;
            }
            ivCapture.setImageResource(R.drawable.ic_stop);
            setResult(RESULT_CANCELED, getIntent());
            finish();
        } catch (Exception exp) {
            Log.e(TAG, " cancelMediaRecorder() : " + exp.getMessage());
        }
    }

    private void releaseMediaRecorder() {
        try {

            if (mediaRecorder != null) {
                mediaRecorder.reset(); // clear recorder configuration
                mediaRecorder.release(); // release the recorder object
                mediaRecorder = null;
                mCamera.lock(); // lock camera for later use
            }
        } catch (Exception exp) {
            Log.e(TAG, "releaseMediaRecorder() : " + exp.getMessage());
        }
    }

    /**
     * @return true if media recorder is set up completely
     */
    private boolean prepareMediaRecorder() {

        mediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        int numCameras = Camera.getNumberOfCameras();

        if (numCameras > 1) {
            if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
                Log.e(TAG, "QUALITY_480P");
                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                mediaRecorder.setProfile(CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_FRONT, CamcorderProfile.QUALITY_480P));
                mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
            } else {
                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
                Log.e(TAG, "QUALITY_480P not supported.");
                mediaRecorder.setProfile(CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_FRONT, CamcorderProfile.QUALITY_LOW));
                mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
            }
        } else {
            if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
                Log.e(TAG, "QUALITY_480P");
                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                mediaRecorder.setProfile(CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_480P));
                mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
            } else {
                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
                Log.e(TAG, "QUALITY_480P not supported.");
                mediaRecorder.setProfile(CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_LOW));
                mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
            }
        }


        output_file = getOutputMediaFile(this);
        if (output_file.exists()) {
            output_file.delete();
        }
        mediaRecorder.setOutputFile(output_file.toString());
        mediaRecorder.setMaxDuration(MAXIMUM_DURATION); // Set max duration 10 sec.
        mediaRecorder.setMaxFileSize(MAX_FILE_SIZE); // Set max file size 20M

        try {
            if (cameraFront) {
                mediaRecorder.setOrientationHint(270);
            } else {
                mediaRecorder.setOrientationHint(90);
            }
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;

    }
  private File getOutputMediaFile(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss"/*, Locale.US*/).format(new Date());
        String imageFileName = "VID_" + timeStamp + ".mp4";

        return new File(new File(context.getFilesDir(), "CheepImages"), imageFileName);
    }
    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * count down timer for 10 sec and update text
     */
    private CountDownTimer mCountDownTimer = new CountDownTimer(MAXIMUM_DURATION, DURATION_SECOND - 500) {
        @Override
        public void onTick(long millisUntilFinished) {
            //Update text
            int remaining_seconds = (int) millisUntilFinished / DURATION_SECOND;
            tvCounter.setText(String.format("%d Sec", remaining_seconds));
        }

        @Override
        public void onFinish() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopMediaRecorder();
                }
            }, 1000);
        }
    };
}