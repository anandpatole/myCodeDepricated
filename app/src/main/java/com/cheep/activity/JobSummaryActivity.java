package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BootstrapConstant;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.adapter.CoverViewPagerAdapter;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.databinding.ActivityJobSummaryBinding;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.model.CoverImageModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.HotlineHelper;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SharedElementTransitionHelper;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by pankaj on 10/5/16.
 */
public class JobSummaryActivity extends BaseAppCompatActivity {

    private static final String TAG = "JobSummaryActivity";

    private ActivityJobSummaryBinding mActivityJobSummaryBinding;
    private TaskDetailModel taskDetailModel;
    private ProviderModel providerModel;
    private String actualQuotePrice;

    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel) {
        Intent intent = new Intent(context, JobSummaryActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(providerModel));
        context.startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent() called with: intent = [" + intent + "]");
        //Checking if user is not registered then directly finish this activity, this is case when user comes from notification.
        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            finish();
            return;
        }
        mActivityJobSummaryBinding = DataBindingUtil.setContentView(this, R.layout.activity_job_summary);
        initiateUI();
        setListeners();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onNewIntent(getIntent());
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStart() {
        super.onStart();


    }


    @Override
    protected void onStop() {
        super.onStop();

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_STATUS_CHANGE) {
            taskDetailModel.taskStatus = event.taskStatus;
            fillProviderDetails(providerModel);
        } else if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.ADDITIONAL_PAYMENT_REQUESTED) {
            taskDetailModel.taskStatus = event.taskStatus;
            taskDetailModel.additionalQuoteAmount = event.additional_quote_amount;
            fillProviderDetails(providerModel);
        }
    }

    @Override
    protected void initiateUI() {
        setSupportActionBar(mActivityJobSummaryBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), TaskDetailModel.class);
        providerModel = (ProviderModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), ProviderModel.class);

        mActivityJobSummaryBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mActivityJobSummaryBinding.textTitle.setText(getString(R.string.label_summary));

        fillProviderDetails(providerModel);

        //Checking if address is null means it is comes from notification
        if (TextUtils.isEmpty(taskDetailModel.taskAddress)) {
            showProgressDialog();
        }
        callTaskDetailWS();
    }

    private BottomAlertDialog dialogDesc;
    private TextView txtMessage;

    private void showFullDesc(String title, String message) {
        if (dialogDesc == null) {
            View view = View.inflate(mContext,R.layout.dialog_information, null);
            txtMessage = (TextView) view.findViewById(R.id.text_message);
            dialogDesc = new BottomAlertDialog(mContext);

            view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDesc.dismiss();
                }
            });

            dialogDesc.setTitle(title);
            dialogDesc.setCustomView(view);
        }
        txtMessage.setText(message);
        dialogDesc.showDialog();
    }

    View.OnClickListener onPayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            payNow(false);
        }
    };

    private String cheepCode;
    View.OnClickListener onCheepCodeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view.getId() == mActivityJobSummaryBinding.layoutCheepCode.getId()) {
                //If there is no cheep code then show dialog else clear cheepcode
                if (TextUtils.isEmpty(cheepCode)) {
                    showCheepCodeDialog();
                }
            } else if (view.getId() == mActivityJobSummaryBinding.imgCheepCodeClose.getId()) {
                cheepCode = null;
                if (TextUtils.isEmpty(actualQuotePrice) == false) {
                    providerModel.quotePrice = actualQuotePrice;
                }
                actualQuotePrice = null;
                mActivityJobSummaryBinding.textCheepCodeTitle.setText(getString(R.string.label_have_cheep_code));
                mActivityJobSummaryBinding.imgCheepCodeClose.setVisibility(View.GONE);
                mActivityJobSummaryBinding.btnPay.setText(getString(R.string.label_pay_X, providerModel.quotePrice));
            }
        }
    };

    EditText edtCheepcode;
    BottomAlertDialog cheepCodeDialog;

    private void showCheepCodeDialog() {

        View view = View.inflate(mContext,R.layout.dialog_add_promocode, null);
        edtCheepcode = (EditText) view.findViewById(R.id.edit_cheepcode);
        cheepCodeDialog = new BottomAlertDialog(mContext);
        view.findViewById(R.id.btn_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtCheepcode.getText().toString())) {
                    Utility.showToast(mContext, getString(R.string.validate_cheepcode));
                    return;
                }
                validateCheepCode(edtCheepcode.getText().toString());
            }
        });
        cheepCodeDialog.setTitle(getString(R.string.label_cheepcode));
        cheepCodeDialog.setCustomView(view);
        cheepCodeDialog.showDialog();
    }

    /**
     * Updating payemnt btn text to show the discounted price and payable amount
     */
    private void updatePaymentBtn(String total, String discount, String payable) {
        // setting payable amount as quote price to pay.
        providerModel.quotePrice = payable;
//        mActivityJobSummaryBinding.btnPay.setText(getString(R.string.label_pay_X_X_X, total, discount, payable));
//        @change only need to show payable amount
        mActivityJobSummaryBinding.btnPay.setText(getString(R.string.label_pay_X, payable));
    }

    /**
     * Used for payment
     */
    private void validateCheepCode(String cheepCode) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityJobSummaryBinding.getRoot());
            return;
        }

        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        actualQuotePrice = providerModel.quotePrice;


        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.quotePrice);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);
        mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.VALIDATE_CHEEP_CODE
                , mCallValidateCheepCodeWSErrorListener
                , mCallValidateCheepCodeWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }


    Response.Listener mCallValidateCheepCodeWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        if (edtCheepcode != null) {
                            cheepCode = edtCheepcode.getText().toString().trim();
                            mActivityJobSummaryBinding.textCheepCodeTitle.setText(getString(R.string.label_cheep_code_applied, cheepCode));
                            mActivityJobSummaryBinding.imgCheepCodeClose.setVisibility(View.VISIBLE);
                            cheepCodeDialog.dismiss();

                            String total = jsonObject.optString(NetworkUtility.TAGS.QUOTE_AMOUNT);
                            String discount = jsonObject.optString(NetworkUtility.TAGS.DISCOUNT_AMOUNT);
                            String payable = jsonObject.optString(NetworkUtility.TAGS.PAYABLE_AMOUNT);
                            updatePaymentBtn(total, discount, payable);
                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mActivityJobSummaryBinding.getRoot());
                        Utility.showToast(mContext, error_message);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallPaymentWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    Response.ErrorListener mCallValidateCheepCodeWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            hideProgressDialog();

//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));

        }
    };

    /**
     * Used for payment
     */
    private void payNow(boolean isForAdditionalQuote) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityJobSummaryBinding.getRoot());
            return;
        }

        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Params
        Map<String, Object> mParams;// = new HashMap<String, Object>();
        mParams = getPaymentTransactionFields(userDetails, isForAdditionalQuote);
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);

        if (!TextUtils.isEmpty(cheepCode))
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
        else
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);


        //Create Asynctask that will do the encryption and afterwords call webservice
        AsyncFetchEnryptedString asyncFetchEnryptedString = new AsyncFetchEnryptedString(isForAdditionalQuote);
        asyncFetchEnryptedString.execute(new JSONObject(mParams).toString());

