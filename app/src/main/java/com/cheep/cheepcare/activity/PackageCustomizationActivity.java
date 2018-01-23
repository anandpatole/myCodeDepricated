package com.cheep.cheepcare.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.adapter.PackageCustomizationPagerAdapter;
import com.cheep.cheepcare.fragment.PackageBundlingFragment;
import com.cheep.cheepcare.fragment.PackageSummaryFragment;
import com.cheep.cheepcare.fragment.SelectPackageSpecificationsFragment;
import com.cheep.cheepcare.model.AdminSettingModel;
import com.cheep.cheepcare.model.CityDetail;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.cheepcare.model.PackageOption;
import com.cheep.cheepcare.model.PackageSubOption;
import com.cheep.databinding.ActivityPackageCustomizationBinding;
import com.cheep.model.MessageEvent;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by pankaj on 12/22/17.
 */

public class PackageCustomizationActivity extends BaseAppCompatActivity {

    private ActivityPackageCustomizationBinding mBinding;
    private PackageCustomizationPagerAdapter mPackageCustomizationPagerAdapter;
    private PackageDetail mPackageModel;
    public CityDetail mCityDetail;
    public String mPackageId = "";
    public AdminSettingModel settingModel;
    private ArrayList<PackageDetail> mPackageList = new ArrayList<>();
    private static final String TAG = "PackageCustomizationAct";
    private int mPreviousState = 0;
    int cartCount = 0;

    public static final int STAGE_1 = 0;
    public static final int STAGE_2 = 1;
    public static final int STAGE_3 = 2;


    public void setPackageList(ArrayList<PackageDetail> mPackageList) {
        this.mPackageList = mPackageList;
    }

    public ArrayList<PackageDetail> getPackageList() {
        return mPackageList;
    }

    public static void newInstance(Context context, PackageDetail model, CityDetail cityDetail, String selectedPackageID, String packageList, AdminSettingModel adminSetting) {
        Intent intent = new Intent(context, PackageCustomizationActivity.class);
        intent.putExtra(Utility.Extra.MODEL, model);
        intent.putExtra(Utility.Extra.CITY_NAME, Utility.getJsonStringFromObject(cityDetail));
        intent.putExtra(Utility.Extra.SELECTED_PACKAGE_ID, selectedPackageID);
        intent.putExtra(Utility.Extra.PACKAGE_LIST, packageList);
        intent.putExtra(Utility.Extra.ADMIN_SETTING, Utility.getJsonStringFromObject(adminSetting));
        context.startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_package_customization);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {

        updateCartCount();

        if (getIntent().hasExtra(Utility.Extra.MODEL)) {
            mPackageModel = (PackageDetail) getIntent().getExtras().getSerializable(Utility.Extra.MODEL);
            mCityDetail = (CityDetail) Utility.getObjectFromJsonString(getIntent().getExtras().getString(Utility.Extra.CITY_NAME), CityDetail.class);
            settingModel = (AdminSettingModel) Utility.getObjectFromJsonString(getIntent().getExtras().getString(Utility.Extra.ADMIN_SETTING), AdminSettingModel.class);
            mPackageId = getIntent().getExtras().getString(Utility.Extra.SELECTED_PACKAGE_ID);
            mPackageList = Utility.getObjectListFromJsonString(getIntent().getExtras().getString(Utility.Extra.PACKAGE_LIST), PackageDetail[].class);
        }

        // Calculate Pager Height and Width
        ViewTreeObserver mViewTreeObserver = mBinding.ivCityImage.getViewTreeObserver();
        mViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.ivCityImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = mBinding.ivCityImage.getMeasuredWidth();
                ViewGroup.LayoutParams params = mBinding.ivCityImage.getLayoutParams();
                params.height = Utility.getHeightFromWidthForOneHalfIsToOneRatio(width);
                mBinding.ivCityImage.setLayoutParams(params);

                // Load the image now.


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

        // Set the default step
        setTaskState(STEP_ONE_UNVERIFIED);

        //Initiate description
        mBinding.textStepDesc.setText(getString(R.string.step_1_desc_cheep_care));

        //Initiate Button text
        setContinueButtonText();

        // Setting viewpager
        setupViewPager(mBinding.viewpager);

        // Manage Click events of TaskCreation steps
        mBinding.textStep1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                gotoStep(STAGE_1);
            }
        });
        mBinding.textStep2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                  Need to check whether first step is verified or not.
                  TODO:: remove comments

                 */
