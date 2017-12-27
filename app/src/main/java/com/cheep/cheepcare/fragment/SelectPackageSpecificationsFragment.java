package com.cheep.cheepcare.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.activity.PackageCustomizationActivity;
import com.cheep.cheepcare.adapter.AddressAdapter;
import com.cheep.cheepcare.adapter.ExpandablePackageServicesRecyclerAdapter;
import com.cheep.cheepcare.model.CheepCarePackageServicesModel;
import com.cheep.cheepcare.model.CheepCarePackageSubServicesModel;
import com.cheep.databinding.FragmentSelectPackageSpecificationBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 12/25/17.
 */

public class SelectPackageSpecificationsFragment extends BaseFragment {

    public static final String TAG = SelectPackageSpecificationsFragment.class.getSimpleName();
    private PackageCustomizationActivity mPackageCustomizationActivity;
    private FragmentSelectPackageSpecificationBinding mBinding;
    private boolean isVerified = false;
    private AddressAdapter<AddressModel> mAdapter;
    private List<AddressModel> mList;
    private boolean isClicked = false;

    public static SelectPackageSpecificationsFragment newInstance() {
        return new SelectPackageSpecificationsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_package_specification, container, false);
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
        mBinding.ivIsAddressSelected.setSelected(true);
    }

    @Override
    public void setListener() {
        mList = getDummyAddressList();
        mList.add(0, new AddressModel() {{
            address = getString(R.string.label_select_address);
        }});
        mAdapter = new AddressAdapter<>(mContext
                , android.R.layout.simple_spinner_item
                , mList);
        mBinding.spinnerAddressSelection.setAdapter(mAdapter);
        mBinding.spinnerAddressSelection.setFocusable(false);
        mBinding.spinnerAddressSelection.setPrompt("Prompt");
        mBinding.spinnerAddressSelection.setSelected(false);
        mBinding.spinnerAddressSelection.setFocusableInTouchMode(false);
        mBinding.spinnerAddressSelection.setSelection(-1);
        mBinding.spinnerAddressSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isClicked && position == 0) {
                    mBinding.tvSpinnerAddress.setText(getString(R.string.label_select_address));
                    mBinding.ivIsAddressSelected.setSelected(false);
                    return;
                }
                mBinding.tvSpinnerAddress.setText(mList.get(position).address);
                mBinding.ivIsAddressSelected.setSelected(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mBinding.tvSpinnerAddress.setText(getString(R.string.label_select_address));
            }
        });

        mBinding.tvSpinnerAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClicked = true;
                mBinding.spinnerAddressSelection.performClick();
            }
        });

        mBinding.recyclerView.setAdapter(new ExpandablePackageServicesRecyclerAdapter(getServiceList(), true));
    }

    public List<AddressModel> getDummyAddressList() {
        List<AddressModel> list = new ArrayList<>();
        list.add(new AddressModel() {{
            address_initials = "Home 1";
            address = "ABCD, EFGJ, HIJKLMNOP, LMNO1";
        }});
        list.add(new AddressModel() {{
            address_initials = "Home 2";
            address = "ABCD, EFGJ, HIJKLMNOP, LMNO2";
        }});
        list.add(new AddressModel() {{
            address_initials = "Home 3";
            address = "ABCD, EFGJ, HIJKLMNOP, LMNO3";
        }});
        return list;
    }

    public List<CheepCarePackageServicesModel> getServiceList() {
        final List<CheepCarePackageSubServicesModel> subList = new ArrayList<>();
        subList.add(new CheepCarePackageSubServicesModel() {{
            subSubCatName = "1 & 2 BHK";
            price = "200/MTH";
            subSubCatId = "1";
            subCategoryName = "Essential Home Care Services";
            isSelected = false;
        }});

        subList.add(new CheepCarePackageSubServicesModel() {{
            subSubCatName = "3 BHK";
            price = "206/MTH";
            subSubCatId = "2";
            subCategoryName = "Essential Home Care Services";
            isSelected = false;
        }});

        subList.add(new CheepCarePackageSubServicesModel() {{
            subSubCatName = "4 BHK";
            price = "225/MTH";
            subSubCatId = "3";
            subCategoryName = "Essential Home Care Services";
            isSelected = false;
        }});

        subList.add(new CheepCarePackageSubServicesModel() {{
            subSubCatName = "5 BHK";
            price = "263/MTH";
            subSubCatId = "4";
            subCategoryName = "Essential Home Care Services";
            isSelected = false;
        }});

        subList.add(new CheepCarePackageSubServicesModel() {{
            subSubCatName = "6 BHK";
            price = "375/MTH";
            subSubCatId = "5";
            subCategoryName = "Essential Home Care Services";
            isSelected = false;
        }});

        List<CheepCarePackageServicesModel> list = new ArrayList<>();
        list.add(new CheepCarePackageServicesModel() {{
            catId = 1;
            sub_cat_id = 1;
            name = "Essential Home Care Services";
            description = "(Plumbing+Carpentry+Electrician)";
            subServices = subList;
            isSelected = false;
        }});
        return list;
    }
}
