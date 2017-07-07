package com.cheep.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.adapter.MyTaskRecyclerViewAdapter;
import com.cheep.adapter.ReviewsRecyclerViewAdapter;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.ActivityProfileBinding;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.model.CoverImageModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.ReviewModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SharedElementTransitionHelper;
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

import static com.cheep.network.NetworkUtility.WS.REVIEW_LIST;
import static com.facebook.login.widget.ProfilePictureView.TAG;

/**
 * Created by pankaj on 10/6/16.
 */

public class ProviderProfileActivity extends BaseAppCompatActivity implements ReviewsRecyclerViewAdapter.ReviewRowInteractionListener {

    private static final String TAG = "ProviderProfileActivity";

    private ActivityProfileBinding mActivityProviderProfileBinding;
    private ProviderModel providerModel;
    private TaskDetailModel taskDetailModel;
    private ErrorLoadingHelper errorLoadingHelper;
    private ReviewsRecyclerViewAdapter reviewsRecyclerViewAdapter;
    private boolean isRefresh = true;
    private boolean reportAbuse = false;
    private String taskId;
    private String providerID;
    private Context mContext;
    private LinearLayoutManager linearLayoutManager;

    // Only to review Provider Profile
    public static void newInstance(Context context, ProviderModel model) {
        Intent intent = new Intent(context, ProviderProfileActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(model));
        context.startActivity(intent);
    }

    // Provider Profile for specific task
    public static void newInstance(Context context, ProviderModel model, TaskDetailModel taskDetailModel) {
        Intent intent = new Intent(context, ProviderProfileActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(model));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        context.startActivity(intent);
    }

    // Provider Profile from Notification Click
    public static void newInstance(Context context, String taskId, String provideId) {
        Intent intent = new Intent(context, ProviderProfileActivity.class);
        intent.putExtra(NetworkUtility.TAGS.TASK_ID, taskId);
        intent.putExtra(NetworkUtility.TAGS.SP_USER_ID, provideId);
        intent.putExtra(Utility.Extra.IS_FIRST_TIME, false);
        context.startActivity(intent);
    }

