package com.cheep.addresspopupsfortask;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.custom_view.tooltips.ViewTooltip;
import com.cheep.databinding.DialogAddressCategorySelectionBinding;
import com.cheep.model.AddressModel;
import com.cheep.network.NetworkUtility;

import java.util.ArrayList;

public class AddressCategorySelectionDialog extends DialogFragment {

    public static final String TAG = "AddressCategorySelectio";
    DialogAddressCategorySelectionBinding mBinding;
    private AddressModel addressModel;
    private ArrayList<AddressModel> addressModelArrayList;
    private ViewTooltip.TooltipView tooltipView;
    private AddressSelectionListener listener;

    public void setListener(AddressSelectionListener listener) {
        this.listener = listener;
    }

    public static AddressCategorySelectionDialog newInstance(AddressSelectionListener listener) {
        Bundle args = new Bundle();
        AddressCategorySelectionDialog fragment = new AddressCategorySelectionDialog();
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    public void setAddressModel(AddressModel addressModel) {
        this.addressModel = addressModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_address_category_selection, container, false);
        initiateUI();
        return mBinding.getRoot();
    }


    public void initiateUI() {
        Log.e(TAG, "initiateUI: ********************");
        mBinding.tvTitle.setText(R.string.select_category);
        setListeners();


    }


    protected void setListeners() {
        mBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mBinding.cvOffice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.cvOffice.setSelected(true);
                mBinding.cvHome.setSelected(false);
                openAddNewAddressDialog(NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE);
            }
        });
        mBinding.cvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.cvOffice.setSelected(false);
                mBinding.cvHome.setSelected(true);
                openAddNewAddressDialog(NetworkUtility.TAGS.ADDRESS_TYPE.HOME);
            }
        });

        mBinding.cvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private void openAddNewAddressDialog(String category) {

        AddNewAddressDialog addNewAddressDialog = AddNewAddressDialog.newInstance(category,listener);
        addNewAddressDialog.show(((BaseAppCompatActivity) getContext()).getSupportFragmentManager(), AddNewAddressDialog.TAG);
        dismiss();
    }

}
