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
import com.cheep.cheepcare.activity.ManageSubscriptionActivity;
import com.cheep.cheepcare.model.CheepCareBannerModel;
import com.cheep.databinding.FragmentSubscriptionBannerImageBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.Utility;

import java.util.ArrayList;

public class SubscriptionBannerFragment extends BaseFragment {

    private static final String TAG = "SubscriptionBannerFragm";
    private CheepCareBannerModel bannerImageModel;
    private FragmentSubscriptionBannerImageBinding binding;


    public ArrayList<CheepCareBannerModel> cheepCareBannerModels;

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
    public SubscriptionBannerFragment(CheepCareBannerModel bannerModel) {
        // Required empty public constructor
        this.bannerImageModel = bannerModel;
    }

    public static SubscriptionBannerFragment getInstance(CheepCareBannerModel bannerModel) {
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


            /*LogUtils.LOGE(TAG, "init: " + bannerImageModel.cityName);*/
            binding.tvTitle.setText(bannerImageModel.title);
            binding.tvSubTitle.setText(bannerImageModel.subtitle);
            binding.tvCityName.setText(bannerImageModel.cityName);
            // set banner image

            int resId = R.drawable.banner_mumbai;
            switch (bannerImageModel.citySlug) {
                case NetworkUtility.CARE_CITY_SLUG.MUMBAI:
                    resId = R.drawable.banner_mumbai;
                    break;
                case NetworkUtility.CARE_CITY_SLUG.HYDRABAD:
                    resId = R.drawable.banner_hyderabad;
                    break;
                case NetworkUtility.CARE_CITY_SLUG.BENGALURU:
                    resId = R.drawable.banner_bengaluru;
                    break;
                case NetworkUtility.CARE_CITY_SLUG.DELHI:
                    resId = R.drawable.banner_delhi;
                    break;
                case NetworkUtility.CARE_CITY_SLUG.CHENNAI:
                    resId = R.drawable.banner_chennai;
                    break;
            }

            Glide.with(mContext)
                    .load(resId)
                    .into(binding.imgCover);


            //Click event of banner
            binding.imgCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!processingClick) {
                        processingClick = true;
                        if (bannerImageModel.isSubscribed.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                            ManageSubscriptionActivity.newInstance(mContext, bannerImageModel);
                        } else {
                            LandingScreenPickPackageActivity.newInstance(mContext, bannerImageModel);
                        }
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