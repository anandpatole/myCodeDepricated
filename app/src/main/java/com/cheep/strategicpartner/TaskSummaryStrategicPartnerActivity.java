package com.cheep.strategicpartner;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
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
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.ChatActivity;
import com.cheep.activity.MediaViewFullScreenActivity;
import com.cheep.activity.TaskQuotesActivity;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.databinding.ActivityTaskSummaryStrategicPartnerBinding;
import com.cheep.databinding.DialogChangePhoneNumberBinding;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.strategicpartner.model.SubSubCatModel;
import com.cheep.utils.FreshChatHelper;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.HotlineHelper;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.RoundedBackgroundSpan;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;
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

public class TaskSummaryStrategicPartnerActivity extends BaseAppCompatActivity {
    private static final String TAG = "TaskSummaryStrategicPar";
    private ActivityTaskSummaryStrategicPartnerBinding mBinding;
    private TaskDetailModel mTaskDetailModel;

    /*public static void getInstance(Context mContext, TaskDetailModel taskDetailModel) {
        Intent intent = new Intent(mContext, TaskSummaryActivity.class);
//        intent.putExtra(Utility.Extra.TASK_DETAIL_MODEL, Utility.getJsonStringFromObject(taskDetailModel));
        mContext.startActivity(intent);
    }*/

    public static void getInstance(Context mContext, String taskId) {
        Intent intent = new Intent(mContext, TaskSummaryStrategicPartnerActivity.class);
        intent.putExtra(Utility.Extra.TASK_ID, taskId);
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_task_summary_strategic_partner);
        initiateUI();
        setListeners();
        EventBus.getDefault().register(this);
    }

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
            setUpTaskDetails(mTaskDetailModel);
        }

    }

    private void setUpTaskDetails(final TaskDetailModel mTaskDetailModel) {

        // Set category
        mBinding.textCategoryName.setText(mTaskDetailModel.categoryModel.catName != null ? mTaskDetailModel.categoryModel.catName : Utility.EMPTY_STRING);

        // Set up image
//        GlideUtility.loadImageView(mContext, mBinding.imgService, mTaskDetailModel.bannerImage);
//        GlideUtility.loadImageView(mContext, mBinding.imgService, mTaskDetailModel.catImageExtras.thumb);


        // By Default makethe task completion dialog as gone
        showTaskCompletionDialog(false);
        mBinding.lnTaskCancellation.setVisibility(View.GONE);
        mBinding.lnRatingSection.setVisibility(View.GONE);
        mBinding.lnTaskRescheduleRequested.setVisibility(View.GONE);
        mBinding.lnTaskRescheduleRejected.setVisibility(View.GONE);
        mBinding.lnTaskAdditionalQuoteRequested.setVisibility(View.GONE);


        if (mTaskDetailModel.mMediaModelList != null && !mTaskDetailModel.mMediaModelList.isEmpty()) {
            if (mTaskDetailModel.mMediaModelList.size() > 1)
                mBinding.tvCounter.setText(getString(R.string.label_plus) + (mTaskDetailModel.mMediaModelList.size() - 1));
            else
                mBinding.tvCounter.setVisibility(View.GONE);


            GlideUtility.loadImageView(this, mBinding.imgTaskPicture, mTaskDetailModel.mMediaModelList.get(0).mediaThumbName);

        } else
            mBinding.frameSelectPicture.setVisibility(View.GONE);


        mBinding.frameSelectPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaViewFullScreenActivity.getInstance(TaskSummaryStrategicPartnerActivity.this, mTaskDetailModel.mMediaModelList, true);
            }
        });


        //Bydefault show the chat call icons
        showChatCallButton(true);
        // Hide Bottom Action Button
        mBinding.textBottomAction.setVisibility(View.GONE);
        updateHeightOfLinearLayout(false);

        // Setup First section whether SP is final or not
        if (mTaskDetailModel.selectedProvider == null) {
            // Provider is not final yet, so need to show the nearby available.
            mBinding.lnResponseReceived.setVisibility(View.VISIBLE);
            mBinding.lnProviderProfileSection.setVisibility(View.GONE);
            // Update Task Status
            mBinding.textTaskStatusTop.setText(getString(R.string.label_receiving_quotes));

            // Hide Payment Summary textview
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.GONE);
            updateSPImageStacks(mTaskDetailModel.mQuotedSPList);
        } else {
            // Provider is final.
            mBinding.lnResponseReceived.setVisibility(View.GONE);
            mBinding.lnProviderProfileSection.setVisibility(View.VISIBLE);

            // Show Payment Summary text view
            mBinding.textViewPaymentSummary.setVisibility(View.VISIBLE);

            mBinding.textViewPaymentSummary.setVisibility(View.VISIBLE);
            String s = "";
            if (!TextUtils.isEmpty(mTaskDetailModel.isAnyAmountPending))
                s = mTaskDetailModel.isAnyAmountPending.equalsIgnoreCase(Utility.BOOLEAN.YES) ? getString(R.string.label_not_paid) : getString(R.string.label_paid);

            mBinding.textPaid.setVisibility(View.VISIBLE);
            mBinding.textPaid.setText("(" + s + ")");

            // Set rating
            Utility.showRating(mTaskDetailModel.selectedProvider.rating, mBinding.providerRating);

            // Name of Provider
            mBinding.textProviderName.setText(mTaskDetailModel.categoryModel.catName);
            SpannableString sName = new SpannableString(mTaskDetailModel.categoryModel.catName);

            SpannableString sVerified = new SpannableString(" " + mContext.getString(R.string.label_partner_pro) + " ");
            sVerified.setSpan(new RelativeSizeSpan(0.9f), 0, sVerified.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sVerified.setSpan(new RoundedBackgroundSpan(ContextCompat.getColor(this, R.color.splash_gradient_end), ContextCompat.getColor(this, R.color.white), 0), 0, sVerified.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            mBinding.textProviderName.setText(TextUtils.concat(sName, " ", sVerified));
            // Distance of Provider
            mBinding.textAddressKmAway.setText(mTaskDetailModel.selectedProvider.distance + getString(R.string.label_away));

            // Profile Pic
            GlideUtility.showCircularImageViewWithColorBorder(this, TAG, mBinding.imgProfile, mTaskDetailModel.categoryModel.catImageExtras.original, Utility.DEFAULT_CHEEP_LOGO, R.color.grey_dark_color, true);

            // Manage Click events of Call & Chat
            mBinding.lnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtils.LOGI(TAG, "onClick: Call");
                    Utility.showToast(TaskSummaryStrategicPartnerActivity.this, getString(R.string.label_wrok_in_progress));
//                    Utility.openCustomerCareCallDialer(mContext, mTaskDetailModel.selectedProvider.sp_phone_number);
                }
            });
            mBinding.lnChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtils.LOGI(TAG, "onClick: Chat");
                    Utility.showToast(TaskSummaryStrategicPartnerActivity.this, getString(R.string.label_wrok_in_progress));
