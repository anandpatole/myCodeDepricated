package com.cheep.cheepcarenew.fragments;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cheep.R;
import com.cheep.cheepcarenew.activities.AddressActivity;
import com.cheep.custom_view.tooltips.ToolTip;
import com.cheep.custom_view.tooltips.ToolTipView;
import com.cheep.databinding.ActivityAddressCategorySelectionBinding;
import com.cheep.databinding.TooltipAddressSelectionBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.GuestUserDetails;
import com.cheep.model.UserDetails;
import com.cheep.utils.PreferenceUtility;

public class AddressCategorySelectionFragment extends BaseFragment {

    ActivityAddressCategorySelectionBinding mBinding;
    private ToolTipView toolTipView;
    public static final String TAG = "AddressCategorySelectionFragment";
    private AddressModel addressModel;

    public void setAddressModel(AddressModel addressModel) {
        this.addressModel = addressModel;
    }

    public static AddressCategorySelectionFragment newInstance() {
        Bundle args = new Bundle();
        AddressCategorySelectionFragment fragment = new AddressCategorySelectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.activity_address_category_selection, null, false);
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


        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        if (userDetails != null && !userDetails.addressList.isEmpty()) {
            if (userDetails.addressList.get(0) != null) {
                addressModel = userDetails.addressList.get(0);
                setAddress();
                mBinding.tvAddressTitle.setVisibility(View.VISIBLE);
                mBinding.cvAddress.setVisibility(View.VISIBLE);
            } else {
                mBinding.tvAddressTitle.setVisibility(View.GONE);
                mBinding.cvAddress.setVisibility(View.GONE);
            }

        } else {
            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
            if (guestUserDetails != null && !guestUserDetails.addressList.isEmpty()) {
                if (guestUserDetails.addressList.get(0) != null) {
                    setAddress();
                    addressModel = guestUserDetails.addressList.get(0);
                    mBinding.tvAddressTitle.setVisibility(View.VISIBLE);
                    mBinding.cvAddress.setVisibility(View.VISIBLE);
                } else {
                    mBinding.tvAddressTitle.setVisibility(View.GONE);
                    mBinding.cvAddress.setVisibility(View.GONE);
                }
            } else {
                mBinding.tvAddressTitle.setVisibility(View.GONE);
                mBinding.cvAddress.setVisibility(View.GONE);
            }
        }


    }

    private void setAddress() {
        mBinding.tvAddress.setText(addressModel.getAddressWithInitials());
        openTooltip(true);
    }

    @Override
    public void setListener() {
    }

    protected void setListeners() {
        mBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toolTipView != null)
                    toolTipView.remove();
                ((AddressActivity) mContext).onBackPressed();

            }
        });
        mBinding.cvOffice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.cvOffice.setSelected(true);
                mBinding.cvHome.setSelected(false);
                openAddNewAddressDialog();

            }
        });
        mBinding.cvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.cvOffice.setSelected(false);
                mBinding.cvHome.setSelected(true);
                openAddNewAddressDialog();
            }
        });

        mBinding.cvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTooltip(false);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (toolTipView != null)
            toolTipView.remove();

    }

    private void openAddNewAddressDialog() {
        ((AddressActivity) mContext).loadFragment(AddNewAddressFragment.TAG, AddNewAddressFragment.newInstance());

    }


    private void openTooltip(boolean b) {

        Log.e(TAG, "onGlobalLayout: *******************");
        TooltipAddressSelectionBinding toolTipBinding = DataBindingUtil.inflate(
                LayoutInflater.from(mContext),
                R.layout.tooltip_address_selection,
                null,
                false);

        ToolTip toolTip = new ToolTip.Builder()
                .withTextColor(Color.WHITE)
                .withBackgroundColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end))
                .withCornerRadius(getResources().getDimension(R.dimen.scale_3dp))
                .build();

        toolTipView = new ToolTipView.Builder(mContext)
                .withAnchor(mBinding.cvAddress)
                .withContentView(toolTipBinding.getRoot())
                .withToolTip(toolTip)
                .withGravity(Gravity.BOTTOM)
                .build();

        toolTipBinding.tvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolTipView.remove();
                Toast.makeText(mContext, "YES", Toast.LENGTH_SHORT).show();
            }
        });

        toolTipBinding.tvNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolTipView.remove();
                Toast.makeText(mContext, "NO", Toast.LENGTH_SHORT).show();
            }
        });
        if (b)
            toolTipView.showDelayed(500);
        else toolTipView.show();
    }


}
