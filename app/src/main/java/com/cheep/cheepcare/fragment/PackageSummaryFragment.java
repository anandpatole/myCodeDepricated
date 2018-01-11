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
import com.cheep.cheepcare.model.CheepCarePackageServicesModel;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.cheepcare.model.PackageOption;
import com.cheep.databinding.FragmentPackageSummaryBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class PackageSummaryFragment extends BaseFragment {

    public static final String TAG = "PackageSummaryFragment";
    private PackageCustomizationActivity mPackageCustomizationActivity;
    private FragmentPackageSummaryBinding mBinding;
    private boolean isVerified = false;
    private SelectedPackageSummaryAdapter mPackageAdapter;
    boolean isYearly = true;

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

        if (mPackageAdapter != null && mPackageAdapter.getList() != null) {
            mPackageAdapter.getList().clear();
            mPackageAdapter.addPakcageList(getList());
            calculateTotalPrice();
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

        // package recycler view item click listener
        mPackageAdapter = new SelectedPackageSummaryAdapter(new SelectedPackageSummaryAdapter.PackageItemClickListener() {

            @Override
            public void onPackageItemClick(int position, PackageDetail packageModel) {

            }

            @Override
            public void onRemovePackage(int position, PackageDetail packageModel) {

                for (PackageDetail detail : mPackageCustomizationActivity.getPackageList())
                    if (detail.id.equalsIgnoreCase(packageModel.id)) {
                        detail.isSelected = false;
                        detail.mSelectedAddress = null;
                        detail.packageOptionList = null;
                        mPackageAdapter.getList().remove(position);
                        calculateTotalPrice();
                    }
                mPackageAdapter.notifyDataSetChanged();
            }
        });

        mPackageAdapter.addPakcageList(getList());

        mBinding.rvBundlePackages.setLayoutManager(new LinearLayoutManager(
                mContext
                , LinearLayoutManager.VERTICAL
                , false
        ));

        mBinding.rvBundlePackages.setAdapter(mPackageAdapter);


        mBinding.ivHalfYearlyPackage.setSelected(!isYearly);
        mBinding.ivYearlyPackage.setSelected(isYearly);
        calculateTotalPrice();

    }

    @Override
    public void setListener() {

        mBinding.rlHalfYearlyPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYearly = false;
                calculateTotalPrice();
            }
        });


        mBinding.rlYearlyPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYearly = true;
                calculateTotalPrice();

            }
        });
    }

    private void calculateTotalPrice() {
        double totalPrice = 0;
        mBinding.ivHalfYearlyPackage.setSelected(!isYearly);
        mBinding.ivYearlyPackage.setSelected(isYearly);
        for (PackageDetail detail : mPackageAdapter.getList()) {
            if (detail.packageOptionList != null) {
                for (CheepCarePackageServicesModel service : detail.packageOptionList) {
                    if (service.selectionType.equalsIgnoreCase(CheepCarePackageServicesModel.SELECTION_TYPE.RADIO)) {
                        for (PackageOption option : service.getChildList()) {
                            if (option.isSelected) {
                                totalPrice += isYearly ? Double.valueOf(option.annualPrice) : Double.valueOf(option.sixmonthPrice);
                            }
                        }
                    } else {
                        PackageOption option = service.getChildList().get(0);
                        totalPrice += isYearly ? Double.valueOf(option.annualPrice) : Double.valueOf(option.sixmonthPrice);
                    }
                }
            }


        }
        LogUtils.LOGE(TAG, "calculateTotalPrice: totalPrice :: " + totalPrice);
        mBinding.textTotal.setText(getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(String.valueOf(totalPrice))));
    }

    private List<PackageDetail> getList() {
        ArrayList<PackageDetail> newList = new ArrayList<>();
        for (PackageDetail model : mPackageCustomizationActivity.getPackageList()) {
            if (model.isSelected) {
                newList.add(model);
            }
        }
        return newList;
    }
}