    // Provider Profile from Favorite List Click
    public static void newInstance(Context context, ProviderModel model, boolean fromfavorite) {
        Intent intent = new Intent(context, ProviderProfileActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(model));
        intent.putExtra(Utility.Extra.PROFILE_FROM_FAVOURITE, fromfavorite);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityProviderProfileBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        mContext = this;
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {
        // Setting toolbar
        setSupportActionBar(mActivityProviderProfileBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mActivityProviderProfileBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    onBackPressed();
                }
            });
        }
        linearLayoutManager = new LinearLayoutManager(mContext);
        //This will prevent auto focus to starting of recycler view
        mActivityProviderProfileBinding.recyclerView.setFocusable(false);
        //This will remove the jitter while scrolling (Recycler view inside NestedScrollView)
        mActivityProviderProfileBinding.recyclerView.setNestedScrollingEnabled(false);
        //Setting recycler view adapter
        mActivityProviderProfileBinding.recyclerView.setLayoutManager(linearLayoutManager);

        reviewsRecyclerViewAdapter = new ReviewsRecyclerViewAdapter(this);
        mActivityProviderProfileBinding.recyclerView.setAdapter(reviewsRecyclerViewAdapter);
        //Set dividers to Recyclerview
        mActivityProviderProfileBinding.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal));
        errorLoadingHelper = new ErrorLoadingHelper(mActivityProviderProfileBinding.recyclerView);
        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            if (!getIntent().hasExtra(Utility.Extra.DATA)) {

                if (getIntent().hasExtra(NetworkUtility.TAGS.TASK_ID)) {
                    taskId = bundle.getString(NetworkUtility.TAGS.TASK_ID);
                    providerID = bundle.getString(NetworkUtility.TAGS.SP_USER_ID);
                    showProgressDialog();
                    callSPProfileDetailWS(providerID);
                    callReviewList(providerID);
                }
            } else {
                providerModel = (ProviderModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), ProviderModel.class);
                if (getIntent().hasExtra(Utility.Extra.DATA_2)) {
                    //This is only when provider profile view for specific task (provider gives quote to specific task)
                    taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), TaskDetailModel.class);

                }
                setData();

                callSPProfileDetailWS(providerModel.providerId);
                errorLoadingHelper.showLoading();
                callReviewList(providerModel.providerId);
            }
        }
    }

    public void setData() {
        mActivityProviderProfileBinding.textName.setText(providerModel.userName);
        mActivityProviderProfileBinding.textExpectedTime.setText(providerModel.sp_locality + ", " + providerModel.distance + " away");
        mActivityProviderProfileBinding.textExpectedTime.setSelected(true);

        if (taskDetailModel != null) {
            if (!TextUtils.isEmpty(taskDetailModel.categoryName)) {
                mActivityProviderProfileBinding.textCategory.setText(taskDetailModel.categoryName);
            } else {
                mActivityProviderProfileBinding.textCategory.setVisibility(View.GONE);
            }
        }

        if (!TextUtils.isEmpty(providerModel.isVerified) && providerModel.isVerified.equalsIgnoreCase("yes")) {
            mActivityProviderProfileBinding.textVerified.setVisibility(View.VISIBLE);
        } else {
            mActivityProviderProfileBinding.textVerified.setVisibility(View.GONE);
        }
        /*mActivityProviderProfileBinding.textVerifiedTotalJobs.setText((Utility.BOOLEAN.YES.equalsIgnoreCase(providerModel.isVerified) ? getString(R.string.label_verified) : getString(R.string.label_pending)));
        mActivityProviderProfileBinding.textVerifiedTotalJobs.setText(mActivityProviderProfileBinding.textVerifiedTotalJobs.getText().toString() + " | " + Utility.getJobs(mContext, providerModel.jobsCount));*/


        if (!TextUtils.isEmpty(providerModel.reviews) && Double.parseDouble(providerModel.reviews) > 0) {
            mActivityProviderProfileBinding.textTotalReviews.setText(getString(R.string.label_basedon, providerModel.reviews));
            mActivityProviderProfileBinding.layoutReview.setVisibility(View.VISIBLE);
        } else {
            mActivityProviderProfileBinding.layoutReview.setVisibility(View.GONE);
        }
        mActivityProviderProfileBinding.imgFav.setSelected(Utility.BOOLEAN.YES.equals(providerModel.isFavourite));

        //loading rounded image on profile
        //  Utility.showCircularImageView(mContext, TAG, mActivityProviderProfileBinding.imgProfile, providerModel.profileUrl, Utility.DEFAULT_PROFILE_SRC, true);
        Utility.loadImageView(mContext, mActivityProviderProfileBinding.imgProfile, providerModel.profileUrl, Utility.DEFAULT_PROFILE_SRC);

        if (!TextUtils.isEmpty(providerModel.information)) {
            mActivityProviderProfileBinding.textDesc.setText(providerModel.information);
            mActivityProviderProfileBinding.textDesc.setVisibility(View.VISIBLE);
        } else
            mActivityProviderProfileBinding.textDesc.setVisibility(View.GONE);


        mActivityProviderProfileBinding.textDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFullDesc(getString(R.string.label_desc), mActivityProviderProfileBinding.textDesc.getText().toString());
            }
        });


        mActivityProviderProfileBinding.layoutPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PaymentsStepActivity.newInstance(mContext, taskDetailModel, providerModel, 0);
            }
        });

        // Checking if amount present then show call and paid lables else hide
        if (providerModel.getQuotePriceInInteger() > 0) {
            mActivityProviderProfileBinding.layoutPay.setVisibility(View.VISIBLE);
            mActivityProviderProfileBinding.textPrice.setText(getString(R.string.label_pay_X, providerModel.quotePrice));
            mActivityProviderProfileBinding.textPrice.setSelected(true);
        } else {
            mActivityProviderProfileBinding.layoutPay.setVisibility(View.GONE);
        }


        /**
         * TODO: If in case in future, need to enable Chat call feature we just need to comment below portion
         */
        ////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////// Managing Chat Call Icons[Start] ////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////
        /*mActivityProviderProfileBinding.lnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (providerModel != null && !TextUtils.isEmpty(providerModel.providerId) && taskDetailModel != null) {
                    if (providerModel.request_detail_status.equalsIgnoreCase(Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED)) {
                        Utility.openCustomerCareCallDialer(mContext, providerModel.sp_phone_number);
                        return;
                    }
                    callTaskDetailRequestAcceptWS(Utility.ACTION_CALL, taskDetailModel.taskId, providerModel);
                }
            }
        });
        mActivityProviderProfileBinding.lnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ALREADY_REQUESTED.equalsIgnoreCase(providerModel.request_detail_status)) {
                    showDetailRequestDialog(providerModel);
                } else {
                    if (providerModel != null && taskDetailModel != null) {
                        if (providerModel.request_detail_status.equalsIgnoreCase(Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED)) {
                            TaskChatModel taskChatModel = new TaskChatModel();
                            taskChatModel.categoryName = taskDetailModel.categoryName;
                            taskChatModel.taskDesc = taskDetailModel.taskDesc;
                            taskChatModel.taskId = taskDetailModel.taskId;
                            taskChatModel.receiverId = FirebaseUtils.getPrefixSPId(providerModel.providerId);
                            taskChatModel.participantName = providerModel.userName;
                            taskChatModel.participantPhotoUrl = providerModel.profileUrl;
                            ChatActivity.newInstance(mContext, taskChatModel);
                            return;
                        }
                        callTaskDetailRequestAcceptWS(Utility.ACTION_CHAT, taskDetailModel.taskId, providerModel);
                    }
                }
            }
        });

        // Update the UI
        updateChatUIBasedOnCurrentRequestStatus();

        // Set Listner for Unread Counter
        manageUnreadBadgeCounterForChat();

        */
        ////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////// Managing Chat Call Icons[End] ///////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////

        if (getIntent().hasExtra(Utility.Extra.PROFILE_FROM_FAVOURITE) == true) {


//            TODO: If in case in future, need to enable Chat call feature we just need to comment
//            TODO: below portion
            /*mActivityProviderProfileBinding.lnCall.setVisibility(View.GONE);
            mActivityProviderProfileBinding.lnChat.setVisibility(View.GONE);*/


            mActivityProviderProfileBinding.textCategory.setVisibility(View.GONE);
            mActivityProviderProfileBinding.layoutPay.setVisibility(View.GONE);
        }

        if (EventBus.getDefault().isRegistered(this) == false)
            EventBus.getDefault().register(this);


        initSwipeToRefreshLayout();


    }

    /**
     * TODO: If in case in future, need to enable Chat call feature we just need to comment below portion
     */
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Managing Chat Call Icons[Start] ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    /*private void manageUnreadBadgeCounterForChat() {
        // Read task chat unread count from firebase
        String t_sp_u_formattedId = FirebaseUtils.get_T_SP_U_FormattedId(taskDetailModel.taskId, providerModel.providerId, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);
        FirebaseHelper.getTaskChatRef(FirebaseUtils.getPrefixTaskId(taskDetailModel.taskId)).child(t_sp_u_formattedId).child(FirebaseHelper.KEY_UNREADCOUNT).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer count = dataSnapshot.getValue(Integer.class);
                    Log.d(TAG, "onDataChange() called with: dataSnapshot = Unread Counter [" + count + "]");
                    if (count <= 0) {
                        mActivityProviderProfileBinding.tvChatUnreadCount.setVisibility(View.GONE);
                    } else {
                        mActivityProviderProfileBinding.tvChatUnreadCount.setVisibility(View.VISIBLE);
                        mActivityProviderProfileBinding.tvChatUnreadCount.setText(String.valueOf(count));
                    }
                } else {
                    mActivityProviderProfileBinding.tvChatUnreadCount.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateChatUIBasedOnCurrentRequestStatus() {
        Log.d(TAG, "updateChatUIBasedOnCurrentRequestStatus() called");
        if (providerModel == null) {
            return;
        }
        if (Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ALREADY_REQUESTED.equalsIgnoreCase(providerModel.request_detail_status)) {
            //chat icon
            Glide.with(mContext)
                    .load(R.drawable.ic_chat_requested_animation_with_counter)
                    .asGif()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(mActivityProviderProfileBinding.imgChat);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.icon_chat_smaller)
                    .into(mActivityProviderProfileBinding.imgChat);
        }
    }

    */
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Managing Chat Call Icons[End] ///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////


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

            dialogDesc.setTitle(getString(R.string.label_desc));
            dialogDesc.setCustomView(view);
        }
        txtMessage.setText(message);
        dialogDesc.showDialog();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.e("onMessageEvent", "" + event.BROADCAST_ACTION);
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.UPDATE_COMMENT_COUNT) {
            if (!TextUtils.isEmpty(event.id) && !TextUtils.isEmpty(event.commentCount))
                reviewsRecyclerViewAdapter.updateCommentCounter(event.id, event.commentCount);
        } else if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_PAID
                || event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_PROCESSING) {
            finish();
//            initiateUI();
        } else if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.QUOTE_REQUESTED_BY_PRO
                || event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.REQUEST_FOR_DETAIL) {
            // Only go ahead if we are in same task detail screen whose notification comes
            if (taskId != null && taskId.equals(event.id)) {
                showProgressDialog();
                callSPProfileDetailWS(providerID);
                callReviewList(providerID);
            }
        }
    }

    private void initSwipeToRefreshLayout() {
        mActivityProviderProfileBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                reviewsRecyclerViewAdapter.disableLoadMore();
                errorLoadingHelper.showLoading();
                isRefresh = true;
                callSPProfileDetailWS(providerModel.providerId);
                callReviewList(providerModel.providerId);
            }
        });
        Utility.setSwipeRefreshLayoutColors(mActivityProviderProfileBinding.swipeRefreshLayout);
    }

    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean isLoading;

    @Override
    protected void setListeners() {
        mActivityProviderProfileBinding.imgReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (reportAbuse == false) {
                showReportDialog();
//                }
            }
        });
        mActivityProviderProfileBinding.imgFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callAddToFavWS(providerModel.providerId, !mActivityProviderProfileBinding.imgFav.isSelected());
                mActivityProviderProfileBinding.imgFav.setSelected(!mActivityProviderProfileBinding.imgFav.isSelected());

                //Sending Broadcast so hirenewjobactivity listen and change like icons
                MessageEvent messageEvent = new MessageEvent();
                messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.UPDATE_FAVOURITE;
                messageEvent.id = providerModel.providerId;
                messageEvent.isFav = mActivityProviderProfileBinding.imgFav.isSelected() ? Utility.BOOLEAN.YES : Utility.BOOLEAN.NO;
                EventBus.getDefault().post(messageEvent);
            }
        });

        mActivityProviderProfileBinding.nestedscrollview.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView nestedScrollView, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.e("scrollY", "->" + scrollY);
                Log.e("1st view nested height", "->" + nestedScrollView.getChildAt(0).getMeasuredHeight());
                Log.e("total nested height", "->" + nestedScrollView.getMeasuredHeight());
                Log.e("total recycleview", "->" + mActivityProviderProfileBinding.recyclerView.getMeasuredHeight());
                int diff = (nestedScrollView.getChildAt(0).getMeasuredHeight() - nestedScrollView.getMeasuredHeight()) - scrollY;
                Log.e("diff", "->" + diff);
                if (diff <= 20) {
                    Log.e("on load more", "load more");
                    int count = reviewsRecyclerViewAdapter.getmList().size();
                    String reviewId = reviewsRecyclerViewAdapter.getmList().get(count - 1).reviewId;
                    callLoadMoreReviewList(providerModel.providerId, reviewId);
                }

            }
        });
    }

    BottomAlertDialog reportDialog;

    private void showReportDialog() {
        reportDialog = new BottomAlertDialog(mContext);
        reportDialog.setTitle(reportAbuse ? getString(R.string.tab_unreport) : getString(R.string.tab_report));
        reportDialog.setMessage(reportAbuse ? getString(R.string.label_un_report_user_msg) : getString(R.string.label_report_user_msg));
        reportDialog.addPositiveButton(getString(R.string.label_yes), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callReportSPWS(!reportAbuse);
//                dialog.dismiss();
            }
        });
        reportDialog.addNegativeButton(getString(R.string.label_no), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportDialog.dismiss();
            }
        });
        reportDialog.showDialog();
    }

    @Override
    public void onReviewRowClicked(ReviewModel reviewModel, int position) {
        CommentsActivity.newInstance(mContext, reviewModel, providerModel.userName);
    }

    LinearLayoutManager linearLayoutManagerPastWork;
    MyTaskRecyclerViewAdapter myTaskRecyclerViewAdapter;

    private void setupCoverViewPager(ArrayList<CoverImageModel> mBannerListModels) {
        Log.d(TAG, "setupCoverViewPager() called with: mBannerListModels = [" + mBannerListModels.size() + "]");
        /*CoverViewPagerAdapter coverViewPagerAdapter = new CoverViewPagerAdapter(getSupportFragmentManager(), mBannerListModels);
        mActivityProviderProfileBinding.viewPagerBannerImage.setAdapter(coverViewPagerAdapter);
        mActivityProviderProfileBinding.indicator.setViewPager(mActivityProviderProfileBinding.viewPagerBannerImage);
        if (mBannerListModels != null && mBannerListModels.size() > 1) {
            mActivityProviderProfileBinding.indicator.setVisibility(View.VISIBLE);
        } else {
            mActivityProviderProfileBinding.indicator.setVisibility(View.GONE);
        }*/

        /*mBannerListModels = new ArrayList<>();
        for(int i=0;i<10;i++) {
            mBannerListModels.add(new CoverImageModel());
        }*/
        linearLayoutManagerPastWork = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mActivityProviderProfileBinding.recyclerviewPastWork.setLayoutManager(linearLayoutManagerPastWork);
        myTaskRecyclerViewAdapter = new MyTaskRecyclerViewAdapter(this, mBannerListModels, new CategoryRowInteractionListener() {
            @Override
            public void onCategoryRowClicked(CoverImageModel model, int position) {
                if (!TextUtils.isEmpty(model.imgUrl)) {
                    View view = linearLayoutManagerPastWork.getChildAt(position);
                    ImageView imgpastwork = (ImageView) view.findViewById(R.id.imgpastwork);
                    SharedElementTransitionHelper sharedElementTransitionHelper = new SharedElementTransitionHelper(ProviderProfileActivity.this);
                    sharedElementTransitionHelper.put(imgpastwork, R.string.transition_image_view);
                    ZoomImageActivity.newInstance(mContext, sharedElementTransitionHelper.getBundle(), model.imgUrl);
                }
            }
        });
        mActivityProviderProfileBinding.recyclerviewPastWork.setAdapter(myTaskRecyclerViewAdapter);
        if (mBannerListModels.size() > 0) {
            mActivityProviderProfileBinding.llpastwork.setVisibility(View.VISIBLE);
        } else {
            mActivityProviderProfileBinding.llpastwork.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this) == true) {
            EventBus.getDefault().unregister(this);
        }

        /*
          Cancel the request as it no longer available
         */
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SP_ADD_TO_FAV);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SP_PROFILE_DETAILS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.REVIEW_LIST);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.REPORT_SP);
        super.onDestroy();

    }


    /**
     * Call Report User to Admin
     */
    private void callReportSPWS(boolean addToAbuse) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityProviderProfileBinding.getRoot());
            return;
        }

        //Showing progress dialog
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        mParams.put(NetworkUtility.TAGS.REPORT_ABUSE, addToAbuse ? Utility.ADD : Utility.REMOVE);

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.REPORT_SP
                , mCallReportSPWSErrorListener
                , mCallReportSPWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }

    Response.Listener mCallReportSPWSResponseListener = new Response.Listener() {
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

                        reportAbuse = !reportAbuse;
                        mActivityProviderProfileBinding.imgReport.setImageResource(reportAbuse ? R.drawable.icon_flag_red : R.drawable.icon_flag);
                        if (reportDialog != null) {
                            reportDialog.dismiss();
                        }

                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        Utility.showToast(mContext, error_message);

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityProviderProfileBinding.getRoot());
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mActivityProviderProfileBinding.getRoot());
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
                mCallReportSPWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    Response.ErrorListener mCallReportSPWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            hideProgressDialog();


