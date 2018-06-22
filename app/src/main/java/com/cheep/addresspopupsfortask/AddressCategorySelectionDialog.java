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
import android.widget.Toast;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcarenew.dialogs.EditAddressDialog;
import com.cheep.custom_view.tooltips.ViewTooltip;
import com.cheep.databinding.DialogAddressCategorySelectionBinding;
import com.cheep.model.AddressModel;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.Utility;

import java.util.ArrayList;

public class AddressCategorySelectionDialog extends DialogFragment {

    public static final String TAG = "AddressCategorySelectio";
    private DialogAddressCategorySelectionBinding mBinding;
    private AddressModel addressModel;
    private ArrayList<AddressModel> addressModelArrayList;
    private ViewTooltip.TooltipView tooltipView;
    private AddressSelectionListener listener;
    private String COMING_FORM;
    private ArrayList<AddressModel> listOfAddress;
    private EditAddressDialog editAddressDialog;

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

    public static AddressCategorySelectionDialog newInstance(String comingFrom,ArrayList<AddressModel> addressList) {
        Bundle args = new Bundle();
        AddressCategorySelectionDialog fragment = new AddressCategorySelectionDialog();
        args.putString(Utility.TAG, comingFrom);
        args.putString(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(addressList));
        fragment.setArguments(args);
        return fragment;
    }

    public void setAddressModel(AddressModel addressModel) {
        this.addressModel = addressModel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            COMING_FORM = getArguments().getString(Utility.TAG);
            if (getArguments().getString(Utility.Extra.DATA) != null)
                listOfAddress = GsonUtility.getObjectListFromJsonString(getArguments().getString(Utility.Extra.DATA), AddressModel[].class);
        }
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
        if(COMING_FORM.equalsIgnoreCase(Utility.EDIT_PROFILE_ACTIVITY)){
            mBinding.tvTitle.setText(R.string.label_please_tell_us_where_do_you_need_the_amc_for);
            if (listOfAddress != null) {
                for (int i = 0; i < listOfAddress.size(); i++) {
                    if(listOfAddress.get(0).category.equalsIgnoreCase(NetworkUtility.TAGS.HOME)){
                        mBinding.cvHome.setSelected(true);
                        mBinding.cvOffice.setSelected(false);

                    }else if(listOfAddress.get(0).category.equalsIgnoreCase(NetworkUtility.TAGS.OFFICE)){
                        mBinding.cvOffice.setSelected(false);
                        mBinding.cvHome.setSelected(true);
                    }
                    break;
                }
            }
        }else {
            mBinding.tvTitle.setText(R.string.select_category);
        }

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
                if(COMING_FORM.equalsIgnoreCase(Utility.EDIT_PROFILE_ACTIVITY)){
                    showEditAddressDialog(NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE);
                }else {
                    mBinding.cvOffice.setSelected(true);
                    mBinding.cvHome.setSelected(false);
                    openAddNewAddressDialog(NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE);
                }

            }
        });
        mBinding.cvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(COMING_FORM.equalsIgnoreCase(Utility.EDIT_PROFILE_ACTIVITY)){
                    showEditAddressDialog(NetworkUtility.TAGS.ADDRESS_TYPE.HOME);
                }else {
                    mBinding.cvOffice.setSelected(false);
                    mBinding.cvHome.setSelected(true);
                    openAddNewAddressDialog(NetworkUtility.TAGS.ADDRESS_TYPE.HOME);
                }

            }
        });

        mBinding.cvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private void openAddNewAddressDialog(String category) {

        AddNewAddressDialog addNewAddressDialog = AddNewAddressDialog.newInstance(category, listener);
        addNewAddressDialog.show(((BaseAppCompatActivity) getContext()).getSupportFragmentManager(), AddNewAddressDialog.TAG);
        dismiss();
    }

    // open show Edit Address Dialog
    private void showEditAddressDialog(String addressType) {
        if (editAddressDialog != null) {
            editAddressDialog.dismissAllowingStateLoss();
            editAddressDialog = null;
        }
        editAddressDialog = EditAddressDialog.newInstance(addressType, listOfAddress);
        editAddressDialog.show(getActivity().getSupportFragmentManager(), TAG);
    }

}