//        String encryptedText = Utility.encryptUsingRNCryptorNative(new JSONObject(mParams).toString());


    }

    Response.Listener mCallPaymentWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        //TODO: Remove this when release and it is saving cc detail in clipboard only
                        if ("debug".equalsIgnoreCase(BuildConfig.BUILD_TYPE)) {
                            //Copy dummy creditcard detail in clipboard
                            try {
                                Utility.setClipboard(mContext, BootstrapConstant.CC_DETAILS);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        Intent intent = new Intent(JobSummaryActivity.this, PaymentsActivity.class);
                        intent.putExtra("url", BuildConfig.PAYUBIZ_HDFC_URL);
                        intent.putExtra("postData", postData.replaceAll("hash=", "hash=" + jsonObject.optString("hash_string")));

                        if (jsonObject.getString(NetworkUtility.TAGS.IS_FOR_ADDITIONAL_QUOTE).equalsIgnoreCase(getString(R.string.label_yes))) {
                            startActivityForResult(intent, Utility.ADDITIONAL_REQUEST_START_PAYMENT);
                        } else {
                            startActivityForResult(intent, Utility.REQUEST_START_PAYMENT);
                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityJobSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallPaymentWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    Response.ErrorListener mCallPaymentWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            hideProgressDialog();

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.REQUEST_START_PAYMENT) {
//            Toast.makeText(mContext, "OnActivityResult called with resultCode:" + resultCode + ", requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
            if (resultCode == RESULT_OK) {
                //success
                if (data != null) {
                    Log.d(TAG, "onActivityResult() called with success: result= [" + data.getStringExtra("result") + "]");
                    //Call update payment service from here with all the response come from service
                    updatePaymentStatus(true, data.getStringExtra("result"), false);
                }
            }
            if (resultCode == RESULT_CANCELED) {
                //failed
                if (data != null) {
                    Log.d(TAG, "onActivityResult() called with failed: result= [" + data.getStringExtra("result") + "]");
                    //Call update payment service from here with all the response come from service
                    updatePaymentStatus(false, data.getStringExtra("result"), false);
                    Utility.showSnackBar(getString(R.string.msg_payment_failed), mActivityJobSummaryBinding.getRoot());
                }
            }
        } else if (requestCode == Utility.ADDITIONAL_REQUEST_START_PAYMENT) {
//            Toast.makeText(mContext, "OnActivityResult called with resultCode:" + resultCode + ", requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
            if (resultCode == RESULT_OK) {
                //success
                if (data != null) {
                    Log.d(TAG, "onActivityResult() called with success: result= [" + data.getStringExtra("result") + "]");
                    //Call update payment service from here with all the response come from service
                    updatePaymentStatus(true, data.getStringExtra("result"), true);
                }
            }
            if (resultCode == RESULT_CANCELED) {
                //failed
                if (data != null) {
                    Log.d(TAG, "onActivityResult() called with failed: result= [" + data.getStringExtra("result") + "]");
                    //Call update payment service from here with all the response come from service
                    updatePaymentStatus(false, data.getStringExtra("result"), true);
                    Utility.showSnackBar(getString(R.string.msg_payment_failed), mActivityJobSummaryBinding.getRoot());
                }
            }
        }
    }

    String postData;
    String transaction_Id;

    private Map<String, Object> getPaymentTransactionFields(UserDetails userDetails, boolean isForAdditionalQuote) {

        Map<String, Object> mParams = new HashMap<>();

        transaction_Id = System.currentTimeMillis() + "";
        mParams.put("key", BuildConfig.PAYUBIZ_HDFC_KEY);
        mParams.put("amount", isForAdditionalQuote ? taskDetailModel.additionalQuoteAmount : providerModel.quotePrice);
        mParams.put("txnid", transaction_Id);
        mParams.put("email", userDetails.Email);
        mParams.put("productinfo", taskDetailModel.taskId);
        mParams.put("firstname", userDetails.UserName);
        mParams.put("udf1", "Task Start Date : " + taskDetailModel.taskStartdate);
        mParams.put("udf2", "Provider Id : " + providerModel.providerId);
        mParams.put("udf3", NetworkUtility.TAGS.PLATFORMTYPE.ANDROID);
        mParams.put("udf4", isForAdditionalQuote ? Utility.TASK_STATUS.ADDITIONAL_PAYMENT_REQUESTED : "");
        mParams.put("udf5", "");
        mParams.put("user_credentials", BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.Email);


        return mParams;
    }

    private String getPaymentUrl(UserDetails userDetails, boolean isAdditionalPayment) {

        postData = "&txnid=" + transaction_Id +
                "&device_type=1" +
                "&ismobileview=1" +
                "&productinfo=" + taskDetailModel.taskId +
                "&user_credentials=" + userDetails.Email +
                "&key=" + BuildConfig.PAYUBIZ_HDFC_KEY +
                "&instrument_type=" + PreferenceUtility.getInstance(mContext).getFCMRegID() +
                "&surl=" + BuildConfig.PAYUBIZ_SUCCESS_URL +
                "&furl=" + BuildConfig.PAYUBIZ_FAIL_URL + "" +
                "&instrument_id=7dd17561243c202" +
                "&firstname=" + userDetails.UserName +
                "&email=" + userDetails.Email +
                "&phone=" + userDetails.PhoneNumber +
                "&amount=" + (isAdditionalPayment ? taskDetailModel.additionalQuoteAmount : providerModel.quotePrice) +
//                "&bankcode=PAYUW" + //for PayU Money
//                "&pg=WALLET"+//for PayU Money
                "&udf1=Task Start Date : " + taskDetailModel.taskStartdate +
                "&udf2=Provider Id : " + providerModel.providerId +
                "&udf3=" + NetworkUtility.TAGS.PLATFORMTYPE.ANDROID +
                "&udf4=" + (isAdditionalPayment ? Utility.TASK_STATUS.ADDITIONAL_PAYMENT_REQUESTED : "") +
                "&udf5=" +
                "&hash=";

        return postData;
    }


    private void fillProviderDetails(ProviderModel providerModel) {
        this.providerModel = providerModel;

        /*
          Changes on 6thFeb2017, @Bhavesh
          In case PRO doesn't set any Image-URLs, we will display the default Profile PIC.
         */
        Utility.showCircularImageView(mContext, TAG, mActivityJobSummaryBinding.imgProfile, providerModel.profileUrl, Utility.DEFAULT_CHEEP_LOGO, true);
//        if (!TextUtils.isEmpty(providerModel.profileUrl)) {
//            //loading rounded image on profile
//            Utility.showCircularImageView(mContext, TAG, mActivityJobSummaryBinding.imgProfile, providerModel.profileUrl, Utility.DEFAULT_PROFILE_SRC, true);
//            mActivityJobSummaryBinding.imgProfile.setVisibility(View.VISIBLE);
//        } else {
//            mActivityJobSummaryBinding.imgProfile.setVisibility(View.GONE);
//        }

        //Loading Task Details Fields
        /*
          Setting dynamic fields based on current status of task(Job)
         */
        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
        try {
            superCalendar.setTimeInMillis(Long.parseLong(taskDetailModel.taskStartdate));
        } catch (Exception e) {
            e.printStackTrace();
        }
        superCalendar.setLocaleTimeZone();
        String task_original_date_time = superCalendar.format(Utility.DATE_FORMAT_DD_MMM + " " + Utility.DATE_FORMAT_HH_MM_AM);
        mActivityJobSummaryBinding.textWhen.setText(task_original_date_time);

        mActivityJobSummaryBinding.textTaskDesc.setText(taskDetailModel.taskDesc);
        mActivityJobSummaryBinding.textWhere.setText(taskDetailModel.taskAddress);

        mActivityJobSummaryBinding.textTaskDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFullDesc(getString(R.string.label_desc), mActivityJobSummaryBinding.textTaskDesc.getText().toString());
            }
        });
        mActivityJobSummaryBinding.textWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFullDesc(getString(R.string.label_address), mActivityJobSummaryBinding.textWhere.getText().toString());
            }
        });

        mActivityJobSummaryBinding.iconAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(taskDetailModel.taskImage)) {
                    SharedElementTransitionHelper sharedElementTransitionHelper = new SharedElementTransitionHelper(JobSummaryActivity.this);
                    sharedElementTransitionHelper.put(mActivityJobSummaryBinding.iconAttachment, R.string.transition_image_view);
                    ZoomImageActivity.newInstance(mContext, sharedElementTransitionHelper.getBundle(), taskDetailModel.taskImage);
                }
            }
        });

        Utility.loadImageView(mContext, mActivityJobSummaryBinding.iconAttachment, taskDetailModel.taskImage, 0);

        //Loading fields
        mActivityJobSummaryBinding.textName.setText(providerModel.userName);

        mActivityJobSummaryBinding.textConfirmText.setText(getString(R.string.label_complete_job_confirm, providerModel.userName));
        mActivityJobSummaryBinding.textMinToArrive.setText(providerModel.distance);
        //Setting like "verifed | 4 jobs text"
        mActivityJobSummaryBinding.textVerifiedTotalJobs.setText((Utility.BOOLEAN.YES.equalsIgnoreCase(providerModel.isVerified) ? getString(R.string.label_verified) : getString(R.string.label_pending))
                + " | "
                + Utility.getJobs(mContext, providerModel.jobsCount)
        );

        //Checking with request type is quote is required then "PAY" title is there else "PAID" title is there
        if (Utility.REQUEST_TYPE.DETAIL_REQUIRED.equalsIgnoreCase(providerModel.requestType)) {
            mActivityJobSummaryBinding.textPayPaid.setText(getString(R.string.label_book));
        } else {
            mActivityJobSummaryBinding.textPayPaid.setText(getString(R.string.label_paid));
        }

        /*mActivityJobSummaryBinding.dividerHorizontal2.setVisibility(View.GONE);
        mActivityJobSummaryBinding.dividerHorizontal3.setVisibility(View.GONE);
        mActivityJobSummaryBinding.dividerHorizontal4.setVisibility(View.GONE);
        mActivityJobSummaryBinding.dividerHorizontal5.setVisibility(View.VISIBLE);*/

        mActivityJobSummaryBinding.textMinToArrive.setText(providerModel.distance);
        mActivityJobSummaryBinding.textPrice.setSelected(true);
        mActivityJobSummaryBinding.textPrice.setText(getString(R.string.rupee_symbol_x, Utility.getActualPrice(taskDetailModel.taskPaidAmount, providerModel.quotePrice)));
        mActivityJobSummaryBinding.textThanksForChoosing.setText(getString(R.string.label_thanks_for_choosing_x_summary, providerModel.userName));

        mActivityJobSummaryBinding.imgFav.setSelected(Utility.BOOLEAN.YES.equals(providerModel.isFavourite));

        // Enable Chat Call as we would anyway would going to show this
        // Enable  Chat Call based on status
        enableChatCall(true);

        // Updating field and views for status wise, (layoutActionBar is top layout where distance, fav, and pay button is there)
        if (Utility.TASK_STATUS.PENDING.equalsIgnoreCase(taskDetailModel.taskStatus)) {
            mActivityJobSummaryBinding.layoutActionBar.setVisibility(View.GONE);

            // Setting below status bars according to status
            /*mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatus.setText(getString(R.string.task_status_pending));
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);*/

            // Change on 5thApril 2017
            mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutStatusProcessBlueStrip.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatusBlueStrip.setText(getString(R.string.task_status_pending));
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);

            if (providerModel != null && Utility.REQUEST_TYPE.QUOTE_REQUESTED.equalsIgnoreCase(providerModel.requestType)) {
                // Setting below status bars according to status
                mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.GONE);
                // @change on 5thApril 2017
                mActivityJobSummaryBinding.layoutStatusProcessBlueStrip.setVisibility(View.GONE);
                mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);
            }

            mActivityJobSummaryBinding.divider1.setVisibility(View.GONE);
            mActivityJobSummaryBinding.divider2.setVisibility(View.GONE);
            mActivityJobSummaryBinding.btnPay.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.btnPay.setText(getString(R.string.label_pay_X, providerModel.quotePrice));
            mActivityJobSummaryBinding.btnPay.setOnClickListener(onPayClickListener);
            mActivityJobSummaryBinding.textPayPaid.setText(getString(R.string.label_book));

            //cheepcode
            mActivityJobSummaryBinding.dividerPromoCode.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.layoutCheepCode.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.layoutCheepCode.setOnClickListener(onCheepCodeClickListener);
            mActivityJobSummaryBinding.imgCheepCodeClose.setOnClickListener(onCheepCodeClickListener);

            // Display the task desclaimer
            mActivityJobSummaryBinding.textTaskDesclaimer.setVisibility(View.VISIBLE);

            //Hide Additional Payment request module
            mActivityJobSummaryBinding.lnAcceptRejectAdditionalQuote.setVisibility(View.GONE);

        } else if (Utility.TASK_STATUS.PROCESSING.equalsIgnoreCase(taskDetailModel.taskStatus)) {
            mActivityJobSummaryBinding.layoutActionBar.setVisibility(View.VISIBLE);

            //Setting below status bars according to status
            /*mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatus.setText(getString(R.string.task_status_processing));
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);*/
            // @change on 5thApril2017
            mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutStatusProcessBlueStrip.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatusBlueStrip.setText(getString(R.string.task_status_processing));
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);


            mActivityJobSummaryBinding.divider1.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.divider2.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.btnPay.setVisibility(View.GONE);
            ((View) mActivityJobSummaryBinding.textPrice.getParent()).setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textPayPaid.setText(getString(R.string.label_paid));

            //cheepcode
            mActivityJobSummaryBinding.dividerPromoCode.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutCheepCode.setVisibility(View.GONE);

            // Hide the task desclaimer
            mActivityJobSummaryBinding.textTaskDesclaimer.setVisibility(View.GONE);

            //Hide Additional Payment request module
            mActivityJobSummaryBinding.lnAcceptRejectAdditionalQuote.setVisibility(View.GONE);

        } else if (Utility.TASK_STATUS.COMPLETION_REQUEST.equalsIgnoreCase(taskDetailModel.taskStatus)) {
            mActivityJobSummaryBinding.layoutActionBar.setVisibility(View.VISIBLE);

            //Setting below status bars according to status
            mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.GONE);
            // @change at 5thApril2017
            mActivityJobSummaryBinding.layoutStatusProcessBlueStrip.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.VISIBLE);

            mActivityJobSummaryBinding.divider1.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.divider2.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.btnPay.setVisibility(View.GONE);
            ((View) mActivityJobSummaryBinding.textPrice.getParent()).setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textPayPaid.setText(getString(R.string.label_paid));
            //cheepcode
            mActivityJobSummaryBinding.dividerPromoCode.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutCheepCode.setVisibility(View.GONE);

            // Hide the task desclaimer
            mActivityJobSummaryBinding.textTaskDesclaimer.setVisibility(View.GONE);

            //Hide Additional Payment request module
            mActivityJobSummaryBinding.lnAcceptRejectAdditionalQuote.setVisibility(View.GONE);


        } else if (Utility.TASK_STATUS.COMPLETION_CONFIRM.equalsIgnoreCase(taskDetailModel.taskStatus)) {
            mActivityJobSummaryBinding.layoutActionBar.setVisibility(View.VISIBLE);

            /*//Setting below status bars according to status
            mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatus.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatus.setText(getString(R.string.task_completion_confirm));
            if (Utility.BOOLEAN.YES.equalsIgnoreCase(taskDetailModel.ratingDone)) {
                Utility.showRating(taskDetailModel.taskRatings, mActivityJobSummaryBinding.ratingBar);
                mActivityJobSummaryBinding.ratingBar.setVisibility(View.VISIBLE);
            } else {
                mActivityJobSummaryBinding.ratingBar.setVisibility(View.GONE);
            }
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);*/


//            @changes on 5thApril 2017
            //Setting below status bars according to status
            mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutStatusProcessBlueStrip.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatusBlueStrip.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatusBlueStrip.setText(getString(R.string.task_completion_confirm));
            if (Utility.BOOLEAN.YES.equalsIgnoreCase(taskDetailModel.ratingDone)) {
                Utility.showRating(taskDetailModel.taskRatings, mActivityJobSummaryBinding.ratingBarBlueStrip);
                mActivityJobSummaryBinding.ratingBarBlueStrip.setVisibility(View.VISIBLE);
            } else {
                mActivityJobSummaryBinding.ratingBarBlueStrip.setVisibility(View.GONE);
            }
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);


            mActivityJobSummaryBinding.divider1.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.divider2.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.btnPay.setVisibility(View.GONE);
            ((View) mActivityJobSummaryBinding.textPrice.getParent()).setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textPayPaid.setText(getString(R.string.label_paid));
            //cheepcode
            mActivityJobSummaryBinding.dividerPromoCode.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutCheepCode.setVisibility(View.GONE);

            // Hide the task desclaimer
            mActivityJobSummaryBinding.textTaskDesclaimer.setVisibility(View.GONE);

            //Hide Additional Payment request module
            mActivityJobSummaryBinding.lnAcceptRejectAdditionalQuote.setVisibility(View.GONE);

            // Hide the chat call for this as its not required, task is completed by User
            enableChatCall(false);
        }

