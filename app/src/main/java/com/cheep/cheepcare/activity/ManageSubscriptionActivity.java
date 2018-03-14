package com.cheep.cheepcare.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.adapter.ExpandableSubscribedPackagesRecyclerAdapter;
import com.cheep.cheepcare.adapter.ManageSubscriptionAddPackageAdapter;
import com.cheep.cheepcare.model.AdminSettingModel;
import com.cheep.cheepcare.model.CityDetail;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.databinding.ActivityManageSubscriptionBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.MessageEvent;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 12/28/17.
 */

public class ManageSubscriptionActivity extends BaseAppCompatActivity {

    private static final String TAG = ManageSubscriptionActivity.class.getSimpleName();
    private ActivityManageSubscriptionBinding mBinding;
    private boolean isManageSubscription;
    private List<PackageDetail> mSubscribedPackageList;
    private List<PackageDetail> mAllPackagesList;
    private CityDetail mCityDetail;
    private AdminSettingModel mAdminSettingModel;
    private ArrayList<CityDetail> bannerCityDetailsList;


    private ExpandableSubscribedPackagesRecyclerAdapter.ParentViewsClickListener parentClickListener = new ExpandableSubscribedPackagesRecyclerAdapter.ParentViewsClickListener() {
        @Override
        public void onParentViewClick(PackageDetail packageDetail) {
            AddressModel addressModel = null;
            if (packageDetail.mSelectedAddressList != null) {
                for (AddressModel addressModel1 : packageDetail.mSelectedAddressList) {
                    if (addressModel1.isSelected)
                        addressModel = addressModel1;
                }
            }

            TaskCreationCCActivity.getInstance(ManageSubscriptionActivity.this,
                    packageDetail.getChildList().get(0),
                    addressModel,
                    packageDetail.packageType,
                    packageDetail.id,
                    packageDetail.mSelectedAddressList, mAdminSettingModel);
        }
    };

    public static void newInstance(Context context, CityDetail city, boolean isManageSubscription, String cheepcareBannerListString) {
        Intent intent = new Intent(context, ManageSubscriptionActivity.class);
        intent.putExtra(Utility.Extra.CITY_DETAIL, GsonUtility.getJsonStringFromObject(city));
        intent.putExtra(Utility.Extra.ACTIVITY_TYPE, isManageSubscription);
        intent.putExtra(Utility.Extra.DATA, cheepcareBannerListString);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_manage_subscription);
        initiateUI();
        setListeners();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initiateUI() {
        if (getIntent().hasExtra(Utility.Extra.CITY_DETAIL)) {
            mCityDetail = (CityDetail) GsonUtility.getObjectFromJsonString(getIntent().getExtras().getString(Utility.Extra.CITY_DETAIL), CityDetail.class);
            isManageSubscription = getIntent().getExtras().getBoolean(Utility.Extra.ACTIVITY_TYPE);
            if (isManageSubscription) {
                bannerCityDetailsList = GsonUtility.getObjectListFromJsonString(getIntent().getExtras().getString(Utility.Extra.DATA), CityDetail[].class);
            } else {
                mBinding.tvCityName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

        }
        if (mCityDetail == null)
            return;

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

        // Calculate Pager Height and Width
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
                switch (mCityDetail.citySlug) {
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
                .asGif()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mBinding.ivCheepCareGif);
        /*// Start cheep care animations
        mBinding.ivCheepCareGif.setBackgroundResource(R.drawable.cheep_care_animation);
        ((AnimationDrawable) mBinding.ivCheepCareGif.getBackground()).start();*/

        mBinding.tvCityName.setText(mCityDetail.cityName);

        String name = PreferenceUtility.getInstance(this).getUserDetails().userName;
        if (!isManageSubscription) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.msg_welcome_x, name));
            spannableStringBuilder.append(Utility.ONE_CHARACTER_SPACE).append(Utility.ONE_CHARACTER_SPACE);
            ImageSpan span = new ImageSpan(getBaseContext(), R.drawable.emoji_folded_hands_big, ImageSpan.ALIGN_BASELINE);
            spannableStringBuilder.setSpan(span, spannableStringBuilder.length() - 1, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            mBinding.tvWelcomeText.setText(spannableStringBuilder);
            mBinding.tvWelcomeText.setVisibility(View.VISIBLE);
            mBinding.tvInfoText.setText(getString(R.string.msg_welcoming_on_subscription));
        } else {
            mBinding.tvWelcomeText.setVisibility(View.GONE);
            mBinding.tvInfoText.setText(getString(R.string.cheep_care_work_flow_desc, name));
        }

