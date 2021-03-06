package com.cheep.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.cheepcarenew.dialogs.CancelRescheduleTaskDialog;
import com.cheep.cheepcarenew.dialogs.SomeoneElseWillAttendDialog;
import com.cheep.cheepcarenew.dialogs.UserAvailabilityDialog;
import com.cheep.cheepcarenew.dialogs.UserAvailableDialog;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.databinding.ActivityTaskSummaryBinding;
import com.cheep.databinding.DialogChangePhoneNumberBinding;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.CalendarUtility;
import com.cheep.utils.FreshChatHelper;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.RoundedBackgroundSpan;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhavesh on 26/4/17.
 */

public class TaskSummaryActivity extends BaseAppCompatActivity {
    private static final String TAG = TaskSummaryActivity.class.getSimpleName();
    private TaskDetailModel mTaskDetailModel;

    private final SomeoneElseWillAttendDialog.DialogInteractionListener someoneElseWillAttendListener =
            new SomeoneElseWillAttendDialog.DialogInteractionListener() {
                @Override
                public void okClicked(String personName) {
                    Log.d(TAG, "okClicked() called with: personName = [" + personName + "]");
                }

                @Override
                public void onBackPressed() {
                    UserAvailabilityDialog.newInstance(mContext, userAvailabilityListener);
                }
            };
    public SuperCalendar startDateTimeSuperCalendar = SuperCalendar.getInstance();

    public SuperCalendar superCalendar;
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////  Rate Dialog //////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    BottomAlertDialog rateDialog;
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
    private ActivityTaskSummaryBinding mBinding;
    private final CancelRescheduleTaskDialog.DialogInteractionListener cancelRescheduleTaskListener =
            new CancelRescheduleTaskDialog.DialogInteractionListener() {
                @Override
                public void cancelTaskClicked() {
                    Log.d(TAG, "cancelTaskClicked() called");
                }

                @Override
                public void rescheduleTaskClicked() {
                    showDateTimePickerDialog();
                }

                @Override
                public void onBackPressed() {
                    UserAvailabilityDialog.newInstance(mContext, userAvailabilityListener);
                }
            };
    private final AcknowledgementInteractionListener userAvailableListener =
            new AcknowledgementInteractionListener() {
                @Override
                public void onAcknowledgementAccepted() {
                    Log.d(TAG, "onAcknowledgementAccepted() called user available");
                    setConfirmAvailabilityVisible(false);
                }
            };
    private final UserAvailabilityDialog.DialogInteractionListener userAvailabilityListener =
            new UserAvailabilityDialog.DialogInteractionListener() {
                @Override
                public void someoneElseWillAttendClicked() {
                    Log.d(TAG, "someoneElseWillAttendClicked() called");
                    SomeoneElseWillAttendDialog.newInstance(mContext, someoneElseWillAttendListener);
                }

                @Override
                public void rescheduleTaskClicked() {
                    Log.d(TAG, "rescheduleTaskClicked() called");
                    showDateTimePickerDialog();
                }

                @Override
                public void cancelTaskClicked() {
                    Log.d(TAG, "cancelTaskClicked() called");
                    CancelRescheduleTaskDialog.newInstance(mContext, cancelRescheduleTaskListener);
                }

                @Override
                public void userWillBeAvailableClicked() {
                    Log.d(TAG, "userWillBeAvailableClicked() called");
                    UserAvailableDialog.newInstance(mContext, userAvailableListener);
                }
            };
    Response.ErrorListener mCallTaskDetailWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            showProgressBar(false);

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());

        }
    };
    Response.ErrorListener mCallCompleteTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };
    Response.ErrorListener mCallDeclineAdditionalPaymentRequestWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };
    Response.ErrorListener mGetTaskStatusWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };
    Response.ErrorListener mCallAddSPToFavWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            hideProgressDialog();
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());

        }
    };
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
            } catch (JSONException e) {
                e.printStackTrace();
                mCallAddSPToFavWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////WHEN Feature [END]//////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
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
                        Utility.showSnackBar(jsonObject.getString(NetworkUtility.TAGS.MESSAGE), mBinding.getRoot());
                       /* Utility.showSnackBar(getString(R.string.msg_thanks_for_confirmation), mActivityJobSummaryBinding.getRoot());
                        mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);
                        showRateDialog();*/

                        mTaskDetailModel.taskStatus = Utility.TASK_STATUS.PROCESSING;
                        callTaskDetailWS(mTaskDetailModel.taskId);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        // Logout and finish the current activity
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
    Response.Listener mAcceptAdditionalPaymentRequestWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            hideProgressDialog();
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        callTaskDetailWS(mTaskDetailModel.taskId);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        hideProgressDialog();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mBinding.getRoot());
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
                                Utility.showSnackBar(getString(R.string.msg_thanks_for_confirmation), mBinding.getRoot());

                                /*
                                  Update the UI Accordingly.
                                 */
                                mTaskDetailModel.taskStatus = taskStatus;

                                //Refresh UI for Paid status
                                setUpUI();

                                // Notify the Home Screen to check for ongoing task counter.
                                MessageEvent messageEvent = new MessageEvent();
                                messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_START_ALERT;
                                EventBus.getDefault().post(messageEvent);

                            } else if (taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.PROCESSING)) {

                                /*
                                  Update the UI Accordingly.
                                 */
                                mTaskDetailModel.taskStatus = taskStatus;

                                //Refresh UI for Paid status
                                setUpUI();

                                /*
                                   Show Information Dialog about getting Cheep Help
                                 */
                                showIncompleteTaskDialog();
                            }
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
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
            } catch (JSONException e) {
                e.printStackTrace();
                mCallCompleteTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };
    Response.Listener mCallTaskDetailWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                showProgressBar(false);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

