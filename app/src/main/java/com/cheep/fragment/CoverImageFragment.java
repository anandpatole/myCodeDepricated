package com.cheep.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cheep.R;
import com.cheep.model.CoverImageModel;

public class CoverImageFragment extends BaseFragment {
    private static final String TAG = "CoverImageFragment";
    private ImageView img_cover;
    private CoverImageModel coverImageModel;
    private ProgressBar progress;

    public CoverImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void initiateUI() {

    }

    @Override
    public void setListener() {

    }

    @SuppressLint("ValidFragment")
    public CoverImageFragment(CoverImageModel bannerModel) {
        // Required empty public constructor
        this.coverImageModel = bannerModel;
    }

    public static CoverImageFragment getInstance(CoverImageModel bannerModel) {
        CoverImageFragment coverImageFragment = new CoverImageFragment(bannerModel);
        return coverImageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called with: inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");
        // Inflate the layout for this fragment
        FrameLayout view = (FrameLayout) inflater.inflate(R.layout.fragment_cover_image, container, false);
        img_cover = (ImageView) view.findViewById(R.id.img_cover);
        progress = (ProgressBar) view.findViewById(R.id.progress);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called: " + coverImageModel);
        if (coverImageModel != null) {
            Glide.with(mContext)
                    .load(coverImageModel.imgUrl)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            progress.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(img_cover);
        } else {
            progress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach() called");
        super.onDetach();
    }
}