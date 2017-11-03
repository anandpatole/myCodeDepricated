package com.cheep.custom_view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.cheep.R;

/**
 * Created by Anurag on 06-06-2017.
 */
public class MaskableFrameLayout extends FrameLayout {

    //Constants
    private static final String TAG = MaskableFrameLayout.class.getSimpleName();

    private Handler mHandler;

    //Mask props
    @Nullable
    private Drawable mDrawableMask = null;
    @Nullable
    private Bitmap mFinalMask = null;

    //Drawing props
    private Paint mPaint = null;
    private PorterDuffXfermode mPorterDuffXferMode = null;

    public MaskableFrameLayout(Context context) {
        super(context);
        construct(context);
    }

    public MaskableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct(context);
    }

    public MaskableFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        construct(context);
    }

    private void construct(Context context) {
        mHandler = new Handler();
        setDrawingCacheEnabled(true);
        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(LAYER_TYPE_SOFTWARE, null); //Only works for software layers
        }
        mPaint = createPaint(false);
        mPorterDuffXferMode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);;

        initMask(ContextCompat.getDrawable(context, R.drawable.circle_masking));

        registerMeasure();
    }

    private Paint createPaint(boolean antiAliasing) {
        Paint output = new Paint(Paint.ANTI_ALIAS_FLAG);
        output.setAntiAlias(antiAliasing);
        output.setXfermode(mPorterDuffXferMode);
        return output;
    }

    private void initMask(@Nullable Drawable input) {
        if (input != null) {
            mDrawableMask = input;
            if (mDrawableMask instanceof AnimationDrawable) {
                mDrawableMask.setCallback(this);
            }
        } else {
            log("Are you sure you don't want to provide a mask ?");
        }
    }

    @Nullable
    private Bitmap makeBitmapMask(@Nullable Drawable drawable) {
        if (drawable != null) {
            if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
                Bitmap mask = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(),
                        Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(mask);
                drawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                drawable.draw(canvas);
                return mask;
            } else {
                log("Can't create a mask with height 0 or width 0. Or the layout has no children and is wrap content");
                return null;
            }
        } else {
            log("No bitmap mask loaded, view will NOT be masked !");
        }
        return null;
    }

    //Once the size has changed we need to remake the mask.
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setSize(w, h);
    }

    private void setSize(int width, int height) {
        if (width > 0 && height > 0) {
            if (mDrawableMask != null) {
                //Remake the 9patch
                swapBitmapMask(makeBitmapMask(mDrawableMask));
            }
        } else {
            log("Width and height must be higher than 0");
        }
    }

    //Drawing
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mFinalMask != null && mPaint != null) {
            mPaint.setXfermode(mPorterDuffXferMode);
            canvas.drawBitmap(mFinalMask, 0.0f, 0.0f, mPaint);
            mPaint.setXfermode(null);
        } else {
            log("Mask or paint is null ...");
        }
    }

    //Once inflated we have no height or width for the mask. Wait for the layout.
    private void registerMeasure() {
        final ViewTreeObserver treeObserver = MaskableFrameLayout.this.getViewTreeObserver();
        if (treeObserver != null && treeObserver.isAlive()) {
            treeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver aliveObserver = treeObserver;
                    if (!aliveObserver.isAlive()) {
                        aliveObserver = MaskableFrameLayout.this.getViewTreeObserver();
                    }
                    if (aliveObserver != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            aliveObserver.removeOnGlobalLayoutListener(this);
                        } else {
                            aliveObserver.removeGlobalOnLayoutListener(this);
                        }
                    } else {
                        log("GlobalLayoutListener not removed as ViewTreeObserver is not valid");
                    }
                    swapBitmapMask(makeBitmapMask(mDrawableMask));
                }
            });
        }
    }

    //Logging
    private void log(String message) {
        Log.d(TAG, message);
    }

    //Animation
    @Override
    public void invalidateDrawable(Drawable dr) {
        if (dr != null) {
            initMask(dr);
            swapBitmapMask(makeBitmapMask(dr));
            invalidate();
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (who != null && what != null) {
            mHandler.postAtTime(what, when);
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (who != null && what != null) {
            mHandler.removeCallbacks(what);
        }
    }

    private void swapBitmapMask(@Nullable Bitmap newMask) {
        if (newMask != null) {
            if (mFinalMask != null && !mFinalMask.isRecycled()) {
                mFinalMask.recycle();
            }
            mFinalMask = newMask;
        }
    }
}