//                        JSONObject jsonData = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);

                        mTaskDetailModel = (TaskDetailModel) GsonUtility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);

                        setUpUI();

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
                mCallTaskDetailWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };
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
                        Utility.showSnackBar(getString(R.string.msg_thanks_for_rating), mBinding.getRoot());
                        if (rateDialog != null)
                            rateDialog.dismiss();
                        mTaskDetailModel.ratingDone = Utility.BOOLEAN.YES;
                        mTaskDetailModel.taskRatings = jsonObject.optString(NetworkUtility.TAGS.TASK_RATINGS);
                        // Update the UI According to Updated Model.
                        setUpUI();
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
    private BottomAlertDialog dialogDesc;
    private TextView txtMessage;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // Onclick of when and Where section
                case R.id.ln_where:
                case R.id.text_task_where:
                    showFullDesc(getString(R.string.label_address), mBinding.textTaskWhere.getText().toString());
                    break;
                case R.id.ln_task_desc:
                    showFullDesc(getString(R.string.label_desc), mBinding.textTaskDesc.getText().toString());
                    break;
                case R.id.tv_confirm_availability:
//                    UserAvailabilityDialog.newInstance(mContext, userAvailabilityListener);
                    break;
                // Onclick of when and Where section
            }
        }
    };

    public static void getInstance(Context mContext, String taskId) {
        Intent intent = new Intent(mContext, TaskSummaryActivity.class);
        intent.putExtra(Utility.Extra.TASK_ID, taskId);
        mContext.startActivity(intent);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Reload SP Listing based on AddressID [START]//////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_task_summary);
        initiateUI();
        setListeners();
        EventBus.getDefault().register(this);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Reload SP Listing based on AddressID [END]//////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Task Detail Service[Start] ////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initiateUI() {
        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra(Utility.Extra.TASK_DETAIL_MODEL)) {
                // Fetch Task Detail Model
                mTaskDetailModel = (TaskDetailModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.TASK_DETAIL_MODEL), TaskDetailModel.class);
            }
        }

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

        if (mTaskDetailModel == null) {
            callTaskDetailWS(getIntent().getExtras().getString(Utility.Extra.TASK_ID));
        } else {
            setUpUI();
        }

    }

    private void setUpUI() {
        //set appbar type section
        setAppBarSection();

        //set pro section
        setProSection();

        //set task description
        setUpTaskDetails();

        //set task when
        setTaskWhen();

        //set task when
        setTaskWhere();
    }

    private void setAppBarSection() {
        // Set category
        mBinding.textCategoryName.setText(mTaskDetailModel.categoryModel.catName != null ? mTaskDetailModel.categoryModel.catName : Utility.EMPTY_STRING);

        // Set up image
        GlideUtility.loadImageView(mContext, mBinding.imgService, mTaskDetailModel.categoryModel.catImageExtras.original, R.drawable.gradient_black);
        GlideUtility.loadImageView(mContext, mBinding.imgService, mTaskDetailModel.categoryModel.catImageExtras.thumb, R.drawable.gradient_black);
    }

    private void setProSection() {

        if (mTaskDetailModel.selectedProvider != null && !TextUtils.isEmpty(mTaskDetailModel.selectedProvider.providerId)) {
            // Provider is final.
            mBinding.lnResponseReceived.setVisibility(View.GONE);
            mBinding.lnProviderProfileSection.setVisibility(View.VISIBLE);

            //anand       // Show Payment Summary textview
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            String s = "";
            if (!TextUtils.isEmpty(mTaskDetailModel.isAnyAmountPending))
                s = mTaskDetailModel.isAnyAmountPending.equalsIgnoreCase(Utility.BOOLEAN.YES) ? getString(R.string.label_not_paid) : getString(R.string.label_paid);

            mBinding.textPaid.setVisibility(View.GONE);
            mBinding.textPaid.setText("(" + s + ")");


            // Set rating
            Utility.showRating(mTaskDetailModel.selectedProvider.rating, mBinding.providerRating);
            mBinding.textExperience.setText(mTaskDetailModel.selectedProvider.rating);

            if (Utility.BOOLEAN.YES.equals(mTaskDetailModel.selectedProvider.isFavourite))
                mBinding.imgFav.setSelected(true);
            else
                mBinding.imgFav.setSelected(false);

            mBinding.imgFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callAddToFavWS(mTaskDetailModel.selectedProvider.providerId, !mBinding.imgFav.isSelected());
                    mBinding.imgFav.setSelected(!mBinding.imgFav.isSelected());

                }
            });

            // Name of Provider
            mBinding.textProviderName.setText(mTaskDetailModel.selectedProvider.userName);

            SpannableString sName = new SpannableString(mTaskDetailModel.selectedProvider.userName);
            SpannableString sVerified;
            if (Utility.BOOLEAN.YES.equalsIgnoreCase(mTaskDetailModel.selectedProvider.isVerified)) {
                sVerified = new SpannableString(" " + mContext.getString(R.string.label_verified_pro) + " ");
                sVerified.setSpan(new RelativeSizeSpan(0.9f), 0, sVerified.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sVerified.setSpan(new RoundedBackgroundSpan(ContextCompat.getColor(this, R.color.splash_gradient_end), ContextCompat.getColor(this, R.color.white), 0), 0, sVerified.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                mBinding.textProviderName.setText(TextUtils.concat(sName, " ", sVerified));
            }
            // Distanceof Provider
            mBinding.textAddressKmAway.setText(mTaskDetailModel.selectedProvider.distance + getString(R.string.label_away));

            // Profile Pic
            GlideUtility.showCircularImageView(mContext, TAG, mBinding.imgProfile, mTaskDetailModel.selectedProvider.profileUrl, Utility.DEFAULT_CHEEP_LOGO, true);

//            // Whether Provider Verified or not
//            if (Utility.BOOLEAN.YES.equalsIgnoreCase(mTaskDetailModel.selectedProvider.isVerified)) {
//                mBinding.textProVerified.setVisibility(View.VISIBLE);
//            } else {
//                mBinding.textProVerified.setVisibility(View.GONE);
//            }

            // Manage Click events of Call & Chat
            mBinding.lnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: Call");
                    Utility.openCustomerCareCallDialer(mContext, mTaskDetailModel.selectedProvider.sp_phone_number);
                }
            });
            mBinding.lnChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: Chat");
                    TaskChatModel taskChatModel = new TaskChatModel();
                    taskChatModel.categoryName = mTaskDetailModel.categoryModel.catName;
                    taskChatModel.taskDesc = mTaskDetailModel.taskDesc;
                    taskChatModel.taskId = mTaskDetailModel.taskId;
                    taskChatModel.receiverId = FirebaseUtils.getPrefixSPId(mTaskDetailModel.selectedProvider.providerId);
                    taskChatModel.participantName = mTaskDetailModel.selectedProvider.userName;
                    taskChatModel.participantPhotoUrl = mTaskDetailModel.selectedProvider.profileUrl;
                    ChatActivity.newInstance(mContext, taskChatModel);
                }
            });


            // On Click on Payment Summary
            mBinding.textViewPaymentSummary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Redirect the user to Payment Summary screen.
                    double pendingAmount;
                    try {
                        pendingAmount = Double.parseDouble(mTaskDetailModel.taskTotalPendingAmount);
                    } catch (NumberFormatException e) {
                        pendingAmount = 0;
                    }
                    LogUtils.LOGE(TAG, "showTaskCompletionDialog: pendingAmount :: " + pendingAmount);

