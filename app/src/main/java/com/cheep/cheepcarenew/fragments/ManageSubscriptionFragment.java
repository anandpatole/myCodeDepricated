package com.cheep.cheepcarenew.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.adapter.ManageSubscriptionAddressAdpater;
import com.cheep.cheepcare.fragment.ProfileTabFragment;
import com.cheep.custom_view.tooltips.ViewTooltip;
import com.cheep.databinding.FragmentManageSubscriptionBinding;
import com.cheep.databinding.TooltipAddressSelectionBinding;
import com.cheep.databinding.TooltipManageSubscriptionBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.GsonUtility;
import com.freshchat.consumer.sdk.beans.User;

import java.util.ArrayList;

public class ManageSubscriptionFragment extends BaseFragment implements ManageSubscriptionAddressAdpater.AddressItemClickListener
{
    private ArrayList<AddressModel> addressList;
    ManageSubscriptionAddressAdpater adapter;
    public static final String TAG = "ManageSubscriptionFragment";
    FragmentManageSubscriptionBinding mBinding;
    private ViewTooltip.TooltipView tooltipView;

    public static ManageSubscriptionFragment newInstance(ArrayList<AddressModel> list) {
        Bundle args = new Bundle();
        args.putSerializable("list", list);
        ManageSubscriptionFragment fragment = new ManageSubscriptionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_manage_subscription, container, false);
        return mBinding.getRoot();
    }
    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);

    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }
    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach: ");



        // Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.CHANGE_PASSWORD);*/

        super.onDetach();
    }
    @Override
    public void initiateUI() {

        openTooltip(true);
        mBinding.textTitle.setText("Manage Subscription");
        mBinding.textSubscriptionDuration.setText("3 months");
        mBinding.textAmountPaid.setText("233");
        mBinding.textPaymentMethod.setText("HDFC Credit Card");
        mBinding.textSubscribedOn.setText("23rd May 2018");
        mBinding.textSubscriptionEndDate.setText("23rd May 2018");
        if(mBinding.autoRenewalToggle.isChecked())
        {

        }
        else
        {
            //mBinding.autoRenewalRl.setVisibility(View.GONE);
        }
        Bundle bundle = getArguments();
        addressList= (ArrayList<AddressModel>) bundle.getSerializable("list");
        fillRecyclerViewSingleItem();

    }

    @Override
    public void setListener() {
        mBinding.back.setOnClickListener(onClickListener);
        mBinding.notification.setOnClickListener(onClickListener);
        mBinding.addressDropDowns.setOnClickListener(onClickListener);
        mBinding.addressDropUp.setOnClickListener(onClickListener);
        mBinding.upgradeSubscriptionBtn.setOnClickListener(onClickListener);
        mBinding.autoRenewalToggle.setOnClickListener(onClickListener);

    }
    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId())
            {
                case R.id.auto_renewal_toggle:
                    break;
                case R.id.address_drop_downs:
                    fillRecyclerView();
                    break;
                case R.id.address_drop_up:
                    fillRecyclerViewSingleItem();
                    break;
                case R.id.back:
                    if(tooltipView!=null)
                    {
                      hideToolTip(true);
                    }
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, ProfileTabFragment.newInstance(), ProfileTabFragment.TAG).commitAllowingStateLoss();
                    break;
                case R.id.upgrade_subscription_btn:
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, UpgradeSubscriptionFragment.newInstance(addressList.get(0)), UpgradeSubscriptionFragment.TAG).commitAllowingStateLoss();
                    break;
            }
        }
    };

    public void fillRecyclerView()
    {
        mBinding.addressDropDowns.setVisibility(View.GONE);
        mBinding.addressDropUp.setVisibility(View.VISIBLE);
        adapter=new ManageSubscriptionAddressAdpater(addressList,1,ManageSubscriptionFragment.this);
        mBinding.subscriptionRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.subscriptionRecyclerView.setNestedScrollingEnabled(false);
        mBinding.subscriptionRecyclerView.setAdapter(adapter);
    }

    public void fillRecyclerViewSingleItem()
    {
        mBinding.addressDropDowns.setVisibility(View.VISIBLE);
        mBinding.addressDropUp.setVisibility(View.GONE);
        adapter=new ManageSubscriptionAddressAdpater(addressList,0,ManageSubscriptionFragment.this);
        mBinding.subscriptionRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.subscriptionRecyclerView.setNestedScrollingEnabled(false);
        mBinding.subscriptionRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickItem(ArrayList<AddressModel> mList)

    {
        this.addressList=mList;
        fillRecyclerViewSingleItem();

    }
    private void openTooltip(boolean delay) {
        Log.e(TAG, "openTooltip: **********************");
        TooltipManageSubscriptionBinding toolTipBinding = DataBindingUtil.inflate(
                LayoutInflater.from(mContext),
                R.layout.tooltip_manage_subscription,
                null,
                false);
        // set tooltip text


        toolTipBinding.tooltipClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideToolTip(false);
            }
        });

        ViewTooltip viewTooltip = ViewTooltip.on( this ,mBinding.upgradeSubscriptionBtn).customView(toolTipBinding.getRoot(), delay)
                .position(ViewTooltip.Position.TOP)
                .clickToHide(true)
                .animation(new ViewTooltip.FadeTooltipAnimation(500))
                .padding(500,0,500,500)
                .align(ViewTooltip.ALIGN.START)
                .autoHide(false, 0);

        if(tooltipView!=null)
            hideToolTip(true);
        tooltipView = viewTooltip.getTooltip_view();

        viewTooltip.show();


    }
    private void hideToolTip(boolean removeNow) {
        if (tooltipView != null) {
            if (removeNow)
                tooltipView.removeNow();
            else
                tooltipView.remove();
        }
    }
}