//      PENDING  asdasdasd
        else if (Utility.TASK_STATUS.PAID.equalsIgnoreCase(taskDetailModel.taskStatus)) {
            mActivityJobSummaryBinding.layoutActionBar.setVisibility(View.VISIBLE);

            //Setting below status bars according to status
           /* mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatus.setText(getString(R.string.task_status_paid));
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);*/

            // @change at 5thApril2017
            mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutStatusProcessBlueStrip.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatusBlueStrip.setText(getString(R.string.task_status_paid));
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);


            mActivityJobSummaryBinding.divider1.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.divider2.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.btnPay.setVisibility(View.GONE);
            ((View) mActivityJobSummaryBinding.textPrice.getParent()).setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textPayPaid.setText(getString(R.string.label_paid));
            //cheepcode
            mActivityJobSummaryBinding.dividerPromoCode.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutCheepCode.setVisibility(View.GONE);

            // Hide the task desclaimer
            mActivityJobSummaryBinding.textTaskDesclaimer.setVisibility(View.GONE);

            //Hide Additional Payment request module
            mActivityJobSummaryBinding.lnAcceptRejectAdditionalQuote.setVisibility(View.GONE);
        } else if (Utility.TASK_STATUS.CANCELLED_CUSTOMER.equalsIgnoreCase(taskDetailModel.taskStatus)) {
            mActivityJobSummaryBinding.layoutActionBar.setVisibility(View.VISIBLE);

            //Setting below status bars according to status
           /* mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatus.setText(getString(R.string.task_was_cancelled));
            mActivityJobSummaryBinding.textStatus.setTextColor(ContextCompat.getColor(mContext, R.color.red));
            if (!TextUtils.isEmpty(taskDetailModel.taskCancelReason))
                mActivityJobSummaryBinding.textReason.setText(getString(R.string.label_quote_x_quote, taskDetailModel.taskCancelReason));
            mActivityJobSummaryBinding.textReason.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);*/

            // @change at 5thApril 2017
            mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutStatusProcessBlueStrip.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatusBlueStrip.setText(getString(R.string.task_was_cancelled));
            mActivityJobSummaryBinding.textStatusBlueStrip.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            if (!TextUtils.isEmpty(taskDetailModel.taskCancelReason))
                mActivityJobSummaryBinding.textReasonBlueStrip.setText(getString(R.string.label_quote_x_quote, taskDetailModel.taskCancelReason));
            mActivityJobSummaryBinding.textReasonBlueStrip.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);


            mActivityJobSummaryBinding.divider1.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.divider2.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.btnPay.setVisibility(View.GONE);
            ((View) mActivityJobSummaryBinding.textPrice.getParent()).setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textPayPaid.setText(getString(R.string.label_paid));
            //cheepcode
            mActivityJobSummaryBinding.dividerPromoCode.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutCheepCode.setVisibility(View.GONE);

            // Hide the task desclaimer
            mActivityJobSummaryBinding.textTaskDesclaimer.setVisibility(View.GONE);

            //Hide Additional Payment request module
            mActivityJobSummaryBinding.lnAcceptRejectAdditionalQuote.setVisibility(View.GONE);

            // Hide the chat call for this as its not required, task is completed by User
            enableChatCall(false);
        } else if (Utility.TASK_STATUS.CANCELLED_SP.equalsIgnoreCase(taskDetailModel.taskStatus)) {
            mActivityJobSummaryBinding.layoutActionBar.setVisibility(View.VISIBLE);

            //Setting below status bars according to status
            /*mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatus.setText(getString(R.string.task_was_cancelled_by_x, providerModel.userName));
            mActivityJobSummaryBinding.textStatus.setTextColor(ContextCompat.getColor(mContext, R.color.red));
            if (!TextUtils.isEmpty(taskDetailModel.taskCancelReason))
                mActivityJobSummaryBinding.textReason.setText(getString(R.string.label_quote_x_quote, taskDetailModel.taskCancelReason));
            mActivityJobSummaryBinding.textReason.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);*/

            // @change at 5thApril 2017
            mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutStatusProcessBlueStrip.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatusBlueStrip.setText(getString(R.string.task_was_cancelled_by_x, providerModel.userName));
            mActivityJobSummaryBinding.textStatusBlueStrip.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            if (!TextUtils.isEmpty(taskDetailModel.taskCancelReason))
                mActivityJobSummaryBinding.textReasonBlueStrip.setText(getString(R.string.label_quote_x_quote, taskDetailModel.taskCancelReason));
            mActivityJobSummaryBinding.textReasonBlueStrip.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);


            mActivityJobSummaryBinding.divider1.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.divider2.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.btnPay.setVisibility(View.GONE);
            ((View) mActivityJobSummaryBinding.textPrice.getParent()).setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textPayPaid.setText(getString(R.string.label_paid));
            //cheepcode
            mActivityJobSummaryBinding.dividerPromoCode.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutCheepCode.setVisibility(View.GONE);

            // Hide the task desclaimer
            mActivityJobSummaryBinding.textTaskDesclaimer.setVisibility(View.GONE);

            //Hide Additional Payment request module
            mActivityJobSummaryBinding.lnAcceptRejectAdditionalQuote.setVisibility(View.GONE);

            // Hide the chat call for this as its not required, task is completed by User
            enableChatCall(false);
        }
        //reschedule task status
        else if (Utility.TASK_STATUS.RESCHEDULE_REQUESTED.equalsIgnoreCase(taskDetailModel.taskStatus)) {
            mActivityJobSummaryBinding.layoutActionBar.setVisibility(View.VISIBLE);

            //Setting below status bars according to status
            mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textStatus.setText(getString(R.string.task_is_reschedule_by_you));
            mActivityJobSummaryBinding.textStatus.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
            // @change at 5thApril 2017
            mActivityJobSummaryBinding.layoutStatusProcessBlueStrip.setVisibility(View.GONE);

            //Calculate Reschedule Date & Time
            superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            try {
                superCalendar.setTimeInMillis(Long.parseLong(taskDetailModel.taskRescheduleDateTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
            superCalendar.setLocaleTimeZone();
            String task_reschedule_date_time = superCalendar.format(Utility.DATE_FORMAT_DD_MMM + " " + Utility.DATE_FORMAT_HH_MM_AM);

//            String message = getString(R.string.label_reschedule_desc, providerModel.userName, task_reschedule_date_time);
            String message = getString(R.string.label_reschedule_desc, task_reschedule_date_time);
            Spannable spannable = new SpannableString(message);
            int startIndex = message.indexOf(task_reschedule_date_time);
            int endIndex = startIndex + task_reschedule_date_time.length();
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.splash_gradient_end)), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mActivityJobSummaryBinding.textReason.setText(spannable);
            mActivityJobSummaryBinding.textReason.setLineSpacing(1.0f, 1.1f);
            mActivityJobSummaryBinding.textReason.setVisibility(View.VISIBLE);

            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);

            mActivityJobSummaryBinding.divider1.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.divider2.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.btnPay.setVisibility(View.GONE);
            ((View) mActivityJobSummaryBinding.textPrice.getParent()).setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textPayPaid.setText(getString(R.string.label_paid));
            //cheepcode
            mActivityJobSummaryBinding.dividerPromoCode.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutCheepCode.setVisibility(View.GONE);

            // Hide the task desclaimer
            mActivityJobSummaryBinding.textTaskDesclaimer.setVisibility(View.GONE);

            //Hide Additional Payment request module
            mActivityJobSummaryBinding.lnAcceptRejectAdditionalQuote.setVisibility(View.GONE);
        }
        //Task's Reschedule request got cancelled
        else if (Utility.TASK_STATUS.RESCHEDULE_REQUEST_REJECTED.equalsIgnoreCase(taskDetailModel.taskStatus)) {
            mActivityJobSummaryBinding.layoutActionBar.setVisibility(View.VISIBLE);

            //Setting below status bars according to status
            mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.VISIBLE);