//                    if (pendingAmount > 0) {
////                        PaymentChoiceActivity.newInstance(mContext, mTaskDetailModel);
//                        PaymentDetailsActivity.newInstance(mContext, mTaskDetailModel);
//                    } else {
//                        mBinding.textTaskCompletionYes.setText(R.string.label_yes);
                    PaymentSummaryActivity.newInstance(TaskSummaryActivity.this, mTaskDetailModel, Utility.EMPTY_STRING, Utility.EMPTY_STRING);
//                    }
                }
            });

            // Manage UI Based on Status
            updateUIBasedOnTaskStatus();

            // Manage UnreadBadge count for Task
            manageUnreadBadgeCounterForChat();
        } else {
            // Setup First section whether SP is final or not
            // Provider is not final yet, so need to show the nearby available.
            mBinding.lnResponseReceived.setVisibility(View.VISIBLE);
            mBinding.lnProviderProfileSection.setVisibility(View.GONE);
            // Update Task Status
            mBinding.textTaskStatusTop.setText(getString(R.string.label_receiving_quotes));

            // Hide Payment Summary textview
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.GONE);

            updateSPImageStacks(mTaskDetailModel.mQuotedSPList);
        }
    }

    private void setUpTaskDetails() {

        // By Default makethe task completion dialog as gone
        showTaskCompletionDialog(false);
        LogUtils.LOGE(TAG, "showTaskCompletionDialog: taskTotalPendingAmount :: " + mTaskDetailModel.taskTotalPendingAmount);
        mBinding.lnTaskCancellation.setVisibility(View.GONE);
        mBinding.lnRatingSection.setVisibility(View.GONE);
        mBinding.lnTaskRescheduleRequested.setVisibility(View.GONE);
        mBinding.lnTaskRescheduleRejected.setVisibility(View.GONE);
        mBinding.lnTaskAdditionalQuoteRequested.setVisibility(View.GONE);

        //Bydefault show the chat call icons
        showChatCallButton(true);

        // Hide Bottom Action Button
        mBinding.textBottomAction.setVisibility(View.GONE);
        updateHeightOfLinearLayout(false);

        // Set Second Section
        mBinding.textSubCategoryName.setText(mTaskDetailModel.subCategoryName);
        mBinding.textTaskDesc.setText(mTaskDetailModel.taskDesc);
//        GlideUtility.loadImageView(mContext, mBinding.imgTaskPicture, mTaskDetailModel.taskImage, 0);
        if (mTaskDetailModel.mMediaModelList != null && !mTaskDetailModel.mMediaModelList.isEmpty()) {
//            if (mTaskDetailModel.mMediaModelList.size() > 1)
//                mBinding.tvCounter.setText("+" + (mTaskDetailModel.mMediaModelList.size() - 1));
//            else
//                mBinding.tvCounter.setVisibility(View.GONE);

            Collections.reverse(mTaskDetailModel.mMediaModelList);
            GlideUtility.loadImageView(this, mBinding.imgTaskPicture1, mTaskDetailModel.mMediaModelList.get(0).mediaThumbName);
            if (mTaskDetailModel.mMediaModelList.size() == 3) {
                GlideUtility.loadImageView(this, mBinding.imgTaskPicture1, mTaskDetailModel.mMediaModelList.get(0).mediaThumbName);
                GlideUtility.loadImageView(this, mBinding.imgTaskPicture2, mTaskDetailModel.mMediaModelList.get(1).mediaThumbName);
                GlideUtility.loadImageView(this, mBinding.imgTaskPicture3, mTaskDetailModel.mMediaModelList.get(2).mediaThumbName);
            } else if (mTaskDetailModel.mMediaModelList.size() == 2) {
                mBinding.framePicture3.setVisibility(View.GONE);
                GlideUtility.loadImageView(this, mBinding.imgTaskPicture2, mTaskDetailModel.mMediaModelList.get(1).mediaThumbName);
                GlideUtility.loadImageView(this, mBinding.imgTaskPicture1, mTaskDetailModel.mMediaModelList.get(0).mediaThumbName);

            } else {
                mBinding.framePicture3.setVisibility(View.GONE);
                mBinding.framePicture2.setVisibility(View.GONE);
                GlideUtility.loadImageView(this, mBinding.imgTaskPicture1, mTaskDetailModel.mMediaModelList.get(0).mediaThumbName);
            }

        } else
            mBinding.frameSelectPicture.setVisibility(View.GONE);

        // Onclick of when and Where section
        mBinding.lnTaskDesc.setOnClickListener(mOnClickListener);
    }

    private void setTaskWhen() {
        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

        String task_original_date = "";
        try {
            superCalendar.setTimeInMillis(Long.parseLong(mTaskDetailModel.taskStartdate));
            superCalendar.setLocaleTimeZone();
            task_original_date = superCalendar.format(Utility.DATE_FORMAT_DD_MMM) + getString(R.string.label_between);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        String time = CalendarUtility.get2HourTimeSlots(mTaskDetailModel.taskStartdate);
        mBinding.textTaskWhen.setText(task_original_date + time);
    }

    private void setTaskWhere() {
        mBinding.textTaskWhere.setText(mTaskDetailModel.taskAddress.address);
        mBinding.lnTaskWhere.setOnClickListener(mOnClickListener);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////WHEN Feature [START]//////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private void showDateTimePickerDialog() {
        // Get Current Date
        final Calendar c = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (view.isShown()) {
                    Log.d(TAG, "onDateSet() called with: view = [" + view + "], year = [" + year + "], monthOfYear = [" + monthOfYear + "], dayOfMonth = [" + dayOfMonth + "]");
                    startDateTimeSuperCalendar.set(Calendar.YEAR, year);
                    startDateTimeSuperCalendar.set(Calendar.MONTH, monthOfYear);
                    startDateTimeSuperCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePickerDialog();
                }
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////// Task Detail WS[END] ///////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////

    private void showTimePickerDialog() {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(mContext,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (view.isShown()) {

                            Log.d(TAG, "onTimeSet() called with: view = [" + view + "], hourOfDay = [" + hourOfDay + "], minute = [" + minute + "]");

                            startDateTimeSuperCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            startDateTimeSuperCalendar.set(Calendar.MINUTE, minute);

                            superCalendar = SuperCalendar.getInstance();
                            superCalendar.setTimeInMillis(startDateTimeSuperCalendar.getTimeInMillis());
                            superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

                            // Get date-time for next 3 hours
                            SuperCalendar calAfter3Hours = SuperCalendar.getInstance().getNext3HoursTime(false);

//                            TODO: This needs to Be UNCOMMENTED DO NOT FORGET
//                            if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase(Utility.DEBUG)) {
                            if (superCalendar.getTimeInMillis() < calAfter3Hours.getTimeInMillis()) {
                                Utility.showSnackBar(getString(R.string.can_only_start_task_after_3_hours, "3"), mBinding.getRoot());
//                                mBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
//                                mBinding.textTaskWhen.setVisibility(View.GONE);
                                superCalendar = null;
//                                updateWhenLabelWithIcon( Utility.EMPTY_STRING);
                                return;
                            }
//                            }

                            if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
                                String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                                        + getString(R.string.label_at)
                                        + startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
                                updateWhenLabelWithIcon(selectedDateTime);
                                setConfirmAvailabilityVisible(false);
                            } else {
//                                updateWhenLabelWithIcon( Utility.EMPTY_STRING);
                                Utility.showSnackBar(getString(R.string.validate_future_date), mBinding.getRoot());
                            }
                        }
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
        timePickerDialog.show();

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////// Task Completion Yes /////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void updateWhenLabelWithIcon(String whenValue) {
        mBinding.textTaskWhen.setText(whenValue);
    }

    private void setConfirmAvailabilityVisible(boolean isConfirmAvailabilityVisible) {
        mBinding.tvConfirmAvailability.setVisibility(isConfirmAvailabilityVisible ? View.VISIBLE : View.GONE);
        mBinding.tvConfirmAvailabilityInfo.setVisibility(isConfirmAvailabilityVisible ? View.VISIBLE : View.GONE);
    }

    private void updateUIBasedOnTaskStatus() {
        if (Utility.TASK_STATUS.PENDING.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.task_confirmed));
            //anand
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.GONE);

        } else if (Utility.TASK_STATUS.PROCESSING.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.task_status_processing));
            //anand
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.GONE);

        } else if (Utility.TASK_STATUS.COMPLETION_REQUEST.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.task_status_processing));
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.GONE);

            // Setup Task Completion Request Dialog
            showTaskCompletionDialog(true);
        } else if (Utility.TASK_STATUS.COMPLETION_CONFIRM.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.label_task_complete));
            //anand
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.GONE);

            // No need to hide ChatCall Button Now.
            showChatCallButton(false);

            // Check if Rating is done or not
            if (Utility.BOOLEAN.YES.equalsIgnoreCase(mTaskDetailModel.ratingDone)) {
                // Rating Section
                mBinding.lnRatingSection.setVisibility(View.VISIBLE);
                Utility.showRating(mTaskDetailModel.taskRatings, mBinding.taskRatingbar);

                // No need to Show bottom action button with rate and review
                mBinding.textBottomAction.setVisibility(View.GONE);
                mBinding.textBottomAction.setOnClickListener(null);
                updateHeightOfLinearLayout(false);
            } else {
                // Rating Section
                mBinding.lnRatingSection.setVisibility(View.GONE);

                // Show bottom action button with rate & review
                mBinding.textBottomAction.setText(getString(R.string.label_rate_and_review));
                mBinding.textBottomAction.setVisibility(View.VISIBLE);
                mBinding.textBottomAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showRateDialog();
                    }
                });
                updateHeightOfLinearLayout(true);
            }
        } else if (Utility.TASK_STATUS.COD.equalsIgnoreCase(mTaskDetailModel.taskStatus) || Utility.TASK_STATUS.PAID.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.task_confirmed));
            //anand
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.GONE);

        } else if (Utility.TASK_STATUS.CANCELLED_CUSTOMER.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.msg_task_cancelled_title));
            //anand
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.GONE);

            // Cancellation Reason
            mBinding.lnTaskCancellation.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(mTaskDetailModel.taskCancelReason)) {
                mBinding.textTaskCancellationReason.setText(mTaskDetailModel.taskCancelReason);
            }

            // No need to hide ChatCall Button Now.
            showChatCallButton(false);
        } else if (Utility.TASK_STATUS.CANCELLED_SP.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
//            mBinding.textTaskStatusTop.setText(getString(R.string.task_was_cancelled_by_x, mTaskDetailModel.selectedProvider.userName));
            mBinding.textTaskStatusTop.setText(getString(R.string.msg_task_cancelled_title));
            //anand
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.GONE);

            // Cancellation Reason
            mBinding.lnTaskCancellation.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(mTaskDetailModel.taskCancelReason)) {
                mBinding.textTaskCancellationReason.setText(mTaskDetailModel.taskCancelReason);
            }

            // No need to hide ChatCall Button Now.
            showChatCallButton(false);
        }
        // reschedule task status
        else if (Utility.TASK_STATUS.RESCHEDULE_REQUESTED.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.label_reschedule_requested));
            //anand
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.GONE);

            // Reschedule request desc
            mBinding.lnTaskRescheduleRequested.setVisibility(View.VISIBLE);

            //Calculate Reschedule Date & Time
            SuperCalendar superCalendar = SuperCalendar.getInstance();
            superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            try {
                superCalendar.setTimeInMillis(Long.parseLong(mTaskDetailModel.taskRescheduleDateTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
            superCalendar.setLocaleTimeZone();
            String task_reschedule_date = superCalendar.format(Utility.DATE_FORMAT_DD_MMM);
            String task_reschedule_time = superCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
            String message = getString(R.string.label_reschedule_desc, task_reschedule_date + getString(R.string.label_at) + task_reschedule_time);
            mBinding.textTaskRescheduleRequestDesc.setText(message);

        }
        //Task's Reschedule request got cancelled
        else if (Utility.TASK_STATUS.RESCHEDULE_REQUEST_REJECTED.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.label_reschedule_rejected));
            //anand
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.GONE);

            mBinding.lnTaskRescheduleRejected.setVisibility(View.VISIBLE);

            // Chat & Call with @Cheep team click event of buttons
            mBinding.textContactCheepViaCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //callToCheepAdmin(mActivityHomeBinding.getRoot());
                    Utility.initiateCallToCheepHelpLine(mContext);
                }
            });

            mBinding.textContactCheepViaChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //    HotlineHelper.getInstance(mContext).showConversation(mContext);
                    FreshChatHelper.getInstance(mContext).showConversation(mContext);
                }
            });

            // No need to hide ChatCall Button Now.
            showChatCallButton(false);
        }
        //Task's Additional Payment Request comes
        else if (Utility.TASK_STATUS.ADDITIONAL_PAYMENT_REQUESTED.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.task_status_processing));
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.GONE);
            mBinding.lnTaskAdditionalQuoteRequested.setVisibility(View.VISIBLE);

            String additionalQuoteAmount = getString(R.string.rupee_symbol_x, mTaskDetailModel.additionalQuoteAmount);
            mBinding.textAdditionalPaymentDesc.setText(getString(R.string.label_additional_payment_desc, additionalQuoteAmount));

            mBinding.textAdditionalPaymentAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: Accept Additional Payment");

                    // First Call Asynctask that would going to check whether current status of Progressing or not.
