package com.cheep.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.TaskCreationActivity;
import com.cheep.activity.TaskCreationForBannerActivity;
import com.cheep.adapter.BannerServiceRecyclerViewAdapter;
import com.cheep.databinding.FragmentServiceQuestionsBinding;
import com.cheep.utils.ErrorLoadingHelper;

/**
 * Created by bhavesh on 28/4/17.
 */

public class ServiceQuestionsFragment extends BaseFragment {
    public static final String TAG = "ServiceDetailsFragment";
    private FragmentServiceQuestionsBinding mFragmentSelectSubserviceBinding;
    private BannerServiceRecyclerViewAdapter mSubServiceRecyclerViewAdapter;
    ErrorLoadingHelper errorLoadingHelper;
    private TaskCreationForBannerActivity mTaskCreationActivity;
    private boolean isVerified = false;

    @SuppressWarnings("unused")
    public static ServiceQuestionsFragment newInstance() {
        return new ServiceQuestionsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentSelectSubserviceBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_questions, container, false);
        return mFragmentSelectSubserviceBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
        if (!isVisibleToUser || mTaskCreationActivity == null) {
            return;
        }

        if (isVerified) {
            mTaskCreationActivity.setTaskState(TaskCreationActivity.STEP_ONE_VERIFIED);
        } else {
            mTaskCreationActivity.setTaskState(TaskCreationActivity.STEP_ONE_NORMAL);
        }

        // Hide the post task button
        mTaskCreationActivity.showPostTaskButton(false, false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    void initiateUI() {
        Log.d(TAG, "initiateUI() called");

    }

    @Override
    void setListener() {
        Log.d(TAG, "setListener() called");
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof TaskCreationForBannerActivity) {
            mTaskCreationActivity = (TaskCreationForBannerActivity) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.FETCH_SUB_SERVICE_LIST);
    }

    /**
     * This method would return whether the stage is verified or not
     *
     * @return
     */
    public boolean isVerified() {
        return isVerified;
    }

}