//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityProviderProfileBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));

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
            Utility.showSnackBar(getString(R.string.no_internet), mActivityProviderProfileBinding.getRoot());
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
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityProviderProfileBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityProviderProfileBinding.getRoot());
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


            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityProviderProfileBinding.getRoot());

        }
    };


    /**
     * Calling Get SP list web service from server
     */

    private void callSPProfileDetailWS(String providerId) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityProviderProfileBinding.getRoot());
            return;
        }

        //Show Progress
//        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerId);

        /*
           Send TaskID from EXTRA in case TaskDetail Model is NULL.
         */
        if (taskDetailModel != null) {
            mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);
        } else {
            if (taskId != null) {
                mParams.put(NetworkUtility.TAGS.TASK_ID, taskId);
            }
        }

        /*//if address id is greater then 0 then it means we need to update the existing address so sending address_id as parameter also
        if (!"0".equalsIgnoreCase(addressId)) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, String.valueOf(addressId));
        }*/

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPDetail = new VolleyNetworkRequest(NetworkUtility.WS.SP_PROFILE_DETAILS
                , mCallSPListWSErrorListener
                , mCallSPListResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPDetail);
    }

    Response.Listener mCallSPListResponseListener = new Response.Listener() {
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

                        providerModel = (ProviderModel) Utility.getObjectFromJsonString(jsonObject.getString(NetworkUtility.TAGS.DATA), ProviderModel.class);

                        JSONObject jsonData = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);
                        ArrayList<CoverImageModel> coverImageModelArrayList = Utility.getObjectListFromJsonString(jsonData.optString(NetworkUtility.TAGS.SP_EXTRA_IMAGES), CoverImageModel[].class);
                        setupCoverViewPager(coverImageModelArrayList);

                        reportAbuse = Utility.BOOLEAN.YES.equalsIgnoreCase(providerModel.spReported);

                        if (!TextUtils.isEmpty(providerModel.information)) {
                            mActivityProviderProfileBinding.textDesc.setText(providerModel.information);
                            mActivityProviderProfileBinding.textDesc.setVisibility(View.VISIBLE);
                        } else
                            mActivityProviderProfileBinding.textDesc.setVisibility(View.GONE);

                        mActivityProviderProfileBinding.imgReport.setImageResource(reportAbuse ? R.drawable.icon_flag_red : R.drawable.icon_flag);

                        Utility.showRating(providerModel.rating, mActivityProviderProfileBinding.ratingBar);
                        if (getIntent().hasExtra(NetworkUtility.TAGS.TASK_ID) == true) {
                            callTaskDetailWS(taskId, providerID);
                        } else {
                            setData();
                        }


                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityProviderProfileBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityProviderProfileBinding.getRoot());
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
                mCallSPListWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    Response.ErrorListener mCallSPListWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityProviderProfileBinding.getRoot());

        }
    };

    public interface CategoryRowInteractionListener {
        void onCategoryRowClicked(CoverImageModel model, int position);
    }

    /**
     * Calling Get SP list web service from server
     *
     * @param categoryId
     * @param cityId
     */
    VolleyNetworkRequest mVolleyNetworkRequestForReviewList;

    private void reloadReviewListWS() {
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForReviewList);
    }

    private void callReviewList(String providerId) {
        if (!Utility.isConnected(mContext)) {
//            Utility.showSnackBar(getString(R.string.no_internet), mActivityProviderProfileBinding.getRoot());
            errorLoadingHelper.failed(getString(R.string.no_internet), 0, onRetryBtnClickListener);
            return;
        }

        //Show Progress
//        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerId);
//        mParams.put(NetworkUtility.TAGS.LAST_ID, providerId); //for pagination

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        mVolleyNetworkRequestForReviewList = new VolleyNetworkRequest(REVIEW_LIST
                , mCallReviewListWSErrorListener
                , mCallReviewListWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForReviewList);
    }


    /**
     * @param providerId
     * @param reviewId
     * @chirag this method is used when user scroll to last index for pagination.
     */
    private void callLoadMoreReviewList(String providerId, String reviewId) {
        if (!Utility.isConnected(mContext)) {
//            Utility.showSnackBar(getString(R.string.no_internet), mActivityProviderProfileBinding.getRoot());
            errorLoadingHelper.failed(getString(R.string.no_internet), 0, onRetryBtnClickListener);
            return;
        }

        //Show Progress
//        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerId);
        mParams.put(NetworkUtility.TAGS.LAST_ID, reviewId); //for pagination

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        mVolleyNetworkRequestForReviewList = new VolleyNetworkRequest(REVIEW_LIST
                , mCallReviewListWSErrorListener
                , mCallReviewListWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForReviewList);
    }

    View.OnClickListener onRetryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            reloadReviewListWS();
        }
    };
    Response.Listener mCallReviewListWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                mActivityProviderProfileBinding.swipeRefreshLayout.setRefreshing(false);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        ArrayList<ReviewModel> reviewList = Utility.getObjectListFromJsonString(jsonObject.getString(NetworkUtility.TAGS.DATA), ReviewModel[].class);

                        //Setting SP List recycler view adapter
                        if (reviewsRecyclerViewAdapter == null) {
                            reviewsRecyclerViewAdapter = new ReviewsRecyclerViewAdapter(ProviderProfileActivity.this);
                        }

                        //For Pull to refresh
                        if (isRefresh)
                            reviewsRecyclerViewAdapter.setList(reviewList);
                        else { //for load more and
                            reviewsRecyclerViewAdapter.addToList(reviewList);
                        }

                        isRefresh = false;

                        if (reviewsRecyclerViewAdapter.getmList() != null && reviewsRecyclerViewAdapter.getmList().size() > 0) {
                            errorLoadingHelper.success();
                        } else {
//                            errorLoadingHelper.failed(getString(R.string.label_no_reviews), 0, getString(R.string.label_refresh), onRetryBtnClickListener);
                            errorLoadingHelper.failed(getString(R.string.label_no_reviews), 0, null, null);
//                            errorLoadingHelper.failed(null, R.drawable.img_empty_reviews, null, null);
                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityProviderProfileBinding.getRoot());
                        errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mActivityProviderProfileBinding.getRoot());
                        errorLoadingHelper.failed(error_message, 0, onRetryBtnClickListener);
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
                mCallReviewListWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    Response.ErrorListener mCallReviewListWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            hideProgressDialog();

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityProviderProfileBinding.getRoot());
            mActivityProviderProfileBinding.swipeRefreshLayout.setRefreshing(false);

            errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);


        }
    };

    /*
      Display Contact Request Popup by (Chirag)
     */

    /**
     * In case Task is Getting Detail Request
     */
    BottomAlertDialog badForRequestTaskDetail;

    private void showDetailRequestDialog(final ProviderModel providerModel) {
        badForRequestTaskDetail = new BottomAlertDialog(mContext);
        badForRequestTaskDetail.setTitle(getString(R.string.label_action));
        badForRequestTaskDetail.setMessage(getString(R.string.desc_detail_action_request, providerModel.userName));
        badForRequestTaskDetail.addPositiveButton(getString(R.string.label_accept), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick() called with: view = [" + view + "]");
//                callReportSPWS(!reportAbuse);
//                dialog.dismiss();
//                providerModel.request_detail_status = Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED;
//                spRecyclerViewAdapter.updateModelForRequestDetailStatus(providerModel);

                callTaskDetailRequestAcceptRejectWS(Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED, taskDetailModel.taskId, providerModel.providerId);
                badForRequestTaskDetail.dismiss();
            }
        });
        badForRequestTaskDetail.addNegativeButton(getString(R.string.label_decline), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callTaskDetailRequestAcceptRejectWS(Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.REJECTED, taskDetailModel.taskId, providerModel.providerId);
                badForRequestTaskDetail.dismiss();
            }
        });
        badForRequestTaskDetail.showDialog();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////Accept-Reject Detail Service[Start] ///////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void showDialogOnRequestForDetailAccepted(String acknowledgeMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        // Add the buttons
        builder.setPositiveButton(R.string.label_Ok_small, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.dismiss();

                // spRecyclerViewAdapter.updateModelForRequestDetailStatus(spUserID, requestDatailStatus);
                // TODO: It needs to update here.
//                mActivityProviderProfileBinding.textContactRequest.setVisibility(View.GONE);
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.setTitle(mContext.getString(R.string.app_name).toUpperCase());
        dialog.setMessage(acknowledgeMessage);
        dialog.show();
    }

    /**
     * Calling delete address Web service
     */
    private void callTaskDetailRequestAcceptRejectWS(String requestDetailStatus, String taskID, String spUserID) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityProviderProfileBinding.getRoot());
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
        mParams.put(NetworkUtility.TAGS.REQUEST_DETAIL_STATUS, requestDetailStatus);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskID);
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, spUserID);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.ACTION_ON_DETAIL
                , mCallActionOnDetailWSErrorListener
                , mCallActionOnDetailWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }

    Response.Listener mCallActionOnDetailWSResponseListener = new Response.Listener() {
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
//                        providerModel.request_detail_status = Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED;
                        JSONObject jObjData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
                        String task_id = jObjData.getString(NetworkUtility.TAGS.TASK_ID);
                        String spUserID = jObjData.getString(NetworkUtility.TAGS.SP_USER_ID);
                        String spUserName = jObjData.optString(NetworkUtility.TAGS.SP_USER_NAME);
                        String quoted_sp_image_url = jObjData.getString(NetworkUtility.TAGS.QUOTED_SP_IMAGE_URL);
                        String requestDatailStatus = jObjData.getString(NetworkUtility.TAGS.REQUEST_DETAIL_STATUS);
                        if (requestDatailStatus.equalsIgnoreCase(Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED)) {
                            providerModel.request_detail_status = requestDatailStatus;

                            String descriptionForAcknowledgement = mContext.getString(R.string.desc_request_for_detail_accepted_acknowledgment, spUserName);
                            showDialogOnRequestForDetailAccepted(descriptionForAcknowledgement);

                            // Need to pass this details to Pending listing as well.
                            MessageEvent messageEvent = new MessageEvent();
                            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.DETAIL_REQUEST_ACCEPTED;
                            messageEvent.id = task_id;
                            messageEvent.spUserId = spUserID;
                            messageEvent.quoted_sp_image_url = quoted_sp_image_url;
                            messageEvent.request_detail_status = requestDatailStatus;
                            EventBus.getDefault().post(messageEvent);

                            //TODO: If in case in future, need to enable Chat call feature we just need to comment below portion
                            //updateChatUIBasedOnCurrentRequestStatus();

                        } else {
                            providerModel.request_detail_status = requestDatailStatus;

                            // Need to pass this details to Pending listing as well.
                            MessageEvent messageEvent = new MessageEvent();
                            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.DETAIL_REQUEST_REJECTED;
                            messageEvent.id = task_id;
                            messageEvent.spUserId = spUserID;
                            messageEvent.quoted_sp_image_url = quoted_sp_image_url;
                            messageEvent.request_detail_status = requestDatailStatus;
                            EventBus.getDefault().post(messageEvent);

                            //TODO: If in case in future, need to enable Chat call feature we just need to comment below portion
                            //updateChatUIBasedOnCurrentRequestStatus();

                            /*// Update recycler view
                            spRecyclerViewAdapter.removeModelForRequestDetailStatus(spUserID, requestDatailStatus);

                            // Check if listing is empty now, display message
                            if (spRecyclerViewAdapter.getmList().size() == 0) {
                                errorLoadingHelper.failed(getString(R.string.label_no_quotes_available), 0, null, null);
                            }*/
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityProviderProfileBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityProviderProfileBinding.getRoot());
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
                mCallActionOnDetailWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallActionOnDetailWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityProviderProfileBinding.getRoot());
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////Accept-Reject Detail Service[End] //////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Calling accept request Web service
     */
    private void callTaskDetailRequestAcceptWS(final String action, String taskID, final ProviderModel providerModel) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityProviderProfileBinding.getRoot());
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
                Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityProviderProfileBinding.getRoot());
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
                                taskChatModel.categoryName = taskDetailModel.categoryName;
                                taskChatModel.taskDesc = taskDetailModel.taskDesc;
                                taskChatModel.taskId = taskDetailModel.taskId;
                                taskChatModel.receiverId = FirebaseUtils.getPrefixSPId(providerModel.providerId);
                                taskChatModel.participantName = providerModel.userName;
                                taskChatModel.participantPhotoUrl = providerModel.profileUrl;
                                ChatActivity.newInstance(ProviderProfileActivity.this, taskChatModel);
                            } else if (action.equalsIgnoreCase(Utility.ACTION_CALL)) {
//                                callToOtherUser(mActivityProviderProfileBinding.getRoot(), providerModel.providerId);
                                Utility.openCustomerCareCallDialer(mContext, providerModel.sp_phone_number);
                            }
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                            // Show Toast
                            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityProviderProfileBinding.getRoot());
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                            error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                            // Show message
                            Utility.showSnackBar(error_message, mActivityProviderProfileBinding.getRoot());
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
                    mCallActionOnDetailWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
                }
                hideProgressDialog();
            }
        }
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }

    /**
     * Call Task Detail web service
     */
    private void callTaskDetailWS(String mTaskId, String providerId) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityProviderProfileBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerId);
        mParams.put(NetworkUtility.TAGS.TASK_ID, mTaskId);

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

                        taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);
                        /*
                          NOW REFRESHES the listings
                         */
                        setData();

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityProviderProfileBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityProviderProfileBinding.getRoot());
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
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallTaskDetailWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityProviderProfileBinding.getRoot());
        }
    };


}