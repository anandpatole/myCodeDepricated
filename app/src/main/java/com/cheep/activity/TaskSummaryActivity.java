package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.databinding.ActivityTaskSummaryBinding;
import com.cheep.databinding.DialogChangePhoneNumberBinding;
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
import com.cheep.utils.HotlineHelper;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhavesh on 26/4/17.
 */

public class TaskSummaryActivity extends BaseAppCompatActivity {
    private static final String TAG = "TaskSummaryActivity";
    private ActivityTaskSummaryBinding mActivityTaskSummaryBinding;
    private TaskDetailModel mTaskDetailModel;

    /*public static void getInstance(Context mContext, TaskDetailModel taskDetailModel) {
        Intent intent = new Intent(mContext, TaskSummaryActivity.class);
//        intent.putExtra(Utility.Extra.TASK_DETAIL_MODEL, Utility.getJsonStringFromObject(taskDetailModel));
        mContext.startActivity(intent);
    }*/

    public static void getInstance(Context mContext, String taskId) {
        Intent intent = new Intent(mContext, TaskSummaryActivity.class);
        intent.putExtra(Utility.Extra.TASK_ID, taskId);
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityTaskSummaryBinding = DataBindingUtil.setContentView(this, R.layout.activity_task_summary);
        initiateUI();
        setListeners();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initiateUI() {
        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra(Utility.Extra.TASK_DETAIL_MODEL)) {
                // Fetch Task Detail Model
                mTaskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.TASK_DETAIL_MODEL), TaskDetailModel.class);
            }
        }

        // Setting up Toolbar
        setSupportActionBar(mActivityTaskSummaryBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mActivityTaskSummaryBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
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
            setUpTaskDetails(mTaskDetailModel);
        }

    }

    private void setUpTaskDetails(final TaskDetailModel mTaskDetailModel) {

        // Set category
        mActivityTaskSummaryBinding.textCategoryName.setText(mTaskDetailModel.categoryName != null ? mTaskDetailModel.categoryName : Utility.EMPTY_STRING);

        // Set up image
        Utility.loadImageView(mContext, mActivityTaskSummaryBinding.imgService, mTaskDetailModel.catImage, R.drawable.gradient_black);
        Utility.loadImageView(mContext, mActivityTaskSummaryBinding.imgService, mTaskDetailModel.catImageExtras.thumb, R.drawable.gradient_black);


        // By Default makethe task completion dialog as gone
        showTaskCompletionDialog(false);
        mActivityTaskSummaryBinding.lnTaskCancellation.setVisibility(View.GONE);
        mActivityTaskSummaryBinding.lnRatingSection.setVisibility(View.GONE);
        mActivityTaskSummaryBinding.lnTaskRescheduleRequested.setVisibility(View.GONE);
        mActivityTaskSummaryBinding.lnTaskRescheduleRejected.setVisibility(View.GONE);
        mActivityTaskSummaryBinding.lnTaskAdditionalQuoteRequested.setVisibility(View.GONE);

        //Bydefault show the chat call icons
        showChatCallButton(true);

        // Hide Bottom Action Button
        mActivityTaskSummaryBinding.textBottomAction.setVisibility(View.GONE);
        updateHeightOfLinearLayout(false);


        // Setup First section whether SP is final or not
        if (mTaskDetailModel.selectedProvider == null) {
            // Provider is not final yet, so need to show the nearby available.
            mActivityTaskSummaryBinding.lnResponseReceived.setVisibility(View.VISIBLE);
            mActivityTaskSummaryBinding.lnProviderProfileSection.setVisibility(View.GONE);
            // Update Task Status
            mActivityTaskSummaryBinding.textTaskStatusTop.setText(getString(R.string.label_receiving_quotes));

            // Hide Payment Summary textview
            mActivityTaskSummaryBinding.textViewPaymentSummary.setVisibility(View.GONE);

            updateSPImageStacks(mTaskDetailModel.mQuotedSPList);
        } else {
            // Provider is final.
            mActivityTaskSummaryBinding.lnResponseReceived.setVisibility(View.GONE);
            mActivityTaskSummaryBinding.lnProviderProfileSection.setVisibility(View.VISIBLE);

            // Show Payment Summary textview
            mActivityTaskSummaryBinding.textViewPaymentSummary.setVisibility(View.VISIBLE);

            // Set rating
            Utility.showRating(mTaskDetailModel.selectedProvider.rating, mActivityTaskSummaryBinding.providerRating);

            if (Utility.BOOLEAN.YES.equals(mTaskDetailModel.selectedProvider.isFavourite))
                mActivityTaskSummaryBinding.imgFav.setSelected(true);
            else
                mActivityTaskSummaryBinding.imgFav.setSelected(false);

            mActivityTaskSummaryBinding.imgFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callAddToFavWS(mTaskDetailModel.selectedProvider.providerId, !mActivityTaskSummaryBinding.imgFav.isSelected());
                    mActivityTaskSummaryBinding.imgFav.setSelected(!mActivityTaskSummaryBinding.imgFav.isSelected());

                }
            });

            // Name of Provider
            mActivityTaskSummaryBinding.textProviderName.setText(mTaskDetailModel.selectedProvider.userName);

            SpannableString sName = new SpannableString(mTaskDetailModel.selectedProvider.userName);
            SpannableString sVerified = null;
            if (Utility.BOOLEAN.YES.equalsIgnoreCase(mTaskDetailModel.selectedProvider.isVerified)) {
                sVerified = new SpannableString(" " + mContext.getString(R.string.label_verified_pro) + " ");
                sVerified.setSpan(new RelativeSizeSpan(0.9f), 0, sVerified.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sVerified.setSpan(new RoundedBackgroundSpan(ContextCompat.getColor(this, R.color.splash_gradient_end), ContextCompat.getColor(this, R.color.white)), 0, sVerified.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                mActivityTaskSummaryBinding.textProviderName.setText(sVerified != null ? TextUtils.concat(sName, " ", sVerified) : sName);
            }
            // Distanceof Provider
            mActivityTaskSummaryBinding.textAddressKmAway.setText(mTaskDetailModel.selectedProvider.distance + getString(R.string.label_away));

            // Profile Pic
            Utility.showCircularImageView(mContext, TAG, mActivityTaskSummaryBinding.imgProfile, mTaskDetailModel.selectedProvider.profileUrl, Utility.DEFAULT_CHEEP_LOGO, true);

//            // Whether Provider Verified or not
//            if (Utility.BOOLEAN.YES.equalsIgnoreCase(mTaskDetailModel.selectedProvider.isVerified)) {
//                mActivityTaskSummaryBinding.textProVerified.setVisibility(View.VISIBLE);
//            } else {
//                mActivityTaskSummaryBinding.textProVerified.setVisibility(View.GONE);
//            }

            // Manage Click events of Call & Chat
            mActivityTaskSummaryBinding.lnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: Call");
                    Utility.openCustomerCareCallDialer(mContext, mTaskDetailModel.selectedProvider.sp_phone_number);
                }
            });
            mActivityTaskSummaryBinding.lnChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: Chat");
                    TaskChatModel taskChatModel = new TaskChatModel();
                    taskChatModel.categoryName = mTaskDetailModel.categoryName;
                    taskChatModel.taskDesc = mTaskDetailModel.taskDesc;
                    taskChatModel.taskId = mTaskDetailModel.taskId;
                    taskChatModel.receiverId = FirebaseUtils.getPrefixSPId(mTaskDetailModel.selectedProvider.providerId);
                    taskChatModel.participantName = mTaskDetailModel.selectedProvider.userName;
                    taskChatModel.participantPhotoUrl = mTaskDetailModel.selectedProvider.profileUrl;
                    ChatActivity.newInstance(mContext, taskChatModel);
                }
            });


            // On Click on Payment Summary
            mActivityTaskSummaryBinding.textViewPaymentSummary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Redirect the user to Payment Summary screen.
                    PaymentsStepActivity.newInstance(TaskSummaryActivity.this, mTaskDetailModel, mTaskDetailModel.selectedProvider, true);
                }
            });

            // Manage UI Based on Status
            updateUIBasedOnTaskStatus();

            // Manage UnreadBadge count for Task
            manageUnreadBadgeCounterForChat();
        }

        // Set Second Section
        mActivityTaskSummaryBinding.textSubCategoryName.setText(mTaskDetailModel.subCategoryName);
        mActivityTaskSummaryBinding.textTaskDesc.setText(mTaskDetailModel.taskDesc);
        Utility.loadImageView(mContext, mActivityTaskSummaryBinding.imgTaskPicture, mTaskDetailModel.taskImage, 0);


        // Set Up Third Section WHEN
        /*
          Setting dynamic fields based on current status of task(Job)
         */
        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
        try {
            superCalendar.setTimeInMillis(Long.parseLong(mTaskDetailModel.taskStartdate));
        } catch (Exception e) {
            e.printStackTrace();
        }
        superCalendar.setLocaleTimeZone();
        String task_original_date_time = superCalendar.format(Utility.DATE_FORMAT_DD_MMM + " " + Utility.DATE_FORMAT_HH_MM_AM);
        mActivityTaskSummaryBinding.textTaskWhen.setText(task_original_date_time);

        // Setup WHERE section
        mActivityTaskSummaryBinding.textTaskWhere.setText(mTaskDetailModel.taskAddress);


        // Onclick of when and Where section
        mActivityTaskSummaryBinding.lnTaskDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFullDesc(getString(R.string.label_desc), mActivityTaskSummaryBinding.textTaskDesc.getText().toString());
            }
        });
        mActivityTaskSummaryBinding.lnTaskWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFullDesc(getString(R.string.label_address), mActivityTaskSummaryBinding.textTaskWhere.getText().toString());
            }
        });
    }

    private void updateUIBasedOnTaskStatus() {
        if (Utility.TASK_STATUS.PENDING.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mActivityTaskSummaryBinding.textTaskStatusTop.setText(getString(R.string.task_confirmed));
        } else if (Utility.TASK_STATUS.PROCESSING.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mActivityTaskSummaryBinding.textTaskStatusTop.setText(getString(R.string.task_status_processing));
        } else if (Utility.TASK_STATUS.COMPLETION_REQUEST.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mActivityTaskSummaryBinding.textTaskStatusTop.setText(getString(R.string.task_status_processing));

            // Setup Task Completion Request Dialog
            showTaskCompletionDialog(true);
        } else if (Utility.TASK_STATUS.COMPLETION_CONFIRM.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mActivityTaskSummaryBinding.textTaskStatusTop.setText(getString(R.string.label_task_complete));

            // No need to hide ChatCall Button Now.
            showChatCallButton(false);

            // Check if Rating is done or not
            if (Utility.BOOLEAN.YES.equalsIgnoreCase(mTaskDetailModel.ratingDone)) {
                // Rating Section
                mActivityTaskSummaryBinding.lnRatingSection.setVisibility(View.VISIBLE);
                Utility.showRating(mTaskDetailModel.taskRatings, mActivityTaskSummaryBinding.taskRatingbar);

                // No need to Show bottom action button with rate and review
                mActivityTaskSummaryBinding.textBottomAction.setVisibility(View.GONE);
                mActivityTaskSummaryBinding.textBottomAction.setOnClickListener(null);
                updateHeightOfLinearLayout(false);
            } else {
                // Rating Section
                mActivityTaskSummaryBinding.lnRatingSection.setVisibility(View.GONE);

                // Show bottom action button with rate & review
                mActivityTaskSummaryBinding.textBottomAction.setText(getString(R.string.label_rate_and_review));
                mActivityTaskSummaryBinding.textBottomAction.setVisibility(View.VISIBLE);
                mActivityTaskSummaryBinding.textBottomAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showRateDialog();
                    }
                });
                updateHeightOfLinearLayout(true);
            }
        } else if (Utility.TASK_STATUS.PAID.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mActivityTaskSummaryBinding.textTaskStatusTop.setText(getString(R.string.task_confirmed));
        } else if (Utility.TASK_STATUS.CANCELLED_CUSTOMER.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mActivityTaskSummaryBinding.textTaskStatusTop.setText(getString(R.string.msg_task_cancelled_title));

            // Cancellation Reason
            mActivityTaskSummaryBinding.lnTaskCancellation.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(mTaskDetailModel.taskCancelReason)) {
                mActivityTaskSummaryBinding.textTaskCancellationReason.setText(mTaskDetailModel.taskCancelReason);
            }

            // No need to hide ChatCall Button Now.
            showChatCallButton(false);
        } else if (Utility.TASK_STATUS.CANCELLED_SP.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
//            mActivityTaskSummaryBinding.textTaskStatusTop.setText(getString(R.string.task_was_cancelled_by_x, mTaskDetailModel.selectedProvider.userName));
            mActivityTaskSummaryBinding.textTaskStatusTop.setText(getString(R.string.msg_task_cancelled_title));

            // Cancellation Reason
            mActivityTaskSummaryBinding.lnTaskCancellation.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(mTaskDetailModel.taskCancelReason)) {
                mActivityTaskSummaryBinding.textTaskCancellationReason.setText(mTaskDetailModel.taskCancelReason);
            }

            // No need to hide ChatCall Button Now.
            showChatCallButton(false);
        }
        // reschedule task status
        else if (Utility.TASK_STATUS.RESCHEDULE_REQUESTED.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mActivityTaskSummaryBinding.textTaskStatusTop.setText(getString(R.string.label_reschedule_requested));

            // Reschedule request desc
            mActivityTaskSummaryBinding.lnTaskRescheduleRequested.setVisibility(View.VISIBLE);

            //Calculate Reschedule Date & Time
            SuperCalendar superCalendar = SuperCalendar.getInstance();
            superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            try {
                superCalendar.setTimeInMillis(Long.parseLong(mTaskDetailModel.taskRescheduleDateTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
            superCalendar.setLocaleTimeZone();
            String task_reschedule_date= superCalendar.format(Utility.DATE_FORMAT_DD_MMM );
            String task_reschedule_time = superCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
            String message = getString(R.string.label_reschedule_desc, task_reschedule_date + getString(R.string.label_at)+task_reschedule_time);
            mActivityTaskSummaryBinding.textTaskRescheduleRequestDesc.setText(message);

        }
        //Task's Reschedule request got cancelled
        else if (Utility.TASK_STATUS.RESCHEDULE_REQUEST_REJECTED.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mActivityTaskSummaryBinding.textTaskStatusTop.setText(getString(R.string.label_reschedule_rejected));
            mActivityTaskSummaryBinding.lnTaskRescheduleRejected.setVisibility(View.VISIBLE);

            // Chat & Call with @Cheep team click event of buttons
            mActivityTaskSummaryBinding.textContactCheepViaCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //callToCheepAdmin(mActivityHomeBinding.getRoot());
                    Utility.initiateCallToCheepHelpLine(mContext);
                }
            });

            mActivityTaskSummaryBinding.textContactCheepViaChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HotlineHelper.getInstance(mContext).showConversation(mContext);
                }
            });

            // No need to hide ChatCall Button Now.
            showChatCallButton(false);
        }
        //Task's Additional Payment Request comes
        else if (Utility.TASK_STATUS.ADDITIONAL_PAYMENT_REQUESTED.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mActivityTaskSummaryBinding.textTaskStatusTop.setText(getString(R.string.task_status_processing));

            mActivityTaskSummaryBinding.lnTaskAdditionalQuoteRequested.setVisibility(View.VISIBLE);

            String additionalQuoteAmount = getString(R.string.rupee_symbol_x, mTaskDetailModel.additionalQuoteAmount);
            mActivityTaskSummaryBinding.textAdditionalPaymentDesc.setText(getString(R.string.label_additional_payment_desc, additionalQuoteAmount));

            mActivityTaskSummaryBinding.textAdditionalPaymentAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: Accept Additional Payment");

                    // First Call Asynctask that would going to check whether current status of Progressing or not.
                    callCheckingTaskStatus();


                }
            });

            mActivityTaskSummaryBinding.textAdditionalPaymentDecline.setOnClickListener(new View.OnClickListener() {
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
            mActivityTaskSummaryBinding.lnChatCall.setVisibility(View.VISIBLE);
        } else {
            mActivityTaskSummaryBinding.lnChatCall.setVisibility(View.GONE);
        }
    }

    private void showTaskCompletionDialog(boolean flag) {
        if (flag) {
            mActivityTaskSummaryBinding.lnTaskCompletionRequested.setVisibility(View.VISIBLE);
            mActivityTaskSummaryBinding.textConfirmText.setText(getString(R.string.label_complete_job_confirm, mTaskDetailModel.selectedProvider.userName));
            mActivityTaskSummaryBinding.textTaskCompletionYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callCompleteTaskWS(Utility.TASK_STATUS.COMPLETION_CONFIRM);
                }
            });
            mActivityTaskSummaryBinding.textTaskCompletionNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callCompleteTaskWS(Utility.TASK_STATUS.PROCESSING);
                }
            });
        } else {
            mActivityTaskSummaryBinding.lnTaskCompletionRequested.setVisibility(View.GONE);
        }
    }


    @Override
    protected void setListeners() {
        mActivityTaskSummaryBinding.imgTaskPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mTaskDetailModel.taskImage)) {

                    SharedElementTransitionHelper sharedElementTransitionHelper = new SharedElementTransitionHelper(TaskSummaryActivity.this);
                    sharedElementTransitionHelper.put(mActivityTaskSummaryBinding.imgTaskPicture, R.string.transition_image_view);
                    ZoomImageActivity.newInstance(mContext, sharedElementTransitionHelper.getBundle(), mTaskDetailModel.taskImage);
                }
            }
        });
    }

    private BottomAlertDialog dialogDesc;
    private TextView txtMessage;

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
                paddingBottomInPix = paddingBottomInPix + mActivityTaskSummaryBinding.textBottomAction.getHeight();
                mActivityTaskSummaryBinding.lnBottomSection.setPadding(0, 0, 0, flag ? paddingBottomInPix : 0);
            }
        }, 500);

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Reload SP Listing based on AddressID [START]//////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This method would going to update the SP list of images
     *
     * @param list of Providers available for task
     */
    private void updateSPImageStacks(ArrayList<ProviderModel> list) {
        Log.d(TAG, "updateSPImageStacks() called with: list = [" + list.size() + "]");
        for (int i = 0; i < 5; i++) {
            switch (i) {
                case 0:
                    if (list.size() > 0 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mActivityTaskSummaryBinding.img1, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mActivityTaskSummaryBinding.img1.setVisibility(View.VISIBLE);
                    } else {
                        mActivityTaskSummaryBinding.img1.setVisibility(View.GONE);
                    }
                    break;
                case 1:
                    if (list.size() > 1 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mActivityTaskSummaryBinding.img2, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mActivityTaskSummaryBinding.img2.setVisibility(View.VISIBLE);
                    } else {
                        mActivityTaskSummaryBinding.img2.setVisibility(View.GONE);
                    }
                    break;
                case 2:
                    if (list.size() > 2 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mActivityTaskSummaryBinding.img3, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mActivityTaskSummaryBinding.img3.setVisibility(View.VISIBLE);
                    } else {
                        mActivityTaskSummaryBinding.img3.setVisibility(View.GONE);
                    }
                    break;
                case 3:
                    if (list.size() > 3 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mActivityTaskSummaryBinding.img4, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mActivityTaskSummaryBinding.img4.setVisibility(View.VISIBLE);
                    } else {
                        mActivityTaskSummaryBinding.img4.setVisibility(View.GONE);
                    }
                    break;
                case 4:
                    if (list.size() > 4 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mActivityTaskSummaryBinding.img5, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mActivityTaskSummaryBinding.img5.setVisibility(View.VISIBLE);
                    } else {
                        mActivityTaskSummaryBinding.img5.setVisibility(View.GONE);
                    }
                    break;
            }
        }

        // Check if list size is more than 5
        if (list.size() > 5) {
            int extra_count = list.size() - 5;
            mActivityTaskSummaryBinding.extraProCount.setVisibility(View.VISIBLE);
            mActivityTaskSummaryBinding.extraProCount.setText("+" + String.valueOf(extra_count));
        } else {
            mActivityTaskSummaryBinding.extraProCount.setVisibility(View.GONE);
        }

        // Awaiting Response
        if (list.size() == 0) {
            mActivityTaskSummaryBinding.textTaskResponseStatus.setText(getResources().getString(R.string.label_pros_around_you_reviewing_desc));
            mActivityTaskSummaryBinding.textBottomAction.setVisibility(View.GONE);
            mActivityTaskSummaryBinding.textTaskStatusTop.setVisibility(View.GONE);
            updateHeightOfLinearLayout(false);
        } else {
            mActivityTaskSummaryBinding.textTaskResponseStatus.setText(getResources().getQuantityText(R.plurals.getResponseReceivedString, list.size()));
            mActivityTaskSummaryBinding.textBottomAction.setText(getString(R.string.label_view_quotes));
            mActivityTaskSummaryBinding.textBottomAction.setVisibility(View.VISIBLE);
            mActivityTaskSummaryBinding.textTaskStatusTop.setVisibility(View.VISIBLE);
            mActivityTaskSummaryBinding.textBottomAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TaskQuotesActivity.newInstance(mContext, mTaskDetailModel, false);
                }
            });
            updateHeightOfLinearLayout(true);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Reload SP Listing based on AddressID [END]//////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Task Detail Service[Start] ////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Call Task Detail web service
     */
    private void callTaskDetailWS(String taskId) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityTaskSummaryBinding.getRoot());
            return;
        }

        showProgressBar(true);

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

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

                        JSONObject jsonData = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);

                        mTaskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);

                        setUpTaskDetails(mTaskDetailModel);

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityTaskSummaryBinding.getRoot());
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
            showProgressBar(false);

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskSummaryBinding.getRoot());

        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////// Task Detail WS[END] ///////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Showing Progress Bar
     *
     * @param flag whether or not it would get visible
     */
    private void showProgressBar(boolean flag) {
        mActivityTaskSummaryBinding.progress.setVisibility(flag ? View.VISIBLE : View.GONE);
        mActivityTaskSummaryBinding.lnRoot.setVisibility(flag ? View.GONE : View.VISIBLE);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////// Task Completion Yes /////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Call Complete task
     */
    private void callCompleteTaskWS(String status) {

        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityTaskSummaryBinding.getRoot());
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
                                Utility.showSnackBar(getString(R.string.msg_thanks_for_confirmation), mActivityTaskSummaryBinding.getRoot());

                                /*
                                  Update the UI Accordingly.
                                 */
                                mTaskDetailModel.taskStatus = taskStatus;

                                //Refresh UI for Paid status
                                setUpTaskDetails(mTaskDetailModel);

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
                                setUpTaskDetails(mTaskDetailModel);

                                /*
                                   Show Information Dialog about getting Cheep Help
                                 */
                                showIncompleteTaskDialog();
                            }
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityTaskSummaryBinding.getRoot());
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
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskSummaryBinding.getRoot());
        }
    };

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
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////// Task Completion WS [END] ///////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////  Rate Dialog //////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    BottomAlertDialog rateDialog;

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
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityTaskSummaryBinding.getRoot());
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
                        Utility.showSnackBar(getString(R.string.msg_thanks_for_rating), mActivityTaskSummaryBinding.getRoot());
                        if (rateDialog != null)
                            rateDialog.dismiss();
                        mTaskDetailModel.ratingDone = Utility.BOOLEAN.YES;
                        mTaskDetailModel.taskRatings = jsonObject.optString(NetworkUtility.TAGS.TASK_RATINGS);
                        // Update the UI According to Updated Model.
                        setUpTaskDetails(mTaskDetailModel);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// Rate Dialog ////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////Reject Additional Payment[START] //////////////////////////////////////
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

    private void callDeclineAdditionalPaymentRequest(String reason) {
        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityTaskSummaryBinding.getRoot());
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
                        Utility.showSnackBar(jsonObject.getString(NetworkUtility.TAGS.MESSAGE), mActivityTaskSummaryBinding.getRoot());
                       /* Utility.showSnackBar(getString(R.string.msg_thanks_for_confirmation), mActivityJobSummaryBinding.getRoot());
                        mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);
                        showRateDialog();*/

                        mTaskDetailModel.taskStatus = Utility.TASK_STATUS.PROCESSING;
                        setUpTaskDetails(mTaskDetailModel);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityTaskSummaryBinding.getRoot());
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

    Response.ErrorListener mCallDeclineAdditionalPaymentRequestWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskSummaryBinding.getRoot());
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////Reject Additional Payment[END] //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_STATUS_CHANGE) {
            mTaskDetailModel.taskStatus = event.taskStatus;
            setUpTaskDetails(mTaskDetailModel);
        } else if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.ADDITIONAL_PAYMENT_REQUESTED) {
            mTaskDetailModel.taskStatus = event.taskStatus;
            mTaskDetailModel.additionalQuoteAmount = event.additional_quote_amount;
            setUpTaskDetails(mTaskDetailModel);
        } else if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_PROCESSING) {
            // Call Task Detail update WS from here so that it can refresh the content.
            if (mTaskDetailModel.taskId.equalsIgnoreCase(event.id)) {
                callTaskDetailWS(getIntent().getExtras().getString(Utility.Extra.TASK_ID));
            }
        } else if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN) {
            // Finish this activity
            finish();
        }
    }

    @Override
    protected void onDestroy() {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////// Get Task Status [START] //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////Accept-Reject Detail Service[Start] //////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Calling accept request Web service
     */
    private void callTaskDetailRequestAcceptWS(final String action, String taskID, final ProviderModel providerModel) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityTaskSummaryBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

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
                Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskSummaryBinding.getRoot());
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
                                taskChatModel.categoryName = mTaskDetailModel.categoryName;
                                taskChatModel.taskDesc = mTaskDetailModel.taskDesc;
                                taskChatModel.taskId = mTaskDetailModel.taskId;
                                taskChatModel.receiverId = FirebaseUtils.getPrefixSPId(providerModel.providerId);
                                taskChatModel.participantName = providerModel.userName;
                                taskChatModel.participantPhotoUrl = providerModel.profileUrl;
                                ChatActivity.newInstance(mContext, taskChatModel);
                            } else if (action.equalsIgnoreCase(Utility.ACTION_CALL)) {
//                                callToOtherUser(mActivityTaskSummaryBinding.getRoot(), providerModel.providerId);
                                Utility.openCustomerCareCallDialer(mContext, providerModel.sp_phone_number);
                            }
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                            // Show Toast
                            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskSummaryBinding.getRoot());
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                            error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                            // Show message
                            Utility.showSnackBar(error_message, mActivityTaskSummaryBinding.getRoot());
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////Accept-Reject Detail Service[End] //////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    private void callCheckingTaskStatus() {
        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityTaskSummaryBinding.getRoot());
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
        mParams.put(NetworkUtility.TAGS.TASK_ID, mTaskDetailModel.taskId);

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
            hideProgressDialog();
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
                            // payNow(true);
                            PaymentsStepActivity.newInstance(mContext, mTaskDetailModel, mTaskDetailModel.selectedProvider, 1);
                        } else if (taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.COMPLETION_REQUEST)) {
                            Utility.showSnackBar(getString(R.string.message_no_more_payment_task_completed), mActivityTaskSummaryBinding.getRoot());
                            mTaskDetailModel.taskStatus = Utility.TASK_STATUS.COMPLETION_REQUEST;
                            setUpTaskDetails(mTaskDetailModel);

                        }
