package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.cheep.R;
import com.cheep.utils.SharedElementTransitionHelper;
import com.cheep.utils.Utility;

/**
 * Created by pankaj on 12/6/16.
 */

public class ZoomImageActivity extends BaseAppCompatActivity {

    public static void newInstance(Context context, Bundle bnd, String imageUrl) {
        Intent intent = new Intent(context, ZoomImageActivity.class);
        intent.putExtra(Utility.Extra.IMAGE_URL, imageUrl);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            context.startActivity(intent, bnd);
        } else {
            context.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets intermediate background of activity when activity starts
        SharedElementTransitionHelper.enableTransition(this);
        setContentView(R.layout.activity_zoom_image);
        initiateUI();
    }

    @Override
    protected void initiateUI() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //Setting toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(ZoomImageActivity.this);
                    onBackPressed();
                }
            });
        }

        String imageUrl = getIntent().getStringExtra(Utility.Extra.IMAGE_URL);
        final ImageView imageView = (ImageView) findViewById(R.id.image_view);
        Utility.loadImageView(ZoomImageActivity.this, imageView, imageUrl, R.drawable.ic_cheep_pro_logo_square_small);

        /*Glide.with(mContext).load(imageUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                imageView.setImageBitmap(resource);
            }
        });*/
    }

    @Override
    protected void setListeners() {

    }

}
