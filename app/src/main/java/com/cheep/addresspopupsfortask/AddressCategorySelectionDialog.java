package com.cheep.addresspopupsfortask;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private String COMING_FORM = Utility.EMPTY_STRING;
    private ArrayList<AddressModel> listOfAddress;
    private EditAddressDialog editAddressDialog;
    int addressPosition = 0;
    private boolean isWhiteTheme;

    public void setListener(AddressSelectionListener listener) {
        this.listener = listener;
    }

    public static AddressCategorySelectionDialog newInstance(boolean isWhiteTheme, AddressSelectionListener listener) {
        Bundle args = new Bundle();
        AddressCategorySelectionDialog fragment = new AddressCategorySelectionDialog();
        args.putBoolean(Utility.Extra.DATA_3, isWhiteTheme);
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    public static AddressCategorySelectionDialog newInstance(String comingFrom, boolean isWhiteTheme, ArrayList<AddressModel> addressList, int addressPosition) {
        Bundle args = new Bundle();
        AddressCategorySelectionDialog fragment = new AddressCategorySelectionDialog();
        args.putString(Utility.TAG, comingFrom);
        args.putString(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(addressList));
        args.putInt(Utility.Extra.POSITION, addressPosition);
        args.putString(Utility.Extra.DATA_2, comingFrom);
        args.putBoolean(Utility.Extra.DATA_3, isWhiteTheme);
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
            listOfAddress = GsonUtility.getObjectListFromJsonString(getArguments().getString(Utility.Extra.DATA), AddressModel[].class);
            addressPosition = getArguments().getInt(Utility.Extra.POSITION);
            isWhiteTheme = getArguments().getBoolean(Utility.Extra.DATA_3);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_address_category_selection, container, false);
        initiateUI();
        setListeners();
        return mBinding.getRoot();
    }


    public void initiateUI() {
        int position = 0;
        Log.e(TAG, "initiateUI: ********************");
        if (COMING_FORM.equalsIgnoreCase(Utility.EDIT_PROFILE_ACTIVITY)) {
            mBinding.tvTitle.setText(R.string.label_please_tell_us_where_do_you_need_the_amc_for);
            if (listOfAddress != null) {
                for (int i = 0; i < listOfAddress.size(); i++) {
                    if (addressPosition == position) {
                        if (listOfAddress.get(i).category.equalsIgnoreCase(NetworkUtility.TAGS.HOME)) {
                            mBinding.cvHome.setSelected(true);
                            mBinding.cvOffice.setSelected(false);

                        } else if (listOfAddress.get(i).category.equalsIgnoreCase(NetworkUtility.TAGS.OFFICE)) {
                            mBinding.cvOffice.setSelected(false);
                            mBinding.cvHome.setSelected(true);
                        }
                        if (isWhiteTheme) {
                            mBinding.imgBack.setImageResource(R.drawable.icon_arrow_back_blue);
                            mBinding.rlTop.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                            mBinding.tvTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.splash_gradient_end));
                        }
                        mBinding.tvTitle.setText(R.string.label_please_tell_us_where_do_you_need_the_amc_for);

                    }
                }
            }
        } else {
            mBinding.tvTitle.setText(R.string.select_category);
        }

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
                if (COMING_FORM.equalsIgnoreCase(Utility.EDIT_PROFILE_ACTIVITY)) {
                    showEditAddressDialog(NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE);
                    dismiss();
                } else {
                    mBinding.cvOffice.setSelected(true);
                    mBinding.cvHome.setSelected(false);
                    openAddNewAddressDialog(NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE);
                }

            }
        });
        mBinding.cvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (COMING_FORM.equalsIgnoreCase(Utility.EDIT_PROFILE_ACTIVITY)) {
                    showEditAddressDialog(NetworkUtility.TAGS.ADDRESS_TYPE.HOME);
                    dismiss();
                } else {
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
        AddNewAddressDialog addNewAddressDialog = AddNewAddressDialog.newInstance(category, isWhiteTheme, listener);
        addNewAddressDialog.show(((BaseAppCompatActivity) getContext()).getSupportFragmentManager(), AddNewAddressDialog.TAG);
        dismiss();
    }

    // open show Edit Address Dialog
    private void showEditAddressDialog(String addressType) {
        if (editAddressDialog != null) {
            editAddressDialog.dismissAllowingStateLoss();
            editAddressDialog = null;
        }
        editAddressDialog = EditAddressDialog.newInstance(addressType, listOfAddress, addressPosition);
        editAddressDialog.show(getActivity().getSupportFragmentManager(), TAG);
    }

}
