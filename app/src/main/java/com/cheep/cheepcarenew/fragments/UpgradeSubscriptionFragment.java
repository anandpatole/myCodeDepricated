package com.cheep.cheepcarenew.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cheep.R;
import com.cheep.databinding.FragmentUpgradeSubscriptionBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.utils.Utility;

public class UpgradeSubscriptionFragment extends BaseFragment
{
    public static final String TAG = "UpgradeSubscriptionFragment";
    AddressModel addressModel;
    FragmentUpgradeSubscriptionBinding mBinding;
    public static UpgradeSubscriptionFragment newInstance(AddressModel address) {
        Bundle args = new Bundle();
        args.putSerializable("address", address);
        UpgradeSubscriptionFragment fragment = new UpgradeSubscriptionFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_upgrade_subscription, container, false);
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


        super.onDetach();
    }
    @Override
    public void initiateUI() {

        Bundle bundle = getArguments();
        addressModel= (AddressModel) bundle.getSerializable("address");
        mBinding.addressType.setText(Utility.getAddressCategoryString(addressModel.category));
        mBinding.addressType.setCompoundDrawablesWithIntrinsicBounds(Utility.getAddressCategoryBlueIcon(addressModel.category), 0, 0, 0);
        mBinding.address.setText(addressModel.getAddressWithInitials());
        testdata(mBinding.cheepCarePackageCardview1,mBinding.cheepCarePackageMonth1,mBinding.cheepCarePackageNewprice1,mBinding.cheepCarePackageOldprice1,mBinding.cheepCarePackageSave1);

    }

    @Override
    public void setListener()
    {
        mBinding.cardviewLl1.setOnClickListener(onClickListener);
        mBinding.cardviewLl2.setOnClickListener(onClickListener);
        mBinding.cardviewLl3.setOnClickListener(onClickListener);
        mBinding.premiumCardviewLl1.setOnClickListener(onClickListener);
        mBinding.premiumCardviewLl2.setOnClickListener(onClickListener);
        mBinding.premiumCardviewLl3.setOnClickListener(onClickListener);
        mBinding.back.setOnClickListener(onClickListener);
    }
    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId())
            {
                case R.id.cardview_ll1:

                    cheepCareCardviewSelectedState(mBinding.cheepCarePackageCardview1,mBinding.cheepCarePackageMonth1,mBinding.cheepCarePackageNewprice1,mBinding.cheepCarePackageOldprice1,mBinding.cheepCarePackageSave1);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview2,mBinding.cheepCarePackageMonth2,mBinding.cheepCarePackageNewprice2,mBinding.cheepCarePackageOldprice2,mBinding.cheepCarePackageSave2);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview3,mBinding.cheepCarePackageMonth3,mBinding.cheepCarePackageNewprice3,mBinding.cheepCarePackageOldprice3,mBinding.cheepCarePackageSave3);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview1,mBinding.cheepCarePremiumPackageMonth1,mBinding.cheepCarePremiumPackageNewprice1,mBinding.cheepCarePremiumPackageOldprice1,mBinding.cheepCarePremiumPackageSave1);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview2,mBinding.cheepCarePremiumPackageMonth2,mBinding.cheepCarePremiumPackageNewprice2,mBinding.cheepCarePremiumPackageOldprice2,mBinding.cheepCarePremiumPackageSave2);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview3,mBinding.cheepCarePremiumPackageMonth3,mBinding.cheepCarePremiumPackageNewprice3,mBinding.cheepCarePremiumPackageOldprice3,mBinding.cheepCarePremiumPackageSave3);
                    break;
                case R.id.cardview_ll2:
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview1,mBinding.cheepCarePackageMonth1,mBinding.cheepCarePackageNewprice1,mBinding.cheepCarePackageOldprice1,mBinding.cheepCarePackageSave1);
                    cheepCareCardviewSelectedState(mBinding.cheepCarePackageCardview2,mBinding.cheepCarePackageMonth2,mBinding.cheepCarePackageNewprice2,mBinding.cheepCarePackageOldprice2,mBinding.cheepCarePackageSave2);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview3,mBinding.cheepCarePackageMonth3,mBinding.cheepCarePackageNewprice3,mBinding.cheepCarePackageOldprice3,mBinding.cheepCarePackageSave3);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview1,mBinding.cheepCarePremiumPackageMonth1,mBinding.cheepCarePremiumPackageNewprice1,mBinding.cheepCarePremiumPackageOldprice1,mBinding.cheepCarePremiumPackageSave1);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview2,mBinding.cheepCarePremiumPackageMonth2,mBinding.cheepCarePremiumPackageNewprice2,mBinding.cheepCarePremiumPackageOldprice2,mBinding.cheepCarePremiumPackageSave2);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview3,mBinding.cheepCarePremiumPackageMonth3,mBinding.cheepCarePremiumPackageNewprice3,mBinding.cheepCarePremiumPackageOldprice3,mBinding.cheepCarePremiumPackageSave3);
                    break;
                case R.id.cardview_ll3:
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview1,mBinding.cheepCarePackageMonth1,mBinding.cheepCarePackageNewprice1,mBinding.cheepCarePackageOldprice1,mBinding.cheepCarePackageSave1);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview2,mBinding.cheepCarePackageMonth2,mBinding.cheepCarePackageNewprice2,mBinding.cheepCarePackageOldprice2,mBinding.cheepCarePackageSave2);
                    cheepCareCardviewSelectedState(mBinding.cheepCarePackageCardview3,mBinding.cheepCarePackageMonth3,mBinding.cheepCarePackageNewprice3,mBinding.cheepCarePackageOldprice3,mBinding.cheepCarePackageSave3);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview1,mBinding.cheepCarePremiumPackageMonth1,mBinding.cheepCarePremiumPackageNewprice1,mBinding.cheepCarePremiumPackageOldprice1,mBinding.cheepCarePremiumPackageSave1);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview2,mBinding.cheepCarePremiumPackageMonth2,mBinding.cheepCarePremiumPackageNewprice2,mBinding.cheepCarePremiumPackageOldprice2,mBinding.cheepCarePremiumPackageSave2);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview3,mBinding.cheepCarePremiumPackageMonth3,mBinding.cheepCarePremiumPackageNewprice3,mBinding.cheepCarePremiumPackageOldprice3,mBinding.cheepCarePremiumPackageSave3);
                    break;
                case R.id.premium_cardview_ll1:
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview1,mBinding.cheepCarePackageMonth1,mBinding.cheepCarePackageNewprice1,mBinding.cheepCarePackageOldprice1,mBinding.cheepCarePackageSave1);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview2,mBinding.cheepCarePackageMonth2,mBinding.cheepCarePackageNewprice2,mBinding.cheepCarePackageOldprice2,mBinding.cheepCarePackageSave2);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview3,mBinding.cheepCarePackageMonth3,mBinding.cheepCarePackageNewprice3,mBinding.cheepCarePackageOldprice3,mBinding.cheepCarePackageSave3);
                    cheepCareCardviewSelectedState(mBinding.cheepCarePremiumPackageCardview1,mBinding.cheepCarePremiumPackageMonth1,mBinding.cheepCarePremiumPackageNewprice1,mBinding.cheepCarePremiumPackageOldprice1,mBinding.cheepCarePremiumPackageSave1);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview2,mBinding.cheepCarePremiumPackageMonth2,mBinding.cheepCarePremiumPackageNewprice2,mBinding.cheepCarePremiumPackageOldprice2,mBinding.cheepCarePremiumPackageSave2);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview3,mBinding.cheepCarePremiumPackageMonth3,mBinding.cheepCarePremiumPackageNewprice3,mBinding.cheepCarePremiumPackageOldprice3,mBinding.cheepCarePremiumPackageSave3);
                    break;
                case R.id.premium_cardview_ll2:
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview1,mBinding.cheepCarePackageMonth1,mBinding.cheepCarePackageNewprice1,mBinding.cheepCarePackageOldprice1,mBinding.cheepCarePackageSave1);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview2,mBinding.cheepCarePackageMonth2,mBinding.cheepCarePackageNewprice2,mBinding.cheepCarePackageOldprice2,mBinding.cheepCarePackageSave2);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview3,mBinding.cheepCarePackageMonth3,mBinding.cheepCarePackageNewprice3,mBinding.cheepCarePackageOldprice3,mBinding.cheepCarePackageSave3);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview1,mBinding.cheepCarePremiumPackageMonth1,mBinding.cheepCarePremiumPackageNewprice1,mBinding.cheepCarePremiumPackageOldprice1,mBinding.cheepCarePremiumPackageSave1);
                    cheepCareCardviewSelectedState(mBinding.cheepCarePremiumPackageCardview2,mBinding.cheepCarePremiumPackageMonth2,mBinding.cheepCarePremiumPackageNewprice2,mBinding.cheepCarePremiumPackageOldprice2,mBinding.cheepCarePremiumPackageSave2);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview3,mBinding.cheepCarePremiumPackageMonth3,mBinding.cheepCarePremiumPackageNewprice3,mBinding.cheepCarePremiumPackageOldprice3,mBinding.cheepCarePremiumPackageSave3);
                    break;
                case R.id.premium_cardview_ll3:
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview1,mBinding.cheepCarePackageMonth1,mBinding.cheepCarePackageNewprice1,mBinding.cheepCarePackageOldprice1,mBinding.cheepCarePackageSave1);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview2,mBinding.cheepCarePackageMonth2,mBinding.cheepCarePackageNewprice2,mBinding.cheepCarePackageOldprice2,mBinding.cheepCarePackageSave2);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePackageCardview3,mBinding.cheepCarePackageMonth3,mBinding.cheepCarePackageNewprice3,mBinding.cheepCarePackageOldprice3,mBinding.cheepCarePackageSave3);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview1,mBinding.cheepCarePremiumPackageMonth1,mBinding.cheepCarePremiumPackageNewprice1,mBinding.cheepCarePremiumPackageOldprice1,mBinding.cheepCarePremiumPackageSave1);
                    cheepCareCardviewNotSelectedState(mBinding.cheepCarePremiumPackageCardview2,mBinding.cheepCarePremiumPackageMonth2,mBinding.cheepCarePremiumPackageNewprice2,mBinding.cheepCarePremiumPackageOldprice2,mBinding.cheepCarePremiumPackageSave2);
                    cheepCareCardviewSelectedState(mBinding.cheepCarePremiumPackageCardview3,mBinding.cheepCarePremiumPackageMonth3,mBinding.cheepCarePremiumPackageNewprice3,mBinding.cheepCarePremiumPackageOldprice3,mBinding.cheepCarePremiumPackageSave3);
                    break;
                case R.id.back:
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, ProfileTabFragment.newInstance(), ProfileTabFragment.TAG).commitAllowingStateLoss();
                    break;

            }
        }
    };

    public void cheepCareCardviewSelectedState(CardView c, TextView month, TextView newprice, TextView oldprice,TextView saveprie)

    {
        c.setSelected(true);
        month.setSelected(true);
        newprice.setSelected(true);
        oldprice.setSelected(true);
        saveprie.setSelected(true);
    }

    public void cheepCareCardviewNotSelectedState(CardView c, TextView month, TextView newprice, TextView oldprice,TextView saveprie)

    {
        c.setSelected(false);
        month.setSelected(false);
        newprice.setSelected(false);
        oldprice.setSelected(false);
        saveprie.setSelected(false);
    }

    public void testdata(CardView c, TextView month, TextView newprice, TextView oldprice,TextView saveprie)

    {
        month.setText("6 Months");
        newprice.setText(" ₹3000");
        oldprice.setText(" ₹3400");
        oldprice.setPaintFlags(oldprice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG );


        saveprie.setText(" ₹400");
    }

}
