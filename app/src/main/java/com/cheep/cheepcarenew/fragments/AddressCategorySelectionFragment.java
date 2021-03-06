package com.cheep.cheepcarenew.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcarenew.activities.AddressActivity;
import com.cheep.databinding.FragmentAddressCategorySelectionBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.ComparisionChart.ComparisionChartModel;
import com.cheep.model.GuestUserDetails;
import com.cheep.model.MessageEvent;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class AddressCategorySelectionFragment extends BaseFragment {

    FragmentAddressCategorySelectionBinding mBinding;
    public static final String TAG = "AddressCategorySelectionFragment";
    private AddressModel addressModel;
    private ArrayList<AddressModel> addressModelArrayList;
    private String comingFrom=Utility.EMPTY_STRING;
//    private ViewTooltip.TooltipView tooltipView;

    public static AddressCategorySelectionFragment newInstance(String comingFrom) {
        Bundle args = new Bundle();
        AddressCategorySelectionFragment fragment = new AddressCategorySelectionFragment();
        args.putString(Utility.Extra.COMING_FROM,comingFrom);
        fragment.setArguments(args);
        return fragment;
    }



    public void setAddressModel(AddressModel addressModel) {
        this.addressModel = addressModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.fragment_address_category_selection, null, false);
        return mBinding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void initiateUI() {
        Bundle b=getArguments();
        if(b!=null && b.containsKey(Utility.Extra.COMING_FROM))
        {
            comingFrom=b.getString(Utility.Extra.COMING_FROM);
        }
        setListeners();
        if (addressModel != null) {
            setAddress();
            return;
        }
        Log.e(TAG, "initiateUI: ********************");

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        ArrayList<AddressModel> arrayList = new ArrayList<>();
        if (userDetails != null && userDetails.addressList != null && !userDetails.addressList.isEmpty()) {
            arrayList.addAll(userDetails.addressList);
        } else {
            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
            if (guestUserDetails != null && guestUserDetails.addressList != null && !guestUserDetails.addressList.isEmpty()) {
                arrayList.addAll(guestUserDetails.addressList);
            }
        }
        addressModelArrayList = new ArrayList<>();

        if (!arrayList.isEmpty()) {
            for (AddressModel addressModel : arrayList) {
                if (addressModel.is_subscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.NONE)) {
                    addressModelArrayList.add(addressModel);
                }
            }
        }


        if (!addressModelArrayList.isEmpty()) {
            if(comingFrom.equalsIgnoreCase(getContext().getString(R.string.task_change_address)))
            {
                AddressModel model=PreferenceUtility.getInstance(mContext).getAddressModel();
                if(model!=null)
                {
                    addressModel=model;
                }
                else
                {
                    addressModel = addressModelArrayList.get(0);
                }
            }
            else
            {
                addressModel = addressModelArrayList.get(0);
            }
            setAddress();
            mBinding.tvAddressTitle.setVisibility(View.VISIBLE);
            mBinding.cvAddress.setVisibility(View.VISIBLE);
        } else {
            mBinding.tvAddressTitle.setVisibility(View.GONE);
            mBinding.cvAddress.setVisibility(View.GONE);
            mBinding.rlToolTip.setVisibility(View.GONE);
        }

    }

    private void setAddress() {
        mBinding.tvAddress.setText(addressModel.getAddressWithInitials());
        mBinding.tvAddressTitle.setVisibility(View.VISIBLE);
        mBinding.cvAddress.setVisibility(View.VISIBLE);
        if (addressModel.category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.HOME)) {
            mBinding.cvHome.setSelected(true);
            mBinding.cvOffice.setSelected(false);
        } else {
            mBinding.cvHome.setSelected(false);
            mBinding.cvOffice.setSelected(true);
        }
        openTooltip1(true);