//            mActivityJobSummaryBinding.textStatus.setText(getString(R.string.task_is_reschedule_by_you));
//            mActivityJobSummaryBinding.textStatus.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
            mActivityJobSummaryBinding.textStatus.setVisibility(View.GONE);

//            @change at 5thApril 2017
            mActivityJobSummaryBinding.layoutStatusProcessBlueStrip.setVisibility(View.GONE);

            //Calculate Reschedule Date & Time
            /*superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            try {
                superCalendar.setTimeInMillis(Long.parseLong(taskDetailModel.taskRescheduleDateTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
            superCalendar.setLocaleTimeZone();
            String task_reschedule_date_time = superCalendar.format(Utility.DATE_FORMAT_DD_MMM + " " + Utility.DATE_FORMAT_HH_MM_AM);
            String message = getString(R.string.label_reschedule_rejection_desc
                    , PreferenceUtility.getInstance(mContext).getUserDetails().UserName
                    , providerModel.userName
                    , task_reschedule_date_time);
            Spannable spannable = new SpannableString(message);
            int startIndex = message.indexOf(task_reschedule_date_time);
            int endIndex = startIndex + task_reschedule_date_time.length();
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.splash_gradient_end)), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mActivityJobSummaryBinding.textReason.setText(spannable);
            mActivityJobSummaryBinding.textReason.setLineSpacing(1.0f, 1.1f);
            mActivityJobSummaryBinding.textReason.setVisibility(View.VISIBLE);*/

            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);

            mActivityJobSummaryBinding.divider1.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.divider2.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.btnPay.setVisibility(View.GONE);
            ((View) mActivityJobSummaryBinding.textPrice.getParent()).setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textPayPaid.setText(getString(R.string.label_paid));
            //cheepcode
            mActivityJobSummaryBinding.dividerPromoCode.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutCheepCode.setVisibility(View.GONE);

            // Hide the task desclaimer
            mActivityJobSummaryBinding.textTaskDesclaimer.setVisibility(View.GONE);

            //Enable Contact Cheep Description
            mActivityJobSummaryBinding.lnContactCheep.setVisibility(View.VISIBLE);

            mActivityJobSummaryBinding.btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.initiateCallToCheepHelpLine(mContext);
                    //callToCheepAdmin(mActivityJobSummaryBinding.getRoot());
                }
            });

            mActivityJobSummaryBinding.btnChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HotlineHelper.getInstance(mContext).showConversation(mContext);
                }
            });

            //Hide Additional Payment request module
            mActivityJobSummaryBinding.lnAcceptRejectAdditionalQuote.setVisibility(View.GONE);

        }  //Task's Additional Payment Request comes
        else if (Utility.TASK_STATUS.ADDITIONAL_PAYMENT_REQUESTED.equalsIgnoreCase(taskDetailModel.taskStatus)) {
            mActivityJobSummaryBinding.layoutActionBar.setVisibility(View.VISIBLE);

            //Setting below status bars according to status
            mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.GONE);

