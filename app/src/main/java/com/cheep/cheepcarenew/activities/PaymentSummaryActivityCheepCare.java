package com.cheep.cheepcarenew.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.model.AddressModel;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.Utility;

public class PaymentSummaryActivityCheepCare extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PaymentSummaryActivityC";

    private CardView card3Months, card6Months, card12Months;
    private TextView tv3Month, tv6Month, tv12Month;
    private TextView tv3SaveMonth, tv6SaveMonth, tv12SaveMonth;
    private TextView tvMeanPackageAmount;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_summary_new);

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
    }

    @Override
    protected void setListeners() {
        card3Months.setOnClickListener(this);
        card6Months.setOnClickListener(this);
        card12Months.setOnClickListener(this);
    }

    private void setInitialColorOfCardView() {
        card12Months.setCardBackgroundColor(Color.parseColor("#006DDA"));
        tv12Month.setTextColor(Color.parseColor("#FFFFFF"));
        tv12SaveMonth.setTextColor(Color.parseColor("#FFFFFF"));
    }

    private void updatePrice(int howManyMonth) {
        profit = cutPrice - price;
        tvMeanPackageAmount.setText("₹" + String.valueOf(price * howManyMonth));
    }

    private void updateSaveAmountForMonth() {
        profit = cutPrice - price;
        tv3SaveMonth.setText("Save ₹" + String.valueOf(profit * 3));
        tv6SaveMonth.setText("Save ₹" + String.valueOf(profit * 6));
        tv12SaveMonth.setText("Save ₹" + String.valueOf(profit * 12));

    }

    // View.OnClickListener
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.card_3_months:
                card3Months.setCardBackgroundColor(Color.parseColor("#006DDA"));
                card6Months.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                card12Months.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                card3Months.setSelected(false);

                tv3Month.setTextColor(Color.parseColor("#FFFFFF"));
                tv6Month.setTextColor(Color.parseColor("#006DDA"));
                tv12Month.setTextColor(Color.parseColor("#006DDA"));

                tv3SaveMonth.setTextColor(Color.parseColor("#FFFFFF"));
                tv6SaveMonth.setTextColor(Color.parseColor("#5f5f5f"));
                tv12SaveMonth.setTextColor(Color.parseColor("#5f5f5f"));
                updatePrice(3);

                break;
            case R.id.card_6_months:
                card3Months.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                card6Months.setCardBackgroundColor(Color.parseColor("#006DDA"));
                card12Months.setCardBackgroundColor(Color.parseColor("#FFFFFF"));

                tv3Month.setTextColor(Color.parseColor("#006DDA"));
                tv6Month.setTextColor(Color.parseColor("#FFFFFF"));
                tv12Month.setTextColor(Color.parseColor("#006DDA"));

                tv3SaveMonth.setTextColor(Color.parseColor("#5f5f5f"));
                tv6SaveMonth.setTextColor(Color.parseColor("#FFFFFF"));
                tv12SaveMonth.setTextColor(Color.parseColor("#5f5f5f"));
                updatePrice(6);
                break;
            case R.id.card_12_months:
                card3Months.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                card6Months.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                card12Months.setCardBackgroundColor(Color.parseColor("#006DDA"));

                tv3Month.setTextColor(Color.parseColor("#006DDA"));
                tv6Month.setTextColor(Color.parseColor("#006DDA"));
                tv12Month.setTextColor(Color.parseColor("#FFFFFF"));

                tv3SaveMonth.setTextColor(Color.parseColor("#5f5f5f"));
                tv6SaveMonth.setTextColor(Color.parseColor("#5f5f5f"));
                tv12SaveMonth.setTextColor(Color.parseColor("#FFFFFF"));
                updatePrice(12);
                break;

        }
    }
}
