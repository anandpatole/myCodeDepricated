package com.cheep.cheepcare.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.adapter.CheepCareFeatureAdapter;
import com.cheep.cheepcare.adapter.CheepCarePackageAdapter;
import com.cheep.cheepcare.model.CityDetail;
import com.cheep.cheepcare.model.CityLandingPageModel;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.databinding.ActivityLandingScreenPickPackageBinding;
import com.cheep.model.MessageEvent;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.cheep.network.NetworkUtility.TAGS.CARE_CITY_SLUG;
import static com.cheep.network.NetworkUtility.TAGS.DATA;

/**
 * Created by pankaj on 12/21/17.
 */

public class LandingScreenPickPackageActivity extends BaseAppCompatActivity {

    private ActivityLandingScreenPickPackageBinding mBinding;
    private CheepCareFeatureAdapter mFeatureAdapter;
    private CheepCarePackageAdapter mPackageAdapter;
    private CityLandingPageModel mCityLandingPageModel;
    private CityDetail mCity;
    private String mPackageListString = Utility.EMPTY_STRING;
    private ArrayList<CityDetail> bannerCityDetailsList;
    private static final String TAG = "LandingScreenPickPackag";

    public static void newInstance(Context context, CityDetail city, String cheepcareBannerListString) {
        Intent intent = new Intent(context, LandingScreenPickPackageActivity.class);
        intent.putExtra(Utility.Extra.CITY_DETAIL, Utility.getJsonStringFromObject(city));
        intent.putExtra(Utility.Extra.DATA, cheepcareBannerListString);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding
                = DataBindingUtil.setContentView(this, R.layout.activity_landing_screen_pick_package);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {

        EventBus.getDefault().register(this);
        if (getIntent().hasExtra(Utility.Extra.CITY_DETAIL)) {
            mCity = (CityDetail) Utility.getObjectFromJsonString(getIntent().getExtras().getString(Utility.Extra.CITY_DETAIL), CityDetail.class);
            bannerCityDetailsList = Utility.getObjectListFromJsonString(getIntent().getExtras().getString(Utility.Extra.DATA), CityDetail[].class);
        }

        setCityBannerData();
    }

    private void setCityBannerData() {
        ViewTreeObserver mViewTreeObserver = mBinding.ivCityImage.getViewTreeObserver();
        mViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.ivCityImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = mBinding.ivCityImage.getMeasuredWidth();
                ViewGroup.LayoutParams params = mBinding.ivCityImage.getLayoutParams();
                params.height = Utility.getHeightFromWidthForOneHalfIsToOneRatio(width);
                mBinding.ivCityImage.setLayoutParams(params);

                int resId = R.drawable.img_landing_screen_mumbai;
                switch (mCity.citySlug) {
                    case NetworkUtility.CARE_CITY_SLUG.MUMBAI:
                        resId = R.drawable.img_landing_screen_mumbai;
                        break;
                    case NetworkUtility.CARE_CITY_SLUG.HYDRABAD:
                        resId = R.drawable.img_landing_screen_hydrabad;
                        break;
                    case NetworkUtility.CARE_CITY_SLUG.BENGALURU:
                        resId = R.drawable.img_landing_screen_bengaluru;
                        break;
                    case NetworkUtility.CARE_CITY_SLUG.DELHI:
                        resId = R.drawable.img_landing_screen_delhi;
                        break;
                    case NetworkUtility.CARE_CITY_SLUG.CHENNAI:
                        resId = R.drawable.img_landing_screen_chennai;
                        break;
                }

                // Load the image now.
                Utility.loadImageView(mContext, mBinding.ivCityImage
                        , resId
                        , R.drawable.hotline_ic_image_loading_placeholder);
            }
        });

        Glide.with(mContext)
                .load(R.drawable.ic_home_with_heart_text)
                .asGif()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mBinding.ivCheepCareGif);
        /*// Start cheep care animations
        mBinding.ivCheepCareGif.setBackgroundResource(R.drawable.cheep_care_animation);
        ((AnimationDrawable) mBinding.ivCheepCareGif.getBackground()).start();*/

        mBinding.tvCityName.setText(mCity.cityName);

        callGetCityLandingCareDetailWS();
    }

    private void setData() {
        if (mCityLandingPageModel.cityDetail.id.isEmpty())
            return;

        mBinding.nestedScrollView.scrollTo(0, 0);

        SpannableStringBuilder spannableStringBuilder
                = new SpannableStringBuilder(mCityLandingPageModel.cityDetail.greetingMessage);


        int resId = R.drawable.emoji_mic;
        switch (mCity.citySlug) {
            case NetworkUtility.CARE_CITY_SLUG.MUMBAI:
                resId = R.drawable.emoji_mic;
                break;
            case NetworkUtility.CARE_CITY_SLUG.HYDRABAD:
                resId = R.drawable.emoji_mustache;
                break;
            case NetworkUtility.CARE_CITY_SLUG.BENGALURU:
                resId = R.drawable.emoji_folded_hands;
                break;
            case NetworkUtility.CARE_CITY_SLUG.DELHI:
                resId = R.drawable.emoji_heart;
                break;
            case NetworkUtility.CARE_CITY_SLUG.CHENNAI:
                resId = R.drawable.emoji_mobile_phone;
                break;
        }

        spannableStringBuilder.append(Utility.ONE_CHARACTER_SPACE).append(Utility.ONE_CHARACTER_SPACE);
        ImageSpan span = new ImageSpan(mContext, resId, ImageSpan.ALIGN_BASELINE);
        spannableStringBuilder.setSpan(span, spannableStringBuilder.length() - 1
                , spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mBinding.tvGoodMorningText.setText(spannableStringBuilder);

        mBinding.tvInfoText.setText(getString(R.string.landing_page_info_text));

        mBinding.recyclerViewCheepCareFeature.setNestedScrollingEnabled(false);
        mFeatureAdapter = new CheepCareFeatureAdapter();

        // cheep care feature list
        mFeatureAdapter.addFeatureList(mCityLandingPageModel.cityDetail.cityTutorials);

        mBinding.recyclerViewCheepCareFeature.setLayoutManager(new LinearLayoutManager(
                mContext
                , LinearLayoutManager.VERTICAL
                , false
        ));
        mBinding.recyclerViewCheepCareFeature.setAdapter(mFeatureAdapter);

        mBinding.recyclerViewCheepCarePackages.setNestedScrollingEnabled(false);

        mPackageAdapter = new CheepCarePackageAdapter(mPackageItemClickListener);
        mPackageAdapter.addPackageList(mCityLandingPageModel.packageDetailList);

        mBinding.recyclerViewCheepCarePackages.setLayoutManager(new LinearLayoutManager(
                mContext
                , LinearLayoutManager.VERTICAL
                , false
        ));
        mBinding.recyclerViewCheepCarePackages.setAdapter(mPackageAdapter);

        // Setting up Toolbar
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    onBackPressed();
                }
            });
        }
    }

    @Override
    protected void setListeners() {
        mBinding.tvCityName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCitySelectionDialog();
            }
        });
    }

    private void openCitySelectionDialog() {

        String[] cityArray = new String[bannerCityDetailsList.size()];
        for (int i = 0; i < bannerCityDetailsList.size(); i++) {
            CityDetail cityDetail = bannerCityDetailsList.get(i);
            cityArray[i] = cityDetail.cityName;
        }
        Log.d(TAG, "showPictureChooserDialog() called");
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.select_city)
                .setItems(cityArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        LogUtils.LOGE(TAG, "onClick alert dialog : " + bannerCityDetailsList.get(which).cityName);
                        if (!mCity.cityName.equalsIgnoreCase(bannerCityDetailsList.get(which).cityName)) {
                            if (bannerCityDetailsList.get(which).isSubscribed.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                                String cheepcareBannerListString = Utility.getJsonStringFromObject(bannerCityDetailsList);
                                ManageSubscriptionActivity.newInstance(LandingScreenPickPackageActivity.this, bannerCityDetailsList.get(which), true, cheepcareBannerListString);
                                finish();
                            } else {
                                mCity = bannerCityDetailsList.get(which);
                                setCityBannerData();
                            }
                        }
                        dialog.dismiss();
                    }
                });
        builder.create();

        //Show the dialog
        builder.show();
    }

    private final CheepCarePackageAdapter.PackageItemClickListener mPackageItemClickListener
            = new CheepCarePackageAdapter.PackageItemClickListener() {
        @Override
        public void onPackageItemClick(int position, PackageDetail packageModel) {
//            String packageList = Utility.getJsonStringFromObject(mCityLandingPageModel.packageDetailList);
            String packageList = Utility.getJsonStringFromObject(mCityLandingPageModel.packageDetailList);
            PackageCustomizationActivity.newInstance(mContext, packageModel, mCityLandingPageModel.cityDetail, packageModel.id, packageList, mCityLandingPageModel.adminSetting);
        }
    };


    private void callGetCityLandingCareDetailWS() {
        LogUtils.LOGD(TAG, "callGetCityLandingCareDetailWS() called with: catId = [" + mCity + "]");
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(CARE_CITY_SLUG, mCity.citySlug);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.GET_CITY_CARE_DETAIL
                , mCallGetCityCareDetailsWSErrorListener
                , mCallGetCityCareDetailsWSResponseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.GET_CITY_CARE_DETAIL);
    }


    private Response.Listener mCallGetCityCareDetailsWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            hideProgressDialog();
            LogUtils.LOGD(TAG, "onResponse() called with: response = [" + response + "]");
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                LogUtils.LOGI(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        mCityLandingPageModel = (CityLandingPageModel) Utility.getObjectFromJsonString(jsonObject.optString(DATA), CityLandingPageModel.class);
                        mPackageListString = Utility.getJsonStringFromObject(mCityLandingPageModel.packageDetailList);
                        getSavedData();
                        setData();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                mCallGetCityCareDetailsWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };
    private Response.ErrorListener mCallGetCityCareDetailsWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            hideProgressDialog();
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    /**
     * Event Bus Callbacks
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.PACKAGE_SUBSCRIBED_SUCCESSFULLY:
                finish();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.GET_CITY_CARE_DETAIL);
        super.onDestroy();
    }

    private void getSavedData() {
        ArrayList<PackageDetail> savedPackageList = new ArrayList<>();
        String cartDetail = PreferenceUtility.getInstance(this).getCityCartDetail(mCityLandingPageModel.cityDetail.citySlug);
        if (!TextUtils.isEmpty(cartDetail)) {
            ArrayList<PackageDetail> list = Utility.getObjectListFromJsonString(cartDetail, PackageDetail[].class);
            savedPackageList.clear();
            savedPackageList.addAll(list);
            String webData = Utility.getJsonStringFromObject(mCityLandingPageModel.packageDetailList);
            LogUtils.LOGE(TAG, "Saved data--- " + cartDetail);
            LogUtils.LOGE(TAG, "---------------------------------------------------------------------");
            LogUtils.LOGE(TAG, "Saved web data--- " + webData);

            if (!savedPackageList.isEmpty()) {
                for (int i = 0; i < mCityLandingPageModel.packageDetailList.size(); i++) {
                    PackageDetail webPakegDetail = mCityLandingPageModel.packageDetailList.get(i);
                    for (PackageDetail detail : savedPackageList) {
                        if (detail.packageOptionList != null && !detail.packageOptionList.isEmpty() && detail.packageSlug.equalsIgnoreCase(webPakegDetail.packageSlug) && detail.isSelected) {
                            webPakegDetail.packageOptionList = detail.packageOptionList;
                            webPakegDetail.mSelectedAddressList = detail.mSelectedAddressList;
                            webPakegDetail.isSelected = true;
                        }
                    }
                }
                LogUtils.LOGE(TAG, "replacedData: " + Utility.getJsonStringFromObject(mCityLandingPageModel.packageDetailList));
            }
        } else {
            if (!TextUtils.isEmpty(mPackageListString)) {
                ArrayList<PackageDetail> list = Utility.getObjectListFromJsonString(mPackageListString, PackageDetail[].class);
                mCityLandingPageModel.packageDetailList.clear();
                mCityLandingPageModel.packageDetailList.addAll(list);
            }
            LogUtils.LOGE(TAG, "getSavedData: no cart data found");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mCityLandingPageModel != null) {
            LogUtils.LOGE(TAG, "onResume:cart detail found");
            getSavedData();
        } else {
            LogUtils.LOGE(TAG, "onResume: no city data found");

        }
    }
}
