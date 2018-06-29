package com.cheep.cheepcare.fragment;

import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.cheepcare.model.CareCityDetail;
import com.cheep.cheepcarenew.activities.LandingScreenPickPackageActivity;
import com.cheep.databinding.FragmentSubscriptionBannerImageBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.fragment.HomeFragment;
import com.cheep.fragment.HomeTabFragment;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.Scanner;

public class SubscriptionBannerFragment extends BaseFragment {

    private static final String TAG = "SubscriptionBannerFragm";
    private CareCityDetail bannerImageModel;
    private FragmentSubscriptionBannerImageBinding binding;
    SpannableString oldPrice = null, newPrice = null;
    boolean isPriceIsZero;
    private String firstVariable,secondVariable;
    private int stringWordCountAfterDivision;


    public ArrayList<CareCityDetail> cheepCareBannerModels;

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
    public SubscriptionBannerFragment(CareCityDetail bannerModel) {
        // Required empty public constructor
        this.bannerImageModel = bannerModel;
    }

    public static SubscriptionBannerFragment getInstance(CareCityDetail bannerModel) {
        return new SubscriptionBannerFragment(bannerModel);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
            String subtitle = addingSubtitleWithOldAndNewPrice(bannerImageModel.subtitle);
            if(isPriceIsZero){
                SpannableStringBuilder ss= drawLineOnText(subtitle);
                binding.tvSubTitle.setText(ss);
            }else {
                binding.tvSubTitle.setText(subtitle);
            }

            //binding.tvSubTitle.setText(subtitle);
            binding.tvCityName.setText(bannerImageModel.cityName);
            // set banner image


            int wordCount = getStringWordCount(bannerImageModel.subtitle);
            getStringForTwoPart(wordCount,bannerImageModel.subtitle);


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


            if (bannerImageModel.isSubscribed.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                binding.tvSubscribe.setText(R.string.label_book_now);
            } else {
                binding.tvSubscribe.setText(R.string.label_subscribe);
            }

            //Click event of banner
            binding.imgCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!processingClick) {
                        processingClick = true;
                        HomeFragment homeFragment = (HomeFragment) getActivity().getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
                        if (homeFragment == null)
                            return;
                        HomeTabFragment fragmentByTag = (HomeTabFragment) homeFragment.getChildFragmentManager().findFragmentByTag(HomeTabFragment.TAG);
                        if (fragmentByTag == null)
                            return;
                        String cheepcareBannerListString = GsonUtility.getJsonStringFromObject(fragmentByTag.careBannerModelArrayList);
                        LandingScreenPickPackageActivity.newInstance(mContext, bannerImageModel, cheepcareBannerListString);


                    }
                }
            });
        } else {
            binding.progress.setVisibility(View.GONE);
        }

    }


    private String addingSubtitleWithOldAndNewPrice(String subTitle) {
        if (!bannerImageModel.oldPrice.equals(Utility.ZERO_VALUE.THREE_ZERO)) {
            isPriceIsZero = true;
            oldPrice = Utility.getCheepCarePackageMonthlyPrice(binding.tvSubTitle.getContext()
                    , R.string.rupee_symbol_x_package_price, bannerImageModel.oldPrice);
        } else {
            isPriceIsZero = false;
            oldPrice = SpannableString.valueOf("");
        }
        newPrice = Utility.getCheepCarePackageMonthlyPrice(binding.tvSubTitle.getContext()
                , R.string.rupee_symbol_x_package_price, bannerImageModel.newPrice);

        return subTitle + " " + oldPrice + " " + newPrice;

    }

    private SpannableStringBuilder drawLineOnText(String subTitle) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(subTitle);
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
        spannableStringBuilder.setSpan(strikethroughSpan,49,57, Paint.STRIKE_THRU_TEXT_FLAG);
        return spannableStringBuilder;

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

    private int getStringWordCount(String data){
        int count = 1;

        for (int i = 0; i < data.length() - 1; i++)
        {
            if ((data.charAt(i) == ' ') && (data.charAt(i + 1) != ' '))
            {
                count++;

            }
        }
        return  count;
    }

    private void getStringForTwoPart(int totalStringWordCount,String data){

        stringWordCountAfterDivision = totalStringWordCount/3;

        LogUtils.LOGE(TAG, "FINAL STRING WORD COUNT: " + stringWordCountAfterDivision);

        Scanner scanner = new Scanner(data);
        String sentence = scanner.nextLine();
        String[] words = sentence.split(" ");
        for (String word : words) {
            LogUtils.LOGE(TAG, "SUBTITLE: " + word);
        }
        scanner.close();
    }
}