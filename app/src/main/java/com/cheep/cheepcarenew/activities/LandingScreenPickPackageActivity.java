package com.cheep.cheepcarenew.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
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
import com.cheep.cheepcarenew.adapters.CheepCareFeatureAdapter;
import com.cheep.cheepcarenew.adapters.CheepCarePackageAdapter;
import com.cheep.cheepcarenew.model.CareCityDetail;
import com.cheep.cheepcarenew.model.CityLandingPageModel;
import com.cheep.cheepcarenew.model.PackageDetail;
import com.cheep.cheepcarenew.dialogs.ComparisionChartFragmentDialog;
import com.cheep.cheepcarenew.dialogs.PackageDetailModelDialog;
import com.cheep.databinding.ActivityLandingScreenPickPackageBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.ComparisionChart.ComparisionChartModel;
import com.cheep.model.MessageEvent;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pankaj on 12/21/17.
 */

public class LandingScreenPickPackageActivity extends BaseAppCompatActivity {

    private ActivityLandingScreenPickPackageBinding mBinding;
    private CheepCareFeatureAdapter mFeatureAdapter;
    private CheepCarePackageAdapter mPackageAdapter;
    private CityLandingPageModel mCityLandingPageModel;
    private CareCityDetail mCity;
    private String mPackageListString = Utility.EMPTY_STRING;
    private ArrayList<CareCityDetail> bannerCareCityDetailsList;
    private static final String TAG = "LandingScreenPickPackag";
    private String comingFrom=Utility.EMPTY_STRING;
    // written by majid 106 to 108
    private ComparisionChartFragmentDialog comparisionChartFragmentDialog;
    private PackageDetailModelDialog packageDetailModelDialog;
    private PackageDetail packageDetailData;
    private ComparisionChartModel comparisionChartModel;

    private WebCallClass.CommonResponseListener commonErrorResponse = new WebCallClass.CommonResponseListener() {
        @Override
        public void volleyError(VolleyError error) {
            hideProgressDialog();
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }

        @Override
        public void showSpecificMessage(String message) {
            hideProgressDialog();
            Utility.showSnackBar(message, mBinding.getRoot());
        }

        @Override
        public void forceLogout() {
            hideProgressDialog();
            finish();
        }
    };