//                    TaskChatModel taskChatModel = new TaskChatModel();
//                    taskChatModel.categoryName = mTaskDetailModel.categoryName;
//                    taskChatModel.taskDesc = mTaskDetailModel.taskDesc;
//                    taskChatModel.taskId = mTaskDetailModel.taskId;
//                    taskChatModel.receiverId = FirebaseUtils.getPrefixSPId(mTaskDetailModel.selectedProvider.providerId);
//                    taskChatModel.participantName = mTaskDetailModel.selectedProvider.userName;
//                    taskChatModel.participantPhotoUrl = mTaskDetailModel.selectedProvider.profileUrl;
//                    ChatActivity.newInstance(mContext, taskChatModel);
                }
            });


            // On Click on Payment Summary
            mBinding.textViewPaymentSummary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PaymentsSummaryStrategicPartnerActivity.newInstance(TaskSummaryStrategicPartnerActivity.this, mTaskDetailModel);
                }
            });

            // Manage UI Based on Status
            updateUIBasedOnTaskStatus();

            // Manage UnreadBadge count for Task
            manageUnreadBadgeCounterForChat();

            setSelectedServicesDetails();
        }


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
        String task_original_date_time = superCalendar.format(Utility.DATE_FORMAT_DD_MMM_YYYY + " " + Utility.DATE_FORMAT_HH_MM_AM);
        mBinding.textTaskWhen.setText(task_original_date_time);

        // Setup WHERE section
        mBinding.textTaskWhere.setText(mTaskDetailModel.taskAddress.address);


        // Onclick of when and Where section
