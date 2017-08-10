package com.cheep.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.HomeActivity;
import com.cheep.adapter.TaskRecyclerViewAdapter;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.CommonRecyclerViewBinding;
import com.cheep.interfaces.TaskRowDataInteractionListener;
import com.cheep.model.MessageEvent;
import com.cheep.model.TaskDetailModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.LoadMoreSwipeRecyclerAdapter;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.cheep.network.NetworkUtility.WS.PAST_TASK;

/**
 * Created by pankaj on 9/30/16.
 */

public class TaskFragment extends BaseFragment {

    private static final String TAG = "TaskFragment";
    public static final int TAB_PENDING_TASK = 0;
    public static final int TAB_PAST_TASK = 1;

    private TaskRecyclerViewAdapter taskRecyclerViewAdapter;
    private TaskRowDataInteractionListener mListener;
    CommonRecyclerViewBinding commonRecyclerViewBinding;
    ErrorLoadingHelper errorLoadingHelper;
    private int whichFrg;
    private String nextPageId;
    private String loadMoreKey;

    @SuppressWarnings("unused")
    public static TaskFragment newInstance(int whichFrag) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putInt(Utility.Extra.WHICH_FRAG, whichFrag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Register the broadcast receiver
        mContext.registerReceiver(mBRAddTaskDetail, new IntentFilter(Utility.BR_NEW_TASK_ADDED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister the broadcast receiver
        mContext.unregisterReceiver(mBRAddTaskDetail);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        commonRecyclerViewBinding = DataBindingUtil.inflate(inflater, R.layout.common_recycler_view, container, false);
        return commonRecyclerViewBinding.getRoot();
    }

    Paint p = new Paint();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof TaskRowDataInteractionListener) {
            mListener = (TaskRowDataInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement TaskFragment");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }

    @Override
    void initiateUI() {
        //Setting recycler view
        errorLoadingHelper = new ErrorLoadingHelper(commonRecyclerViewBinding.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        commonRecyclerViewBinding.recyclerView.setLayoutManager(linearLayoutManager);

        whichFrg = getArguments().getInt(Utility.Extra.WHICH_FRAG);

        if (whichFrg == TAB_PENDING_TASK) {
            loadMoreKey = NetworkUtility.TAGS.TIMESTAMP;
        } else {
            loadMoreKey = NetworkUtility.TAGS.LAST_ID;
        }

        taskRecyclerViewAdapter = new TaskRecyclerViewAdapter(mContext, whichFrg, mListener);
        commonRecyclerViewBinding.recyclerView.setAdapter(taskRecyclerViewAdapter);
        //Set dividers to Recyclerview
        commonRecyclerViewBinding.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal));

        errorLoadingHelper.showLoading();
        callTasksWS();
        if (EventBus.getDefault().isRegistered(this) == false)
            EventBus.getDefault().register(this);
        initSwipeToRefreshLayout();
    }

    private void initSwipeToRefreshLayout() {
        commonRecyclerViewBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                nextPageId = null;
                taskRecyclerViewAdapter.enableLoadMore();
                reloadTaskListFromServer();
            }
        });
        Utility.setSwipeRefreshLayoutColors(commonRecyclerViewBinding.swipeRefreshLayout);
    }


    @Override
    void setListener() {
        taskRecyclerViewAdapter.setIsLoadMoreEnabled(true, R.layout.load_more_progress, commonRecyclerViewBinding.recyclerView, new LoadMoreSwipeRecyclerAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (taskRecyclerViewAdapter.getmList().size() > 0) {
                    callTasksWS();
                }
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.UPDATE_FAVOURITE:
                if (!TextUtils.isEmpty(event.isFav)) {
                    if (taskRecyclerViewAdapter != null)
                        taskRecyclerViewAdapter.updateFavStatus(event.id, event.isFav);
                }
                break;
            case Utility.BROADCAST_TYPE.TASK_PAID:
            case Utility.BROADCAST_TYPE.TASK_PROCESSING:
                if (taskRecyclerViewAdapter != null) {
                    nextPageId = null;
                    taskRecyclerViewAdapter.enableLoadMore();
                    reloadTaskListFromServer();
                }
                break;
            case Utility.BROADCAST_TYPE.TASK_RATED:
                if (taskRecyclerViewAdapter != null)
                    taskRecyclerViewAdapter.updateRatedStatus(event.id);
                break;
            case Utility.BROADCAST_TYPE.TASK_CANCELED:
                if (taskRecyclerViewAdapter != null && taskRecyclerViewAdapter.cancelTask(event.id, event.taskStatus) == 0) {
                    errorLoadingHelper.failed(null, R.drawable.img_empty_pending_task, null, null, onMakeAPostClickListener);
                }
                break;
            case Utility.BROADCAST_TYPE.TASK_RESCHEDULED:
                if (taskRecyclerViewAdapter != null)
                    taskRecyclerViewAdapter.rescheduleTask(event.id, event.taskStartdate);
                break;
            case Utility.BROADCAST_TYPE.QUOTE_REQUESTED_BY_PRO:
                if (taskRecyclerViewAdapter != null)
                    taskRecyclerViewAdapter.updateOnNewQuoteRequested(event.id, event.max_quote_price, event.sp_counts, event.quoted_sp_image_url);
                break;
            case Utility.BROADCAST_TYPE.REQUEST_FOR_DETAIL:
                if (taskRecyclerViewAdapter != null)
                    taskRecyclerViewAdapter.updateOnNewDetailRequested(event.id, event.sp_counts, event.quoted_sp_image_url);
                break;
            case Utility.BROADCAST_TYPE.TASK_STATUS_CHANGE:
                if (taskRecyclerViewAdapter != null)
                    taskRecyclerViewAdapter.updateTaskStatus(event);
                break;
            case Utility.BROADCAST_TYPE.ADDITIONAL_PAYMENT_REQUESTED:
                if (taskRecyclerViewAdapter != null)
                    taskRecyclerViewAdapter.updateOnAdditionalPaymentRequested(event);
                break;
            case Utility.BROADCAST_TYPE.DETAIL_REQUEST_REJECTED:
                if (taskRecyclerViewAdapter != null)
                    taskRecyclerViewAdapter.updateOnDetailRequestRejected(event);
                break;
        }
    }

    View.OnClickListener onMakeAPostClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            Toast.makeText(mContext, "Click", Toast.LENGTH_SHORT).show();
            HomeActivity activity = (HomeActivity) mContext;
            activity.redirectToHomeTab();
        }
    };

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach() called");
        if (EventBus.getDefault().isRegistered(this) == true)
            EventBus.getDefault().unregister(this);

        mListener = null;

        /*
          Cancel the request as it no longer available
         */
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.PENDING_TASK);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.PAST_TASK);

        super.onDetach();
    }


    /**
     * Get Tasks list from server
     */

    private void reloadTaskListFromServer() {
        callTasksWS();
    }

    private void callTasksWS() {

        if (!Utility.isConnected(mContext)) {
//            Utility.showSnackBar(getString(R.string.no_internet), mFragmentFavouriteFragment.getRoot());
            errorLoadingHelper.failed(getString(R.string.no_internet), 0, onRetryBtnClickListener);
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        if (!TextUtils.isEmpty(nextPageId) && loadMoreKey != null) {
            mParams.put(loadMoreKey, nextPageId);
        } else {

        }

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(whichFrg == TAB_PENDING_TASK ? NetworkUtility.WS.PENDING_TASK : PAST_TASK
                , mCallPendingTaskWSErrorListener
                , mCallPendingTaskWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList);
    }

    Response.Listener mCallPendingTaskWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");

            String strResponse = (String) response;

            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                commonRecyclerViewBinding.swipeRefreshLayout.setRefreshing(false);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        ArrayList<TaskDetailModel> list = Utility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel[].class);

                        //Setting RecyclerView Adapter
                        if (TextUtils.isEmpty(nextPageId)) {
                            taskRecyclerViewAdapter.setItem(list);
//                            nextPageId = list.get(list.size() - 1).taskStartdate;
                        } else {
                            taskRecyclerViewAdapter.addItem(list);
                        }
                        nextPageId = jsonObject.optString(loadMoreKey);
                        errorLoadingHelper.success();
                        taskRecyclerViewAdapter.onLoadMoreComplete();
                        if (list.size() == 0) {
                            taskRecyclerViewAdapter.disableLoadMore();
                        }

                        if (taskRecyclerViewAdapter.getmList().size() <= 0) {
                            /*errorLoadingHelper.failed(getString(whichFrg == TAB_PENDING_TASK ? R.string.hint_no_pending_task : R.string.hint_no_pending_task), 0, "Create Task", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    if (mListener != null)
                                        mListener.onCreateNewTask();

                                }
                            });*/
                            errorLoadingHelper.failed(null, R.drawable.img_empty_pending_task, null, null, onMakeAPostClickListener);
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
                        if (getActivity() != null)
                            getActivity().finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallPendingTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };


    Response.ErrorListener mCallPendingTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            commonRecyclerViewBinding.swipeRefreshLayout.setRefreshing(false);

            errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentTabHomeBinding.getRoot());
        }
    };
    View.OnClickListener onRetryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            errorLoadingHelper.showLoading();
            reloadTaskListFromServer();
        }
    };

    /**
     * Broadcastreceiver for updating the tasklisting
     */
    private BroadcastReceiver mBRAddTaskDetail = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (whichFrg == TAB_PAST_TASK) {
                Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
                TaskDetailModel enquiryDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(intent.getExtras().getString(Utility.Extra.TASK_DETAIL), TaskDetailModel.class);
                taskRecyclerViewAdapter.addData(enquiryDetailModel);
            }

        }
    };


}