//            @change at 5thApril 2017
            //Setting below status bars according to status
            mActivityJobSummaryBinding.layoutStatusProcessBlueStrip.setVisibility(View.GONE);

            // Hide Layout status confirmation
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);

            mActivityJobSummaryBinding.divider1.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.divider2.setVisibility(View.VISIBLE);

            mActivityJobSummaryBinding.btnPay.setVisibility(View.GONE);
            ((View) mActivityJobSummaryBinding.textPrice.getParent()).setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.textPayPaid.setText(getString(R.string.label_paid));

            //cheepcode
            mActivityJobSummaryBinding.dividerPromoCode.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutCheepCode.setVisibility(View.GONE);

            // Hide the task desclaimer
            mActivityJobSummaryBinding.textTaskDesclaimer.setVisibility(View.GONE);

            //Enable HideContact Cheep Description
            mActivityJobSummaryBinding.lnContactCheep.setVisibility(View.GONE);

            //Enable Additional Payment request module
            mActivityJobSummaryBinding.lnAcceptRejectAdditionalQuote.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.tvAdditionalPaymentDesc.setText(getString(R.string.desc_additional_payment_requsted_by_sp, providerModel.userName));
            mActivityJobSummaryBinding.textAdditionalPaymentQuotePrice.setText(getString(R.string.rupee_symbol_x_space, taskDetailModel.additionalQuoteAmount));
            mActivityJobSummaryBinding.tvAdditionalQuoteDesclaimer.setVisibility(View.VISIBLE);

            mActivityJobSummaryBinding.btnAcceptAdditionalRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: Accept Additional Payment");

                    // First Call Asynctask that would going to check whether current status of Progressing or not.
                    callCheckingTaskStatus();
