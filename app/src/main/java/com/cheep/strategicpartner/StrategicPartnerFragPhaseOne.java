package com.cheep.strategicpartner;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.TaskCreationActivity;
import com.cheep.adapter.BannerServiceRecyclerViewAdapter;
import com.cheep.databinding.FragmentStrategicPartnerPhaseOneBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.utils.ErrorLoadingHelper;

/**
 * Created by bhavesh on 28/4/17.
 */

public class StrategicPartnerFragPhaseOne extends BaseFragment {
    public static final String TAG = "SelectSubServicesForStr";
    private FragmentStrategicPartnerPhaseOneBinding mFragmentStrategicPartnerPhaseOneBinding;
    private BannerServiceRecyclerViewAdapter mSubServiceRecyclerViewAdapter;
    ErrorLoadingHelper errorLoadingHelper;
    private StrategicPartnerTaskCreationAct mStrategicPartnerTaskCreationAct;
    private boolean isVerified = false;

    @SuppressWarnings("unused")
    public static StrategicPartnerFragPhaseOne newInstance() {
        StrategicPartnerFragPhaseOne fragment = new StrategicPartnerFragPhaseOne();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentStrategicPartnerPhaseOneBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_strategic_partner_phase_one, container, false);
        return mFragmentStrategicPartnerPhaseOneBinding.getRoot();
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
        if (!isVisibleToUser || mStrategicPartnerTaskCreationAct == null) {
            return;
        }

        if (isVerified) {
            mStrategicPartnerTaskCreationAct.setTaskState(TaskCreationActivity.STEP_ONE_VERIFIED);
        } else {
            mStrategicPartnerTaskCreationAct.setTaskState(TaskCreationActivity.STEP_ONE_NORMAL);
        }

        // Hide the post task button
        mStrategicPartnerTaskCreationAct.showPostTaskButton(false, false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void initiateUI() {
        Log.d(TAG, "initiateUI() called");

        //Setting recycler view
        errorLoadingHelper = new ErrorLoadingHelper(mFragmentStrategicPartnerPhaseOneBinding.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mFragmentStrategicPartnerPhaseOneBinding.recyclerView.setLayoutManager(linearLayoutManager);
        mSubServiceRecyclerViewAdapter = new BannerServiceRecyclerViewAdapter(mSubServiceListInteractionListener);
        mFragmentStrategicPartnerPhaseOneBinding.recyclerView.setAdapter(mSubServiceRecyclerViewAdapter);
        errorLoadingHelper.showLoading();
    }

    @Override
    public void setListener() {
        Log.d(TAG, "setListener() called");
    }

    public interface SubServiceListInteractionListener {
        void onSubCategoryRowItemClicked(SubServiceDetailModel subServiceDetailModel);
    }

    private SubServiceListInteractionListener mSubServiceListInteractionListener = new SubServiceListInteractionListener() {
        @Override
        public void onSubCategoryRowItemClicked(SubServiceDetailModel subServiceDetailModel) {
            mStrategicPartnerTaskCreationAct.setSelectedSubService(subServiceDetailModel);

            Log.d(TAG, "onSubCategoryRowItemClicked() called with: subServiceDetailModel = [" + subServiceDetailModel.name + "]");
            // Make the status Verified
            isVerified = true;

            //Alert The activity that step one is been varified.
            mStrategicPartnerTaskCreationAct.setTaskState(TaskCreationActivity.STEP_ONE_VERIFIED);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mStrategicPartnerTaskCreationAct.gotoStep(TaskCreationActivity.STAGE_2);
                }
            }, 500);

        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof StrategicPartnerTaskCreationAct) {
            mStrategicPartnerTaskCreationAct = (StrategicPartnerTaskCreationAct) activity;
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
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
