package com.cheep.custom_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint.Style;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.cheep.R;
import com.cheep.utils.Utility;

/**
 * Created by kruti on 18/1/18.
 */

public class DashedBlueUnderlineCFTextViewRegular extends CFTextViewRegular {

    private final Rect mRect;
    private final Paint mPaint;

    {
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.splash_gradient_end));
        mPaint.setStyle(Style.STROKE);
        mPaint.setPathEffect(new DashPathEffect(new float[]{Utility.convertDpToPixel(2f, getContext())
                , Utility.convertDpToPixel(1f, getContext())}, 0));
        mPaint.setStrokeWidth(Utility.convertDpToPixel(1f, getContext()));
    }

    public DashedBlueUnderlineCFTextViewRegular(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DashedBlueUnderlineCFTextViewRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DashedBlueUnderlineCFTextViewRegular(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Rect r = mRect;
        Paint paint = mPaint;

        int lineCount = getLineCount();
        int size = getLayout().getLineStart(lineCount - 1);

        String str = getText().toString().substring(size);

        float densityMultiplier = getContext().getResources().getDisplayMetrics().density;
        float scaledPx = 20 * densityMultiplier;
        paint.setTextSize(scaledPx);
        float i = paint.measureText(str);

        for (int k = 0; k < lineCount - 1; k++) {
            int baseline = getLineBounds(k, r);
            canvas.drawLine(r.left, baseline + 2, r.right, baseline + 2, paint);
        }

        int baseline = getLineBounds(lineCount - 1, r);
        canvas.drawLine(r.left, baseline + 2, i, baseline + 2, paint);

        super.onDraw(canvas);
    }
}