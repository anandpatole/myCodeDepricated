package com.cheep.custom_view;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cheep.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anurag on 06-06-2017.
 */

public class GridImageView extends MaskableFrameLayout {
    private static final int MAX_IMAGE_COUNT = 3;

    private ViewGroup mRootView;

    public GridImageView(Context context) {
        super(context);
        init(null);
    }

    public GridImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public GridImageView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
    }

    public void createWithUrls(List<Uri> urls) {
        LayoutInflater mLayoutInflater = LayoutInflater.from(getContext());

        int length = urls != null ? urls.size() : 0;
        if (length <= 1) {
            mRootView = (ViewGroup) mLayoutInflater.inflate(R.layout.layout_grid_image_single, this, true);
        } else if (length == 2) {
            mRootView = (ViewGroup) mLayoutInflater.inflate(R.layout.layout_grid_image_double, this, true);
        } else {
            mRootView = (ViewGroup) mLayoutInflater.inflate(R.layout.layout_grid_image_n, this, true);
        }

        populateView(urls, length);
    }

    private void populateView(List<Uri> urls, int count) {
        if (mRootView != null) {
            TextView tvCounter = (TextView) mRootView.findViewById(R.id.tv_badge_count);
            tvCounter.setBackgroundResource(R.color.splash_gradient_end);
            tvCounter.setText(count > MAX_IMAGE_COUNT ? TextUtils.concat("+", String.valueOf(count - MAX_IMAGE_COUNT)) : String.valueOf(count));
            if (count > 1) {
                int padding = getContext().getResources().getDimensionPixelSize(R.dimen.scale_2dp);
                tvCounter.setGravity(Gravity.TOP | Gravity.LEFT);
                tvCounter.setPadding(padding, padding, 0, 0);
            }else{
                tvCounter.setGravity(Gravity.CENTER);
                tvCounter.setPadding(0, 0, 0, 0);
            }


            List<ImageView> imageViews = new ArrayList<>();
            getImageViews(mRootView, imageViews);

            int imageViewCount = imageViews.size();
            for (int i = 0; i < count; i++) {
                if (i >= MAX_IMAGE_COUNT || i >= imageViewCount) break;
                Glide.with(getContext()).load(urls.get(i)).placeholder(R.drawable.icon_profile_img_solid).centerCrop().into(imageViews.get(i));
            }
        }
    }

    private void getImageViews(View view, List<ImageView> imageViews) {
        if (view != null && view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            int childCount = vg.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View subView = vg.getChildAt(i);
                if (subView instanceof ImageView) {
                    imageViews.add((ImageView) subView);
                } else {
                    getImageViews(subView, imageViews);
                }
            }
        }
    }
}