        callGetUserSubscribedPackages();
    }

    @Override
    protected void setListeners() {
        mBinding.tvCityName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isManageSubscription)
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
                        if (!mCityDetail.cityName.equalsIgnoreCase(bannerCityDetailsList.get(which).cityName)) {
                            if (bannerCityDetailsList.get(which).isSubscribed.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                                mCityDetail = bannerCityDetailsList.get(which);
                                setCityBannerData();
                            } else {
                                String cheepcareBannerListString = GsonUtility.getJsonStringFromObject(bannerCityDetailsList);
                                LandingScreenPickPackageActivity.newInstance(ManageSubscriptionActivity.this,
                                        bannerCityDetailsList.get(which), cheepcareBannerListString);
                                finish();
                            }
                        }
                        dialog.dismiss();
                    }
                });
        builder.create();

        //Show the dialog
        builder.show();
    }

    private void callGetUserSubscribedPackages() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressDialog();

        WebCallClass.getSubscribedCarePackage(mContext, mCityDetail.citySlug
                , mCommonResponseListener, mGetSubscribedCarePackageResponseListener);
    }

    private void initiateDynamicUI() {

        mBinding.rvBoughtPackages.setNestedScrollingEnabled(false);
        ExpandableSubscribedPackagesRecyclerAdapter subscribedPackagesAdapter =
                new ExpandableSubscribedPackagesRecyclerAdapter(mSubscribedPackageList, true
                        , childViewsClickListener, parentClickListener);
        mBinding.rvBoughtPackages.setAdapter(subscribedPackagesAdapter);

        setAllPackageListData();
    }

    private void setAllPackageListData() {
        mBinding.rvAddPackage.setNestedScrollingEnabled(false);
        ManageSubscriptionAddPackageAdapter addPackageAdapter = new ManageSubscriptionAddPackageAdapter(addPackageInteractionListener, mAllPackagesList);
        mBinding.rvAddPackage.setAdapter(addPackageAdapter);
    }

    private final ExpandableSubscribedPackagesRecyclerAdapter.ChildViewsClickListener childViewsClickListener =
            new ExpandableSubscribedPackagesRecyclerAdapter.ChildViewsClickListener() {
                @Override
                public void onBookClicked(JobCategoryModel jobCategoryModel, int childAdapterPosition, PackageDetail packageDetail) {
                    AddressModel addressModel = null;
                    if (packageDetail.mSelectedAddressList != null) {
                        for (AddressModel addressModel1 : packageDetail.mSelectedAddressList) {
                            if (addressModel1.isSelected)
                                addressModel = addressModel1;
                        }
                    }
                    TaskCreationCCActivity.getInstance(mContext,
                            jobCategoryModel,
                            addressModel,
                            packageDetail.packageType,
                            packageDetail.id,
                            packageDetail.mSelectedAddressList,
                            mAdminSettingModel);

                }
            };

    private final ManageSubscriptionAddPackageAdapter.AddPackageInteractionListener addPackageInteractionListener =
            new ManageSubscriptionAddPackageAdapter.AddPackageInteractionListener() {
                @Override
                public void onPackageItemClick(PackageDetail model) {
                    String packageList = GsonUtility.getJsonStringFromObject(mAllPackagesList);
                    PackageCustomizationActivity.newInstance(mContext, mCityDetail, model, packageList, mAdminSettingModel);
                }
            };

    private final WebCallClass.CommonResponseListener mCommonResponseListener =
            new WebCallClass.CommonResponseListener() {
                @Override
                public void volleyError(VolleyError error) {
                    hideProgressDialog();
                    Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                }

                @Override
                public void showSpecificMessage(String message) {
                    hideProgressDialog();
                    Utility.showToast(mContext, message);
                }

                @Override
                public void forceLogout() {
                    hideProgressDialog();
                    finish();
                }
            };

    private String mPackageListString = Utility.EMPTY_STRING;
    private final WebCallClass.GetSubscribedCarePackageResponseListener mGetSubscribedCarePackageResponseListener =
            new WebCallClass.GetSubscribedCarePackageResponseListener() {
                @Override
                public void getSubscribedCarePackageSuccessResponse(CityDetail cityDetail, List<PackageDetail> subscribedList, List<PackageDetail> allPackageList, AdminSettingModel adminSettingModel) {
                    mCityDetail = cityDetail;
                    mSubscribedPackageList = subscribedList;
                    /*for (PackageDetail subscribedPackageDetail : mSubscribedPackageList) {
                        for (AddressModel selectedAddressModel : subscribedPackageDetail.mSelectedAddressList) {
                            selectedAddressModel.is_subscribe = "1";
                        }
                    }*/
                    mAllPackagesList = allPackageList;
                    mPackageListString = GsonUtility.getJsonStringFromObject(mAllPackagesList);
                    getSavedData();
                    mAdminSettingModel = adminSettingModel;
                    hideProgressDialog();
                    initiateDynamicUI();
                    setListeners();
                }
            };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        //remove volley callbacks
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.GET_USER_SUBSCRIBED_CARE_PACKAGE);
    }

    private void getSavedData() {
        ArrayList<PackageDetail> savedPackageList = new ArrayList<>();
        String cartDetail = PreferenceUtility.getInstance(this).getCityCartDetail(mCityDetail.citySlug);
        if (!TextUtils.isEmpty(cartDetail)) {
            ArrayList<PackageDetail> list = GsonUtility.getObjectListFromJsonString(cartDetail, PackageDetail[].class);
            savedPackageList.clear();
            savedPackageList.addAll(list);
            String webData = GsonUtility.getJsonStringFromObject(mAllPackagesList);
            LogUtils.LOGE(TAG, "Saved data--- " + cartDetail);
            LogUtils.LOGE(TAG, "---------------------------------------------------------------------");
            LogUtils.LOGE(TAG, "Saved web data--- " + webData);

            if (!savedPackageList.isEmpty()) {
                for (int i = 0; i < mAllPackagesList.size(); i++) {
                    PackageDetail webPakegDetail = mAllPackagesList.get(i);
                    for (PackageDetail detail : savedPackageList) {
                        if (detail.packageOptionList != null && !detail.packageOptionList.isEmpty() && detail.packageSlug.equalsIgnoreCase(webPakegDetail.packageSlug) && detail.isSelected) {
                            webPakegDetail.packageOptionList = detail.packageOptionList;
                            webPakegDetail.mSelectedAddressList = detail.mSelectedAddressList;
                            webPakegDetail.isSelected = true;
                        }
                    }
                }
                LogUtils.LOGE(TAG, "replacedData: " + GsonUtility.getJsonStringFromObject(mAllPackagesList));
            }
        } else {
            if (!TextUtils.isEmpty(mPackageListString)) {
                ArrayList<PackageDetail> list = GsonUtility.getObjectListFromJsonString(mPackageListString, PackageDetail[].class);
                mAllPackagesList.clear();
                mAllPackagesList.addAll(list);
            }
            LogUtils.LOGE(TAG, "getSavedData: no cart data found");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCityDetail != null && mAllPackagesList != null) {
            LogUtils.LOGE(TAG, "onResume:cart detail found");
            getSavedData();
        } else {
            LogUtils.LOGE(TAG, "onResume: no city data found");

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        LogUtils.LOGE(TAG, "onMessageEvent: " + event.BROADCAST_ACTION);
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.PACKAGE_SUBSCRIBED_SUCCESSFULLY) {
            finish();
        } else if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.SUBSCRIBED_TASK_CREATE_SUCCESSFULLY) {
            finish();
        } else if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.OTHER_SUBSCRIBED_ADDRESS_SELECTED) {
            TaskCreationCCActivity.getInstance(mContext,
                    event.jobCategoryModel,
                    event.addressModel,
                    event.packageType,
                    event.id,
                    event.selectedAddressList,
                    event.adminSettingModel);
        }
    }

}