package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.adapter.CommentsRecyclerViewAdapter;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.ActivityCommentsBinding;
import com.cheep.model.CommentsModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.ReviewModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.cheep.utils.LogUtils.LOGD;
import static com.cheep.utils.LogUtils.LOGI;
import static com.cheep.utils.LogUtils.makeLogTag;


/**
 * Created by Bhavesh Patadiya on 10/5/16.
 * This class involves code regarding listing of comments(reviews) user provided to particular PROs.
 * It can be initiated from @{@link ProviderProfileActivity}
 */
public class CommentsActivity extends BaseAppCompatActivity {
    // Constants
    private static final String TAG = makeLogTag(CommentsActivity.class);

    private ActivityCommentsBinding mActivityCommentsBinding;
    private ReviewModel reviewModel;
    private ErrorLoadingHelper errorLoadingHelper;
    private CommentsRecyclerViewAdapter commentsRecyclerViewAdapter;

    private boolean isRefresh = true;

    /**
     * Static method that would start @{@link CommentsActivity} and would required @{@link ReviewModel}
     * & Username
     *
     * @param context     context of the activity
     * @param reviewModel ReviewModel information
     * @param name        user name
     */
    public static void newInstance(Context context, ReviewModel reviewModel, String name) {
        Intent intent = new Intent(context, CommentsActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(reviewModel));
        intent.putExtra(Utility.Extra.USER_NAME, name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityCommentsBinding = DataBindingUtil.setContentView(this, R.layout.activity_comments);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {
        // Setup ActiionBar/Toolbar
        setSupportActionBar(mActivityCommentsBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Manage event when back arrow from @android.support.v7.widget.Toolbar pressed.
        mActivityCommentsBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Set Title of the activity
        mActivityCommentsBinding.textTitle.setText(getString(R.string.label_comments));

        // Initialize Error loading helper
        errorLoadingHelper = new ErrorLoadingHelper(mActivityCommentsBinding.recyclerView);

        // Fetch the contents from Intents
        reviewModel = (ReviewModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), ReviewModel.class);

        // Setting up hint for comment label
        mActivityCommentsBinding.editComment.setHint(getString(R.string.hint_comments_edit_text, getIntent().getStringExtra(Utility.Extra.USER_NAME)));

        //Setting adapter on recycler view
        commentsRecyclerViewAdapter = new CommentsRecyclerViewAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mActivityCommentsBinding.recyclerView.setLayoutManager(layoutManager);
        mActivityCommentsBinding.recyclerView.setAdapter(commentsRecyclerViewAdapter);
        mActivityCommentsBinding.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_20dp)));

        /**
         * This call would eventually a network call that would fetch comments based on @reviewId provided.
         */
        callCommentsList(reviewModel.reviewId);
    }

    @Override
    protected void setListeners() {
        /**
         * Manage click event of Send Button.
         */
        mActivityCommentsBinding.textSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sending the comment to server
                sendComment(mActivityCommentsBinding.editComment.getText().toString().trim());

                // Reset the value of edit label.
                mActivityCommentsBinding.editComment.setText(Utility.EMPTY_STRING);
            }
        });
    }

    @Override
    protected void onDestroy() {
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.COMMENT_LIST);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.ADD_COMMENT);
        super.onDestroy();
    }


    /***********************************************************************************************
     *********************** [Send Comment API Call] [Start] ******************************
     ***********************************************************************************************/
    /**
     * Network call for sending the comment to server
     *
     * @param comment Message user wants to send.
     */
    @SuppressWarnings("unchecked")
    private void sendComment(String comment) {
        // Check Internet Connectivity
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityCommentsBinding.getRoot());
            return;
        }

        // Validation in case comment provided is empty.
        if (TextUtils.isEmpty(comment)) {
            Utility.showSnackBar(getString(R.string.validate_comment), mActivityCommentsBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, reviewModel.taskId);
        mParams.put(NetworkUtility.TAGS.REVIEW_ID, reviewModel.reviewId);
        mParams.put(NetworkUtility.TAGS.COMMENT, comment);

        /**
         * Url is based on condition if address id is greater then 0 then it means we need to update the existing address
         */
        mVolleyNetworkRequestForCommentList = new VolleyNetworkRequest(NetworkUtility.WS.ADD_COMMENT
                , mCallPostCommentWSErrorListener
                , mCallPostCommentWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCommentList, NetworkUtility.WS.ADD_COMMENT);
    }

    /**
     * Implementing @{@link com.android.volley.Response.Listener} for managing success response.
     */
    Response.Listener mCallPostCommentWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                LOGI(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;

                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        CommentsModel commentsModel = (CommentsModel) Utility.getObjectFromJsonString(jsonObject.getString(NetworkUtility.TAGS.DATA), CommentsModel.class);

                        //Setting SP List recycler view adapter
                        if (commentsRecyclerViewAdapter == null) {
                            commentsRecyclerViewAdapter = new CommentsRecyclerViewAdapter();
                        }

                        commentsRecyclerViewAdapter.addToList(commentsModel);

                        mActivityCommentsBinding.recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                //call smooth scroll
                                mActivityCommentsBinding.recyclerView.scrollToPosition(commentsRecyclerViewAdapter.getItemCount());
                            }
                        });

                        errorLoadingHelper.success();
                        mActivityCommentsBinding.editComment.setText("");

                        try {
                            int totalCounter = Integer.parseInt(reviewModel.commentCount) + 1;
                            reviewModel.commentCount = String.valueOf(totalCounter);

                            //Sending Broadcast so ProviderProfileActivity listen and change comment count
                            MessageEvent messageEvent = new MessageEvent();
                            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.UPDATE_COMMENT_COUNT;
                            messageEvent.id = reviewModel.reviewId;
                            messageEvent.commentCount = reviewModel.commentCount;
                            EventBus.getDefault().post(messageEvent);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityCommentsBinding.getRoot());
