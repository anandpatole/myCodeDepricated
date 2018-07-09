package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.adapter.NotificationRecyclerViewAdapter;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.ActivityNotificationBinding;
import com.cheep.model.NotificationModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.strategicpartner.TaskSummaryStrategicPartnerActivity;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LoadMoreRecyclerAdapter;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pankaj on 10/5/16.
 */

public class NotificationActivity extends BaseAppCompatActivity implements NotificationRecyclerViewAdapter.NotificationItemInteractionListener {

    private static final String TAG = NotificationActivity.class.getSimpleName();

    private ActivityNotificationBinding mActivityNotificationBinding;
    private ErrorLoadingHelper errorLoadingHelper;
    private NotificationRecyclerViewAdapter notificationRecyclerViewAdapter;
    private String nextPageId;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, NotificationActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityNotificationBinding = DataBindingUtil.setContentView(this, R.layout.activity_notification);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {

        PreferenceUtility.getInstance(mContext).clearUnreadNotificationCounter();

        errorLoadingHelper = new ErrorLoadingHelper(mActivityNotificationBinding.commonRecyclerView.recyclerView);
        setSupportActionBar(mActivityNotificationBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mActivityNotificationBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mActivityNotificationBinding.textTitle.setText(getString(R.string.label_notification));

        //Setting adapter on recycler view
        notificationRecyclerViewAdapter = new NotificationRecyclerViewAdapter(this);
        mActivityNotificationBinding.commonRecyclerView.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mActivityNotificationBinding.commonRecyclerView.recyclerView.setAdapter(notificationRecyclerViewAdapter);

        mActivityNotificationBinding.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_20dp)));

        initSwipeToRefreshLayout();
        errorLoadingHelper.showLoading();
        callNotificationList();
    }

    private void initSwipeToRefreshLayout() {
        mActivityNotificationBinding.commonRecyclerView.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                notificationRecyclerViewAdapter.enableLoadMore();
                nextPageId = "0";
                reloadNotificationListFromServer();
            }
        });
        Utility.setSwipeRefreshLayoutColors(mActivityNotificationBinding.commonRecyclerView.swipeRefreshLayout);
    }

    private void reloadNotificationListFromServer() {
        nextPageId = "0";

        callNotificationList();
    }

    private void callNotificationList() {

        if (!Utility.isConnected(mContext)) {
//            Utility.showSnackBar(getString(R.string.no_internet), mActivityNotificationBinding.getRoot());
            mActivityNotificationBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);
            errorLoadingHelper.failed(Utility.NO_INTERNET_CONNECTION, 0, onRetryBtnClickListener);
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            mActivityNotificationBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);
            errorLoadingHelper.failed(null, R.drawable.img_empty_notifications, null);
            return;
        }

//Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        }

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        if (!TextUtils.isEmpty(nextPageId)) {
            mParams.put(NetworkUtility.TAGS.PAGE_NUM, nextPageId);
        }
        VolleyNetworkRequest mVolleyNetworkRequestForNotificationList = new VolleyNetworkRequest(NetworkUtility.WS.NOTIFICATION_LIST
                , mCallNotificationWSErrorListener
                , mCallNotificationWSResponseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForNotificationList, NetworkUtility.WS.NOTIFICATION_LIST);
    }

    Response.Listener mCallNotificationWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");

            String strResponse = (String) response;

            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                mActivityNotificationBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        ArrayList<NotificationModel> list;
                        try {
                            list = GsonUtility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), NotificationModel[].class);
                        } catch (Exception e) {
                            Log.i(TAG, "onResponse: Error" + e.toString());
                            list = new ArrayList<>();
                           /* if (notificationRecyclerViewAdapter.getmList().size() <= 0) {
                                errorLoadingHelper.failed(null, R.drawable.img_empty_notifications, null);
                            }*/
//                            return;
                        }

                        /*notificationRecyclerViewAdapter.setItem(list);
                        errorLoadingHelper.success();
                        if (notificationRecyclerViewAdapter.getmList().size() <= 0) {
                            errorLoadingHelper.failed(null, R.drawable.img_empty_notifications, null);
                        }*/

                        //Setting RecyclerView Adapter
                        if (TextUtils.isEmpty(nextPageId) || nextPageId.equals("0")) {
                            notificationRecyclerViewAdapter.setItem(list);
                        } else {
                            notificationRecyclerViewAdapter.addItem(list);
                        }
                        nextPageId = jsonObject.optString(NetworkUtility.TAGS.PAGE_NUM);
                        errorLoadingHelper.success();
                        notificationRecyclerViewAdapter.onLoadMoreComplete();
                        if (list.size() == 0) {
                            notificationRecyclerViewAdapter.disableLoadMore();
                        }

                        if (notificationRecyclerViewAdapter.getmList().size() <= 0) {
                            errorLoadingHelper.failed(null, R.drawable.img_empty_notifications, null);
                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentTabHomeBinding.getRoot());
                        errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mFragmentTabHomeBinding.getRoot());
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
                mCallNotificationWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };


    Response.ErrorListener mCallNotificationWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            mActivityNotificationBinding.commonRecyclerView.swipeRefreshLayout.setRefreshing(false);

            errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentTabHomeBinding.getRoot());
        }
    };

    View.OnClickListener onRetryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            reloadNotificationListFromServer();
        }
    };

    @Override
    protected void setListeners() {
        notificationRecyclerViewAdapter.setIsLoadMoreEnabled(true, R.layout.load_more_progress, mActivityNotificationBinding.commonRecyclerView.recyclerView, new LoadMoreRecyclerAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                callNotificationList();
            }
        });
    }

    @Override
    public void onNotificationRowClicked(final NotificationModel model, int position) {
       /* View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_information, null, false);
        ((TextView) view.findViewById(R.id.text_message)).setText(model.message);
        final BottomAlertDialog dialog = new BottomAlertDialog(mContext);

        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();*/

        // Forwar the user to Profile screen in case Request for details calls up
        if (model.notificationType.equalsIgnoreCase(Utility.NOTIFICATION_TYPE.REQUEST_FOR_DETAIL)) {
            ProviderProfileActivity.newInstance(mContext, model.task_id, String.valueOf(model.sp_user_id));
           /*String taskID = model.task_id;
           String spUserId = String.valueOf(model.sp_user_id);
           HireNewJobActivity.newInstance(mContext, taskID, spUserId);*/
        } else if (model.notificationType.equalsIgnoreCase(Utility.NOTIFICATION_TYPE.QUOTE_REQUEST)) {
            //Redirecting the user to Quote Screen
//            HireNewJobActivity.newInstance(mContext, model.task_id, String.valueOf(model.sp_user_id));
            TaskSummaryForMultiCatActivity.getInstance(mContext, model.task_id);
        } else if (model.notificationType.equalsIgnoreCase(Utility.NOTIFICATION_TYPE.WEB_CUSTOM_NOTIFICATION)) {
            // Do Nothing for this TYPE of Notification.
        }
        else if(model.notificationType.equalsIgnoreCase(Utility.NOTIFICATION_TYPE.PACKAGE_PURCHASE))
        {


            Intent intent=new Intent(mContext,HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(NetworkUtility.TAGS.TYPE,Utility.NOTIFICATION_TYPE.PACKAGE_PURCHASE);
            startActivity(intent);
            //finish();
        }
        else if(model.notificationType.equalsIgnoreCase(Utility.NOTIFICATION_TYPE.TASK_BOOKING) || model.notificationType.equalsIgnoreCase(Utility.NOTIFICATION_TYPE.RESCHEDULE_TASK) || model.notificationType.equalsIgnoreCase(Utility.NOTIFICATION_TYPE.BOOKING_CONFIRMATION) )
        {
            TaskSummaryForMultiCatActivity.getInstance(mContext, model.task_id);
        }


        else {
            // Need to redirect the user to Task Detail Screen
           /* TaskDetailModel taskDetailModel = new TaskDetailModel();
            taskDetailModel.taskId = model.task_id;

            ProviderModel providerModel = new ProviderModel();
            providerModel.providerId = String.valueOf(model.sp_user_id);

            JobSummaryActivity.newInstance(mContext, taskDetailModel, providerModel);*/
//            if (model.task_type.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC))
//                TaskSummaryStrategicPartnerActivity.getInstance(mContext, model.task_id);
//            else
//                TaskSummaryForMultiCatActivity.getInstance(mContext, model.task_id);
        }
          /*  }
        });*/


        //dialog.setTitle(getString(R.string.label_update));
        /*if (Utility.NOTIFICATION_TYPE.QUOTE_REQUEST.equalsIgnoreCase(model.notificationType)) {
            dialog.setTitle(getString(R.string.label_quote_added));
        } else if (Utility.NOTIFICATION_TYPE.TASK_STATUS_CHANGE.equalsIgnoreCase(model.notificationType)) {
            dialog.setTitle(getString(R.string.label_task_status_change));
        } else {
            dialog.setTitle(model.notificationType);
        }*/
        //dialog.setCustomView(view);
        //dialog.showDialog();

        //Open summary screen(In Future when client says)
        /*TaskDetailModel taskDetailModel = new TaskDetailModel();
        taskDetailModel.taskId = "<copy id from NotificationModel>";
        ProviderModel providerModel = new ProviderModel();
        providerModel.providerId = "<copy id from NotificationModel>";
        JobSummaryActivity.newInstance(mContext, taskDetailModel, providerModel);*/
        Intent intent=new Intent(mContext,HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.putExtra(NetworkUtility.TAGS.TYPE,Utility.NOTIFICATION_TYPE.PACKAGE_PURCHASE);
        startActivity(intent);
    }
}