//                    payNow(true);
                }
            });

            mActivityJobSummaryBinding.btnDeclineAdditionalRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: Decline Additional Payment");
                    callDeclineAdditionalPaymentRequest();
                }
            });

        } else {
            mActivityJobSummaryBinding.layoutActionBar.setVisibility(View.VISIBLE);

            //Setting below status bars according to status
            mActivityJobSummaryBinding.layoutStatusProcess.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);
            // @ change at 5thApril 2017
            mActivityJobSummaryBinding.layoutStatusProcessBlueStrip.setVisibility(View.GONE);


            mActivityJobSummaryBinding.divider1.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.divider2.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.btnPay.setVisibility(View.GONE);
            ((View) mActivityJobSummaryBinding.textPrice.getParent()).setVisibility(View.GONE);

            //cheepcode
            mActivityJobSummaryBinding.dividerPromoCode.setVisibility(View.GONE);
            mActivityJobSummaryBinding.layoutCheepCode.setVisibility(View.GONE);

            // Hide the task desclaimer
            mActivityJobSummaryBinding.textTaskDesclaimer.setVisibility(View.GONE);

            //Hide Additional Payment request module
            mActivityJobSummaryBinding.lnAcceptRejectAdditionalQuote.setVisibility(View.GONE);
        }
    }

    private void setupCoverViewPager(ArrayList<CoverImageModel> mBannerListModels) {
        Log.d(TAG, "setupCoverViewPager() called with: mBannerListModels = [" + mBannerListModels.size() + "]");
        CoverViewPagerAdapter coverViewPagerAdapter = new CoverViewPagerAdapter(getSupportFragmentManager(), mBannerListModels);
        mActivityJobSummaryBinding.viewPagerBannerImage.setAdapter(coverViewPagerAdapter);
        mActivityJobSummaryBinding.indicator.setViewPager(mActivityJobSummaryBinding.viewPagerBannerImage);
        if (mBannerListModels != null && mBannerListModels.size() > 1) {
            mActivityJobSummaryBinding.indicator.setVisibility(View.VISIBLE);
        } else {
            mActivityJobSummaryBinding.indicator.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setListeners() {
        mActivityJobSummaryBinding.imgChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (providerModel != null && taskDetailModel != null) {
                    TaskChatModel taskChatModel = new TaskChatModel();
                    taskChatModel.categoryName = taskDetailModel.categoryName;
                    taskChatModel.taskDesc = taskDetailModel.taskDesc;
                    taskChatModel.taskId = taskDetailModel.taskId;
                    taskChatModel.receiverId = FirebaseUtils.getPrefixSPId(providerModel.providerId);
                    taskChatModel.participantName = providerModel.userName;
                    taskChatModel.participantPhotoUrl = providerModel.profileUrl;
                    ChatActivity.newInstance(JobSummaryActivity.this, taskChatModel);
                }
            }
        });

        mActivityJobSummaryBinding.imgCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (providerModel != null && !TextUtils.isEmpty(providerModel.providerId)) {
//                    callToOtherUser(mActivityJobSummaryBinding.getRoot(), providerModel.providerId);
                    Utility.openCustomerCareCallDialer(mContext,providerModel.sp_phone_number);
                }
            }
        });

        mActivityJobSummaryBinding.imgFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callAddToFavWS(taskDetailModel.selectedProvider.providerId, !mActivityJobSummaryBinding.imgFav.isSelected());
                mActivityJobSummaryBinding.imgFav.setSelected(!mActivityJobSummaryBinding.imgFav.isSelected());

                //Sending Broadcast so hirenewjobactivity listen and change like icons
                MessageEvent messageEvent = new MessageEvent();
                messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.UPDATE_FAVOURITE;
                messageEvent.id = providerModel.providerId;
                messageEvent.isFav = mActivityJobSummaryBinding.imgFav.isSelected() ? Utility.BOOLEAN.YES : Utility.BOOLEAN.NO;
                EventBus.getDefault().post(messageEvent);
            }
        });

        mActivityJobSummaryBinding.btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callCompleteTaskWS(Utility.TASK_STATUS.COMPLETION_CONFIRM);
            }
        });
        mActivityJobSummaryBinding.btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callCompleteTaskWS(Utility.TASK_STATUS.PROCESSING);
            }
        });
    }

    private void showIncompleteTaskDialog() {
        UserDetails user = PreferenceUtility.getInstance(mContext).getUserDetails();
        final BottomAlertDialog dialog = new BottomAlertDialog(mContext);
        dialog.setTitle(getString(R.string.label_task_status));
        dialog.setMessage(getString(R.string.label_thanks_feedback_no, user.UserName));
        dialog.addPositiveButton(getString(R.string.label_call), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                //callToCheepAdmin(mActivityJobSummaryBinding.getRoot());
                Utility.initiateCallToCheepHelpLine(mContext);
            }
        });
        dialog.addNegativeButton(getString(R.string.label_chat), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HotlineHelper.getInstance(mContext).showConversation(mContext);
                dialog.dismiss();
            }
        });

        //Hiding chat dialog as it is not in current phase
        // dialog.hideNegativeButton(true);
        dialog.showDialog();
    }

    BottomAlertDialog rateDialog;

    private void showRateDialog() {

        View view = View.inflate(mContext,R.layout.dialog_rate, null);
        final RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);
        final EditText edtMessage = (EditText) view.findViewById(R.id.edit_message);

        final TextView txtLabel = (TextView) view.findViewById(R.id.text_label);
        txtLabel.setText(getString(R.string.label_write_a_review, providerModel.userName));

        rateDialog = new BottomAlertDialog(mContext);
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callAddReviewWS(ratingBar.getProgress(), edtMessage.getText().toString().trim());
               /* if (!TextUtils.isEmpty(edtMessage.getText().toString().trim())) {
                    callAddReviewWS(ratingBar.getProgress(), edtMessage.getText().toString().trim());
                } else {
                    Utility.showToast(mContext, getString(R.string.validate_review));
                }*/
            }
        });
        rateDialog.setTitle(getString(R.string.label_rate));
        rateDialog.setCustomView(view);
        rateDialog.showDialog();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        /*
          Cancel the request as it no longer available
         */
        Volley.getInstance(mContext).getRequestQueue().cancelAll(Utility.getUniqueTagForNetwork(this, NetworkUtility.WS.TASK_DETAIL));
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.ADD_REVIEW);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SP_ADD_TO_FAV);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.PAYMENT);

        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /**
     * Call Task Detail web service
     */
    private void callTaskDetailWS() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityJobSummaryBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.TASK_DETAIL
                , mCallTaskDetailWSErrorListener
                , mCallTaskDetailWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList, Utility.getUniqueTagForNetwork(this, NetworkUtility.WS.TASK_DETAIL));
    }

    Response.Listener mCallTaskDetailWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        JSONObject jsonData = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);

                        taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);

                        fillProviderDetails(taskDetailModel.selectedProvider);

                        //setting viewpager banner images
                        JSONObject jsonSPData = jsonData.optJSONObject(NetworkUtility.TAGS.SP_DATA);
                        ArrayList<CoverImageModel> coverImageModelArrayList = Utility.getObjectListFromJsonString(jsonSPData.optString(NetworkUtility.TAGS.SP_EXTRA_IMAGES), CoverImageModel[].class);
                        setupCoverViewPager(coverImageModelArrayList);

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityJobSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallTaskDetailWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    Response.ErrorListener mCallTaskDetailWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());

        }
    };

    /**
     * Call Add to fav
     *
     * @param providerId
     * @param isAddToFav
     */
    private void callAddToFavWS(String providerId, boolean isAddToFav) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityJobSummaryBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerId);
        mParams.put(NetworkUtility.TAGS.REQ_FOR, isAddToFav ? "add" : "remove");

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.SP_ADD_TO_FAV
                , mCallAddSPToFavWSErrorListener
                , mCallAddSPToFavWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }

    Response.Listener mCallAddSPToFavWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;

                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityJobSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallAddSPToFavWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    Response.ErrorListener mCallAddSPToFavWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            hideProgressDialog();

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
        }
    };

    /**
     * Call Create Task webservice
     */
    private void callAddReviewWS(int rating, String message) {

        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityJobSummaryBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, taskDetailModel.selectedProvider.providerId);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);
        mParams.put(NetworkUtility.TAGS.RATINGS, String.valueOf(rating));
        if (!TextUtils.isEmpty(message))
            mParams.put(NetworkUtility.TAGS.MESSAGE, message);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.ADD_REVIEW
                , mCallAddReviewWSErrorListener
                , mCallAddReviewWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);

    }

    Response.Listener mCallAddReviewWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        Utility.showSnackBar(getString(R.string.msg_thanks_for_rating), mActivityJobSummaryBinding.getRoot());
                        if (rateDialog != null)
                            rateDialog.dismiss();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mActivityJobSummaryBinding.getRoot());
                        Utility.showToast(mContext, error_message);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallAddReviewWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallAddReviewWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
        }
    };

    /**
     * Call Complete task
     */
    private void callCompleteTaskWS(String status) {

        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityJobSummaryBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);
        mParams.put(NetworkUtility.TAGS.STATUS, status);

        //Sending end datetime millis in GMT timezone
        mParams.put(NetworkUtility.TAGS.TASK_ENDDATE, String.valueOf(superCalendar.getTimeInMillis()));

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.CHANGE_TASK_STATUS
                , mCallCompleteTaskWSErrorListener
                , mCallCompleteTaskWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);

    }

    Response.Listener mCallCompleteTaskWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        String taskStatus = jsonObject.getString(NetworkUtility.TAGS.TASK_STATUS);
                        if (!TextUtils.isEmpty(taskStatus)) {
                            if (taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.COMPLETION_CONFIRM)) {
                                Utility.showSnackBar(getString(R.string.msg_thanks_for_confirmation), mActivityJobSummaryBinding.getRoot());
                                mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);
                                showRateDialog();
                            } else if (taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.PROCESSING)) {

                                /*
                                  Update the UI Accordingly.
                                 */
                                taskDetailModel.taskStatus = taskStatus;
                                //Refresh UI for Paid status
                                fillProviderDetails(providerModel);

                                /*
                                   Show Information Dialog about getting Cheep Help
                                 */
                                showIncompleteTaskDialog();
                            }
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityJobSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallCompleteTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallCompleteTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
        }
    };

    /**
     * Used for payment
     */
    private void updatePaymentStatus(boolean isSuccess, String response, boolean isAdditionalPayment) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityJobSummaryBinding.getRoot());
            return;
        }
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);
        if (!TextUtils.isEmpty(cheepCode))
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
        mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, transaction_Id);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, isSuccess ? Utility.PAYMENT_STATUS.COMPLETED : Utility.PAYMENT_STATUS.FAILED);
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, response);

        mParams.put(NetworkUtility.TAGS.IS_FOR_ADDITIONAL_QUOTE, isAdditionalPayment
                ? getString(R.string.label_yes).toLowerCase() :
                getString(R.string.label_no).toLowerCase());

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.PAYMENT
                , mCallUpdatePaymentStatusWSErrorListener
                , mCallUpdatePaymentStatusWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }


    Response.Listener mCallUpdatePaymentStatusWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        JSONObject jsonData = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);
                        String taskStatus = jsonData.optString(NetworkUtility.TAGS.TASK_STATUS);