//        openTooltip(true);
    }

    @Override
    public void setListener() {

    }

    protected void setListeners() {
        mBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideToolTip();
                ((AddressActivity) mContext).onBackPressed();
            }
        });
        mBinding.cvOffice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.cvOffice.setSelected(true);
                mBinding.cvHome.setSelected(false);
                openAddNewAddressDialog(NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE);
                hideToolTip();

            }
        });
        mBinding.cvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.cvOffice.setSelected(false);
                mBinding.cvHome.setSelected(true);
                openAddNewAddressDialog(NetworkUtility.TAGS.ADDRESS_TYPE.HOME);
                hideToolTip();
            }
        });

        mBinding.cvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        hideToolTip();
    }

    private void openAddNewAddressDialog(String category) {
        hideToolTip();
        addressModelArrayList = new ArrayList<>();
        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        if (userDetails != null && userDetails.addressList != null && !userDetails.addressList.isEmpty()) {
            for (AddressModel addressModel : userDetails.addressList) {
                if (addressModel.is_subscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.NONE))
                    addressModelArrayList.add(addressModel);
            }
            //            if (userDetails.addressList.get(0) != null) {
//                addressModelArrayList = userDetails.addressList;
//            }

        } else {
            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
            if (guestUserDetails != null && guestUserDetails.addressList != null && !guestUserDetails.addressList.isEmpty()) {
//                if (guestUserDetails.addressList.get(0) != null) {
//                    addressModelArrayList = guestUserDetails.addressList;
//                }
                for (AddressModel addressModel : guestUserDetails.addressList) {
                    if (addressModel.is_subscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.NONE))
                        addressModelArrayList.add(addressModel);
                }
            }
        }

        if (addressModelArrayList != null && !addressModelArrayList.isEmpty()) {
            ArrayList<AddressModel> arrayList = new ArrayList<>();
            for (AddressModel addressModel : addressModelArrayList) {
                if (addressModel.category.equalsIgnoreCase(category))
                    arrayList.add(addressModel);
            }
            if (arrayList.size() > 0)
                ((AddressActivity) mContext).loadFragment(AddressListFragment.TAG, AddressListFragment.newInstance(category, GsonUtility.getJsonStringFromObject(arrayList)));
            else
                ((AddressActivity) mContext).loadFragment(AddNewAddressFragment.TAG, AddNewAddressFragment.newInstance(category));
        } else
            ((AddressActivity) mContext).loadFragment(AddNewAddressFragment.TAG, AddNewAddressFragment.newInstance(category));
    }


    private void openTooltip1(boolean delay) {
        Log.e(TAG, "openTooltip: ********************");

        if (addressModel.category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.HOME))
            mBinding.tvTitle.setText(getString(R.string.label_hey_is_this_your_home_address, getString(R.string.label_home)));
        else
            mBinding.tvTitle.setText(getString(R.string.label_hey_is_this_your_home_address, getString(R.string.label_office)));

        mBinding.tvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideToolTip();
                // TODO: UN-COMMENT THIS CODE AND VERIFY ADDRESSCITY
//                ((AddressActivity) mContext).verifyAddressForCity(addressModel);
                ((AddressActivity) mContext).verifyAddressForCity(addressModel);
            }
        });

        mBinding.tvNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideToolTip();
//                openAddNewAddressDialog(addressModel.category);
                ((AddressActivity) mContext).loadFragment(AddNewAddressFragment.TAG, AddNewAddressFragment.newInstance(addressModel.category));

            }
        });

        mBinding.rlToolTip.setVisibility(View.VISIBLE);
    }

    private void hideToolTip() {
        mBinding.rlToolTip.setVisibility(View.GONE);
    }

    public void onEventMainThread(MessageEvent event) {
        Log.e("onEvntMainThread  *******************", "" + event.BROADCAST_ACTION);
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.ADDRESS_SELECTED_POP_UP:
                setAddressModel(event.addressModel);
                initiateUI();
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hideToolTip();
    }
}
