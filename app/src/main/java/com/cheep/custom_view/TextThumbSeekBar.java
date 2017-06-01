package com.cheep.custom_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.cheep.R;

public class TextThumbSeekBar extends SeekBar {

    private int mThumbSize;
    private TextPaint mTextPaint;

    public TextThumbSeekBar(Context context) {
        this(context, null);
    }

    public TextThumbSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.seekBarStyle);
    }

    public TextThumbSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mThumbSize = getResources().getDimensionPixelSize(R.dimen.thumb_size);

        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.thumb_text_size));
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        getProgressDrawable().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.splash_gradient_end), PorterDuff.Mode.MULTIPLY));
    }

    private String prefix, suffix;
    private int min = 0;

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setMin(int min) {
        this.min = min;
    }


    public int getDisplayProgress() {
        return getProgress() + min;
    }

    public void setDisplayProgress(int progress) {
        int tempProgress = progress - min;
        super.setProgress(tempProgress < 0 ? progress : tempProgress);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String progressText = (prefix != null ? prefix : "") + String.valueOf(getDisplayProgress()) + (suffix != null ? suffix : "");
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(progressText, 0, progressText.length(), bounds);

        int leftPadding = getPaddingLeft() - getThumbOffset();
        int rightPadding = getPaddingRight() - getThumbOffset();
        int width = getWidth() - leftPadding - rightPadding;
        float progressRatio = (float) getProgress() / getMax();
        float thumbOffset = mThumbSize * (.5f - progressRatio);
        float thumbX = progressRatio * width + leftPadding + thumbOffset;
//        float thumbOffset = mThumbSize * (.5f - progressRatio);
//        float thumbX = progressRatio * width + leftPadding + thumbOffset;

//        int val = (getProgress() * (getWidth() - 2 * getThumbOffset())) / getMax();
//        float thumbX=getX() + val + getThumbOffset() / 2;

//        float thumbX = getProgress() + getThumbOffset() + getThumb().getIntrinsicWidth() / 2;
        canvas.drawText(progressText, thumbX, 25, mTextPaint);
    }
}