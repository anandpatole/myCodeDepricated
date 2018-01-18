package com.cheep.utils;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.style.ReplacementSpan;
import android.util.Log;

public class DottedUnderlineSpan extends ReplacementSpan {
    private static final String TAG = DottedUnderlineSpan.class.getSimpleName();
    private final float mOffsetY;
    private Paint p;
    private int mWidth;
    private String mSpan;

    private float mSpanLength;
    private boolean mLengthIsCached = false;

    public DottedUnderlineSpan(int _color, String _spannedText, float mDashPath, float mStrokeWidth, float mOffsetY) {
        this.mOffsetY = mOffsetY;
        p = new Paint();
        p.setColor(_color);
        p.setStyle(Paint.Style.STROKE);
        p.setPathEffect(new DashPathEffect(new float[]{mDashPath, mDashPath}, 0));
        p.setStrokeWidth(mStrokeWidth);
        mSpan = _spannedText;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        Log.d(TAG, "getSize() called with: paint = [" + paint + "], text = [" + text + "], start = [" + start + "], end = [" + end + "], fm = [" + fm + "]");
        mWidth = (int) paint.measureText(text, start, end);
        return mWidth;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        canvas.drawText(text, start, end, x, y, paint);
        if (!mLengthIsCached)
            mSpanLength = paint.measureText(mSpan);

        // https://code.google.com/p/android/issues/detail?id=29944
        // canvas.drawLine can't draw dashes when hardware acceleration is enabled,
        // but canvas.drawPath can
        Path path = new Path();
        path.moveTo(x, y + mOffsetY);
        path.lineTo(x + mSpanLength, y + mOffsetY);
        path.rMoveTo(x, y + mOffsetY);
        path.rLineTo(x + mSpanLength, y + mOffsetY);
        canvas.drawPath(path, this.p);
    }
}
