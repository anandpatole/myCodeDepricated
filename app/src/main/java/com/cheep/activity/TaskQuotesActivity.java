package com.cheep.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.adapter.TaskQuotesRecyclerViewAdapter;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.custom_view.GridImageView;
import com.cheep.databinding.DialogFilterBinding;
import com.cheep.databinding.DialogFragmentTaskCreationBinding;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.QuoteListResponse;
import com.cheep.model.TaskDetailModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskQuotesActivity extends BaseAppCompatActivity implements TaskQuotesRecyclerViewAdapter.OnInteractionListener {
    private static final String TAG = "TaskQuotesActivity";

    public static void newInstance(Context context, TaskDetailModel model, boolean isFirstTimeCreate) {
        Intent intent = new Intent(context, TaskQuotesActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(model));
        intent.putExtra(Utility.Extra.IS_FIRST_TIME, isFirstTimeCreate);
        intent.setAction(Utility.ACTION_HIRE_PROVIDER);
        context.startActivity(intent);
    }

    private LinearLayout mRoot;
    private Toolbar mToolbar;
    private TextView tvTitle;
    private TextView tvTaskTitle;
    private TextView tvTaskDescription;
    private TextView tvTaskBookingTime;
    private TextView tvTaskStartsIn;

    private GridImageView mGridImageView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private TaskQuotesRecyclerViewAdapter mAdapter;
    private ErrorLoadingHelper mErrorLoadingHelper;


    private BottomAlertDialog mFilterDialog;
    private DialogFilterBinding mDialogFilterBinding;

    //    private List<ProviderModel> mQuotesList;
    //private String mIntentAction;
    private TaskDetailModel mTaskDetailModel;
    private Gson mGson;
    private Filter mFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_quotes);

        initiateUI();

        callSPListWS();


        // Register EventBus
        EventBus.getDefault().register(this);

    }

    @Override
    protected void initiateUI() {
        mGson = new Gson();
//        mQuotesList = new ArrayList<>();

        mRoot = (LinearLayout) findViewById(R.id.root);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle = (TextView) findViewById(R.id.text_title);
        tvTaskTitle = (TextView) findViewById(R.id.tvTaskTitle);
        tvTaskDescription = (TextView) findViewById(R.id.tvTaskDescription);
        tvTaskBookingTime = (TextView) findViewById(R.id.tvTaskBookingTime);
        tvTaskStartsIn = (TextView) findViewById(R.id.tvTaskStartsIn);
        mGridImageView = (GridImageView) findViewById(R.id.gridImageView);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        mErrorLoadingHelper = new ErrorLoadingHelper(mRecyclerView);

        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new TaskQuotesRecyclerViewAdapter(this, /*mQuotesList,*/ this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal));
        setupActionbar();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callSPListWS();
            }
        });
        Utility.setSwipeRefreshLayoutColors(mSwipeRefreshLayout);

        //mIntentAction = getIntent().getAction();
        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            //mIsFirstTime = bundle.getBoolean(Utility.Extra.IS_FIRST_TIME, false);
            mTaskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), TaskDetailModel.class);

            populateData();
        }
    }

    @Override
    protected void setListeners() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_quotes_filter, menu);
        return true;
    }

    private void setupActionbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(Utility.EMPTY_STRING);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
    }

    private void callSPListWS() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), null);
            return;
        }

        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setEnabled(false);
        } else {
            mErrorLoadingHelper.showLoading();
        }


        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, mTaskDetailModel.taskId);

        String url;
        if (mFilter != null) {
            url = NetworkUtility.WS.SP_LIST_FILTER;
            if (mFilter.cheapest != mDialogFilterBinding.seekbarCheepest.getMax()) {
                mParams.put(NetworkUtility.TAGS.PRICE, mFilter.cheapest);
            }

            if (mFilter.distance != mDialogFilterBinding.seekbarDistance.getMax()) {
                mParams.put(NetworkUtility.TAGS.DISTANCE, mFilter.distance);
            }

            mParams.put(NetworkUtility.TAGS.IS_FAVOURITE, mFilter.isFav ? Utility.BOOLEAN.YES : Utility.BOOLEAN.NO);

            if (mFilter.rating > 0) {
                mParams.put(NetworkUtility.TAGS.RATINGS, mFilter.rating);
            }

            // Now, see if user just did nothing and pressing done button.
            if (!mParams.containsKey(NetworkUtility.TAGS.PRICE)
                    && !mParams.containsKey(NetworkUtility.TAGS.DISTANCE)
                    && !mParams.containsKey(NetworkUtility.TAGS.RATINGS)) {
                // None of the filter is applied so dont need to go ahead.

                resetFilterFields(mDialogFilterBinding);
                mFilterDialog.dismiss();
                mErrorLoadingHelper.showLoading();
                callSPListWS();
                return;
            }

        } else {
            url = NetworkUtility.WS.SP_LIST_TASK_WISE;
        }


        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(url
                , mSPListErrorResponseListener
                , mSPListResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }

    private void populateData() {
        if (mTaskDetailModel != null) {
            tvTitle.setText(checkNonNullAndSet(mTaskDetailModel.categoryName));
            tvTaskTitle.setText(checkNonNullAndSet(mTaskDetailModel.subCategoryName));
            tvTaskDescription.setText(checkNonNullAndSet(mTaskDetailModel.taskDesc));

            SuperCalendar superCalendar = SuperCalendar.getInstance();
            superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            superCalendar.setTimeInMillis(Long.parseLong(mTaskDetailModel.taskStartdate));
            superCalendar.setLocaleTimeZone();

            String mBookingDate = mContext.getString(R.string.format_task_book_date
                    , superCalendar.format(Utility.DATE_FORMAT_DD_MMM_HH_MM_AM));
            tvTaskBookingTime.setText(mBookingDate);


            /*String mStartTime = mContext.getString(R.string.format_task_start_time
                    , Utility.getDateDifference(superCalendar.format(Utility.DATE_FORMAT_FULL_DATE)));
                    tvTaskStartsIn.setText(mStartTime);*/
            tvTaskStartsIn.setText(Utility.getDateDifference(mContext, superCalendar.format(Utility.DATE_FORMAT_FULL_DATE)));
        }
    }

    private void populateGridImageView() {
        mGridImageView.clear();
        List<Uri> uriList = new ArrayList<>();
        for (ProviderModel model : mAdapter.getData()) {
            if (model.profileUrl != null) {
                uriList.add(Uri.parse(model.profileUrl));
            } else {
                uriList.add(Uri.parse("www.dummycheepurl.k"));
            }
        }
        mGridImageView.createWithUrls(uriList);
    }

    private void showFilterDialog() {

        if (mFilterDialog == null) {
            mDialogFilterBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_filter, null, false);
            //Cheepest is max ₹10000
            mDialogFilterBinding.seekbarCheepest.setPrefix("₹");
//            mDialogFilterBinding.seekbarCheepest.setMin(100);
            mDialogFilterBinding.seekbarCheepest.setMax(getMaxQuote());
            mDialogFilterBinding.textLabelMaxPrice.setText(getString(R.string.ruppe_symbol_x, String.valueOf(mDialogFilterBinding.seekbarCheepest.getMax())));
            //Distance is max 50km
            mDialogFilterBinding.seekbarDistance.setSuffix("km");
//            mDialogFilterBinding.seekbarDistance.setMin(3);
            mDialogFilterBinding.seekbarDistance.setMax(getMaxDistance());
            mDialogFilterBinding.textLabelMaxDistance.setText(getMaxDistance() + "km");
            mDialogFilterBinding.imgFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialogFilterBinding.imgFav.setSelected(!mDialogFilterBinding.imgFav.isSelected());
                }
            });
            mFilterDialog = new BottomAlertDialog(mContext);
            mFilterDialog.setExpandedInitially(true);
            mDialogFilterBinding.btnShowResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Saving this field when user fix filter fields
                    mFilter = new Filter();
                    mFilter.cheapest = mDialogFilterBinding.seekbarCheepest.getDisplayProgress();
                    mFilter.distance = mDialogFilterBinding.seekbarDistance.getDisplayProgress();
                    mFilter.rating = mDialogFilterBinding.ratingBar.getProgress();
                    mFilter.isFav = mDialogFilterBinding.imgFav.isSelected();
                    mFilterDialog.dismiss();

                    callSPListWS();
                }
            });

            mDialogFilterBinding.btnReset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resetFilterFields(mDialogFilterBinding);
                    mFilterDialog.dismiss();
                    mErrorLoadingHelper.showLoading();
                    callSPListWS();
                }
            });
            mFilterDialog.setTitle(getString(R.string.label_filter));
            mFilterDialog.setCustomView(mDialogFilterBinding.getRoot());

            //reseting all the fields for first time
            resetFilterFields(mDialogFilterBinding);
        }

        //Prefilling all fields when user filter is already applied
        if (mFilter != null) {
            if (mFilter.cheapest > 0) {
                mDialogFilterBinding.seekbarCheepest.setDisplayProgress(mFilter.cheapest);
            }
            if (mFilter.distance > 0) {
                mDialogFilterBinding.seekbarDistance.setDisplayProgress(mFilter.distance);
            }
            mDialogFilterBinding.imgFav.setSelected(mFilter.isFav);
            mDialogFilterBinding.ratingBar.setRating(mFilter.rating);
        }

        mFilterDialog.showDialog();
    }

    private void resetFilterFields(DialogFilterBinding mDialogFilterBinding) {
        mDialogFilterBinding.seekbarDistance.setProgress(getMaxDistance());
        mDialogFilterBinding.seekbarCheepest.setProgress(getMaxQuote());
        mDialogFilterBinding.imgFav.setSelected(false);
        mDialogFilterBinding.ratingBar.setRating(0);
        mFilter = null;
    }

    private int getMaxQuote() {
        try {
            if (!TextUtils.isEmpty(mTaskDetailModel.maxQuotePrice) && !mTaskDetailModel.maxQuotePrice.equalsIgnoreCase("0")) {
                return (int) Double.parseDouble(mTaskDetailModel.maxQuotePrice) + 1;
            }
        } catch (Exception e) {
        }
        return 20000;
    }

    private int getMaxDistance() {
        return 15;
    }

    private String checkNonNullAndSet(String text) {
        return text != null ? text.trim() : "";
    }

    private Response.Listener mSPListResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object rawResponse) {
            QuoteListResponse response = mGson.fromJson((String) rawResponse, QuoteListResponse.class);
            switch (response.statusCode) {
                case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                    /*//clear the data as there is no pagination login
                    mQuotesList.clear();*/

                    if (response.quoteList != null && response.quoteList.size() > 0) {
                        //repopulate the data
//                        mQuotesList.addAll(response.quoteList);
//                        mAdapter.notifyDataSetChanged();
                        mAdapter.addAll(response.quoteList);

                        mErrorLoadingHelper.success();
                    } else {
                        mErrorLoadingHelper.failed(getString(R.string.label_no_quotes_available), 0, null, null);
                    }

                    populateGridImageView();
                    break;
                case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                    mErrorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                    break;
                case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                    mErrorLoadingHelper.failed(response.message, 0, onRetryBtnClickListener);
                    break;
                case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                    //Logout and finish the current activity
                    Utility.logout(mContext, true, response.statusCode);
                    finish();
                    break;
            }
            mSwipeRefreshLayout.setEnabled(true);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };

    private Response.ErrorListener mSPListErrorResponseListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mSwipeRefreshLayout.setEnabled(true);
            mSwipeRefreshLayout.setRefreshing(false);
            mErrorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
        }
    };

    @Override
    public void onBookClick(ProviderModel provider) {
        PaymentsStepActivity.newInstance(mContext, mTaskDetailModel, provider, 0);
    }

    @Override
    public void onItemClick(ProviderModel provider) {
        ProviderProfileActivity.newInstance(mContext, provider, mTaskDetailModel);
    }

    @Override
    public void onChatClicked(ProviderModel providerModel) {
        /*if (providerModel != null && mTaskDetailModel != null) {
            if (providerModel.request_detail_status.equalsIgnoreCase(Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED)) {
                TaskChatModel taskChatModel = new TaskChatModel();
                taskChatModel.categoryName = mTaskDetailModel.categoryName;
                taskChatModel.taskDesc = mTaskDetailModel.taskDesc;
                taskChatModel.taskId = mTaskDetailModel.taskId;
                taskChatModel.receiverId = FirebaseUtils.getPrefixSPId(providerModel.providerId);
                taskChatModel.participantName = providerModel.userName;
                taskChatModel.participantPhotoUrl = providerModel.profileUrl;
                ChatActivity.newInstance(mContext, taskChatModel);
                return;
            }
            callTaskDetailRequestAcceptWS(Utility.ACTION_CHAT, mTaskDetailModel.taskId, providerModel);
        }*/

        if (providerModel != null) {
            showDetailRequestDialog(providerModel);
        }
    }

    private View.OnClickListener onRetryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            callSPListWS();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_filter:
                showFilterDialog();
                return true;
            default:
                return false;
        }
    }

    private static class Filter {
        private int cheapest;
        private int distance;
        private int rating;
        private boolean isFav;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////Accept-Reject Detail Service[End] //////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Calling accept request Web service
     */
    private void callTaskDetailRequestAcceptWS(final String action, String taskID, final ProviderModel providerModel) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mRoot);
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
                Utility.showSnackBar(getString(R.string.label_something_went_wrong), mRoot);
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
//                                callToOtherUser(mActivityProviderProfileBinding.getRoot(), providerModel.providerId);
                                Utility.openCustomerCareCallDialer(mContext, providerModel.sp_phone_number);
                            }
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                            // Show Toast
                            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mRoot);
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                            error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                            // Show message
                            Utility.showSnackBar(error_message, mRoot);
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
     * Managing Accept/Decline Chat Request
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
                callTaskDetailRequestAcceptRejectWS(Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED, mTaskDetailModel.taskId, providerModel.providerId);
                badForRequestTaskDetail.dismiss();
            }
        });
        badForRequestTaskDetail.addNegativeButton(getString(R.string.label_decline), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                badForRequestTaskDetail.dismiss();
                callTaskDetailRequestAcceptRejectWS(Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.REJECTED, mTaskDetailModel.taskId, providerModel.providerId);
            }
        });
        badForRequestTaskDetail.showDialog();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////Accept-Reject Detail Service[Start] ///////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Calling delete address Web service
     */
    private void callTaskDetailRequestAcceptRejectWS(String requestDetailStatus, String taskID, String spUserID) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mRoot);
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
//                        String task_id = jsonObject.getString(NetworkUtility.TAGS.TASK_ID);
                        String spUserID = jObjData.getString(NetworkUtility.TAGS.SP_USER_ID);
                        String spUserName = jObjData.optString(NetworkUtility.TAGS.SP_USER_NAME);
                        String requestDatailStatus = jObjData.getString(NetworkUtility.TAGS.REQUEST_DETAIL_STATUS);
                        if (requestDatailStatus.equalsIgnoreCase(Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED)) {
                            // spRecyclerViewAdapter.updateModelForRequestDetailStatus(spUserID, requestDatailStatus);
//                            TODO: Update the views & Show popup
                            mAdapter.updateModelForRequestDetailStatus(spUserID, requestDatailStatus);

//                            String username = PreferenceUtility.getInstance(mContext).getUserDetails().UserName;
                            String descriptionForAcknowledgement = mContext.getString(R.string.desc_request_for_detail_accepted_acknowledgment, spUserName);

                            showDialogOnRequestForDetailAccepted(descriptionForAcknowledgement);

                            /*DialogOnRequestForDetailAccepted mDialogOnRequestForDetailAccepted = DialogOnRequestForDetailAccepted.newInstance(descriptionForAcknowledgement);
                            mDialogOnRequestForDetailAccepted.show(getSupportFragmentManager(), DialogOnRequestForDetailAccepted.TAG);*/
//
//
//  mActivityProviderProfileBinding.textContactRequest.setVisibility(View.GONE);
                        } else {
                            mAdapter.updateModelForRequestDetailStatus(spUserID, requestDatailStatus);
                            // Need to pass this details to Pending listing as well.
                            /*MessageEvent messageEvent = new MessageEvent();
                            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.DETAIL_REQUEST_REJECTED;
                            messageEvent.id = taskId;
                            EventBus.getDefault().post(messageEvent);

                            // Update recycler view
                            spRecyclerViewAdapter.removeModelForRequestDetailStatus(spUserID, requestDatailStatus);

                            // Check if listing is empty now, display message
                            if (spRecyclerViewAdapter.getmList().size() == 0) {
                                errorLoadingHelper.failed(getString(R.string.label_no_quotes_available), 0, null, null);
                            }*/
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mRoot);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mRoot);
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
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mRoot);
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////Accept-Reject Detail Service[End] //////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void showDialogOnRequestForDetailAccepted(String acknowledgeMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        // Add the buttons
        builder.setPositiveButton(R.string.label_Ok_small, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.dismiss();
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.setTitle(mContext.getString(R.string.app_name).toUpperCase());
        dialog.setMessage(acknowledgeMessage);
        dialog.show();
    }


    /**
     * Create Dialog which would going to show on successfull completion
     */
    public static class DialogOnRequestForDetailAccepted extends DialogFragment {
        private static final String TAG = "DialogOnRequestForDetailAccepted";
        private String mDescription = Utility.EMPTY_STRING;
        private DialogFragmentTaskCreationBinding mDialogFragmentTaskCreationBinding;

        /**
         * Create a new instance of MyDialogFragment, providing "num"
         * as an argument.
         */
        static DialogOnRequestForDetailAccepted newInstance(String desc) {
            DialogOnRequestForDetailAccepted f = new DialogOnRequestForDetailAccepted();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putString(NetworkUtility.TAGS.DESCRIPTION, desc);
            f.setArguments(args);
            return f;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mDescription = getArguments().getString(NetworkUtility.TAGS.DESCRIPTION);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // Set Window Background as Transparent.
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            mDialogFragmentTaskCreationBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_task_creation, container, false);

            // Hide the title as its not required here.
            mDialogFragmentTaskCreationBinding.title.setText(getString(R.string.label_congratulation));

            // Update the description
            mDialogFragmentTaskCreationBinding.textTaskCreationAcknowledgment.setText(mDescription);

            // Click event of Okay button
            mDialogFragmentTaskCreationBinding.textOkay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            return mDialogFragmentTaskCreationBinding.getRoot();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_PAID
                || event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_PROCESSING) {
            // Refresh the SP listing
            finish();
        }/* else if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.QUOTE_REQUESTED_BY_PRO
                || event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.REQUEST_FOR_DETAIL) {

            // Only go ahead if we are in same task detail screen whose notification comes
            if (taskDetailModel.taskId.equals(event.id)) {
                // We need to refresh the SP listing.
                spRecyclerViewAdapter.enableLoadMore();
                reloadSPListWS();
            }
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
