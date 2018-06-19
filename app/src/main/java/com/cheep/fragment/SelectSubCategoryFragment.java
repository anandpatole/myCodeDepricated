package com.cheep.fragment;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.TaskCreationActivity;
import com.cheep.adapter.SubServiceRecyclerViewAdapter;
import com.cheep.databinding.FragmentSelectSubserviceBinding;
import com.cheep.dialogs.PestControlHelpDialog;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;
import com.payu.magicretry.MainActivity;

import java.util.ArrayList;

/**
 * Created by bhavesh on 28/4/17.
 */

public class SelectSubCategoryFragment extends BaseFragment implements  WebCallClass.GetNeedHelpResponseListener,WebCallClass.CommonResponseListener,PestControlHelpDialog.PestControlHelpListener{
    public static final String TAG = SelectSubCategoryFragment.class.getSimpleName();
    private FragmentSelectSubserviceBinding mFragmentSelectSubserviceBinding;
    private SubServiceRecyclerViewAdapter mSubServiceRecyclerViewAdapter;
    private TaskCreationActivity mTaskCreationActivity;
    public boolean isVerified = false;
    public JobCategoryModel mJobCategoryModel;
    PestControlHelpDialog dialog;
    @SuppressWarnings("unused")
    public static SelectSubCategoryFragment newInstance() {
        SelectSubCategoryFragment fragment = new SelectSubCategoryFragment();

        return fragment;
    }

    public ArrayList<SubServiceDetailModel> getSubCatList() {
        ArrayList<SubServiceDetailModel> subServiceDetailModels = new ArrayList<>();
        for (SubServiceDetailModel subServiceDetailModel : mSubServiceRecyclerViewAdapter.getList()) {
            if (subServiceDetailModel.isSelected)
                subServiceDetailModels.add(subServiceDetailModel);
        }
        return subServiceDetailModels;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentSelectSubserviceBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_subservice, container, false);
        return mFragmentSelectSubserviceBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
        if (getActivity().getIntent().getExtras() != null) {
            // Fetch JobCategory Model
            mJobCategoryModel = (JobCategoryModel) GsonUtility.getObjectFromJsonString(getActivity().getIntent().getStringExtra(Utility.Extra.DATA), JobCategoryModel.class);
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
        if (!isVisibleToUser || mTaskCreationActivity == null) {
            return;
        }

        mTaskCreationActivity.setTaskState(TaskCreationActivity.STEP_ONE_NORMAL);

        // Hide the post task button
        if (getSubCatList().isEmpty()) {
            mTaskCreationActivity.showPostTaskButton(true, false);
        } else {
            mTaskCreationActivity.showPostTaskButton(true, true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void initiateUI() {
        Log.d(TAG, "initiateUI() called");

        mTaskCreationActivity.showPostTaskButton(true, false);
        //Setting recycler view

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mFragmentSelectSubserviceBinding.recyclerView.setLayoutManager(linearLayoutManager);

        mSubServiceRecyclerViewAdapter = new SubServiceRecyclerViewAdapter(mSubServiceListInteractionListener);
        mSubServiceRecyclerViewAdapter.addList(mTaskCreationActivity.allSubCategoryList);
        mFragmentSelectSubserviceBinding.recyclerView.setAdapter(mSubServiceRecyclerViewAdapter);
        if (getSubCatList().isEmpty()) {
            mTaskCreationActivity.showPostTaskButton(true, false);
        } else {
            mTaskCreationActivity.showPostTaskButton(true, true);
        }
    }

    @Override
    public void setListener() {
    }


    @Override
    public void volleyError(VolleyError error) {
        hideProgressDialog();
    }

    @Override
    public void showSpecificMessage(String message) {
        hideProgressDialog();
    }

    @Override
    public void forceLogout() {
        hideProgressDialog();

    }

    @Override
    public void getNeedHelp() {
        hideProgressDialog();
dialog.dismiss();

    }

    @Override
    public void onHelpClick() {
        WebCallClass.getNeedHelp(mContext,SelectSubCategoryFragment.this,SelectSubCategoryFragment.this,mJobCategoryModel.catId);
    }
    public interface SubServiceListInteractionListener {
        void onSubCategoryRowItemClicked(SubServiceDetailModel subServiceDetailModel);
    }

    private SubServiceListInteractionListener mSubServiceListInteractionListener = new SubServiceListInteractionListener() {
        @Override
        public void onSubCategoryRowItemClicked(SubServiceDetailModel subServiceDetailModel) {
//            mTaskCreationActivity.setSelectedSubService(subServiceDetailModel);
//            if(TaskCreationActivity.mJobCategoryModel.catSlug.equalsIgnoreCase(Utility.cat.PESTCONTROL))
//            {
//                for (SubServiceDetailModel model: subServiceDetailModel)
//                {
                    //SubServiceDetailModel model= allSubCategoryList.get(0);
//                    if(subServiceDetailModel.isSelected)
//                    {
                        String s = subServiceDetailModel.name;
                        if(subServiceDetailModel.name.equalsIgnoreCase(Utility.NEED_HELP))
                        {
                            subServiceDetailModel.isSelected=false;
                            mSubServiceRecyclerViewAdapter.notifyDataSetChanged();
                            dialog= PestControlHelpDialog.newInstance(SelectSubCategoryFragment.this);
                            dialog.show(getFragmentManager(),"PestControlHelp");
                         // return;
                           // showProgressDialog();
                           // WebCallClass.getNeedHelp(mContext,TaskCreationActivity.this,TaskCreationActivity.this,mJobCategoryModel.catId);
                           // return;
                        }
               //     }
               // }
         //   }

            Log.d(TAG, "onSubCategoryRowItemClicked() called with: subServiceDetailModel = [" + subServiceDetailModel.name + "]");
            // Make the status Verified
            isVerified = true;
            //Alert The activity that step one is been verified.
            if (getSubCatList().isEmpty()) {
                mTaskCreationActivity.showPostTaskButton(true, false);
            } else {
                mTaskCreationActivity.showPostTaskButton(true, true);
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof TaskCreationActivity) {
            mTaskCreationActivity = (TaskCreationActivity) activity;
        }
    }

    public void setCheepTipUI(String title, String subTitle) {
        mFragmentSelectSubserviceBinding.tvLandingScreenTipTitle.setText(title);
        mFragmentSelectSubserviceBinding.tvLandingScreenTipSubtitle.setText(subTitle);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.FETCH_SUB_SERVICE_LIST);
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
