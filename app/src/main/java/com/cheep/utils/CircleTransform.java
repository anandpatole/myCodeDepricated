package com.cheep.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

public class CircleTransform extends BitmapTransformation {
    private static final String TAG = "CircleTransform";
    private boolean roundedCorner = false;
    private int borderColor = Color.WHITE;
    private int borderWidth = 0;
    private String mUrl;
    private String tag;

    public CircleTransform(Context context, String url, String tag) {
        super(context);
        this.mUrl = url;
        this.tag = url + "=" + tag;
    }

    public CircleTransform(Context context, boolean roundedCorner, int borderColor, int borderWidth, String url, String tag) {
        super(context);
//        Log.d(TAG, "CircleTransform() called with: context = [" + context + "], roundedCorner = [" + roundedCorner + "], borderColor = [" + borderColor + "], borderWidth = [" + borderWidth + "], url = [" + url + "], tag = [" + tag + "]");
        this.roundedCorner = roundedCorner;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        this.mUrl = url;
        this.tag = url + "=" + tag;
    }

    private Bitmap circleCrop(BitmapPool pool, Bitmap source) {
        if (source == null)
            return null;

        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

        Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();

        paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        if (roundedCorner) {
            Paint border = new Paint();
            border.setColor(borderColor);
            border.setStrokeWidth(borderWidth);
            border.setStyle(Paint.Style.STROKE);
            border.setFlags(Paint.ANTI_ALIAS_FLAG);
            canvas.drawCircle(r, r, r - 2, border);
        }
        return result;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
//        return toTransform;
        return circleCrop(pool, toTransform);
    }

    @Override
    public String getId() {
        return tag;
    }
} 