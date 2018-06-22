package com.cheep.cheepcarenew.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.cheep.R;
import com.cheep.databinding.RowAddressItemBinding;
import com.cheep.model.AddressModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;

/**
 * Created by pankaj on 9/29/16.
 */

public class AddressListRecyclerViewAdapter extends RecyclerView.Adapter<AddressListRecyclerViewAdapter.ViewHolder> {

    private ArrayList<AddressModel> mList;
    private String selected = "";
    private RadioButton selectedRadioBtn;
    AddressItemClickListener addressItemClickListener;

    public interface AddressItemClickListener {
        void onClickItem(AddressModel addressModel);
    }

    public AddressListRecyclerViewAdapter(ArrayList<AddressModel> mList, AddressItemClickListener addressItemInteractionListener) {
        if (mList != null)
            this.mList = mList;
        else
            this.mList = new ArrayList<>();
        this.addressItemClickListener = addressItemInteractionListener;
    }

    @Override
    public AddressListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowAddressItemBinding mRowAddressBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_address_item, parent, false);
        return new AddressListRecyclerViewAdapter.ViewHolder(mRowAddressBinding);
    }

    @Override
    public void onBindViewHolder(final AddressListRecyclerViewAdapter.ViewHolder holder, int position) {

        final AddressModel model = mList.get(holder.getAdapterPosition());

        holder.mRowAddressBinding.tvAddressNickname.setText(holder.mView.getContext().getString(Utility.getAddressCategoryString(model.category))
                + (position > 0 ? (Utility.ONE_CHARACTER_SPACE + position) : ""));
        holder.mRowAddressBinding.ivHome.setImageResource(Utility.getAddressCategoryIcon(model.category));
        holder.mRowAddressBinding.tvAddress.setText(model.getAddressWithInitials());

        if (model.is_subscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.NORMAL)) {
            holder.mRowAddressBinding.tvLabelAddressSubscribed.setVisibility(View.VISIBLE);
            holder.mRowAddressBinding.tvLabelAddressSubscribed.setText(R.string.label_subscribed_under_cheep_care);
        } else if (model.is_subscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.PREMIUM)) {
            holder.mRowAddressBinding.tvLabelAddressSubscribed.setVisibility(View.VISIBLE);
            holder.mRowAddressBinding.tvLabelAddressSubscribed.setText(R.string.label_subscribed_under_cheep_care_premium);
        } else {
            holder.mRowAddressBinding.tvLabelAddressSubscribed.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public ArrayList<AddressModel> getmList() {
        return mList;
    }

    public void add(AddressModel addressModel) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        mList.add(addressModel);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowAddressItemBinding mRowAddressBinding;

        public ViewHolder(RowAddressItemBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowAddressBinding = binding;
            mRowAddressBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addressItemClickListener.onClickItem(getmList().get(getAdapterPosition()));
                }
            });
        }
    }


}