package com.cheep.cheepcarenew.adapters;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcarenew.model.UserPackageDataModel;
import com.cheep.databinding.RowAddressSubscriptionBinding;
import com.cheep.utils.Utility;

import java.util.ArrayList;

/**
 * Created by majid on 28-06-2018.
 */

public class ManageSubscriptionAddressAdapter extends RecyclerView.Adapter<ManageSubscriptionAddressAdapter.ViewHolder> {

    private ArrayList<UserPackageDataModel> mList;
    AddressItemClickListener addressItemClickListener;

    public interface AddressItemClickListener {
        void onClickItem(UserPackageDataModel model, int position);
    }

    public ManageSubscriptionAddressAdapter(ArrayList<UserPackageDataModel> mList, AddressItemClickListener listener) {
        this.mList = mList;
        this.addressItemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowAddressSubscriptionBinding mRowAddressBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_address_subscription, parent, false);
        return new ViewHolder(mRowAddressBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if (position == (getItemCount() - 1)) {
            holder.mRowAddressBinding.dividers.setVisibility(View.GONE);
        }
        final UserPackageDataModel model = mList.get(position);

        holder.mRowAddressBinding.textFullAddress.setText(model.address);
        holder.mRowAddressBinding.textAddressCategory.setText(Utility.getAddressCategoryString(model.category));
        holder.mRowAddressBinding.textAddressCategory.setCompoundDrawablesWithIntrinsicBounds(Utility.getAddressCategoryBlueIcon(model.category), 0, 0, 0);
        holder.mRowAddressBinding.frontLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Collections.swap(mList, 0, position);
                addressItemClickListener.onClickItem(getItem(position),position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public UserPackageDataModel getItem(int position) {
        return mList.get(position);
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowAddressSubscriptionBinding mRowAddressBinding;

        public ViewHolder(RowAddressSubscriptionBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowAddressBinding = binding;

        }
    }


}
