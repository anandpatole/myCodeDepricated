package com.cheep.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.cheep.activity.HomeActivity;
import com.cheep.model.BannerImageModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.strategicpartner.StrategicPartnerTaskCreationAct;
import com.cheep.utils.PreferenceUtility;

public class BannerImageFragment extends BaseFragment {
    private static final String TAG = BannerImageFragment.class.getSimpleName();
    private ImageView img_cover;
    private BannerImageModel bannerImageModel;
    private ProgressBar progress;

    public BannerImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void initiateUI() {

    }

    @Override
    public void setListener() {

    }

    @SuppressLint("ValidFragment")
    public BannerImageFragment(BannerImageModel bannerModel) {
        // Required empty public constructor
        this.bannerImageModel = bannerModel;
    }

    public static BannerImageFragment getInstance(BannerImageModel bannerModel) {
        return new BannerImageFragment(bannerModel);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout view = (FrameLayout) inflater.inflate(R.layout.fragment_banner_image, container, false);
        img_cover = (ImageView) view.findViewById(R.id.img_cover);
        progress = (ProgressBar) view.findViewById(R.id.progress);
        return view;
    }

    boolean processingClick = false;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (bannerImageModel != null) {
            Glide.with(mContext)
                    .load(bannerImageModel.bannerImage)
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

            img_cover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!processingClick) {
                        processingClick = true;

                        if (bannerImageModel != null && bannerImageModel.bannerType.equalsIgnoreCase(NetworkUtility.TAGS.BANNER_TYPE.STRATEGIC)) {
                            StrategicPartnerTaskCreationAct.getInstance(mContext, bannerImageModel);
                        } else if (bannerImageModel != null && bannerImageModel.bannerType.equalsIgnoreCase(NetworkUtility.TAGS.BANNER_TYPE.REFERRAL)) {

                            UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                            if (userDetails != null)
                                ((HomeActivity) mContext).loadFragment(ReferAndEarnFragment.TAG, ReferAndEarnFragment.newInstance());
                        } else {
                            // this is normal banner
                            // do nothing
                        }
                    }
                }
            });

        } else {
            progress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        processingClick = false;
    }

    @Override
    public void onDestroy() {
//        Log.d(TAG, "onDestroy() called");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
//        Log.d(TAG, "onDetach() called");
        super.onDetach();
    }
}