//                        errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityCommentsBinding.getRoot());
//                        errorLoadingHelper.failed(error_message, 0, onRetryBtnClickListener);
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
                mCallPostCommentWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();

        }
    };

    /**
     * Implementing @{@link com.android.volley.Response.ErrorListener}
     */
    Response.ErrorListener mCallPostCommentWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            LOGI(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityCommentsBinding.getRoot());
            hideProgressDialog();
        }
    };

    /***********************************************************************************************
     *********************** [Send Comment API Call] [End] ********************************
     ***********************************************************************************************/


    /***********************************************************************************************
     *********************** [Reload Comment Listing] [Start] ******************************
     ***********************************************************************************************/
    /**
     * Calling Get SP list web service from server
     *
     * @param categoryId
     * @param cityId
     */
    VolleyNetworkRequest mVolleyNetworkRequestForCommentList;

    private void reloadCommentsListWS() {
        errorLoadingHelper.showLoading();
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCommentList, NetworkUtility.WS.COMMENT_LIST);
    }

    private void callCommentsList(String reviewId) {
        if (!Utility.isConnected(mContext)) {
//            Utility.showSnackBar(getString(R.string.no_internet), mActivityCommentsBinding.getRoot());
            errorLoadingHelper.failed(Utility.NO_INTERNET_CONNECTION, 0, onRetryBtnClickListener);
            return;
        }

        //Show Progress
//        showProgressDialog();

        errorLoadingHelper.showLoading();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.REVIEW_ID, reviewId);
//        mParams.put(NetworkUtility.TAGS.LAST_ID, providerId); //for pagination

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        mVolleyNetworkRequestForCommentList = new VolleyNetworkRequest(NetworkUtility.WS.COMMENT_LIST
                , mCallCommentListWSErrorListener
                , mCallCommentListWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCommentList, NetworkUtility.WS.COMMENT_LIST);
    }

    /**
     * Managing Click event of Retry button.
     * Note: Currently @{@link ErrorLoadingHelper} is forcefully not showing Retry button so
     * it would not going to show. If it requires in future, it needs to be managed from @{@link ErrorLoadingHelper}
     * class.
     */
    View.OnClickListener onRetryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            reloadCommentsListWS();
        }
    };

    /**
     * Implementing @{@link com.android.volley.Response.Listener} for managing success response.
     */
    Response.Listener mCallCommentListWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                LOGI(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        ArrayList<CommentsModel> reviewList = Utility.getObjectListFromJsonString(jsonObject.getString(NetworkUtility.TAGS.DATA), CommentsModel[].class);

                        //Setting SP List recycler view adapter
                        if (commentsRecyclerViewAdapter == null) {
                            commentsRecyclerViewAdapter = new CommentsRecyclerViewAdapter();
                        }

                        //For Pull to refresh
                        if (isRefresh) {
                            commentsRecyclerViewAdapter.setList(reviewList);
                        } else {
                            //for load more and
                            commentsRecyclerViewAdapter.addToList(reviewList);
                        }

                        // In order to scroll @recyclerView properly need to smoothscroll after some delay to
                        // avoid junking operations so that recyclerview in meantime can load the updated contents.
                        mActivityCommentsBinding.recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //call smooth scroll
                                mActivityCommentsBinding.recyclerView.scrollToPosition(commentsRecyclerViewAdapter.getItemCount());
                            }
                        }, 200);
                        isRefresh = false;

                        // If fetched lists is empty, need to show default image
                        if (commentsRecyclerViewAdapter.getmList() != null && !commentsRecyclerViewAdapter.getmList().isEmpty()) {
                            errorLoadingHelper.success();
                        } else {
                            errorLoadingHelper.failed(null, R.drawable.img_empty_comments, null, null);
                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        errorLoadingHelper.failed(error_message, 0, onRetryBtnClickListener);
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
                mCallCommentListWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    /**
     * Implementing @{@link com.android.volley.Response.ErrorListener}
     */
    Response.ErrorListener mCallCommentListWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
            errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
        }
    };
    /***********************************************************************************************
     *********************** [Reload Comment Listing] [End] ********************************
     ***********************************************************************************************/

}
