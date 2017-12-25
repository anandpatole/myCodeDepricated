package com.cheep.cheepcare.fragment;

import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.cheepcare.activity.LandingScreenPickPackageActivity;
import com.cheep.cheepcare.model.SubscriptionBannerModel;
import com.cheep.databinding.FragmentSubscriptionBannerImageBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.utils.LogUtils;

import java.util.ArrayList;

public class SubscriptionBannerFragment extends BaseFragment {

    private static final String TAG = "SubscriptionBannerFragm";
    private SubscriptionBannerModel bannerImageModel;
    private FragmentSubscriptionBannerImageBinding binding;


    public static ArrayList<SubscriptionBannerModel> subscriptionBannerModels;

    public static ArrayList<SubscriptionBannerModel> getSubscriptionBannerModels() {

        subscriptionBannerModels = new ArrayList<>();
        SubscriptionBannerModel model = new SubscriptionBannerModel();
        model.title = "Discover a new level of carefree!";
        model.subTitle = "Tap here for zippy, doorstep services ";
        model.cityName = "Mumbai";
        model.tempImgRes = R.drawable.banner_mumbai;
        subscriptionBannerModels.add(model);

        model = new SubscriptionBannerModel();
        model.title = "Discover a new level of carefree!";
        model.subTitle = "Tap here for zippy, doorstep services ";
        model.cityName = "Bengaluru";
        model.tempImgRes = R.drawable.banner_bengaluru;
        subscriptionBannerModels.add(model);

        model = new SubscriptionBannerModel();
        model.title = "Discover a new level of carefree!";
        model.subTitle = "Tap here for zippy, doorstep services ";
        model.cityName = "Hyderabad";
        model.tempImgRes = R.drawable.banner_hyderabad;
        subscriptionBannerModels.add(model);


        model = new SubscriptionBannerModel();
        model.title = "Discover a new level of carefree!";
        model.subTitle = "Tap here for zippy, doorstep services ";
        model.cityName = "Delhi";
        model.tempImgRes = R.drawable.banner_delhi;
        subscriptionBannerModels.add(model);


        model = new SubscriptionBannerModel();
        model.title = "Discover a new level of carefree!";
        model.subTitle = "Tap here for zippy, doorstep services ";
        model.cityName = "Chennai";
        model.tempImgRes = R.drawable.banner_chennai;
        subscriptionBannerModels.add(model);


        return subscriptionBannerModels;
    }

    public SubscriptionBannerFragment() {
        // Required empty public constructor
    }

    @Override
    public void initiateUI() {

    }

    @Override
    public void setListener() {

    }

    @SuppressLint("ValidFragment")
    public SubscriptionBannerFragment(SubscriptionBannerModel bannerModel) {
        // Required empty public constructor
        this.bannerImageModel = bannerModel;
    }

    public static SubscriptionBannerFragment getInstance(SubscriptionBannerModel bannerModel) {
        return new SubscriptionBannerFragment(bannerModel);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_subscription_banner_image, container, false);
        init();
        return binding.getRoot();
    }

    boolean processingClick = false;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void init() {

        if (bannerImageModel != null) {
            Glide.with(mContext)
                    .load(R.drawable.gif_cheep_care_unit)
                    .asGif()
                    .dontAnimate()
                    .dontTransform()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(binding.imgCheepCareGif);


            LogUtils.LOGE(TAG, "init: " + bannerImageModel.cityName);
            binding.tvTitle.setText(bannerImageModel.title);
            binding.tvSubTitle.setText(bannerImageModel.subTitle);
            binding.tvCityName.setText(bannerImageModel.cityName);
            // set banner image
            Glide.with(mContext)
                    .load(bannerImageModel.tempImgRes)
                    .into(binding.imgCover);

            //Click event of banner
            binding.imgCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!processingClick) {
                        processingClick = true;

                        LandingScreenPickPackageActivity.newInstance(mContext, bannerImageModel.cityName);
                    }
                }
            });

        } else {
            binding.progress.setVisibility(View.GONE);
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