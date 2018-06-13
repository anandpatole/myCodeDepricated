package com.cheep.cheepcarenew.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.model.AddressModel;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.Utility;

public class PaymentSummaryActivityCheepCare extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PaymentSummaryActivityCheepCare";

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


    public static void newInstance(Context context, PackageDetail packageDetail, AddressModel addressModel) {
        Intent intent = new Intent(context, PaymentSummaryActivityCheepCare.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(packageDetail));
        intent.putExtra(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(addressModel));
        context.startActivity(intent);
    }
    public static void newInstance(Context context) {
        Intent intent = new Intent(context, PaymentSummaryActivityCheepCare.class);
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
            Log.e(TAG, "initiateUI: -------------"+addressModel.address);
            Log.e(TAG, "initiateUI: ------------"+packageDetail.title);

        } else {
            return;
        }

    }

    @Override
    protected void setListeners() {
        card3Months.setOnClickListener(this);
        card6Months.setOnClickListener(this);
        card12Months.setOnClickListener(this);
    }

    private void initView(){

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

    @SuppressLint("ResourceType")
    private void setInitialColorOfCardView() {
        card12Months.setCardBackgroundColor(Color.parseColor(getResources().getString(R.color.cheep_tips_color)));
        tv12Month.setTextColor(Color.parseColor(getResources().getString(R.color.colorPrimary)));
        tv12SaveMonth.setTextColor(Color.parseColor(getResources().getString(R.color.colorPrimary)));
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
                card3Months.setCardBackgroundColor(Color.parseColor(getResources().getString(R.color.cheep_tips_color)));
                card6Months.setCardBackgroundColor(Color.parseColor(getResources().getString(R.color.colorPrimary)));
                card12Months.setCardBackgroundColor(Color.parseColor(getResources().getString(R.color.colorPrimary)));

                tv3Month.setTextColor(Color.parseColor(getResources().getString(R.color.colorPrimary)));
                tv6Month.setTextColor(Color.parseColor(getResources().getString(R.color.cheep_tips_color)));
                tv12Month.setTextColor(Color.parseColor(getResources().getString(R.color.cheep_tips_color)));

                tv3SaveMonth.setTextColor(Color.parseColor(getResources().getString(R.color.colorPrimary)));
                tv6SaveMonth.setTextColor(Color.parseColor(getResources().getString(R.color.colorGray)));
                tv12SaveMonth.setTextColor(Color.parseColor(getResources().getString(R.color.colorGray)));
                updatePrice(3);

                break;
            case R.id.card_6_months:
                card3Months.setCardBackgroundColor(Color.parseColor(getResources().getString(R.color.colorPrimary)));
                card6Months.setCardBackgroundColor(Color.parseColor(getResources().getString(R.color.cheep_tips_color)));
                card12Months.setCardBackgroundColor(Color.parseColor(getResources().getString(R.color.colorPrimary)));

                tv3Month.setTextColor(Color.parseColor(getResources().getString(R.color.cheep_tips_color)));
                tv6Month.setTextColor(Color.parseColor(getResources().getString(R.color.colorPrimary)));
                tv12Month.setTextColor(Color.parseColor(getResources().getString(R.color.cheep_tips_color)));

                tv3SaveMonth.setTextColor(Color.parseColor(getResources().getString(R.color.colorGray)));
                tv6SaveMonth.setTextColor(Color.parseColor(getResources().getString(R.color.colorPrimary)));
                tv12SaveMonth.setTextColor(Color.parseColor(getResources().getString(R.color.colorGray)));
                updatePrice(6);
                break;
            case R.id.card_12_months:
                card3Months.setCardBackgroundColor(Color.parseColor(getResources().getString(R.color.colorPrimary)));
                card6Months.setCardBackgroundColor(Color.parseColor(getResources().getString(R.color.colorPrimary)));
                card12Months.setCardBackgroundColor(Color.parseColor(getResources().getString(R.color.cheep_tips_color)));

                tv3Month.setTextColor(Color.parseColor(getResources().getString(R.color.cheep_tips_color)));
                tv6Month.setTextColor(Color.parseColor(getResources().getString(R.color.cheep_tips_color)));
                tv12Month.setTextColor(Color.parseColor(getResources().getString(R.color.colorPrimary)));

                tv3SaveMonth.setTextColor(Color.parseColor(getResources().getString(R.color.colorGray)));
                tv6SaveMonth.setTextColor(Color.parseColor(getResources().getString(R.color.colorGray)));
                tv12SaveMonth.setTextColor(Color.parseColor(getResources().getString(R.color.colorPrimary)));
                updatePrice(12);
                break;

        }
    }
}
