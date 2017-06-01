package com.cheep.custom_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Bhavesh Patadiya on 25/5/15.
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDrawable;
    private int extraPaddingLeftRightInPixel;

    public DividerItemDecoration(Context context, int drawableID) {
        mDrawable = ContextCompat.getDrawable(context, drawableID);
        extraPaddingLeftRightInPixel = 0;
    }

    public DividerItemDecoration(Context context, int drawableID, int extraPaddingLeftRightInPixel) {
        mDrawable = ContextCompat.getDrawable(context, drawableID);
        this.extraPaddingLeftRightInPixel = extraPaddingLeftRightInPixel;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left;
        int right;
        if (extraPaddingLeftRightInPixel > 0) {
            left = parent.getPaddingLeft() + extraPaddingLeftRightInPixel;
            right = parent.getWidth() - (parent.getPaddingRight() + extraPaddingLeftRightInPixel);
        } else {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
        }

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDrawable.getIntrinsicHeight();

            mDrawable.setBounds(left, top, right, bottom);
            mDrawable.draw(c);
        }
    }
}
