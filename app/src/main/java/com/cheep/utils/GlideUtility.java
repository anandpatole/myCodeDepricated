package com.cheep.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;

/**
 * Created by kruti on 6/3/18.
 */

public class GlideUtility {

    public static void loadImageView(Context context, ImageView img, String imageToLoad, int placeholderRes) {

        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    public static void loadImageView(Context context, ImageView img, String imageToLoad) {
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .crossFade()
                .into(img);
    }

    public static void loadImageView(Context context, ImageView img, int imageToLoad, int placeholderRes) {

        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    public static void loadImageView(Context context, ImageView img, String imageToLoad, int placeholderRes, RequestListener requestListener) {

        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .listener(requestListener)
                .crossFade()
                .into(img);
    }

    private static boolean isActivityCorrectForGlide(Context context) {
        if (context instanceof Activity) {
            if (context == null || ((Activity) context).isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && ((Activity) context).isDestroyed())) {
                return false;
            }
        }
        return true;
    }

    //Loading Circular image from url to imageview
    public static void showCircularImageView(Context context, String tag, ImageView img, String url, int placeholderRes) {
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(url)
                .transform(new CircleTransform(context, url, tag))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    public static void showCircularImageView(Context context, String tag, ImageView img, String imageToLoad, int placeholderRes, boolean isRounded, float strockwidthIndp) {
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context, isRounded, Color.WHITE, (int) Utility.convertDpToPixel(strockwidthIndp, context), imageToLoad, tag))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    public static void showCircularImageView(Context context, String tag, ImageView img, String imageToLoad, int placeholderRes, boolean isRounded) {
//        LogUtils.LOGD(TAG, "showCircularImageView() called with: context = [" + context + "], tag = [" + tag + "], img = [" + img + "], imageToLoad = [" + imageToLoad + "], placeholderRes = [" + placeholderRes + "], isRounded = [" + isRounded + "]");
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context, isRounded, Color.WHITE, 5, imageToLoad, tag))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }


    public static void showCircularImageViewWithColorBorder(Context context, String tag, ImageView img, String imageToLoad, int placeholderRes, int color, boolean isRounded) {
//        LogUtils.LOGD(TAG, "showCircularImageView() called with: context = [" + context + "], tag = [" + tag + "], img = [" + img + "], imageToLoad = [" + imageToLoad + "], placeholderRes = [" + placeholderRes + "], isRounded = [" + isRounded + "]");
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context, isRounded, ContextCompat.getColor(context, color), 5, imageToLoad, tag))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    public static void showCircularImageViewWithColorBorder(Context context, String tag, ImageView img, int imageToLoad, int color, boolean isRounded) {
//        LogUtils.LOGD(TAG, "showCircularImageView() called with: context = [" + context + "], tag = [" + tag + "], img = [" + img + "], imageToLoad = [" + imageToLoad + "], placeholderRes = [" + placeholderRes + "], isRounded = [" + isRounded + "]");
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context, isRounded, ContextCompat.getColor(context, color), 5, imageToLoad, tag))
                .crossFade()
                .into(img);
    }
}