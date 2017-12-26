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
import com.cheep.cheepcare.adapter.PackageBundlingAdapter;
import com.cheep.cheepcare.model.CheepCarePackageModel;
import com.cheep.databinding.FragmentPackageBundlingBinding;
import com.cheep.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PackageSummaryFragment extends BaseFragment {

    public static final String TAG = "PackageBundlingFragment";
    private PackageCustomizationActivity mPackageCustomizationActivity;
    private FragmentPackageBundlingBinding mBinding;
    private boolean isVerified = false;
    private PackageBundlingAdapter mPackageAdapter;

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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_package_bundling, container, false);
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
            mPackageCustomizationActivity.setTaskState(PackageCustomizationActivity.STEP_ONE_VERIFIED);
        } else {
            mPackageCustomizationActivity.setTaskState(PackageCustomizationActivity.STEP_ONE_UNVERIFIED);
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
        mPackageAdapter = new PackageBundlingAdapter(new PackageBundlingAdapter.PackageItemClickListener() {
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

    private List<CheepCarePackageModel> getList(List<CheepCarePackageModel> cheepCarePackages) {

        Collections.sort(cheepCarePackages, new Comparator<CheepCarePackageModel>() {
            @Override
            public int compare(CheepCarePackageModel o1, CheepCarePackageModel o2) {
                boolean b1 = o1.isSelected;
                boolean b2 = o2.isSelected;

                return (b1 != b2) ? (b1) ? -1 : 1 : 0;
            }
        });
        ArrayList<CheepCarePackageModel> newList = new ArrayList<>();
        boolean isHeaderAdded = false;
        for (CheepCarePackageModel model : cheepCarePackages) {
            if (model.isSelected) {
                model.rowType = PackageBundlingAdapter.ROW_PACKAGE_SELECTED;
            } else {
                if (!isHeaderAdded) {
                    CheepCarePackageModel model1 = new CheepCarePackageModel();
                    model1.rowType = PackageBundlingAdapter.ROW_PACKAGE_HEADER;
                    newList.add(model1);
                    isHeaderAdded = true;
                }
                model.rowType = PackageBundlingAdapter.ROW_PACKAGE_NOT_SELECTED;
            }
            newList.add(model);
        }
        return newList;
    }

    @Override
    public void setListener() {

    }


}