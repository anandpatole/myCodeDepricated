package com.cheep.addresspopupsfortask;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcarenew.activities.AddressActivity;
import com.cheep.cheepcarenew.adapters.AddressSizeRecyclerViewAdapter;
import com.cheep.custom_view.GridSpacingItemDecoration;
import com.cheep.databinding.FragmentAddressSizeForHomeOfficeBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.AddressSizeModel;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import java.util.ArrayList;

public class AddressSizeForHomeOfficeDialog extends DialogFragment {

    FragmentAddressSizeForHomeOfficeBinding mBinding;
    public static final String TAG = "AddressSizeForHomeOfficeDialog";
    private AddressSizeRecyclerViewAdapter adapter;
    private ArrayList<AddressSizeModel> list;
    public AddressModel addressModel;
    private Context mContext;
    private AddressSelectionListener addressSelectionListener;

    public void setAddressSelectionListener(AddressSelectionListener addressSelectionListener) {
        this.addressSelectionListener = addressSelectionListener;
    }

    public static AddressSizeForHomeOfficeDialog newInstance(AddressModel addressModel, AddressSelectionListener addressSelectionListener) {
        Bundle args = new Bundle();
        AddressSizeForHomeOfficeDialog fragment = new AddressSizeForHomeOfficeDialog();
        args.putString(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(addressModel));
        fragment.setAddressSelectionListener(addressSelectionListener);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_address_size_for_home_office, null, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
    }

    public void initiateUI() {
        mContext = getContext();
        setListeners();
        if (getArguments() != null)
            addressModel = (AddressModel) GsonUtility.getObjectFromJsonString(getArguments().getString(Utility.Extra.DATA), AddressModel.class);
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
                addressSelectionListener.onAddressSelection(addressModel);
                dismiss();
            }
        });
        mBinding.rvAddress.setAdapter(adapter);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.scale_25dp);
        mBinding.rvAddress.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true, 0));
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
