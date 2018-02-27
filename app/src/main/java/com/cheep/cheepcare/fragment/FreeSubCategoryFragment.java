package com.cheep.cheepcare.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.activity.TaskCreationCCActivity;
import com.cheep.cheepcare.adapter.FreeServicesAdapter;
import com.cheep.cheepcare.dialogs.LimitExceededDialog;
import com.cheep.databinding.FragmentSelectSubCategoryBinding;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.utils.LogUtils;

import java.util.ArrayList;

/**
 * Created by bhavesh on 28/4/17.
 */

public class FreeSubCategoryFragment extends BaseFragment {
    public static final String TAG = FreeSubCategoryFragment.class.getSimpleName();
    private FragmentSelectSubCategoryBinding mBinding;
    private FreeServicesAdapter mFreeServicesAdapter;
    private TaskCreationCCActivity mTaskCreationCCActivity;
    private boolean isVerified = false;



    @SuppressWarnings("unused")
    public static FreeSubCategoryFragment newInstance() {
        FreeSubCategoryFragment fragment = new FreeSubCategoryFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_sub_category, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }


   /* @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
        if (!isVisibleToUser || mTaskCreationCCActivity == null) {
            return;
        }

        if (!mFreeServicesAdapter.getSelectedList().isEmpty()) {
            mTaskCreationCCActivity.setTaskState(TaskCreationCCActivity.STEP_ONE_VERIFIED);
        } else {
            mTaskCreationCCActivity.setTaskState(TaskCreationCCActivity.STEP_ONE_NORMAL);
        }

        // Hide the post task button
        mTaskCreationCCActivity.showPostTaskButton(true, true);
    }*/

    @Override
    public void initiateUI() {
        Log.d(TAG, "initiateUI() called");

        //Setting recycler view

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mBinding.recyclerView.setLayoutManager(linearLayoutManager);
        mFreeServicesAdapter = new FreeServicesAdapter(mFreeItemInteractionListener);
        mBinding.recyclerView.setAdapter(mFreeServicesAdapter);
//        fetchListOfSubCategory(mTaskCreationCCActivity.mJobCategoryModel.catId);
    }

    @Override
    public void setListener() {
        Log.d(TAG, "setListener() called");
    }

    /*public interface SubServiceListInteractionListener {
        void onSubCategoryRowItemClicked(SubServiceDetailModel subServiceDetailModel);
    }

    private SubServiceListInteractionListener mSubServiceListInteractionListener = new SubServiceListInteractionListener() {
        @Override
        public void onSubCategoryRowItemClicked(SubServiceDetailModel subServiceDetailModel) {
//            mTaskCreationCCActivity.setSelectedSubService(subServiceDetailModel);

            Log.d(TAG, "onSubCategoryRowItemClicked() called with: subServiceDetailModel = [" + subServiceDetailModel.name + "]");
            // Make the status Verified
            isVerified = true;

            //Alert The activity that step one is been varified.
            mTaskCreationCCActivity.setTaskState(TaskCreationCCActivity.STEP_ONE_VERIFIED);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTaskCreationCCActivity.gotoStep(TaskCreationCCActivity.STAGE_2);
                }
            }, 500);

        }
    };*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof TaskCreationCCActivity) {
            mTaskCreationCCActivity = (TaskCreationCCActivity) activity;
        }
    }

    /**
     * This method would return the selected list by getting
     * from adapter
     *
     * @return
     */
    public ArrayList<SubServiceDetailModel> getSelectedSubServices() {
        return mFreeServicesAdapter.getSelectedList();
    }

    public void setSubCatList(ArrayList<SubServiceDetailModel> list){
        mFreeServicesAdapter.addAll(list);
    }

    private final FreeServicesAdapter.ItemInteractionListener mFreeItemInteractionListener =
            new FreeServicesAdapter.ItemInteractionListener() {
                @Override
                public void onLimitExceeded(SubServiceDetailModel subServiceDetailModel, int position) {
                    showLimitExceedDialog();
                }
            };

    private void showLimitExceedDialog() {
        LogUtils.LOGE(TAG, "showLimitExceedDialog: ");
        // TODO remove dummy name in dialog params
        LimitExceededDialog.newInstance(mContext, "Appliance Care",
                new AcknowledgementInteractionListener() {

                    @Override
                    public void onAcknowledgementAccepted() {
                    }
                });
    }

}