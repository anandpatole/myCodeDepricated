package com.cheep.addresspopupsfortask;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcarenew.adapters.AddressListRecyclerViewAdapter;
import com.cheep.databinding.DialogAddressListBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.GuestUserDetails;
import com.cheep.model.UserDetails;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import java.util.ArrayList;

public class AddressListDialog extends DialogFragment implements AddressSelectionListener {

    public static final String TAG = "AddressListDialog";
    DialogAddressListBinding mBinding;
    AddressListRecyclerViewAdapter adapter;
    private ArrayList<AddressModel> list = new ArrayList<>();
    private String category;
    private AddressSelectionListener addressSelectionListener;

    public void setAddressSelectionListener(AddressSelectionListener addressSelectionListener) {
        this.addressSelectionListener = addressSelectionListener;
    }

    public static AddressListDialog newInstance(AddressSelectionListener addressSelectionListener) {
        Bundle args = new Bundle();
        AddressListDialog dialog = new AddressListDialog();
        dialog.setAddressSelectionListener(addressSelectionListener);
        dialog.setArguments(args);
        return dialog;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_address_list, container, false);
        initiateUI();
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    public void initiateUI() {
        setListeners();
        list.clear();
        if (getArguments() == null)
            return;
        category = getArguments().getString(Utility.Extra.DATA);

        UserDetails userDetails = PreferenceUtility.getInstance(getContext()).getUserDetails();
        if (userDetails != null && userDetails.addressList != null && !userDetails.addressList.isEmpty()) {
            if (userDetails.addressList.get(0) != null) {
                list.addAll(userDetails.addressList);
            }

        } else {
            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(getContext()).getGuestUserDetails();
            if (guestUserDetails != null && guestUserDetails.addressList != null && !guestUserDetails.addressList.isEmpty()) {
                if (guestUserDetails.addressList.get(0) != null) {
                    list.addAll(guestUserDetails.addressList);
                }
            }
        }


        mBinding.rvAddress.setNestedScrollingEnabled(false);
        mBinding.rvAddress.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new AddressListRecyclerViewAdapter(list, new AddressListRecyclerViewAdapter.AddressItemClickListener() {
            @Override
            public void onClickItem(AddressModel addressModel) {
                addressSelectionListener.onAddressSelection(addressModel);
                dismiss();
            }
        });
        mBinding.rvAddress.setAdapter(adapter);

        if (list.isEmpty()) {
            openAddAddressDialog();
        }
    }

    private void openAddAddressDialog() {
        AddressCategorySelectionDialog addressCategorySelectionDialog = AddressCategorySelectionDialog.newInstance(this);
        addressCategorySelectionDialog.show(((BaseAppCompatActivity) getContext()).getSupportFragmentManager(), AddressCategorySelectionDialog.TAG);
    }

    protected void setListeners() {
        mBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mBinding.rlBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddAddressDialog();
            }
        });
    }

    @Override
    public void onAddressSelection(AddressModel addressModel) {
        adapter.add(addressModel);
        adapter.notifyDataSetChanged();
    }
}
