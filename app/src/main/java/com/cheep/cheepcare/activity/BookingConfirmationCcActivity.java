package com.cheep.cheepcare.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.adapter.SelectedSubServicePriceAdapter;
import com.cheep.databinding.ActivityBookingConfirmationCcBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;


public class BookingConfirmationCcActivity extends BaseAppCompatActivity {


    private static final String TAG = BookingConfirmationCcActivity.class.getSimpleName();
    private ActivityBookingConfirmationCcBinding mBinding;
    private ArrayList<SubServiceDetailModel> freeList;
    private ArrayList<SubServiceDetailModel> paidList;
    private AddressModel mAddressModel;
    private String startDateTime;
    private String mCarePackageId;
    private String mCategoryId;
    private String taskDes;
    //added by bhavesh 26/2/18


    public static void newInstance(Context context, String carePackageId, String catId, ArrayList<SubServiceDetailModel> freeList
            , ArrayList<SubServiceDetailModel> paidList, AddressModel addressModel, String startDateTime, String taskDes) {
        Intent intent = new Intent(context, BookingConfirmationCcActivity.class);
        intent.putExtra(Utility.Extra.SELECTED_PACKAGE_ID, carePackageId);
        intent.putExtra(Utility.Extra.CATEGORY_ID, catId);
        intent.putExtra("Utility.Extra.DATA", freeList);
        intent.putExtra("Utility.Extra.DATA_2", paidList);
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, addressModel);
        intent.putExtra(Utility.Extra.DATA_3, taskDes);
        intent.putExtra("startDateTime", startDateTime);
        context.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
          when the device runing out of memory we dont want the user to restart the payment. rather we close it and redirect them to previous activity.
         */
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_booking_confirmation_cc);
        // add event bus listener
        EventBus.getDefault().register(this);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {

        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.SELECTED_PACKAGE_ID))
            mCarePackageId = getIntent().getExtras().getString(Utility.Extra.SELECTED_PACKAGE_ID);
        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.CATEGORY_ID))
            mCategoryId = getIntent().getExtras().getString(Utility.Extra.CATEGORY_ID);
        if (getIntent().getExtras() != null && getIntent().hasExtra("Utility.Extra.DATA"))
            freeList = (ArrayList<SubServiceDetailModel>) getIntent().getExtras().getSerializable("Utility.Extra.DATA");
        if (getIntent().getExtras() != null && getIntent().hasExtra("Utility.Extra.DATA_2"))
            paidList = (ArrayList<SubServiceDetailModel>) getIntent().getExtras().getSerializable("Utility.Extra.DATA_2");
        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.SELECTED_ADDRESS_MODEL))
            mAddressModel = (AddressModel) getIntent().getSerializableExtra(Utility.Extra.SELECTED_ADDRESS_MODEL);
        if (getIntent().getExtras() != null && getIntent().hasExtra("startDateTime"))
            startDateTime = getIntent().getExtras().getString("startDateTime");
        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.DATA_3))
            taskDes = getIntent().getExtras().getString(Utility.Extra.DATA_3);

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

        // Enable Step Three Unverified state
        setTaskState(STEP_THREE_UNVERIFIED);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(getSpannableString(getString(R.string.msg_task_description), ContextCompat.getColor(this, R.color.splash_gradient_end), true));
        spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(this, R.color.grey_varient_8), false));
        spannableStringBuilder.append(getSpannableString("25th April, 1100 hrs - 1400 hrs", ContextCompat.getColor(this, R.color.splash_gradient_end), true));
        spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(this, R.color.grey_varient_8), false));
        spannableStringBuilder.append(getSpannableString("1401/1402 Ghanshyam Enclave, Lalji Pada, ICICI Bank, New Link Road, Kandivali West, Mumbai, Maharashtra", ContextCompat.getColor(this, R.color.splash_gradient_end), true));


        mBinding.tvTaskDescription.setText(spannableStringBuilder);

        if (!freeList.isEmpty()) {
            mBinding.recyclerViewFree.setAdapter(new SelectedSubServicePriceAdapter(freeList));
        } else {
            mBinding.tvFreeCc.setVisibility(View.GONE);
            mBinding.recyclerViewFree.setVisibility(View.GONE);
        }

        if (!paidList.isEmpty()) {
            mBinding.recyclerViewPaid.setAdapter(new SelectedSubServicePriceAdapter(paidList));
        } else {
            mBinding.tvPaidServices.setVisibility(View.GONE);
            mBinding.recyclerViewPaid.setVisibility(View.GONE);
        }

        mBinding.ivTermsTick.setSelected(true);

    }

    @Override
    protected void setListeners() {
        mBinding.tvBookAndPay.setOnClickListener(onPayClickListener);

    }

    View.OnClickListener onPayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            WebCallClass.createTask(mContext, mCarePackageId, mCategoryId, freeList, paidList, mAddressModel
                    , "0.00", "0.00", startDateTime, taskDes, "", NetworkUtility.PAYMENT_METHOD_TYPE.FREE);
//            TaskSummaryForMultiCatActivity.getInstance(mContext, Utility.EMPTY_STRING);
        }
    };

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Payment Detail Detail Service[Start] ////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public SpannableStringBuilder getSpannableString(String fullstring, int color, boolean isBold) {
        SpannableStringBuilder text = new SpannableStringBuilder(fullstring);
        text.setSpan(new ForegroundColorSpan(color), 0, fullstring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (isBold) {
            text.setSpan(new StyleSpan(Typeface.BOLD), 0, fullstring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }


    /**
     * =======
     * >>>>>>> PaytmApiIntegrationCheepUserAndroid:app/src/main/java/com/cheep/activity/PaymentDetailsActivity.java
     * Below would manage the state of Step while creating task creation
     */
    public static final int STEP_THREE_UNVERIFIED = 8;
    public static final int STEP_THREE_VERIFIED = 9;
    public int mCurrentStep = -1;

    public void setTaskState(int step_state) {
        mCurrentStep = step_state;
        switch (step_state) {

            case STEP_THREE_UNVERIFIED:
                mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                break;
            case STEP_THREE_VERIFIED:
                mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();

    }


    // check is task is from insta booking or not

//    Blue Heart Emoji (U+1F499) - iEmoji.com
}
