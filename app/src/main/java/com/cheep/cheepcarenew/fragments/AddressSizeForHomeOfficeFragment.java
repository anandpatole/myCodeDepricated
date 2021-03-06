package com.cheep.cheepcarenew.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.activity.LoginActivity;
import com.cheep.cheepcarenew.activities.AddressActivity;
import com.cheep.cheepcarenew.activities.PaymentSummaryCheepCareActivity;
import com.cheep.cheepcarenew.adapters.AddressSizeRecyclerViewAdapter;
import com.cheep.cheepcarenew.model.CareCityDetail;
import com.cheep.cheepcarenew.model.PackageDetail;
import com.cheep.custom_view.GridSpacingItemDecoration;
import com.cheep.databinding.FragmentAddressSizeForHomeOfficeBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.AddressSizeModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import java.util.ArrayList;

public class AddressSizeForHomeOfficeFragment extends BaseFragment {

    private FragmentAddressSizeForHomeOfficeBinding mBinding;
    public static final String TAG = "AddressCategorySelectionFragment";
    private AddressSizeRecyclerViewAdapter adapter;
    private ArrayList<AddressSizeModel> list;
    public AddressModel addressModel;
    public PackageDetail packageDetail;
    public CareCityDetail careCityDetail;

    public static AddressSizeForHomeOfficeFragment newInstance(AddressModel addressModel, PackageDetail packageDetail, CareCityDetail careCityDetail) {
        Bundle args = new Bundle();
        AddressSizeForHomeOfficeFragment fragment = new AddressSizeForHomeOfficeFragment();
        args.putString(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(addressModel));
        args.putString(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(packageDetail));
        args.putString(Utility.Extra.DATA_3, GsonUtility.getJsonStringFromObject(careCityDetail));
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.fragment_address_size_for_home_office, null, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
    }

    @Override
    public void initiateUI() {
        setListeners();
        if (getArguments() != null) {
            addressModel = (AddressModel) GsonUtility.getObjectFromJsonString(getArguments().getString(Utility.Extra.DATA), AddressModel.class);
            packageDetail = (PackageDetail) GsonUtility.getObjectFromJsonString(getArguments().getString(Utility.Extra.DATA_2), PackageDetail.class);
            careCityDetail = (CareCityDetail) GsonUtility.getObjectFromJsonString(getArguments().getString(Utility.Extra.DATA_3), CareCityDetail.class);
        }
        if (addressModel == null)
            return;

        if (addressModel.category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.HOME)) {
            mBinding.tvTitle.setText(R.string.label_how_big_is_your_home);
            list = GsonUtility.getObjectListFromJsonString(PreferenceUtility.getInstance(mContext).getHomeAddressSize(), AddressSizeModel[].class);
        } else {
            mBinding.tvTitle.setText(R.string.label_how_big_is_your_office);
            list = GsonUtility.getObjectListFromJsonString(PreferenceUtility.getInstance(mContext).getOfficeAddressSize(), AddressSizeModel[].class);
        }

        mBinding.rvAddress.setLayoutManager(new GridLayoutManager(mContext, 2));
        adapter = new AddressSizeRecyclerViewAdapter(list, new AddressSizeRecyclerViewAdapter.AddressSizeClickListener() {
            @Override
            public void onClickAddressSize(AddressSizeModel model) {
                addressModel.addressSizeModel = model;
                if (packageDetail.type.equalsIgnoreCase(Utility.CAR_PACKAGE_TYPE.NORMAL)) {
                    packageDetail.priceModel = model.normalPriceModel;
                } else
                    packageDetail.priceModel = model.premiumPriceModel;

                UserDetails userDetails = PreferenceUtility.getInstance(getContext()).getUserDetails();
                if (userDetails == null) {
                    LoginActivity.newInstance(getContext());
                } else {
                    //PaymentSummaryCheepCareActivity.newInstance(mContext, packageDetail, careCityDetail, addressModel);
                    PaymentSummaryCheepCareActivity.newInstance(mContext, careCityDetail,packageDetail, addressModel);
                }

            }
        });
        mBinding.rvAddress.setAdapter(adapter);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.scale_25dp);
        mBinding.rvAddress.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true, 0));
    }

    @Override
    public void setListener() {

    }

    protected void setListeners() {
        mBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AddressActivity) mContext).onBackPressed();

            }
        });

    }


}