    public static void newInstance(Context context, CareCityDetail city, String cheepcareBannerListString) {
        Intent intent = new Intent(context, LandingScreenPickPackageActivity.class);
        intent.putExtra(Utility.Extra.CITY_DETAIL, GsonUtility.getJsonStringFromObject(city));
        intent.putExtra(Utility.Extra.DATA, cheepcareBannerListString);
        context.startActivity(intent);
    }
    public static void newInstance(Context context, CareCityDetail city, String cheepcareBannerListString,String comingFrom) {
        Intent intent = new Intent(context, LandingScreenPickPackageActivity.class);
        intent.putExtra(Utility.Extra.CITY_DETAIL, GsonUtility.getJsonStringFromObject(city));
        intent.putExtra(Utility.Extra.DATA, cheepcareBannerListString);
        intent.putExtra(Utility.Extra.COMING_FROM,comingFrom);
        context.startActivity(intent);
    }
    public static void newInstance(Context context) {
        Intent intent = new Intent(context, LandingScreenPickPackageActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_landing_screen_pick_package);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {

        EventBus.getDefault().register(this);
        if (getIntent().hasExtra(Utility.Extra.CITY_DETAIL)) {
            mCity = (CareCityDetail) GsonUtility.getObjectFromJsonString(getIntent().getExtras().getString(Utility.Extra.CITY_DETAIL), CareCityDetail.class);
            bannerCareCityDetailsList = GsonUtility.getObjectListFromJsonString(getIntent().getExtras().getString(Utility.Extra.DATA), CareCityDetail[].class);
            if(getIntent().hasExtra(Utility.Extra.COMING_FROM))
            {
                comingFrom=getIntent().getExtras().getString(Utility.Extra.COMING_FROM);
            }
        }

        setCityBannerData();
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



    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.GET_CITY_CARE_DETAIL);
        super.onDestroy();
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
                GlideUtility.loadImageView(mContext, mBinding.ivCityImage
                        , resId
                        , R.drawable.hotline_ic_image_loading_placeholder);
            }
        });

        Glide.with(mContext)
                .load(R.drawable.ic_home_with_heart_text)
                .into(mBinding.ivCheepCareGif);

        mBinding.tvCityName.setText(mCity.cityName);

        final SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getString(R.string.cheep_care_value));
        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
        sb.setSpan(bss, 0, 5, Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold
        mBinding.tvCheepCare.setText(sb);

        callGetCityLandingCareDetailWS();

    }

    private void callGetCityLandingCareDetailWS() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }
        showProgressDialog();
        WebCallClass.getCityCareDetail(mContext, mCity.citySlug, commonErrorResponse, new WebCallClass.GetCityCareDataListener() {
            @Override
            public void getCityCareData(CityLandingPageModel cityLandingPageModel) {
                hideProgressDialog();
                mCityLandingPageModel = cityLandingPageModel;
                mPackageListString = GsonUtility.getJsonStringFromObject(mCityLandingPageModel.packageDetailList);

                setData();
            }
        });
    }

    private void setData() {
        if (mCityLandingPageModel.careCityDetail.id.isEmpty())
            return;

        mBinding.nestedScrollView.scrollTo(0, 0);

        SpannableStringBuilder spannableStringBuilder
                = new SpannableStringBuilder(mCityLandingPageModel.careCityDetail.greetingMessage);


        int resId = R.drawable.emoji_mic;
        switch (mCity.citySlug) {
            case NetworkUtility.CARE_CITY_SLUG.MUMBAI:
                resId = R.drawable.emoji_hand;
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

        // writing by majid khan 217 to 222
        mBinding.tvLandingScreenTipTitle.setText(mCityLandingPageModel.packageTip.title);
        mBinding.tvLandingScreenTipSubtitle.setText(mCityLandingPageModel.packageTip.subtitle);

        SpannableStringBuilder spannableOne =null,spannableTwo=null,spannableThree =null;
        for(int i=0; i<3; i++){
            if(i == 0){
                spannableOne = new SpannableStringBuilder(mCityLandingPageModel.careCityDetail.landingScreenTitle1);
                spannableOne.append(Utility.ONE_CHARACTER_SPACE).append(Utility.ONE_CHARACTER_SPACE);
                ImageSpan spanThumb = new ImageSpan(mContext, R.drawable.emoji_heart, ImageSpan.ALIGN_BOTTOM);
                spannableOne.setSpan(spanThumb, spannableOne.length() - 1
                        , spannableOne.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }else if( i==1)
            {
                spannableTwo = new SpannableStringBuilder(spannableOne);
                spannableTwo.append(Utility.ONE_CHARACTER_SPACE).append(Utility.ONE_CHARACTER_SPACE);
                ImageSpan spanThumb = new ImageSpan(mContext, R.drawable.emoji_heart, ImageSpan.ALIGN_BOTTOM);
                spannableTwo.setSpan(spanThumb, spannableTwo.length() - 1
                        , spannableTwo.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            }
            else if( i==2)
            {
                spannableThree = new SpannableStringBuilder(spannableTwo);
                spannableThree.append(Utility.ONE_CHARACTER_SPACE).append(Utility.ONE_CHARACTER_SPACE);
                ImageSpan spanThumb = new ImageSpan(mContext, R.drawable.emoji_heart, ImageSpan.ALIGN_BOTTOM);
                spannableThree.setSpan(spanThumb, spannableThree.length() - 1
                        , spannableThree.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            }
        }


        mBinding.tvLandingScreenTitle1.setText(spannableThree);

        mBinding.tvLandingScreenTitle2.setText(mCityLandingPageModel.careCityDetail.landingScreenTitle2);
        mBinding.imgCheepTips.setVisibility(View.VISIBLE);

        mBinding.tvInfoText.setText(mCityLandingPageModel.careCityDetail.description);

        mBinding.recyclerViewCheepCareFeature.setNestedScrollingEnabled(false);
        mFeatureAdapter = new CheepCareFeatureAdapter();

        // cheep care feature list
        mFeatureAdapter.addFeatureList(mCityLandingPageModel.careCityDetail.cityTutorials);

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

    private void openCitySelectionDialog() {

        String[] cityArray = new String[bannerCareCityDetailsList.size()];
        for (int i = 0; i < bannerCareCityDetailsList.size(); i++) {
            CareCityDetail careCityDetail = bannerCareCityDetailsList.get(i);
            cityArray[i] = careCityDetail.cityName;
        }
        Log.d(TAG, "showPictureChooserDialog() called");
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.select_city)
                .setItems(cityArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        LogUtils.LOGE(TAG, "onClick alert dialog : " + bannerCareCityDetailsList.get(which).cityName);
                        mCity = bannerCareCityDetailsList.get(which);
                        setCityBannerData();
                        dialog.dismiss();
                    }
                });
        builder.create();

        //Show the dialog
        builder.show();
    }

    // open show Comparision Chart Fragment Dialog
    private void showComparisionChartFragmentDialog() {
        if (comparisionChartFragmentDialog != null) {
            comparisionChartFragmentDialog.dismissAllowingStateLoss();
            comparisionChartFragmentDialog = null;
        }
        comparisionChartFragmentDialog = ComparisionChartFragmentDialog.newInstance(comparisionChartModel, mCityLandingPageModel.packageDetailList, mCity,comingFrom);
        comparisionChartFragmentDialog.show(getSupportFragmentManager(), TAG);
    }

    // open show Package Detail Model Fragment Dialog
    private void showPackageDetailModelFragmentDialog(PackageDetail packageDetail) {
        if (packageDetailModelDialog != null) {
            packageDetailModelDialog.dismissAllowingStateLoss();
            packageDetailModelDialog = null;
        }
        packageDetailModelDialog = PackageDetailModelDialog.newInstance(packageDetail, mCityLandingPageModel.careCityDetail,comingFrom);
        packageDetailModelDialog.show(getSupportFragmentManager(), TAG);
    }

    private final CheepCarePackageAdapter.PackageItemClickListener mPackageItemClickListener
            = new CheepCarePackageAdapter.PackageItemClickListener() {
        @Override
        public void onPackageItemClick(int position, PackageDetail packageModel) {
            packageDetailData = packageModel;

            callGetPackageFeatureListDetailWS();

        }
    };

    private void navigateToFragment() {
        if (packageDetailData.type.equalsIgnoreCase(Utility.CAR_PACKAGE_TYPE.PREMIUM)) {
           // PreferenceUtility.getInstance(getApplicationContext()).saveTypeOfPackage(Utility.CAR_PACKAGE_TYPE.PREMIUM);
            showPackageDetailModelFragmentDialog(packageDetailData);
        } else if (packageDetailData.type.equalsIgnoreCase(Utility.CAR_PACKAGE_TYPE.NORMAL)) {
            showComparisionChartFragmentDialog();
        }
    }
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

//    private void getSavedData() {
//        ArrayList<PackageDetail> savedPackageList = new ArrayList<>();
//        String cartDetail = PreferenceUtility.getInstance(this).getCityCartDetail(mCityLandingPageModel.careCityDetail.citySlug);
//        if (!TextUtils.isEmpty(cartDetail)) {
//            ArrayList<PackageDetail> list = GsonUtility.getObjectListFromJsonString(cartDetail, PackageDetail[].class);
//            savedPackageList.clear();
//            savedPackageList.addAll(list);
//            String webData = GsonUtility.getJsonStringFromObject(mCityLandingPageModel.packageDetailList);
//            LogUtils.LOGE(TAG, "Saved data--- " + cartDetail);
//            LogUtils.LOGE(TAG, "---------------------------------------------------------------------");
//            LogUtils.LOGE(TAG, "Saved web data--- " + webData);
//
//            if (!savedPackageList.isEmpty()) {
//                for (int i = 0; i < mCityLandingPageModel.packageDetailList.size(); i++) {
//                    PackageDetail webPakegDetail = mCityLandingPageModel.packageDetailList.get(i);
//                    for (PackageDetail detail : savedPackageList)
//                    {
//                        if (detail.packageOptionList != null && !detail.packageOptionList.isEmpty() && detail.packageSlug.equalsIgnoreCase(webPakegDetail.packageSlug) && detail.isSelected) {
//                            webPakegDetail.packageOptionList = detail.packageOptionList;
//                            webPakegDetail.mSelectedAddressList = detail.mSelectedAddressList;
//                            webPakegDetail.isSelected = true;
//                        }
//                    }
//                }
//                LogUtils.LOGE(TAG, "replacedData: " + GsonUtility.getJsonStringFromObject(mCityLandingPageModel.packageDetailList));
//            }
//        } else {
//            if (!TextUtils.isEmpty(mPackageListString)) {
//                ArrayList<PackageDetail> list = GsonUtility.getObjectListFromJsonString(mPackageListString, PackageDetail[].class);
//                mCityLandingPageModel.packageDetailList.clear();
//                mCityLandingPageModel.packageDetailList.addAll(list);
//            }
//            LogUtils.LOGE(TAG, "getSavedData: no cart data found");
//        }
//    }

    /************************************************************************************************
     **********************************Calling Webservice for old and new price *********************
     *************************************** of premium and normal ********************************
     ************************************************************************************************/

    private void callGetPackageFeatureListDetailWS() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.GET_PACKAGE_FEATURE_LIST
                , mCallGetCityCareDetailsWSErrorListener
                , mCallGetCityCareDetailsWSResponseListener
                , mHeaderParams
                , null
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.GET_PACKAGE_FEATURE_LIST);
    }


    private Response.Listener mCallGetCityCareDetailsWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            hideProgressDialog();
            Log.e(TAG, "onResponse() called with: response = [" + response + "]");
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.e(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        comparisionChartModel = (ComparisionChartModel) GsonUtility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), ComparisionChartModel.class);
                        //PreferenceUtility.getInstance(mContext).saveComparisonChatDetails(comparisionChartModel);
                        navigateToFragment();

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
            Log.e(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

}
