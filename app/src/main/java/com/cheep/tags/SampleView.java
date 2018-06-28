package com.cheep.tags;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by majid on 26-06-2018.
 */

public class SampleView extends View {

    // CONSTRUCTOR
    public SampleView(Context context) {
        super(context);
        setFocusable(true);

    }

    @Override
    public void onDraw(Canvas canvas) {

        canvas.drawColor(Color.YELLOW);
        Paint p = new Paint();
        // smooths
        p.setAntiAlias(true);
        p.setColor(Color.RED);
        p.setStrokeWidth(4.5f);
        // opacity
        p.setAlpha(0x80); //
        // drawLine (float startX, float startY, float stopX, float stopY,
        // Paint paint)
        canvas.drawLine(0, 0, 40, 40, p);
        canvas.drawLine(40, 0, 0, 40, p);

    }

}