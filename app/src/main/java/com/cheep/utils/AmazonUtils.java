package com.cheep.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.cheep.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by giteeka on 24/8/17.
 */

public class AmazonUtils {
    private static AmazonS3Client sS3Client;
    private static CognitoCachingCredentialsProvider sCredProvider;
    //    private static TransferUtility sTransferUtility;
    private static final String TAG = AmazonUtils.class.getSimpleName();


    //    private static final String COGNITO_POOL_ID = "ap-south-1:219f4bf8-6f0c-4fbe-aacf-1079267e128d";
//    private static final String BUCKET_NAME = "cheepapp";
    private static final String AMAZON_REGION = Regions.AP_SOUTH_1.getName();
//    private static final String BUCKET_REGION = Regions.AP_SOUTH_1.getName();
    //    public static final String FOLDER_ORIGINAL = "strategic_partner/original";
//    public static final String FOLDER_THUMB = "strategic_partner/thumb";
//    static final String FOLDER_ORIGINAL = "task_image/original";
//    static final String FOLDER_THUMB = "task_image/thumb";

    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    BuildConfig.COGNITO_POOL_ID,
                    Regions.fromName(AMAZON_REGION));
        }
        return sCredProvider;
    }

    private static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client(getCredProvider(context.getApplicationContext()));
            sS3Client.setRegion(Region.getRegion(Regions.fromName(AMAZON_REGION)));
        }
        return sS3Client;
    }
//
//    private static TransferUtility getTransferUtility(Context context) {
//        if (sTransferUtility == null) {
//            sTransferUtility = new TransferUtility(getS3Client(context.getApplicationContext()),
//                    context.getApplicationContext());
//        }
//
//        return sTransferUtility;
//    }

    public static TransferObserver uploadMedia(final Context context, File file, String s3Path, TransferListener l) {

        TransferUtility sTransferUtility = TransferUtility.builder()
                .context(context)
                .s3Client(getS3Client(context))
                .defaultBucket(BuildConfig.BUCKET_NAME)
                .build();
        TransferObserver observer = sTransferUtility.upload(BuildConfig.BUCKET_NAME, s3Path, file);
        observer.setTransferListener(l);
        return observer;
    }

    public static String getFileNameWithExt(String mFilePath, boolean withExt) {
        if (!TextUtils.isEmpty(mFilePath)) {
            if (withExt)
                return mFilePath.substring(mFilePath.lastIndexOf("/") + 1);
            else {
                String name = mFilePath.substring(mFilePath.lastIndexOf("/") + 1, mFilePath.lastIndexOf("."));
                LogUtils.LOGI(TAG, "getNameFronPath: name : " + name);
                return name;
            }
        }
        return "";
    }

    public static String getExtension(String mFilePath) {
        if (!TextUtils.isEmpty(mFilePath)) {
            String ext = mFilePath.substring(mFilePath.lastIndexOf("."), mFilePath.length());
            LogUtils.LOGD(TAG, "getExtension() called with: ext = [" + ext + "]");
            return ext;
        }
        return "";
    }


    private static final int THUMBNAIL_SIZE = 400;

    public static String getImageThumbPath(Context context, String path) {
        String filepath = "";
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
            filepath = "";

        int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
                : bounds.outWidth;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = originalSize / THUMBNAIL_SIZE;
        Bitmap thumbnail = BitmapFactory.decodeFile(path, opts);
        File outputDir = context.getCacheDir(); // context being the Activity pointer
        try {
            File file = File.createTempFile(getFileNameWithExt(path, false), ".jpg", outputDir);
            if (thumbnail != null) {
                FileOutputStream out;
                try {
                    out = new FileOutputStream(file);
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    filepath = file.getAbsolutePath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException exp) {
            exp.printStackTrace();
        }
        return filepath;
    }

    public static String getVideoThumbPath(Context mContext, String mVideoPath) {
        try {
            File outputDir = mContext.getCacheDir(); // context being the Activity pointer
            File file = File.createTempFile(getFileNameWithExt(mVideoPath, false), ".jpg", outputDir);
            Bitmap mThumbBitmap = ThumbnailUtils.createVideoThumbnail(mVideoPath, MediaStore.Video.Thumbnails.MINI_KIND);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            mThumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        } catch (Exception e) {
            LogUtils.LOGI("TAG", "Exception while creating temp file" + e.toString());
            return "";
        }
        return "";
    }


    /**
     * @param name folder path of
     * @return path of uploaded file - amazon s3 url
     */
    public static String getOriginalURL(String name) {
        return BuildConfig.AMAZON_S3_URL + BuildConfig.BUCKET_NAME + File.separator + name;
    }

    public static String getThumbURL(String name) {
        return BuildConfig.AMAZON_S3_URL + BuildConfig.BUCKET_NAME + File.separator + name;
    }

    /**
     * when user click on cancel button from list
     * when user goes back to home screen without creating task.
     *
     * @param context
     * @param original
     * @param thumb
     */
    public static void deleteFiles(final Context context, final String original, final String thumb) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                getS3Client(context).deleteObject(new DeleteObjectRequest(BuildConfig.BUCKET_NAME + File.separator + BuildConfig.TASK_ORIGINAL_FOLDER, AmazonUtils.getFileNameWithExt(original, true)));
                getS3Client(context).deleteObject(new DeleteObjectRequest(BuildConfig.BUCKET_NAME + File.separator + BuildConfig.TASK_THUMB_FOLDER, AmazonUtils.getFileNameWithExt(thumb, true)));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

            }
        }.execute();
    }

    public static long getDuration(String selectedImagePath) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        use one of overloaded setDataSource() functions to set your data source
            retriever.setDataSource(selectedImagePath);

            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInSec = 0;
            try {
                timeInSec = Long.parseLong(time) / 1000;
            } catch (NumberFormatException e) {
                timeInSec = 0;
            }
            retriever.release();
            return timeInSec;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