//                    callCheckingTaskStatus();
                    callAcceptAdditionalPaymentRequest();


                }
            });

            mBinding.textAdditionalPaymentDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: Decline Additional Payment");
                    showAdditionalPaymentRejectionDialog();
                }
            });
        }
    }

    /**
     * This would make the Chat Call Visible/Invisible
     *
     * @param flag
     */
    private void showChatCallButton(boolean flag) {
        if (flag) {
            mBinding.lnChatCall.setVisibility(View.VISIBLE);
        } else {
            mBinding.lnChatCall.setVisibility(View.GONE);
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// Task Completion WS [END] ///////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////

    private void showTaskCompletionDialog(boolean flag) {
        if (flag) {
            mBinding.lnTaskCompletionRequested.setVisibility(View.VISIBLE);
            String mainText = getString(R.string.label_complete_job_confirm, "PRO");
            String s = "";
            if (!TextUtils.isEmpty(mTaskDetailModel.isAnyAmountPending)) {
                s = mTaskDetailModel.isAnyAmountPending.equalsIgnoreCase(Utility.BOOLEAN.YES) ? getString(R.string.label_not_paid) : getString(R.string.label_paid);
                s = "(" + s + ")";
            }
            if (!s.isEmpty()) {
                String fullstring = mainText + s;
                SpannableStringBuilder text = new SpannableStringBuilder(fullstring);
                text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.grey_varient_23)), fullstring.indexOf(s.charAt(0)), fullstring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mBinding.textConfirmText.setText(text);
            } else {
                mBinding.textConfirmText.setText(mainText);

            }

//            if (mTaskDetailModel.paymentStatus.equalsIgnoreCase(Utility.TASK_STATUS.PAID)) {
//            double pendingAmount;
//            try {
//                LogUtils.LOGE(TAG, "showTaskCompletionDialog: taskTotalPendingAmount :: " + mTaskDetailModel.taskTotalPendingAmount);
//                pendingAmount = Double.parseDouble(mTaskDetailModel.taskTotalPendingAmount);
//            } catch (NumberFormatException e) {
//                pendingAmount = 0;
//            }
//            LogUtils.LOGE(TAG, "showTaskCompletionDialog: pendingAmount :: " + pendingAmount);
//            if (pendingAmount > 0) {
//                mBinding.textTaskCompletionYes.setText(R.string.label_yes_pay_now);
//            } else {
//                mBinding.textTaskCompletionYes.setText(R.string.label_yes);
//            }

            mBinding.textTaskCompletionYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

//                    if (mTaskDetailModel.paymentStatus.equalsIgnoreCase(Utility.TASK_STATUS.PAID)) {
//                        callCompleteTaskWS(Utility.TASK_STATUS.COMPLETION_CONFIRM);
//                    } else {
//                        PaymentChoiceActivity.newInstance(mContext, mTaskDetailModel);
//                    }
                    double pendingAmount;
                    try {
                        LogUtils.LOGE(TAG, "showTaskCompletionDialog: taskTotalPendingAmount :: " + mTaskDetailModel.taskTotalPendingAmount);
                        pendingAmount = Double.parseDouble(mTaskDetailModel.taskTotalPendingAmount);
                    } catch (NumberFormatException e) {
                        pendingAmount = 0;
                    }
                    LogUtils.LOGE(TAG, "showTaskCompletionDialog: pendingAmount :: " + pendingAmount);

                    if (pendingAmount > 0) {
//                        PaymentDetailsActivity.newInstance(mContext, mTaskDetailModel);
                        PaymentSummaryActivity.newInstance(mContext, mTaskDetailModel,  Utility.EMPTY_STRING, Utility.EMPTY_STRING);
                    } else {
                        callCompleteTaskWS(Utility.TASK_STATUS.COMPLETION_CONFIRM);
//                        mBinding.textTaskCompletionYes.setText(R.string.label_yes);
                    }
                }
            });
            mBinding.textTaskCompletionNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callCompleteTaskWS(Utility.TASK_STATUS.PROCESSING);
                }
            });
            mBinding.textTaskSeekSupport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.initiateCallToCheepHelpLine(mContext);
                }
            });

        } else {
            mBinding.lnTaskCompletionRequested.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setListeners() {
        mBinding.frameSelectPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTaskDetailModel.mMediaModelList != null && !mTaskDetailModel.mMediaModelList.isEmpty()) {

                    SharedElementTransitionHelper sharedElementTransitionHelper = new SharedElementTransitionHelper(TaskSummaryActivity.this);
                    sharedElementTransitionHelper.put(mBinding.imgTaskPicture1, R.string.transition_image_view);
//                    ZoomImageActivity.newInstance(mContext, sharedElementTransitionHelper.getBundle(), mTaskDetailModel.taskImage);
                    MediaViewFullScreenActivity.getInstance(TaskSummaryActivity.this, mTaskDetailModel.mMediaModelList, false);
                }
            }
        });
    }

    private void showFullDesc(String title, String message) {
        if (dialogDesc == null) {
            View view = View.inflate(mContext, R.layout.dialog_information, null);
            txtMessage = (TextView) view.findViewById(R.id.text_message);
            dialogDesc = new BottomAlertDialog(mContext);
            view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDesc.dismiss();
                }
            });
            dialogDesc.setCustomView(view);
        }
        dialogDesc.setTitle(title);
        txtMessage.setText(message);
        dialogDesc.showDialog();
    }

    /**
     * This method would provide some bottom padding to LinearLayout
     */
    public void updateHeightOfLinearLayout(final boolean flag) {
        // Change the Linearlayout bottom Padding
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int paddingBottomInPix = (int) Utility.convertDpToPixel(20, mContext);
                paddingBottomInPix = paddingBottomInPix + mBinding.textBottomAction.getHeight();
                mBinding.lnBottomSection.setPadding(0, 0, 0, flag ? paddingBottomInPix : 0);
            }
        }, 500);

    }

    /**
     * This method would going to update the SP list of images
     *
     * @param list of Providers available for task
     */
    private void updateSPImageStacks(ArrayList<ProviderModel> list) {
        if (list == null || list.isEmpty()) {
            mBinding.textTaskResponseStatus.setText(getResources().getString(R.string.label_pros_around_you_reviewing_desc));
            mBinding.textBottomAction.setVisibility(View.GONE);
            mBinding.textTaskStatusTop.setVisibility(View.GONE);
            updateHeightOfLinearLayout(false);
        } else {
            Log.d(TAG, "updateSPImageStacks() called with: list = [" + list.size() + "]");
            for (int i = 0; i < 5; i++) {
                switch (i) {
                    case 0:
                        if (!list.isEmpty() && list.get(i) != null) {
                            GlideUtility.showCircularImageView(mContext, TAG, mBinding.img1, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                            mBinding.img1.setVisibility(View.VISIBLE);
                        } else {
                            mBinding.img1.setVisibility(View.GONE);
                        }
                        break;
                    case 1:
                        if (list.size() > 1 && list.get(i) != null) {
                            GlideUtility.showCircularImageView(mContext, TAG, mBinding.img2, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                            mBinding.img2.setVisibility(View.VISIBLE);
                        } else {
                            mBinding.img2.setVisibility(View.GONE);
                        }
                        break;
                    case 2:
                        if (list.size() > 2 && list.get(i) != null) {
                            GlideUtility.showCircularImageView(mContext, TAG, mBinding.img3, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                            mBinding.img3.setVisibility(View.VISIBLE);
                        } else {
                            mBinding.img3.setVisibility(View.GONE);
                        }
                        break;
                    case 3:
                        if (list.size() > 3 && list.get(i) != null) {
                            GlideUtility.showCircularImageView(mContext, TAG, mBinding.img4, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                            mBinding.img4.setVisibility(View.VISIBLE);
                        } else {
                            mBinding.img4.setVisibility(View.GONE);
                        }
                        break;
                    case 4:
                        if (list.size() > 4 && list.get(i) != null) {
                            GlideUtility.showCircularImageView(mContext, TAG, mBinding.img5, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                            mBinding.img5.setVisibility(View.VISIBLE);
                        } else {
                            mBinding.img5.setVisibility(View.GONE);
                        }
                        break;
                }
            }

            // Check if list size is more than 5
            if (list.size() > 5) {
                int extra_count = list.size() - 5;
                mBinding.extraProCount.setVisibility(View.VISIBLE);
                mBinding.extraProCount.setText("+" + String.valueOf(extra_count));
            } else {
                mBinding.extraProCount.setVisibility(View.GONE);
            }

            if (list.isEmpty()) {
                mBinding.textTaskResponseStatus.setText(getResources().getString(R.string.label_pros_around_you_reviewing_desc));
                mBinding.textBottomAction.setVisibility(View.GONE);
                mBinding.textTaskStatusTop.setVisibility(View.GONE);
                updateHeightOfLinearLayout(false);
            } else {
                mBinding.textTaskResponseStatus.setText(getResources().getQuantityText(R.plurals.getResponseReceivedString, list.size()));
                mBinding.textBottomAction.setText(getString(R.string.label_view_quotes));
                mBinding.textBottomAction.setVisibility(View.VISIBLE);
                mBinding.textTaskStatusTop.setVisibility(View.VISIBLE);
                mBinding.textBottomAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TaskQuotesActivity.newInstance(mContext, mTaskDetailModel, false);
                    }
                });
                updateHeightOfLinearLayout(true);
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// Rate Dialog ////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////Reject Additional Payment[START] //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Call Task Detail web service
     */

    private void callTaskDetailWS(String taskId) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressBar(true);

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskId);

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.TASK_DETAIL
                , mCallTaskDetailWSErrorListener
                , mCallTaskDetailWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList, Utility.getUniqueTagForNetwork(this, NetworkUtility.WS.TASK_DETAIL));
    }

    /**
     * Showing Progress Bar
     *
     * @param flag whether or not it would get visible
     */
    private void showProgressBar(boolean flag) {
        mBinding.progress.setVisibility(flag ? View.VISIBLE : View.GONE);
        mBinding.lnRoot.setVisibility(flag ? View.GONE : View.VISIBLE);
    }

    /**
     * Call Complete task
     */
    private void callCompleteTaskWS(String status) {

        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, mTaskDetailModel.taskId);
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

    private void showIncompleteTaskDialog() {
        UserDetails user = PreferenceUtility.getInstance(mContext).getUserDetails();
        final BottomAlertDialog dialog = new BottomAlertDialog(mContext);
        dialog.setTitle(getString(R.string.label_task_status));
        dialog.setMessage(getString(R.string.label_thanks_feedback_no, user.userName));
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
                //   HotlineHelper.getInstance(mContext).showConversation(mContext);
                FreshChatHelper.getInstance(mContext).showConversation(mContext);
                dialog.dismiss();
            }
        });

        //Hiding chat dialog as it is not in current phase
        // dialog.hideNegativeButton(true);
        dialog.showDialog();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////Reject Additional Payment[END] //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void showRateDialog() {

        View view = View.inflate(mContext, R.layout.dialog_rate, null);
        final RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);
        final EditText edtMessage = (EditText) view.findViewById(R.id.edit_message);
        final TextView txtLabel = (TextView) view.findViewById(R.id.text_label);
        txtLabel.setText(getString(R.string.label_write_a_review, mTaskDetailModel.selectedProvider.userName));

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

    /**
     * Call Create Task webservice
     */
    private void callAddReviewWS(int rating, String message) {

        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, mTaskDetailModel.selectedProvider.providerId);
        mParams.put(NetworkUtility.TAGS.TASK_ID, mTaskDetailModel.taskId);
        mParams.put(NetworkUtility.TAGS.RATINGS, String.valueOf(rating));
        if (!TextUtils.isEmpty(message)) {
            mParams.put(NetworkUtility.TAGS.MESSAGE, message);
        } else {
            mParams.put(NetworkUtility.TAGS.MESSAGE, Utility.EMPTY_STRING);
        }

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.ADD_REVIEW
                , mCallAddReviewWSErrorListener
                , mCallAddReviewWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////// Get Task Status [START] //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////Accept-Reject Detail Service[Start] //////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void showAdditionalPaymentRejectionDialog() {
        final BottomAlertDialog reasonForAddiPaymentRejectionDialog;
        final DialogChangePhoneNumberBinding dialogChangePhoneNumberBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_change_phone_number, null, false);
        reasonForAddiPaymentRejectionDialog = new BottomAlertDialog(mContext);
        //Fetch User Details from Preference
        dialogChangePhoneNumberBinding.editPhoneNumber.setHint(getString(R.string.label_enter_reason));
        dialogChangePhoneNumberBinding.editPhoneNumber.setInputType(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
        dialogChangePhoneNumberBinding.editPhoneNumber.setMinLines(3);
        dialogChangePhoneNumberBinding.editPhoneNumber.setSingleLine(false);
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(Integer.MAX_VALUE);
        dialogChangePhoneNumberBinding.editPhoneNumber.setFilters(FilterArray);
        dialogChangePhoneNumberBinding.btnUpdate.setText(getString(R.string.label_submit));
        dialogChangePhoneNumberBinding.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String reason = dialogChangePhoneNumberBinding.editPhoneNumber.getText().toString();
                if (!TextUtils.isEmpty(reason)) {
                    callDeclineAdditionalPaymentRequest(reason);
                    reasonForAddiPaymentRejectionDialog.dismiss();
                } else {
                    Utility.showToast(mContext, getString(R.string.label_enter_reason));
                }
            }
        });
        reasonForAddiPaymentRejectionDialog.setTitle(getString(R.string.label_reason));
        reasonForAddiPaymentRejectionDialog.setCustomView(dialogChangePhoneNumberBinding.getRoot());
        reasonForAddiPaymentRejectionDialog.showDialog();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////Accept Detail Service[START] //////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void callDeclineAdditionalPaymentRequest(String reason) {
        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, mTaskDetailModel.taskId);
        mParams.put(NetworkUtility.TAGS.REASON, reason);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.DECLINE_ADDITIONAL_PAYMENT_REQUEST
                , mCallDeclineAdditionalPaymentRequestWSErrorListener
                , mCallDeclineAdditionalPaymentRequestWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.DECLINE_ADDITIONAL_PAYMENT_REQUEST);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.TASK_STATUS_CHANGE:
                mTaskDetailModel.taskStatus = event.taskStatus;
                setUpUI();
                break;
            case Utility.BROADCAST_TYPE.ADDITIONAL_PAYMENT_REQUESTED:
                mTaskDetailModel.taskStatus = event.taskStatus;
                mTaskDetailModel.additionalQuoteAmount = event.additional_quote_amount;
                setUpUI();
                break;
            case Utility.BROADCAST_TYPE.TASK_PROCESSING:
                // Call Task Detail update WS from here so that it can refresh the content.
                if (mTaskDetailModel.taskId.equalsIgnoreCase(event.id)) {
                    callTaskDetailWS(getIntent().getExtras().getString(Utility.Extra.TASK_ID));
                }
                break;
            case Utility.BROADCAST_TYPE.PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN:
                // Finish this activity
                finish();
                break;
            case Utility.BROADCAST_TYPE.TASK_PAID_SUCCESSFULLY:

                //Refresh UI for complete status
