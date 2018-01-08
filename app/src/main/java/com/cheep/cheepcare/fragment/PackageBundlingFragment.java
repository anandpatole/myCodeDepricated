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
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.databinding.FragmentPackageBundlingBinding;
import com.cheep.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PackageBundlingFragment extends BaseFragment {

    public static final String TAG = "PackageBundlingFragment";
    private PackageCustomizationActivity mPackageCustomizationActivity;
    private FragmentPackageBundlingBinding mBinding;
    private boolean isVerified = false;
    private PackageBundlingAdapter mPackageAdapter;

    public static PackageBundlingFragment newInstance() {
        return new PackageBundlingFragment();
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
            mPackageCustomizationActivity.setTaskState(PackageCustomizationActivity.STEP_TWO_VERIFIED);
        } else {
            mPackageCustomizationActivity.setTaskState(PackageCustomizationActivity.STEP_TWO_UNVERIFIED);
        }
        mPackageAdapter = new PackageBundlingAdapter(new PackageBundlingAdapter.PackageItemClickListener() {
            @Override
            public void onPackageItemClick(int position, PackageDetail packageModel) {
                mPackageCustomizationActivity.mPackageId = packageModel.id;
                mPackageCustomizationActivity.gotoStep(PackageCustomizationActivity.STAGE_1);
                mPackageCustomizationActivity.loadAnotherPackage();
            }
        });
        mPackageAdapter.addPakcageList(getList());
        mBinding.rvBundlePackages.setLayoutManager(new LinearLayoutManager(
                mContext
                , LinearLayoutManager.VERTICAL
                , false
        ));
        mBinding.rvBundlePackages.setAdapter(mPackageAdapter);


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


    }

    private List<PackageDetail> getList() {

        Collections.sort(mPackageCustomizationActivity.getPackageList(), new Comparator<PackageDetail>() {
            @Override
            public int compare(PackageDetail o1, PackageDetail o2) {
                boolean b1 = o1.isSelected;
                boolean b2 = o2.isSelected;

                return (b1 != b2) ? (b1) ? -1 : 1 : 0;
            }
        });
        ArrayList<PackageDetail> newList = new ArrayList<>();
        boolean isHeaderAdded = false;
        for (PackageDetail model : mPackageCustomizationActivity.getPackageList()) {
            if (model.isSelected) {
                model.rowType = PackageBundlingAdapter.ROW_PACKAGE_SELECTED;
            } else {
                if (!isHeaderAdded) {
                    PackageDetail model1 = new PackageDetail();
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