//                        callTaskDetailWS();

                        if (Utility.TASK_STATUS.PAID.equalsIgnoreCase(taskStatus)) {
                            //We are commenting it because from here we are intiating a payment flow and
                            // after that we need to call update payment status on server
                            String taskPaidAmount = jsonData.optString(NetworkUtility.TAGS.TASK_PAID_AMOUNT);
                            if (taskDetailModel != null) {
                                taskDetailModel.taskStatus = taskStatus;
                                if (!TextUtils.isEmpty(taskPaidAmount))
                                    taskDetailModel.taskPaidAmount = taskPaidAmount;
                                /*
                                * Update selected sp on firebase
                                * @Sanjay 20 Feb 2016
                                * */
                                if (providerModel != null) {
                                    updateSelectedSpOnFirebase(taskDetailModel, providerModel);
                                }
                            }

                            //Refresh UI for Paid status
                            fillProviderDetails(providerModel);

                            MessageEvent messageEvent = new MessageEvent();
                            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_PAID;
                            EventBus.getDefault().post(messageEvent);
                        } else if (Utility.TASK_STATUS.PROCESSING.equalsIgnoreCase(taskStatus)) {
                            //We are commenting it because from here we are intiating a payment flow and after that we need to call update payment status on server
                            String taskPaidAmount = jsonData.optString(NetworkUtility.TAGS.TASK_PAID_AMOUNT);
                            if (taskDetailModel != null) {
                                taskDetailModel.taskStatus = taskStatus;
                                if (!TextUtils.isEmpty(taskPaidAmount))
                                    taskDetailModel.taskPaidAmount = taskPaidAmount;
                            }
                            //Refresh UI for Paid status
                            fillProviderDetails(providerModel);
                            MessageEvent messageEvent = new MessageEvent();
                            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_PROCESSING;
                            EventBus.getDefault().post(messageEvent);
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityJobSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        ;
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallUpdatePaymentStatusWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };
    Response.ErrorListener mCallUpdatePaymentStatusWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());

        }
    };

    /**
     * Asynctask that will do encryption
     *
     * @Dated : 6th Feb 2017
     * input: String that needs to be converted
     * output: String after Encryption completed
     */
    private class AsyncFetchEnryptedString extends AsyncTask<String, Void, String> {
        boolean isForAdditionalQuote;

        public AsyncFetchEnryptedString(boolean isForAdditionalQuote) {
            this.isForAdditionalQuote = isForAdditionalQuote;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String result = Utility.applyAESEncryption(new JSONObject(params[0]).toString());
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String encryptedData) {
            super.onPostExecute(encryptedData);

            UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

            //Add Header parameters
            Map<String, String> mHeaderParams = new HashMap<>();
            mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);

            Map<String, Object> mFinalParams = new HashMap<>();
            mFinalParams.put(NetworkUtility.TAGS.DATA, encryptedData);

            //calling this to create post data
            getPaymentUrl(userDetails, isForAdditionalQuote);

            //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
            VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.GET_PAYMENT_HASH
                    , mCallPaymentWSErrorListener
                    , mCallPaymentWSResponseListener
                    , mHeaderParams
                    , mFinalParams
                    , null);
            Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
        }
    }

    /*
    * Update finalized sp id on firebase.
    * @Sanjay 20 Feb 2016
    * */
    private void updateSelectedSpOnFirebase(TaskDetailModel taskDetailModel, ProviderModel providerModel) {
        String formattedTaskId = FirebaseUtils.getPrefixTaskId(taskDetailModel.taskId);
        String formattedSpId = FirebaseUtils.getPrefixSPId(providerModel.providerId);
        String formattedUserId = "";
        final UserDetails userDetails = PreferenceUtility.getInstance(JobSummaryActivity.this).getUserDetails();
        if (userDetails != null) {
            formattedUserId = FirebaseUtils.getPrefixUserId(userDetails.UserID);
        }
        FirebaseHelper.getRecentChatRef(formattedUserId).child(formattedTaskId).removeValue();
        if (!TextUtils.isEmpty(formattedTaskId) && !TextUtils.isEmpty(formattedSpId)) {
            FirebaseHelper.getTaskRef(formattedTaskId).child(FirebaseHelper.KEY_SELECTEDSPID).setValue(formattedSpId);
        }

        final String formattedId = FirebaseUtils.get_T_SP_U_FormattedId(formattedTaskId, formattedSpId, formattedUserId);
        final String finalFormattedUserId = formattedUserId;
        FirebaseHelper.getTaskChatRef(formattedTaskId).child(formattedId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    TaskChatModel taskChatModel = dataSnapshot.getValue(TaskChatModel.class);
                    taskChatModel.chatId = formattedId;
                    FirebaseHelper.getRecentChatRef(finalFormattedUserId).child(taskChatModel.chatId).setValue(taskChatModel);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////Reject Additional Payment[START] //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void callDeclineAdditionalPaymentRequest() {
        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityJobSummaryBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.DECLINE_ADDITIONAL_PAYMENT_REQUEST
                , mCallDeclineAdditionalPaymentRequestWSErrorListener
                , mCallDeclineAdditionalPaymentRequestWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.DECLINE_ADDITIONAL_PAYMENT_REQUEST);

    }

    Response.Listener mCallDeclineAdditionalPaymentRequestWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        JSONObject jData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
                        String taskID = jData.getString(NetworkUtility.TAGS.TASK_ID);
                        Utility.showSnackBar(jsonObject.getString(NetworkUtility.TAGS.MESSAGE), mActivityJobSummaryBinding.getRoot());
                       /* Utility.showSnackBar(getString(R.string.msg_thanks_for_confirmation), mActivityJobSummaryBinding.getRoot());
                        mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);
                        showRateDialog();*/

                        taskDetailModel.taskStatus = Utility.TASK_STATUS.PROCESSING;
                        fillProviderDetails(taskDetailModel.selectedProvider);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityJobSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallCompleteTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallDeclineAdditionalPaymentRequestWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////Reject Additional Payment[END] //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////// Get Task Status [START] //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void callCheckingTaskStatus() {
        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityJobSummaryBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.GET_TASK_STATUS
                , mGetTaskStatusWSErrorListener
                , mGetTaskStatusWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.GET_TASK_STATUS);

    }

    Response.Listener mGetTaskStatusWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        JSONObject jData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
                        String taskStatus = jData.getString(NetworkUtility.TAGS.TASK_STATUS);

                        if (taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.ADDITIONAL_PAYMENT_REQUESTED)) {
                            payNow(true);
                        } else if (taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.COMPLETION_REQUEST)) {
                            Utility.showSnackBar(getString(R.string.message_no_more_payment_task_completed), mActivityJobSummaryBinding.getRoot());
                            taskDetailModel.taskStatus = Utility.TASK_STATUS.COMPLETION_REQUEST;
                            fillProviderDetails(taskDetailModel.selectedProvider);
                        }
