package com.cheep.firebase;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

import com.cheep.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.UUID;

/**
 * Created by sanjay on 27/7/16.
 */
public class FirebaseUtils
{
    /**
     * One second (in milliseconds)
     */
    private static final int _A_SECOND = 1000;
    /**
     * One minute (in milliseconds)
     */
    private static final int _A_MINUTE = 60 * _A_SECOND;
    /**
     * One hour (in milliseconds)
     */
    private static final int _AN_HOUR = 60 * _A_MINUTE;
    /**
     * One day (in milliseconds)
     */
    private static final int _A_DAY = 24 * _AN_HOUR;
    /* default chat thumbnail size */
    private static final int CHAT_THUMBNAIL_SIZE = 20;

    /*
    * get T_SP_U formatted Id
    * */
    public static String get_T_SP_U_FormattedId(String taskId, String mSpId, String mUserId)
    {
        if (!TextUtils.isEmpty(taskId) && !TextUtils.isEmpty(mSpId) && !TextUtils.isEmpty(mUserId))
        {
            String formattedTaskId=getPrefixTaskId(taskId);
            String formattedSpId=getPrefixSPId(mSpId);
            String formattedUserId=getPrefixUserId(mUserId);
            return String.format("%s_%s_%s",formattedTaskId,formattedSpId,formattedUserId);
        }
        return "";
    }

    /**
     * Used to get prefix userId
     * @param userId pass the userId
     */
    public static String getPrefixUserId(String userId)
    {
        if(!TextUtils.isEmpty(userId))
        {
            if(userId.toUpperCase().startsWith("U"))
            {
                return userId;
            }
            return String.format("U%s",userId);
        }
        return "";
    }


    /**
     * Used to get prefix spId
     * @param spId pass the spId
     */
    public static String getPrefixSPId(String spId)
    {
        if(!TextUtils.isEmpty(spId))
        {
            if(spId.toUpperCase().startsWith("SP"))
            {
                return spId;
            }
            return String.format("SP%s",spId);
        }
        return "";
    }

    /**
     * get formatted prefix(T) task id
     * @param taskId
     */
    public static String getPrefixTaskId(String taskId)
    {
        if(!TextUtils.isEmpty(taskId))
        {
            if(taskId.toUpperCase().startsWith("T"))
            {
                return taskId;
            }
            return String.format("T%s",taskId);
        }
        return "";
    }

    public static String removePrefixTaskId(String taskId)
    {
        if(!TextUtils.isEmpty(taskId))
        {
            if(taskId.toUpperCase().startsWith("T"))
            {
                return taskId.replace("T","").trim();
            }
        }
        return taskId;
    }

    public static String removePrefixUserIdId(String userId)
    {
        if(!TextUtils.isEmpty(userId))
        {
            if(userId.toUpperCase().startsWith("U"))
            {
                return userId.replace("U","").trim();
            }
        }
        return userId;
    }

    public static String removePrefixSpId(String spId)
    {
        if(!TextUtils.isEmpty(spId))
        {
            if(spId.toUpperCase().startsWith("SP"))
            {
                return spId.replace("SP","").trim();
            }
        }
        return spId;
    }

    /*
    * get unique identifier
    * */
    public static String getUUID() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    // Used to get current time in milliseconds
    public static long getCurrentTimeInMilliseconds() {
        return System.currentTimeMillis();
    }

    /**
     * Used to get formatted time from millisecond timestamp
     * @param time timestamp in milliseconds
     * @param context the context
     */
    public static String getTimeAgo(long time, Context context) {
        if (time < 1000000000000L)
            // if timestamp given in seconds, convert to millis
            time *= 1000;

        final long now = getCurrentTimeInMilliseconds();
        if (time > now || time <= 0) return "";


        final Resources res = context.getResources();
        final long time_difference = now - time;
        if (time_difference < _A_MINUTE)
            return res.getString(R.string.just_now);
        else if (time_difference < 50 * _A_MINUTE)
            return res.getString(R.string.time_ago,
                    res.getQuantityString(R.plurals.minutes, (int) time_difference / _A_MINUTE, time_difference / _A_MINUTE));
        else if (time_difference < 24 * _AN_HOUR)
            return res.getString(R.string.time_ago,
                    res.getQuantityString(R.plurals.hours, (int) time_difference / _AN_HOUR, time_difference / _AN_HOUR));
        else if (time_difference < 48 * _AN_HOUR)
            return res.getString(R.string.yesterday);
        else
            return res.getString(R.string.time_ago,
                    res.getQuantityString(R.plurals.days, (int) time_difference / _A_DAY, time_difference / _A_DAY));
    }

    /*
    * Return file size from sdcard file path.
    * */
    public static long getFileSize(String filePath)
    {
        if (!TextUtils.isEmpty(filePath))
        {
            File file = new File(filePath);
            if (file != null && file.exists())
            {
                return file.length();
            }
        }
        return 0;
    }

    /*
    * Return readable file size from actual file(long) size.
    * */
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String getFilename(Context context, String mMessageId) {
        if (context != null && !TextUtils.isEmpty(mMessageId)) {
            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + context.getResources().getString(R.string.app_name) + File.separator + "Images";
            if (!TextUtils.isEmpty(fileName)) {
                File file = new File(fileName);
                if (!file.exists()) {
                    file.mkdirs();
                }
                fileName = fileName + File.separator + mMessageId + ".jpg";
                return fileName;
            }
        }
        return "";
    }

    public static boolean isFileExists(Context context, String mMessageId) {
        if (!TextUtils.isEmpty(mMessageId)) {
            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + context.getResources().getString(R.string.app_name) + File.separator + "Images" +
                    File.separator + mMessageId + ".jpg";
            File file = new File(fileName);
            if (file != null && file.exists()) {
                return true;
            }
        }
        return false;
    }

    public static File getFileobject(Context context, String mMessageId) {
        if (!TextUtils.isEmpty(mMessageId)) {
            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + context.getResources().getString(R.string.app_name) + File.separator + "Images" +
                    File.separator + mMessageId + ".jpg";
            File file = new File(fileName);
            if (file != null && file.exists()) {
                return file;
            }
        }
        return null;
    }

    public static String getChatThumbnailPath(Context context, String path, String mMessageId) {
        String filepath = "";
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
            filepath = "";

        int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
                : bounds.outWidth;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = originalSize / CHAT_THUMBNAIL_SIZE;
        Bitmap thumbnail = BitmapFactory.decodeFile(path, opts);
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(String.format("%s/%s/Images", sdCard.getAbsolutePath(), context.getResources().getString(R.string.app_name)));
            dir.mkdirs();
            File file = new File(dir, String.format("%s_THUMB.jpg", mMessageId));
            if (thumbnail != null) {
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    if (file.getAbsolutePath() != null) {
                        filepath = file.getAbsolutePath();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception exp) {
            if (exp != null) {
                exp.printStackTrace();
            }
        }
        return filepath;
    }

    public static String getStorageFileName(String mMessageId, boolean isThumb) {
        if (!TextUtils.isEmpty(mMessageId)) {
            if (isThumb) {
                return String.format("%s_THUMB.jpg", mMessageId);
            } else {
                return String.format("%s.jpg", mMessageId);
            }
        }
        return "";
    }
}
