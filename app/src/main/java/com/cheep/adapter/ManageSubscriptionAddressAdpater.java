package com.cheep.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;

import com.cheep.databinding.RowAddressSubscriptionBinding;
import com.cheep.model.AddressModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.Collections;

public class ManageSubscriptionAddressAdpater extends RecyclerView.Adapter<ManageSubscriptionAddressAdpater.ViewHolder>
{
    private ArrayList<AddressModel> mList;
    private String selected = "";
    int flag;
    AddressItemClickListener addressItemClickListener;
    public interface AddressItemClickListener {
        void onClickItem(ArrayList<AddressModel> mList);
    }

    public ManageSubscriptionAddressAdpater( ArrayList<AddressModel> mList,int flag,AddressItemClickListener listener)
    {
        if (mList != null)
            this.mList = mList;
        else
            this.mList = new ArrayList<>();

        this.flag=flag;
        this.addressItemClickListener = listener;
    }

    @Override
    public ManageSubscriptionAddressAdpater.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowAddressSubscriptionBinding   mRowAddressBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_address_subscription, parent, false);
        return new ManageSubscriptionAddressAdpater.ViewHolder(mRowAddressBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if(position==(getItemCount()-1)){
            holder.mRowAddressBinding.dividers.setVisibility(View.GONE);
        }
        final AddressModel model = mList.get(holder.getAdapterPosition());

        holder.mRowAddressBinding.textFullAddress.setText(model.getAddressWithInitials());
        holder.mRowAddressBinding.textAddressCategory.setText(Utility.getAddressCategoryString(model.category));
        holder.mRowAddressBinding.textAddressCategory.setCompoundDrawablesWithIntrinsicBounds(Utility.getAddressCategoryBlueIcon(model.category), 0, 0, 0);
holder.mRowAddressBinding.frontLayout.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v)
    {

        Collections.swap(mList,0,position);
        addressItemClickListener.onClickItem(mList);
    }
});
    }



    @Override
    public int getItemCount() {
        if(flag==0)
        {
            if(mList.size()>0) {
                return 1;
            }
            else
            {
                return mList.size();
            }
        }
        else {
            return mList.size();
        }
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
