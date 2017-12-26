package com.cheep.utils;

import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class CustomCharacterSpan extends MetricAffectingSpan {
    double ratio =0.8;

    public CustomCharacterSpan() {
    }

    public CustomCharacterSpan(double ratio) {
        this.ratio = ratio;
    }

    @Override
    public void updateDrawState(TextPaint paint) {
        paint.baselineShift += (int) (paint.ascent() * ratio);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        paint.baselineShift += (int) (paint.ascent() * ratio);
    }
}