//                        Utility.showSnackBar(jsonObject.getString(NetworkUtility.TAGS.MESSAGE), mActivityJobSummaryBinding.getRoot());
                       /* Utility.showSnackBar(getString(R.string.msg_thanks_for_confirmation), mActivityJobSummaryBinding.getRoot());
                        mActivityJobSummaryBinding.layoutStatusConfirmationRequired.setVisibility(View.GONE);
                        showRateDialog();*/

//                        hideProgressDialog();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskSummaryBinding.getRoot());
                        hideProgressDialog();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityTaskSummaryBinding.getRoot());
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
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskSummaryBinding.getRoot());
        }
    };

    private void manageUnreadBadgeCounterForChat() {
        Log.d(TAG, "manageUnreadBadgeCounterForChat() called");
        // Read task chat unread count from firebase
        String t_sp_u_formattedId = FirebaseUtils.get_T_SP_U_FormattedId(mTaskDetailModel.taskId, mTaskDetailModel.selectedProvider.providerId, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);
        FirebaseHelper.getRecentChatRef(FirebaseUtils.getPrefixUserId(PreferenceUtility.getInstance(mContext).getUserDetails().UserID)).child(t_sp_u_formattedId).child(FirebaseHelper.KEY_UNREADCOUNT).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange() called with: dataSnapshot = [" + dataSnapshot + "]");
                if (dataSnapshot.exists()) {
                    Integer count = dataSnapshot.getValue(Integer.class);
                    Log.d(TAG, "onDataChange() called with: dataSnapshot = Unread Counter [" + count + "]");
                    if (count <= 0) {
                        mActivityTaskSummaryBinding.tvChatUnreadCount.setVisibility(View.GONE);
                    } else {
                        mActivityTaskSummaryBinding.tvChatUnreadCount.setVisibility(View.VISIBLE);
                        mActivityTaskSummaryBinding.tvChatUnreadCount.setText(String.valueOf(count));
                    }
                } else {
                    mActivityTaskSummaryBinding.tvChatUnreadCount.setVisibility(View.GONE);
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
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityTaskSummaryBinding.getRoot());
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
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskSummaryBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityTaskSummaryBinding.getRoot());
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


            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskSummaryBinding.getRoot());

        }
    };

}
