package com.cheep.cheepcarenew.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.model.CityLandingPageModel;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.databinding.ActivityPaymentSummaryNewBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.ComparisionChart.ComparisionChartModel;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

public class PaymentSummaryCheepCareActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PaymentSummaryCheepCareActivity";

    private android.support.v7.widget.CardView card3Months, card6Months, card12Months;
    private TextView tv3Month, tv6Month, tv12Month;
    private TextView tv3SaveMonth, tv6SaveMonth, tv12SaveMonth;
    private TextView tvMeanPackageAmount;
    private Toolbar toolbar;
    double oldPrice = 0;
    double newPrice = 0;
    double profit = 0;

    private PackageDetail packageDetail;
    private AddressModel addressModel;
    private ComparisionChartModel comparisionChartModel;
    private CityLandingPageModel cityLandingPageModel;
    private ActivityPaymentSummaryNewBinding mBinding;


    public static void newInstance(Context context) {
        Intent intent = new Intent(context, PaymentSummaryCheepCareActivity.class);
        context.startActivity(intent);
    }


    public static void newInstance(Context context, PackageDetail packageDetail, AddressModel addressModel) {
        Intent intent = new Intent(context, PaymentSummaryCheepCareActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(packageDetail));
        intent.putExtra(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(addressModel));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_payment_summary_new);
        initView();
        initiateUI();
        setListeners();
        setInitialColorOfCardView();
        updateSaveAmountForMonth();
        updatePrice(12);

    }

    @Override
    protected void initiateUI() {

        if (getIntent() != null && getIntent().hasExtra(Utility.Extra.DATA) && getIntent().hasExtra(Utility.Extra.DATA_2)) {
            packageDetail = (PackageDetail) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), PackageDetail.class);
            addressModel = (AddressModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), AddressModel.class);
            Log.e(TAG, "initiateUI: -------------" + addressModel.address);
            Log.e(TAG, "initiateUI: ------------" + packageDetail.title);

        }
        comparisionChartModel = PreferenceUtility.getInstance(mContext).getComparisonChatDetails();
        cityLandingPageModel = PreferenceUtility.getInstance(mContext).getCityLandingPageModel();

        setPrice();
        setCityBannerData();

    }

    @Override
    protected void setListeners() {
        card3Months.setOnClickListener(this);
        card6Months.setOnClickListener(this);
        card12Months.setOnClickListener(this);
    }

    private void initView() {

        toolbar = findViewById(R.id.toolbar);

        card3Months = findViewById(R.id.card_3_months);

        card6Months = findViewById(R.id.card_6_months);
        card12Months = findViewById(R.id.card_12_months);


        tv3Month = findViewById(R.id.tv_3_month);
        tv6Month = findViewById(R.id.tv_6_month);
        tv12Month = findViewById(R.id.tv_12_month);

        tv3SaveMonth = findViewById(R.id.tv_3_save_month);
        tv6SaveMonth = findViewById(R.id.tv_6_save_month);
        tv12SaveMonth = findViewById(R.id.tv_12_save_month);

        tvMeanPackageAmount = findViewById(R.id.tv_mean_package_amount);

        // Setting up Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    onBackPressed();
                }
            });
        }

    }

    private void setPrice() {
        for (int i = 0; comparisionChartModel.priceLists.size() > i; i++) {

            String TYPE = comparisionChartModel.priceLists.get(i).type;

            if (TYPE.equalsIgnoreCase(PreferenceUtility.getInstance(mContext).getTypeOfPackage())) {
                mBinding.tvNewPrice.setText(comparisionChartModel.priceLists.get(i).newPrice);
                mBinding.tvOldPrice.setText(comparisionChartModel.priceLists.get(i).oldPrice);

                newPrice =  Double.parseDouble(comparisionChartModel.priceLists.get(i).newPrice);
                oldPrice =  Double.parseDouble(comparisionChartModel.priceLists.get(i).oldPrice);

            } else if (TYPE.equalsIgnoreCase(PreferenceUtility.getInstance(mContext).getTypeOfPackage())) {
                mBinding.tvNewPrice.setText(comparisionChartModel.priceLists.get(i).newPrice);
                mBinding.tvOldPrice.setText(comparisionChartModel.priceLists.get(i).oldPrice);

                newPrice =  Double.parseDouble(comparisionChartModel.priceLists.get(i).newPrice);
                oldPrice =  Double.parseDouble(comparisionChartModel.priceLists.get(i).oldPrice);
            }

        }
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
                switch (cityLandingPageModel.careCityDetail.citySlug) {
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

        mBinding.tvCityName.setText(cityLandingPageModel.careCityDetail.cityName);


    }

    @SuppressLint("ResourceType")
    private void setInitialColorOfCardView() {
        card3Months.setSelected(false);
        tv3Month.setSelected(false);
        tv3SaveMonth.setSelected(false);


        card6Months.setSelected(false);
        tv6Month.setSelected(false);
        tv6SaveMonth.setSelected(false);

        card12Months.setSelected(true);
        tv12Month.setSelected(true);
        tv12SaveMonth.setSelected(true);
    }

    private void updatePrice(int howManyMonth) {
        profit = oldPrice - newPrice;
        tvMeanPackageAmount.setText(Utility.CHEEP_CARE.RS + String.valueOf(newPrice * howManyMonth));
    }

    private void updateSaveAmountForMonth() {
        profit = oldPrice - newPrice;
        tv3SaveMonth.setText(Utility.CHEEP_CARE.SAVE + String.valueOf(profit * 3));
        tv6SaveMonth.setText(Utility.CHEEP_CARE.SAVE + String.valueOf(profit * 6));
        tv12SaveMonth.setText(Utility.CHEEP_CARE.SAVE + String.valueOf(profit * 12));

    }

    // View.OnClickListener
    @SuppressLint("ResourceType")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.card_3_months:
                card3Months.setSelected(true);
                tv3Month.setSelected(true);
                tv3SaveMonth.setSelected(true);

                card6Months.setSelected(false);
                tv6Month.setSelected(false);
                tv6SaveMonth.setSelected(false);

                card12Months.setSelected(false);
                tv12Month.setSelected(false);
                tv12SaveMonth.setSelected(false);

                updatePrice(3);


                break;
            case R.id.card_6_months:
                card3Months.setSelected(false);
                tv3Month.setSelected(false);
                tv3SaveMonth.setSelected(false);

                card6Months.setSelected(true);
                tv6Month.setSelected(true);
                tv6SaveMonth.setSelected(true);

                card12Months.setSelected(false);
                tv12Month.setSelected(false);
                tv12SaveMonth.setSelected(false);
                updatePrice(6);
                break;
            case R.id.card_12_months:
                card3Months.setSelected(false);
                tv3Month.setSelected(false);
                tv3SaveMonth.setSelected(false);


                card6Months.setSelected(false);
                tv6Month.setSelected(false);
                tv6SaveMonth.setSelected(false);

                card12Months.setSelected(true);
                tv12Month.setSelected(true);
                tv12SaveMonth.setSelected(true);
                updatePrice(12);
                break;

        }
    }
}
