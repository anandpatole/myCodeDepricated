package com.cheep.cheepcare.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.ProviderProfileActivity;
import com.cheep.activity.TaskSummaryActivity;
import com.cheep.adapter.NotificationRecyclerViewAdapter;
import com.cheep.databinding.CommonRecyclerViewBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.NotificationModel;
import com.cheep.strategicpartner.TaskSummaryStrategicPartnerActivity;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.LoadMoreRecyclerAdapter;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import java.util.ArrayList;

/**
 * Created by kruti on 12/3/18.
 */

public class AllNotificationsFragment extends BaseFragment implements NotificationRecyclerViewAdapter.NotificationItemInteractionListener {

    private static final String TAG = AllNotificationsFragment.class.getSimpleName();
    private CommonRecyclerViewBinding mBinding;
    private ErrorLoadingHelper errorLoadingHelper;
    private NotificationRecyclerViewAdapter notificationRecyclerViewAdapter;
    private String nextPageId;

    public static AllNotificationsFragment newInstance(){
        return new AllNotificationsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.common_recycler_view, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }

    @Override
    public void initiateUI() {
        PreferenceUtility.getInstance(mContext).clearUnreadNotificationCounter();

        errorLoadingHelper = new ErrorLoadingHelper(mBinding/*.commonRecyclerView*/.recyclerView);

        //Setting adapter on recycler view
        notificationRecyclerViewAdapter = new NotificationRecyclerViewAdapter(this);
        mBinding/*.commonRecyclerView*/.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding/*.commonRecyclerView*/.recyclerView.setAdapter(notificationRecyclerViewAdapter);

//        mBinding.commonRecyclerView.recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_20dp)));

        initSwipeToRefreshLayout();
        errorLoadingHelper.showLoading();
        callNotificationList();
    }

    @Override
    public void setListener() {
        notificationRecyclerViewAdapter.setIsLoadMoreEnabled(true, R.layout.load_more_progress
                , mBinding/*.commonRecyclerView*/.recyclerView, new LoadMoreRecyclerAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                callNotificationList();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private void initSwipeToRefreshLayout() {
        mBinding/*.commonRecyclerView*/.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                notificationRecyclerViewAdapter.enableLoadMore();
                nextPageId = "0";
                reloadNotificationListFromServer();
            }
        });
        Utility.setSwipeRefreshLayoutColors(mBinding/*.commonRecyclerView*/.swipeRefreshLayout);
    }

    private void callNotificationList() {

        if (!Utility.isConnected(mContext)) {
//            Utility.showSnackBar(getString(R.string.no_internet), mBinding.getRoot());
            mBinding/*.commonRecyclerView*/.swipeRefreshLayout.setRefreshing(false);
            errorLoadingHelper.failed(Utility.NO_INTERNET_CONNECTION, 0, onRetryBtnClickListener);
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            mBinding/*.commonRecyclerView*/.swipeRefreshLayout.setRefreshing(false);
            errorLoadingHelper.failed(null, R.drawable.img_empty_notifications, null);
            return;
        }

        WebCallClass.getNotificationList(mContext, nextPageId, mCommonResponseListener, mGetNotificationListListener);
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
            TaskSummaryActivity.getInstance(mContext, model.task_id);
        } else if (model.notificationType.equalsIgnoreCase(Utility.NOTIFICATION_TYPE.WEB_CUSTOM_NOTIFICATION)) {
            // Do Nothing for this TYPE of Notification.
        } else {
            // Need to redirect the user to Task Detail Screen
           /* TaskDetailModel taskDetailModel = new TaskDetailModel();
            taskDetailModel.taskId = model.task_id;

            ProviderModel providerModel = new ProviderModel();
            providerModel.providerId = String.valueOf(model.sp_user_id);

            JobSummaryActivity.newInstance(mContext, taskDetailModel, providerModel);*/
            if (model.task_type.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC))
                TaskSummaryStrategicPartnerActivity.getInstance(mContext, model.task_id);
            else
                TaskSummaryActivity.getInstance(mContext, model.task_id);
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
    }

    View.OnClickListener onRetryBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            reloadNotificationListFromServer();
        }
    };

    private void reloadNotificationListFromServer() {
        nextPageId = "0";

        callNotificationList();
    }

    private final WebCallClass.CommonResponseListener mCommonResponseListener =
            new WebCallClass.CommonResponseListener() {
                @Override
                public void volleyError(VolleyError error) {
                    Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                    mBinding/*.commonRecyclerView*/.swipeRefreshLayout.setRefreshing(false);
                    errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                }

                @Override
                public void showSpecificMessage(String message) {
                    mBinding/*.commonRecyclerView*/.swipeRefreshLayout.setRefreshing(false);
                    errorLoadingHelper.failed(message, 0, onRetryBtnClickListener);
                }

                @Override
                public void forceLogout() {
                    mBinding/*.commonRecyclerView*/.swipeRefreshLayout.setRefreshing(false);
                    //Logout and finish the current activity
                    if (getActivity() != null)
                        getActivity().finish();
                }
            };

    private final WebCallClass.GetNotificationListListener mGetNotificationListListener =
            new WebCallClass.GetNotificationListListener() {
                @Override
                public void getNotificationList(ArrayList<NotificationModel> list, String pageNumber) {
                    mBinding/*.commonRecyclerView*/.swipeRefreshLayout.setRefreshing(false);

                    //Setting RecyclerView Adapter
                    if (TextUtils.isEmpty(nextPageId) || nextPageId.equals("0")) {
                        notificationRecyclerViewAdapter.setItem(list);
                    } else {
                        notificationRecyclerViewAdapter.addItem(list);
                    }
                    nextPageId = pageNumber;
                    errorLoadingHelper.success();
                    notificationRecyclerViewAdapter.onLoadMoreComplete();
                    if (list.size() == 0) {
                        notificationRecyclerViewAdapter.disableLoadMore();
                    }

                    if (notificationRecyclerViewAdapter.getmList().size() <= 0) {
                        errorLoadingHelper.failed(null, R.drawable.img_empty_notifications, null);
                    }
                }
            };
}
