package com.cheep.cheepcarenew.dialogs;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.DialogAddNewAddressBinding;

/**
 * Created by meet on 20/9/17.
 */

public class AddNewAddressDialog extends DialogFragment {
    public static final String TAG = "AddNewAddressDialog";
    private DialogAddNewAddressBinding mBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set Window Background as Transparent.
        if (getDialog() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().setCanceledOnTouchOutside(false);
        }
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_add_new_address, container, false);
        init();
        return mBinding.getRoot();
    }

    private void init() {
        setListener();
        mBinding.editAddressCity.setHint(getSpannableString(getString(R.string.hint_address_city)));
        mBinding.editAddressPincode.setHint(getSpannableString(getString(R.string.hint_pincode)));
        mBinding.editAddressInitials.setHint(getSpannableString(getString(R.string.hint_address_initials)));
        mBinding.editAddressLocality.setHint(getSpannableString(getString(R.string.hint_address_locality)));
    }

    private void setListener() {

        mBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

    public SpannableStringBuilder getSpannableString(String fullString) {
        String newString = fullString + getString(R.string.label_star);
        SpannableStringBuilder text = new SpannableStringBuilder(newString);
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red)), fullString.length(), newString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }
}