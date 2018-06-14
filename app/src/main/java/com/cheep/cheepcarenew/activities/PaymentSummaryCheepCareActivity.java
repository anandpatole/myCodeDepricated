package com.cheep.cheepcarenew.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.model.AddressModel;
import com.cheep.model.ComparisionChart.ComparisionChartModel;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.Utility;

public class PaymentSummaryCheepCareActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PaymentSummaryCheepCareActivity";

    private android.support.v7.widget.CardView card3Months, card6Months, card12Months;
    private TextView tv3Month, tv6Month, tv12Month;
    private TextView tv3SaveMonth, tv6SaveMonth, tv12SaveMonth;
    private TextView tvMeanPackageAmount;
    private Toolbar toolbar;
    double cutPrice = 600;
    double price = 400;
    double profit = 0;

    private PackageDetail packageDetail;
    private AddressModel addressModel;
    private ComparisionChartModel comparisionChartModel;


    public static void newInstance(Context context, PackageDetail packageDetail, AddressModel addressModel) {
        Intent intent = new Intent(context, PaymentSummaryCheepCareActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(packageDetail));
        intent.putExtra(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(addressModel));
        context.startActivity(intent);
    }
<<<<<<< HEAD:app/src/main/java/com/cheep/cheepcarenew/activities/PaymentSummaryActivityCheepCare.java
    public static void newInstance(Context context, ComparisionChartModel comparisionChartModel,String typeOfCheepCarePackage) {
        Intent intent = new Intent(context, PaymentSummaryActivityCheepCare.class);
        intent.putExtra(Utility.Extra.DATA_4, GsonUtility.getJsonStringFromObject(comparisionChartModel));
        intent.putExtra(Utility.TYPE_OF_PACKAGE,typeOfCheepCarePackage);
=======

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, PaymentSummaryCheepCareActivity.class);
>>>>>>> 1fb3d424cc477adc5b97a5ab4fe4a6ea1a631cf8:app/src/main/java/com/cheep/cheepcarenew/activities/PaymentSummaryCheepCareActivity.java
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_summary_new);
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

        } else if(getIntent() != null && getIntent().hasExtra(Utility.Extra.DATA_4)){
            comparisionChartModel = (ComparisionChartModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_4), ComparisionChartModel.class);
            Log.e(TAG, "initiateUI: -------------"+ comparisionChartModel.priceLists.toString());
        }

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

            if (TYPE.equalsIgnoreCase(NetworkUtility.PACKAGE_DETAIL_TYPE.premium)) {
               // mBinding.tvPremiumNewPrice.setText(comparisionChartModel.priceLists.get(i).newPrice);
               // mBinding.tvPremiumOldPrice.setText(comparisionChartModel.priceLists.get(i).oldPrice);
            } else if (TYPE.equalsIgnoreCase(NetworkUtility.PACKAGE_DETAIL_TYPE.normal)) {
              //  mBinding.tvNormalNewPrice.setText(comparisionChartModel.priceLists.get(i).newPrice);
              //  mBinding.tvNormalOldPrice.setText(comparisionChartModel.priceLists.get(i).oldPrice);
            }

        }
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
        profit = cutPrice - price;
        tvMeanPackageAmount.setText(Utility.CHEEP_CARE.RS + String.valueOf(price * howManyMonth));
    }

    private void updateSaveAmountForMonth() {
        profit = cutPrice - price;
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