//                if (mCurrentStep > 1) {
//                gotoStep(STAGE_2);
//                } else {
//                    Utility.showSnackBar(getString(R.string.step_1_desc_cheep_care), mBinding.getRoot());
//                }

            }
        });
        mBinding.textStep3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                  Need to check whether first step is verified or not.
                 */
//                TODO:: remove comments
//                if (mCurrentStep > 1) {
//                gotoStep(STAGE_3);
//                } else {
//                    Utility.showSnackBar(getString(R.string.step_1_desc_cheep_care), mBinding.getRoot());
//                }

            }
        });

        //set phase description

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


    /**
     * This will setup the viewpager and tabs as well
     *
     * @param pager pager
     */
    private void setupViewPager(ViewPager pager) {
        pager.setOffscreenPageLimit(2);
        mPackageCustomizationPagerAdapter = new PackageCustomizationPagerAdapter(getSupportFragmentManager());
        mPackageCustomizationPagerAdapter.addFragment(SelectPackageSpecificationsFragment.TAG);
        mPackageCustomizationPagerAdapter.addFragment(PackageBundlingFragment.TAG);
        mPackageCustomizationPagerAdapter.addFragment(PackageSummaryFragment.TAG);
        pager.setAdapter(mPackageCustomizationPagerAdapter);
    }

    /**
     * Below would manage the state of Step while creating task creation
     */
    public static final int STEP_ONE_UNVERIFIED = 1;
    public static final int STEP_ONE_VERIFIED = 2;
    public static final int STEP_TWO_UNVERIFIED = 3;
    public static final int STEP_TWO_VERIFIED = 4;
    public static final int STEP_THREE_UNVERIFIED = 5;
    public static final int STEP_THREE_VERIFIED = 6;
    public int mCurrentStep = -1;

    public void setTaskState(int step_state) {
        mCurrentStep = step_state;
        switch (step_state) {
            case STEP_ONE_UNVERIFIED:
                mBinding.textStep1.setSelected(true);
                mBinding.textStep2.setSelected(false);
                mBinding.textStep3.setSelected(false);
                break;

            case STEP_ONE_VERIFIED:
            case STEP_TWO_UNVERIFIED:
                mBinding.textStep1.setSelected(false);
                mBinding.textStep2.setSelected(true);
                mBinding.textStep3.setSelected(false);
                break;
            case STEP_TWO_VERIFIED:
            case STEP_THREE_UNVERIFIED:
                mBinding.textStep1.setSelected(false);
                mBinding.textStep2.setSelected(false);
                mBinding.textStep3.setSelected(true);
                break;
            case STEP_THREE_VERIFIED:
                mBinding.textStep1.setSelected(false);
                mBinding.textStep2.setSelected(false);
                mBinding.textStep3.setSelected(true);
                break;
        }
    }

    public void gotoStep(int step) {
        mPreviousState = mBinding.viewpager.getCurrentItem();
        switch (step) {
            case STAGE_1:
                mBinding.viewpager.setCurrentItem(0);
                // Change description
                mBinding.textStepDesc.setText(getString(R.string.step_1_desc_cheep_care));
                setContinueButtonText();
                break;
            case STAGE_2:
                mBinding.viewpager.setCurrentItem(1);
                // Change description
                mBinding.textStepDesc.setText(getString(R.string.step_2_desc_cheep_care));
                mBinding.textContinue.setText(getString(R.string.label_proceed_to_checkout));
                mBinding.textService.setText(Utility.EMPTY_STRING);
                mBinding.textPrice.setText(Utility.EMPTY_STRING);
                break;
            case STAGE_3:
                mBinding.viewpager.setCurrentItem(2);
                // Change description
                mBinding.textStepDesc.setText(getString(R.string.step_3_desc_cheep_care));
                mBinding.textContinue.setText(getString(R.string.label_pay_now));
                mBinding.textService.setText(Utility.EMPTY_STRING);
                mBinding.textPrice.setText(Utility.EMPTY_STRING);
                break;
        }
    }


    @Override
    public void onBackPressed() {


        if (mBinding.viewpager.getCurrentItem() == 0) {
            if (cartCount > 0)
                showAlertDialog();
            else
                super.onBackPressed();
        } else gotoStep(mPreviousState);


/*
            if (mBinding.viewpager.getCurrentItem() == 2) {
                gotoStep(STAGE_2);
                return;
            } else if (mBinding.viewpager.getCurrentItem() == 1) {
                gotoStep(STAGE_1);
                return;
            }
        super.onBackPressed();
*/
    }

    public void showAlertDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.app_name));
        alertDialog.setMessage(getString(R.string.cheep_care_alert_message));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.label_Ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                PackageCustomizationActivity.super.onBackPressed();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();
    }

    @Override
    protected void setListeners() {
        mBinding.textContinue.setOnClickListener(mOnClickListener);
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.text_continue:
                    if (mBinding.viewpager.getCurrentItem() == STAGE_1) {
                        goToPackageBundling(STAGE_3);
                    } else if (mBinding.viewpager.getCurrentItem() == STAGE_2) {
                        gotoStep(STAGE_3);
                    } else {
                        PackageSummaryFragment summaryFragment = (PackageSummaryFragment) mPackageCustomizationPagerAdapter.getItem(STAGE_3);
                        if (summaryFragment != null)
                            PaymentChoiceCheepCareActivity.newInstance(PackageCustomizationActivity.this, createSubscriptionPackageRequest(), summaryFragment.getPaymentData(),mCityDetail);
                    }
                    break;
            }
        }
    };

    public void goToPackageBundling(int step) {
        SelectPackageSpecificationsFragment fragment = (SelectPackageSpecificationsFragment) mPackageCustomizationPagerAdapter.getItem(mBinding.viewpager.getCurrentItem());
        cartCount = 0;
        if (fragment != null) {
            if (fragment.validateData()) {
                for (PackageDetail detail : mPackageList) {
                    if (detail.id.equalsIgnoreCase(mPackageId)) {
                        detail.isSelected = true;
                        detail.mSelectedAddress = fragment.mSelectedAddress;
                    }
                    if (detail.packageOptionList != null && detail.isSelected) {
                        cartCount++;
                        for (PackageOption service : detail.packageOptionList) {
                            if (service.selectionType.equalsIgnoreCase(PackageOption.SELECTION_TYPE.RADIO)) {
                                for (PackageSubOption option : service.getChildList()) {
                                    if (option.isSelected) {
                                        detail.monthlyPrice = Double.parseDouble(option.monthlyPrice);
                                        detail.yearlyPrice = Double.parseDouble(option.annualPrice);
                                        detail.halfYearlyPrice = Double.parseDouble(option.sixmonthPrice);
                                    }
                                }
                            } else {
                                PackageSubOption option = service.getChildList().get(0);

                                detail.monthlyPrice = Double.parseDouble(option.monthlyPrice);
                                detail.yearlyPrice = Double.parseDouble(option.annualPrice);
                                detail.halfYearlyPrice = Double.parseDouble(option.sixmonthPrice);

                                for (PackageSubOption option1 : service.getChildList()) {
                                    if (option1.qty > 1) {
                                        detail.monthlyPrice += Double.valueOf(option1.unitPrice) * (option1.qty - 1);
                                        detail.yearlyPrice += Double.valueOf(option1.unitPrice) * (option1.qty - 1) * 12;
                                        detail.halfYearlyPrice += Double.valueOf(option1.unitPrice) * (option1.qty - 1) * 6;
                                    }
                                }
                            }
                        }
                    }
                }
                gotoStep(step);
            }
            updateCartCount();
        }
    }

    public void updateCartCount() {
        mBinding.tvBadgeCartCount.setVisibility(cartCount == 0 ? View.GONE : View.VISIBLE);
        mBinding.tvBadgeCartCount.setText(String.valueOf(cartCount));
    }

    private String createSubscriptionPackageRequest() {

        JSONArray cartArray = new JSONArray();

        for (PackageDetail detail : mPackageList) {
            JSONObject packageObject = new JSONObject();
            if (detail.isSelected) {
                try {
                    packageObject.put("package_id", detail.id);
                    JSONArray multiPackageArray = new JSONArray();
                    JSONObject singleObj = new JSONObject();
                    JSONObject addressObject = new JSONObject();
                    int addressId;
                    try {
                        addressId = Integer.parseInt(detail.mSelectedAddress.address_id);
                    } catch (Exception e) {
                        addressId = 0;
                    }
                    if (addressId <= 0) {
                        addressObject.put(NetworkUtility.TAGS.ADDRESS, detail.mSelectedAddress.address);
                        addressObject.put(NetworkUtility.TAGS.ADDRESS_INITIALS, detail.mSelectedAddress.address_initials);
                        addressObject.put(NetworkUtility.TAGS.CATEGORY, detail.mSelectedAddress.category);
                        addressObject.put(NetworkUtility.TAGS.LAT, detail.mSelectedAddress.lat);
                        addressObject.put(NetworkUtility.TAGS.LNG, detail.mSelectedAddress.lng);
                        addressObject.put(NetworkUtility.TAGS.COUNTRY, detail.mSelectedAddress.countryName);
                        addressObject.put(NetworkUtility.TAGS.STATE, detail.mSelectedAddress.stateName);
                        addressObject.put(NetworkUtility.TAGS.CITY_NAME, detail.mSelectedAddress.cityName);
                    } else {
                        addressObject.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);
                    }

                    singleObj.put("address", addressObject);
                    JSONArray optionArray = new JSONArray();
                    for (PackageOption servicesModel : detail.packageOptionList) {
                        JSONObject optionObj = new JSONObject();
                        optionObj.put("package_option_id", servicesModel.packageId);

                        JSONArray subOptionArray = new JSONArray();
                        if (servicesModel.selectionType.equalsIgnoreCase(PackageOption.SELECTION_TYPE.RADIO))
                            for (PackageSubOption option : servicesModel.getChildList()) {
                                if (option.isSelected) {
                                    JSONObject object = new JSONObject();
                                    object.put("package_suboption_id", option.packageOptionId);
                                    object.put("selected_unit", "1");
                                    subOptionArray.put(object);
                                }
                            }
                        else
                            for (PackageSubOption option : servicesModel.getChildList()) {
                                JSONObject object = new JSONObject();
                                object.put("package_suboption_id", option.packageOptionId);
                                object.put("selected_unit", option.qty);
                                subOptionArray.put(object);
                            }

                        optionObj.put("package_suboption", subOptionArray);
                        optionArray.put(optionObj);
                    }
                    singleObj.put("package_options", optionArray);
                    multiPackageArray.put(singleObj);
                    packageObject.put("package_details", multiPackageArray);
                    cartArray.put(packageObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        LogUtils.LOGE(TAG, "createSubscriptionPackageRequest: " + cartArray.toString());
        return cartArray.toString();
    }

    public void loadAnotherPackage() {
        SelectPackageSpecificationsFragment fragment =
                (SelectPackageSpecificationsFragment) mPackageCustomizationPagerAdapter.getItem(mBinding.viewpager.getCurrentItem());
        if (fragment != null) {
            fragment.initiateUI();
        }
    }

    public void setContinueButtonText() {
        mBinding.textContinue.setText(R.string.add_package_to_cart);
        for (PackageDetail detail : mPackageList) {
            if (detail.id.equalsIgnoreCase(mPackageId)) {
                if (detail.packageOptionList != null && detail.id.equalsIgnoreCase(mPackageId)) {
                    for (PackageOption packageOption : detail.packageOptionList) {
                        if (packageOption.selectionType.equalsIgnoreCase(PackageOption.SELECTION_TYPE.RADIO))
                            for (PackageSubOption option : packageOption.getChildList()) {
                                if (option.isSelected) {
                                    setContinueButtonText(option.packageSuboptionTitle, option.monthlyPrice);
                                }
                            }
                        else {
                            int totalCount = 0;
                            String monthlyPrice = packageOption.getChildList().get(0).monthlyPrice;
                            for (PackageSubOption option : packageOption.getChildList()) {
                                totalCount += option.qty;
                            }
                            setContinueButtonText(totalCount, monthlyPrice);
                        }
                    }
                }
                mBinding.textContinue.setText(getString(R.string.add_package_to_cart));
                break;
            }
        }
    }

    public void setContinueButtonText(String selectedService, String price) {
        mBinding.textService.setText(selectedService);
        mBinding.textPrice.setText(Utility.getMonthlyPrice(price, this));
    }

    public void setContinueButtonText(int totalAppliance, String price) {
        mBinding.textService.setText(getString(R.string.label_appliance, totalAppliance));
        mBinding.textPrice.setText(Utility.getMonthlyPrice(price, this));
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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
}