//        mBinding.lnTaskDesc.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showFullDesc(getString(R.string.label_desc), mBinding.textTaskDesc.getText().toString());
//            }
//        });
        mBinding.lnTaskWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFullDesc(getString(R.string.label_address), mBinding.textTaskWhere.getText().toString());
            }
        });

        // Update the banner image
        // Calculat Pager Height and Width
        ViewTreeObserver mViewTreeObserver = mBinding.frameBannerImage.getViewTreeObserver();
        mViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.frameBannerImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = mBinding.frameBannerImage.getMeasuredWidth();
                ViewGroup.LayoutParams params = mBinding.frameBannerImage.getLayoutParams();
                params.height = Utility.getHeightFromWidthForTwoOneRatio(width);
                mBinding.frameBannerImage.setLayoutParams(params);

                // Load the image now.
                GlideUtility.loadImageView(mContext, mBinding.imgService, mTaskDetailModel.bannerImage, R.drawable.gradient_black);
            }
        });
    }

    private void setSelectedServicesDetails() {
        if (mTaskDetailModel.subCatList != null && !mTaskDetailModel.subCatList.isEmpty()) {
            ArrayList<SubServiceDetailModel> subSubCategoryList = mTaskDetailModel.subCatList;

            SubServiceDetailModel serviceTaskDetailModel2 = subSubCategoryList.get(0);
            mBinding.textSubCategoryName.setText(serviceTaskDetailModel2.name);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            for (SubSubCatModel subSubCat : serviceTaskDetailModel2.subSubCatModels) {
                if (spannableStringBuilder.length() == 0) {
                    spannableStringBuilder.append(getSpannableString(subSubCat.subSubCatName, ContextCompat.getColor(this, R.color.grey_varient_2), false));
                } else {
                    spannableStringBuilder.append(getSpannableString(" + ", ContextCompat.getColor(this, R.color.dark_blue_variant_1), false));
                    spannableStringBuilder.append(getSpannableString(subSubCat.subSubCatName, ContextCompat.getColor(this, R.color.grey_varient_2), false));
                }
            }
            mBinding.textSubSubCategoryName.setText(spannableStringBuilder);

//            HashMap<String, String> stringStringHashMap = new HashMap<>();
//            for (int i = 1; i < subSubCategoryList.size(); i++) {
//                SubSubCatModel subSubCat = subSubCategoryList.get(i);
//                if (!stringStringHashMap.containsKey(subSubCat.subCategoryName)) {
//                    stringStringHashMap.put(subSubCat.subCategoryName, subSubCat.subSubCatName);
//                } else {
//                    stringStringHashMap.put(subSubCat.subCategoryName, stringStringHashMap.get(subSubCat.subCategoryName) + " + " + subSubCat.subSubCatName);
//                }
//            }

            mBinding.lnTaskDetails.removeAllViews();

            for (int i = 1; i < mTaskDetailModel.subCatList.size(); i++) {
                SubServiceDetailModel subServiceDetailModel = subSubCategoryList.get(i);
                View view = LayoutInflater.from(this).inflate(R.layout.layout_selected_service_task_summary, null);
                TextView textSubCategoryName = view.findViewById(R.id.text_sub_category_name);
                TextView textSubSubCategoryName = view.findViewById(R.id.text_sub_sub_category_name);
                textSubCategoryName.setText(subServiceDetailModel.name);
                SpannableStringBuilder spannableStringBuilder1 = new SpannableStringBuilder();
                for (SubSubCatModel subSubCat : subServiceDetailModel.subSubCatModels) {
                    if (spannableStringBuilder1.length() == 0) {
                        spannableStringBuilder1.append(getSpannableString(subSubCat.subSubCatName, ContextCompat.getColor(this, R.color.grey_varient_2), false));
                    } else {
                        spannableStringBuilder1.append(getSpannableString(" + ", ContextCompat.getColor(this, R.color.dark_blue_variant_1), false));
                        spannableStringBuilder1.append(getSpannableString(subSubCat.subSubCatName, ContextCompat.getColor(this, R.color.grey_varient_2), false));
                    }

                }
                textSubSubCategoryName.setText(spannableStringBuilder1);
                mBinding.lnTaskDetails.addView(view);
            }

            if (mTaskDetailModel.categoryModel.catName.equalsIgnoreCase(Utility.STRATEGIC_PARTNER_BRAND.VLCC)) {
                View view = LayoutInflater.from(this).inflate(R.layout.layout_selected_service_task_summary, null);
                TextView textSubCategoryName = view.findViewById(R.id.text_sub_category_name);
                TextView textSubSubCategoryName = view.findViewById(R.id.text_sub_sub_category_name);
                textSubCategoryName.setText(R.string.label_service_required_for);

                if (mTaskDetailModel.mQuesList != null && !mTaskDetailModel.mQuesList.isEmpty())
                    textSubSubCategoryName.setText(mTaskDetailModel.mQuesList.get(0).answer);
                mBinding.lnTaskDetails.addView(view);
            }

            View view = LayoutInflater.from(this).inflate(R.layout.layout_selected_service_task_summary, null);
            TextView textSubCategoryName = view.findViewById(R.id.text_sub_category_name);
            TextView textSubSubCategoryName = view.findViewById(R.id.text_sub_sub_category_name);
            textSubCategoryName.setText(R.string.special_instructions);
            textSubSubCategoryName.setText(mTaskDetailModel.taskDesc);
            mBinding.lnTaskDetails.addView(view);

        }

    }

    private SpannableStringBuilder getSpannableString(String string, int color, boolean isBold) {
        SpannableStringBuilder text = new SpannableStringBuilder(string);
        text.setSpan(new ForegroundColorSpan(color), 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (isBold) {
            text.setSpan(new StyleSpan(Typeface.BOLD), 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    private void updateUIBasedOnTaskStatus() {
        if (Utility.TASK_STATUS.PENDING.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.task_confirmed));
            mBinding.textViewPaymentSummary.setVisibility(View.VISIBLE);
            mBinding.textPaid.setVisibility(View.VISIBLE);

        } else if (Utility.TASK_STATUS.PROCESSING.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.task_status_processing));
            mBinding.textViewPaymentSummary.setVisibility(View.VISIBLE);
            mBinding.textPaid.setVisibility(View.VISIBLE);

        } else if (Utility.TASK_STATUS.COMPLETION_REQUEST.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.task_status_processing));
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.GONE);

            // Setup Task Completion Request Dialog
            showTaskCompletionDialog(true);
        } else if (Utility.TASK_STATUS.COMPLETION_CONFIRM.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.label_task_complete));
            mBinding.textViewPaymentSummary.setVisibility(View.VISIBLE);
            mBinding.textPaid.setVisibility(View.VISIBLE);

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
            mBinding.textViewPaymentSummary.setVisibility(View.VISIBLE);
            mBinding.textPaid.setVisibility(View.VISIBLE);
        } else if (Utility.TASK_STATUS.CANCELLED_CUSTOMER.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.msg_task_cancelled_title));
            mBinding.textViewPaymentSummary.setVisibility(View.VISIBLE);
            mBinding.textPaid.setVisibility(View.VISIBLE);

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
            mBinding.textViewPaymentSummary.setVisibility(View.VISIBLE);
            mBinding.textPaid.setVisibility(View.VISIBLE);

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
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.VISIBLE);

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
            mBinding.lnTaskRescheduleRejected.setVisibility(View.VISIBLE);
            mBinding.textViewPaymentSummary.setVisibility(View.VISIBLE);
            mBinding.textPaid.setVisibility(View.VISIBLE);

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
                   // HotlineHelper.getInstance(mContext).showConversation(mContext);
                    FreshChatHelper.getInstance(mContext).showConversation(mContext);
                }
            });

            // No need to hide ChatCall Button Now.
            showChatCallButton(false);
        }
        //Task's Additional Payment Request comes
        else if (Utility.TASK_STATUS.ADDITIONAL_PAYMENT_REQUESTED.equalsIgnoreCase(mTaskDetailModel.taskStatus)) {
            mBinding.textTaskStatusTop.setText(getString(R.string.task_status_processing));

            mBinding.lnTaskAdditionalQuoteRequested.setVisibility(View.VISIBLE);
            mBinding.textViewPaymentSummary.setVisibility(View.GONE);
            mBinding.textPaid.setVisibility(View.GONE);

            String additionalQuoteAmount = getString(R.string.rupee_symbol_x, mTaskDetailModel.additionalQuoteAmount);
            mBinding.textAdditionalPaymentDesc.setText(getString(R.string.label_additional_payment_desc, additionalQuoteAmount));

            mBinding.textAdditionalPaymentAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtils.LOGI(TAG, "onClick: Accept Additional Payment");

                    // First Call Asynctask that would going to check whether current status of Progressing or not.
                    callCheckingTaskStatus();


                }
            });

            mBinding.textAdditionalPaymentDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtils.LOGI(TAG, "onClick: Decline Additional Payment");
                    showAdditionalPaymentRejectionDialog();
                }
            });
        }
    }

    /**
     * This would make the Chat Call Visible/Invisible
     *
     * @param flag not used
     */
    private void showChatCallButton(boolean flag) {
        // TODO :: changed on sept 12
//        if (flag) {
//            mBinding.lnChatCall.setVisibility(View.VISIBLE);
//        } else {
        mBinding.lnChatCall.setVisibility(View.GONE);
//        }

    }

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
                        PaymentsSummaryStrategicPartnerActivity.newInstance(mContext, mTaskDetailModel);
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
                paddingBottomInPix = paddingBottomInPix + mBinding.textBottomAction.getHeight();
                mBinding.lnBottomSection.setPadding(0, 0, 0, flag ? paddingBottomInPix : 0);
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
        LogUtils.LOGD(TAG, "updateSPImageStacks() called with: list = [" + list.size() + "]");
        for (int i = 0; i < 5; i++) {
            switch (i) {
                case 0:
                    if (list.size() > 0 && list.get(i) != null) {
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

        // Awaiting Response
        if (list.size() == 0) {
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

    Response.Listener mCallTaskDetailWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                LogUtils.LOGI(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                showProgressBar(false);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        JSONObject jsonData = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);

                        mTaskDetailModel = (TaskDetailModel) GsonUtility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);

                        setUpTaskDetails(mTaskDetailModel);
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
                mCallTaskDetailWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    Response.ErrorListener mCallTaskDetailWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            showProgressBar(false);

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());

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
        mBinding.progress.setVisibility(flag ? View.VISIBLE : View.GONE);
        mBinding.lnRoot.setVisibility(flag ? View.GONE : View.VISIBLE);
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

    Response.Listener mCallCompleteTaskWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                LogUtils.LOGI(TAG, "onResponse: " + jsonObject.toString());
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

    Response.ErrorListener mCallCompleteTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

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
              //  HotlineHelper.getInstance(mContext).showConversation(mContext);
                FreshChatHelper.getInstance(mContext).showConversation(mContext);
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
        txtLabel.setText(getString(R.string.label_write_a_review, mTaskDetailModel.categoryModel.catName));

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
    private void callAddReviewWS(final int rating, String message) {

        //Validation


        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        WebCallClass.submitReviewWS(this, mTaskDetailModel.taskId, mTaskDetailModel.selectedProvider.providerId, String.valueOf(rating), message, Utility.EMPTY_STRING, new WebCallClass.SubmitRateAndReviewListener() {
            @Override
            public void onSuccessOfRateAndReviewSubmit() {
                Utility.showSnackBar(getString(R.string.msg_thanks_for_rating), mBinding.getRoot());
                if (rateDialog != null)
                    rateDialog.dismiss();
                mTaskDetailModel.ratingDone = Utility.BOOLEAN.YES;
                mTaskDetailModel.taskRatings = String.valueOf(rating);
                // Update the UI According to Updated Model.
                setUpTaskDetails(mTaskDetailModel);

            }
        }, errorListener);


    /*      UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

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
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);*/
    }

    private WebCallClass.CommonResponseListener errorListener = new WebCallClass.CommonResponseListener() {
        @Override
        public void volleyError(VolleyError error) {
            hideProgressDialog();
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }

        @Override
        public void showSpecificMessage(String message) {
            hideProgressDialog();
            Utility.showSnackBar(message, mBinding.getRoot());
        }

        @Override
        public void forceLogout() {
            hideProgressDialog();
            finish();
        }
    };

   /* Response.ErrorListener mCallAddReviewWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
        }
    };*/

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

    Response.Listener mCallDeclineAdditionalPaymentRequestWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                LogUtils.LOGI(TAG, "onResponse: " + jsonObject.toString());
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
                        setUpTaskDetails(mTaskDetailModel);
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

    Response.ErrorListener mCallDeclineAdditionalPaymentRequestWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////Reject Additional Payment[END] //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        LogUtils.LOGD(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.TASK_STATUS_CHANGE:
                mTaskDetailModel.taskStatus = event.taskStatus;
                setUpTaskDetails(mTaskDetailModel);
                break;
            case Utility.BROADCAST_TYPE.ADDITIONAL_PAYMENT_REQUESTED:
                mTaskDetailModel.taskStatus = event.taskStatus;
                mTaskDetailModel.additionalQuoteAmount = event.additional_quote_amount;
                setUpTaskDetails(mTaskDetailModel);
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
                LogUtils.LOGE(TAG, "onMessageEvent:taskStatus " + event.taskStatus);
                if (mTaskDetailModel.taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.COMPLETION_REQUEST)) {
                    callCompleteTaskWS(Utility.TASK_STATUS.COMPLETION_CONFIRM);
                } else {
                    mTaskDetailModel.taskStatus = event.taskStatus;
                    mTaskDetailModel.isAnyAmountPending = Utility.BOOLEAN.NO;
                    mTaskDetailModel.taskTotalPendingAmount = "0";
                    setUpTaskDetails(mTaskDetailModel);
                }
                break;
        }

    }

    @Override
    protected void onDestroy() {
        /*
          Cancel the request as it no longer available
         */
        Volley.getInstance(mContext).getRequestQueue().cancelAll(Utility.getUniqueTagForNetwork(this, NetworkUtility.WS.TASK_DETAIL));
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.ADD_REVIEW);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.ACCEPT_ADDITIONAL_PAYMENT_REQUEST);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.DECLINE_ADDITIONAL_PAYMENT_REQUEST);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.CHANGE_TASK_STATUS);
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
                LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");

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
                    LogUtils.LOGI(TAG, "onResponse: " + jsonObject.toString());
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////Accept-Reject Detail Service[End] //////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    private void callCheckingTaskStatus() {
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
                LogUtils.LOGI(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        JSONObject jData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
                        String taskStatus = jData.getString(NetworkUtility.TAGS.TASK_STATUS);

                        if (taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.ADDITIONAL_PAYMENT_REQUESTED)) {
                            // payNow(true);
//                            PaymentsStepActivity.newInstance(mContext, mTaskDetailModel, mTaskDetailModel.selectedProvider, 1);
                        } else if (taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.COMPLETION_REQUEST)) {
                            Utility.showSnackBar(getString(R.string.message_no_more_payment_task_completed), mBinding.getRoot());
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

    Response.ErrorListener mGetTaskStatusWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    private void manageUnreadBadgeCounterForChat() {
        LogUtils.LOGD(TAG, "manageUnreadBadgeCounterForChat() called");
        // Read task chat unread count from firebase
        String t_sp_u_formattedId = FirebaseUtils.get_T_SP_U_FormattedId(mTaskDetailModel.taskId, mTaskDetailModel.selectedProvider.providerId, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        FirebaseHelper.getRecentChatRef(FirebaseUtils.getPrefixUserId(PreferenceUtility.getInstance(mContext).getUserDetails().userID)).child(t_sp_u_formattedId).child(FirebaseHelper.KEY_UNREADCOUNT).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogUtils.LOGD(TAG, "onDataChange() called with: dataSnapshot = [" + dataSnapshot + "]");
                if (dataSnapshot.exists()) {
                    Integer count = dataSnapshot.getValue(Integer.class);
                    LogUtils.LOGD(TAG, "onDataChange() called with: dataSnapshot = Unread Counter [" + count + "]");
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
                LogUtils.LOGD(TAG, "onCancelled() called with: databaseError = [" + databaseError + "]");
            }
        });
    }

}