//                        Utility.showSnackBar(jsonObject.getString(NetworkUtility.TAGS.MESSAGE), mActivityJobSummaryBinding.getRoot());
                       /* Utility.showSnackBar(getString(R.string.msg_thanks_for_confirmation), mActivityJobSummaryBinding.getRoot());
                        mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);
                        showRateDialog();*/

//                        hideProgressDialog();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
                        hideProgressDialog();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityJobSummaryBinding.getRoot());
                        hideProgressDialog();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        hideProgressDialog();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallCompleteTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    Response.ErrorListener mGetTaskStatusWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////Get Task Status [END] //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void enableChatCall(boolean flag) {
        if (flag) {
            mActivityJobSummaryBinding.lnCall.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.lnChat.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.verticalDividerCall.setVisibility(View.VISIBLE);
            mActivityJobSummaryBinding.verticalDividerChat.setVisibility(View.VISIBLE);
        } else {
            mActivityJobSummaryBinding.lnCall.setVisibility(View.GONE);
            mActivityJobSummaryBinding.lnChat.setVisibility(View.GONE);
            mActivityJobSummaryBinding.verticalDividerCall.setVisibility(View.GONE);
            mActivityJobSummaryBinding.verticalDividerChat.setVisibility(View.GONE);
        }
    }
}