//                mTaskDetailModel.taskStatus = event.taskStatus;
//                mTaskDetailModel.isAnyAmountPending = Utility.BOOLEAN.NO;
//                setUpUI();

                LogUtils.LOGE(TAG, "onMessageEvent:taskStatus " + event.taskStatus);
//                if (mTaskDetailModel.taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.COMPLETION_REQUEST)) {
//                    callCompleteTaskWS(Utility.TASK_STATUS.COMPLETION_CONFIRM);
//                } else {
                LogUtils.LOGE(TAG, "onMessageEvent:taskStatus " + event.taskStatus);
                mTaskDetailModel.taskStatus = event.taskStatus;
                mTaskDetailModel.isAnyAmountPending = Utility.BOOLEAN.NO;
                mTaskDetailModel.taskTotalPendingAmount = "0";
                setUpUI();
//                }
                break;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////Accept-Reject Detail Service[End] //////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onDestroy() {
        /*
          Cancel the request as it no longer available
         */
        Volley.getInstance(mContext).getRequestQueue().cancelAll(Utility.getUniqueTagForNetwork(this, NetworkUtility.WS.TASK_DETAIL));
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.ADD_REVIEW);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SP_ADD_TO_FAV);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.PAYMENT);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.TASK_DETAIL);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.ACCEPT_ADDITIONAL_PAYMENT_REQUEST);

        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    /**
     * Calling accept request Web service
     */
    private void callTaskDetailRequestAcceptWS(final String action, String taskID,
                                               final ProviderModel providerModel) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.REQUEST_DETAIL_STATUS, Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskID);
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.ACTION_ON_DETAIL
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

                // Close Progressbar
                hideProgressDialog();

                // Show Toast
                Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
            }
        }
                , new Response.Listener() {
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
                            if (action.equalsIgnoreCase(Utility.ACTION_CHAT)) {
                                TaskChatModel taskChatModel = new TaskChatModel();
                                taskChatModel.categoryName = mTaskDetailModel.categoryModel.catName;
                                taskChatModel.taskDesc = mTaskDetailModel.taskDesc;
                                taskChatModel.taskId = mTaskDetailModel.taskId;
                                taskChatModel.receiverId = FirebaseUtils.getPrefixSPId(providerModel.providerId);
                                taskChatModel.participantName = providerModel.userName;
                                taskChatModel.participantPhotoUrl = providerModel.profileUrl;
                                ChatActivity.newInstance(mContext, taskChatModel);
                            } else if (action.equalsIgnoreCase(Utility.ACTION_CALL)) {
//                                callToOtherUser(mBinding.getRoot(), providerModel.providerId);
                                Utility.openCustomerCareCallDialer(mContext, providerModel.sp_phone_number);
                            }
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                hideProgressDialog();
            }
        }
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }

    private void callAcceptAdditionalPaymentRequest() {
        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, mTaskDetailModel.taskId);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.ACCEPT_ADDITIONAL_PAYMENT_REQUEST
                , mGetTaskStatusWSErrorListener
                , mAcceptAdditionalPaymentRequestWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.ACCEPT_ADDITIONAL_PAYMENT_REQUEST);

    }

    private void manageUnreadBadgeCounterForChat() {
        Log.d(TAG, "manageUnreadBadgeCounterForChat() called");
        // Read task chat unread count from firebase
        String t_sp_u_formattedId = FirebaseUtils.get_T_SP_U_FormattedId(mTaskDetailModel.taskId, mTaskDetailModel.selectedProvider.providerId, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        FirebaseHelper.getRecentChatRef(FirebaseUtils.getPrefixUserId(PreferenceUtility.getInstance(mContext).getUserDetails().userID)).child(t_sp_u_formattedId).child(FirebaseHelper.KEY_UNREADCOUNT).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange() called with: dataSnapshot = [" + dataSnapshot + "]");
                if (dataSnapshot.exists()) {
                    Integer count = dataSnapshot.getValue(Integer.class);
                    Log.d(TAG, "onDataChange() called with: dataSnapshot = Unread Counter [" + count + "]");
                    if (count <= 0) {
                        mBinding.tvChatUnreadCount.setVisibility(View.GONE);
                    } else {
                        mBinding.tvChatUnreadCount.setVisibility(View.VISIBLE);
                        mBinding.tvChatUnreadCount.setText(String.valueOf(count));
                    }
                } else {
                    mBinding.tvChatUnreadCount.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled() called with: databaseError = [" + databaseError + "]");
            }
        });
    }

    /**
     * Call Add to fav
     *
     * @param providerId
     * @param isAddToFav
     */
    private void callAddToFavWS(String providerId, boolean isAddToFav) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

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

}
