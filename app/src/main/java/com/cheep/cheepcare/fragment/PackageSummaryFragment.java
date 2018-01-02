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
import com.cheep.cheepcare.activity.PackageCustomizationActivity;
import com.cheep.cheepcare.adapter.SelectedPackageSummaryAdapter;
import com.cheep.cheepcare.model.CheepCarePackageModel;
import com.cheep.databinding.FragmentPackageSummaryBinding;
import com.cheep.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class PackageSummaryFragment extends BaseFragment {

    public static final String TAG = "PackageSummaryFragment";
    private PackageCustomizationActivity mPackageCustomizationActivity;
    private FragmentPackageSummaryBinding mBinding;
    private boolean isVerified = false;
    private SelectedPackageSummaryAdapter mPackageAdapter;

    public static PackageSummaryFragment newInstance() {
        return new PackageSummaryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_package_summary, container, false);
        return mBinding.getRoot();
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
        if (!isVisibleToUser || mPackageCustomizationActivity == null) {
            return;
        }

        if (isVerified) {
            mPackageCustomizationActivity.setTaskState(PackageCustomizationActivity.STEP_THREE_VERIFIED);
        } else {
            mPackageCustomizationActivity.setTaskState(PackageCustomizationActivity.STEP_THREE_UNVERIFIED);
        }

        // Hide the post task button
//        mPackageCustomizationActivity.showPostTaskButton(false, false);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof PackageCustomizationActivity) {
            mPackageCustomizationActivity = (PackageCustomizationActivity) activity;
        }
    }

    @Override
    public void initiateUI() {

        mBinding.rvBundlePackages.setNestedScrollingEnabled(false);
        mPackageAdapter = new SelectedPackageSummaryAdapter(new SelectedPackageSummaryAdapter.PackageItemClickListener() {
            @Override
            public void onPackageItemClick(int position, CheepCarePackageModel packageModel) {

            }
        });
        mPackageAdapter.addPakcageList(getList(CheepCarePackageModel.getCheepCarePackages()));
        mBinding.rvBundlePackages.setLayoutManager(new LinearLayoutManager(
                mContext
                , LinearLayoutManager.VERTICAL
                , false
        ));
        mBinding.rvBundlePackages.setAdapter(mPackageAdapter);
    }

    @Override
    public void setListener() {

    }

    private List<CheepCarePackageModel> getList(List<CheepCarePackageModel> cheepCarePackages) {
        ArrayList<CheepCarePackageModel> newList = new ArrayList<>();
        for (CheepCarePackageModel model : cheepCarePackages) {
            if (model.isSelected) {
                newList.add(model);
            }
        }
        return newList;
    }
}