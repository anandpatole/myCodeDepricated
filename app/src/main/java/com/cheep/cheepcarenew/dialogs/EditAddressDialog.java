package com.cheep.cheepcarenew.dialogs;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.cheep.R;
import com.cheep.databinding.DialogEditAddressBinding;
import com.cheep.model.AddressModel;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.Utility;

import java.util.ArrayList;

public class EditAddressDialog extends DialogFragment {

    public static final String TAG = EditAddressDialog.class.getSimpleName();
    private DialogEditAddressBinding mBinding;
    private String isHomeOrIsOffice;
    private ArrayList<AddressModel> listOfAddress;

    public EditAddressDialog() {
        // Required empty public constructor
    }


    public static EditAddressDialog newInstance(String addressType, ArrayList<AddressModel> addressList) {
        EditAddressDialog fragment = new EditAddressDialog();
        Bundle args = new Bundle();
        args.putString(Utility.Extra.DATA, addressType);
        args.putString(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(addressList));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isHomeOrIsOffice = getArguments().getString(Utility.Extra.DATA);
            listOfAddress = GsonUtility.getObjectListFromJsonString(getArguments().getString(Utility.Extra.DATA_2), AddressModel[].class);
        }
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimationZoomInOut;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setCanceledOnTouchOutside(true);
        this.setCancelable(true);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.AlertAnimation;
        return dialog;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_edit_address, container, false);
        initiateUI();
        return mBinding.getRoot();
    }

    private void initiateUI() {
        // set address
        if (listOfAddress != null) {
            for (int i = 0; i < listOfAddress.size(); i++) {
                mBinding.tvAddress.setText(listOfAddress.get(0).address);

                break;
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }
}
