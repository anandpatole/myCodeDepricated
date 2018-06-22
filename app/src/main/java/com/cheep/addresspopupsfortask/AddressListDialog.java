package com.cheep.addresspopupsfortask;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
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
    private ArrayList<AddressModel> addressList = new ArrayList<>();
    private boolean needsAskForAddressSize;
    private AddressSelectionListener addressSelectionListener;
    private boolean isWhiteTheme;

    public void setAddressSelectionListener(AddressSelectionListener addressSelectionListener) {
        this.addressSelectionListener = addressSelectionListener;
    }

    public static AddressListDialog newInstance(boolean isWhiteTheme, boolean needsAskForAddressSize, AddressSelectionListener addressSelectionListener) {
        Bundle args = new Bundle();
//        args.putString(Utility.Extra.DATA, subscriptionType);
        args.putBoolean(Utility.Extra.DATA, isWhiteTheme);
        args.putBoolean(Utility.Extra.DATA_2, needsAskForAddressSize);
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
        addressList.clear();
        if (getArguments() == null)
            return;
        isWhiteTheme = getArguments().getBoolean(Utility.Extra.DATA);
        needsAskForAddressSize = getArguments().getBoolean(Utility.Extra.DATA_2);
        if (isWhiteTheme) {
            mBinding.imgBack.setImageResource(R.drawable.icon_arrow_back_blue);
            mBinding.rlTop.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
            mBinding.tvTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.splash_gradient_end));
        }
        UserDetails userDetails = PreferenceUtility.getInstance(getContext()).getUserDetails();
        if (userDetails != null && userDetails.addressList != null && !userDetails.addressList.isEmpty()) {
            addressList.addAll(userDetails.addressList);
        } else {
            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(getContext()).getGuestUserDetails();
            if (guestUserDetails != null && guestUserDetails.addressList != null && !guestUserDetails.addressList.isEmpty()) {
                addressList.addAll(guestUserDetails.addressList);
            }
        }

//        // if is_subscribe is yes than
//        if (subscriptionType.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.PREMIUM)) {
//            for (AddressModel addressModel : arrayList) {
//                if (addressModel.is_subscribe.equalsIgnoreCase(subscriptionType)) {
//                    addressList.add(addressModel);
//                }
//            }
//
//        } else if (subscriptionType.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.NORMAL)) {
//            for (AddressModel addressModel : arrayList) {
//                if (!addressModel.is_subscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.NONE)) {
//                    addressList.add(addressModel);
//                }
//            }
//        } else {
//            for (AddressModel addressModel : arrayList) {
//                if (addressModel.is_subscribe.equalsIgnoreCase(subscriptionType)) {
//                    addressList.add(addressModel);
//                }
//            }
//        }

        mBinding.rvAddress.setNestedScrollingEnabled(false);
        mBinding.rvAddress.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new AddressListRecyclerViewAdapter(addressList, new AddressListRecyclerViewAdapter.AddressItemClickListener() {
            @Override
            public void onClickItem(AddressModel addressModel) {

                if (needsAskForAddressSize) {
                    AddressSizeForHomeOfficeDialog addressSizeForHomeOfficeDialog = AddressSizeForHomeOfficeDialog.newInstance(addressModel, new AddressSelectionListener() {
                        @Override
                        public void onAddressSelection(AddressModel addressModel) {
                            addressSelectionListener.onAddressSelection(addressModel);
                            dismiss();
                        }
                    });
                    addressSizeForHomeOfficeDialog.show(((BaseAppCompatActivity) getContext()).getSupportFragmentManager(), AddressSizeForHomeOfficeDialog.TAG);
                } else {
                    addressSelectionListener.onAddressSelection(addressModel);
                    dismiss();
                }
            }
        });
        mBinding.rvAddress.setAdapter(adapter);

        if (addressList.isEmpty()) {
            openAddAddressDialog();
        }
    }


    private void openAddAddressDialog() {
        AddressCategorySelectionDialog addressCategorySelectionDialog = AddressCategorySelectionDialog.newInstance(isWhiteTheme,this);
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
