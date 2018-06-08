package com.cheep.cheepcarenew.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.cheep.R;
import com.cheep.databinding.RowAddressItemBinding;
import com.cheep.model.AddressModel;

import java.util.ArrayList;

/**
 * Created by pankaj on 9/29/16.
 */

public class AddressListRecyclerViewAdapter extends RecyclerView.Adapter<AddressListRecyclerViewAdapter.ViewHolder> {

    private ArrayList<AddressModel> mList;
    private String selected = "";
    private RadioButton selectedRadioBtn;

    public AddressListRecyclerViewAdapter(ArrayList<AddressModel> mList) {
        if (mList != null)
            this.mList = mList;
        else
            this.mList = new ArrayList<>();
    }

    @Override
    public AddressListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowAddressItemBinding mRowAddressBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_address_item, parent, false);
        return new AddressListRecyclerViewAdapter.ViewHolder(mRowAddressBinding);
    }

    @Override
    public void onBindViewHolder(final AddressListRecyclerViewAdapter.ViewHolder holder, int position) {

        final AddressModel model = mList.get(holder.getAdapterPosition());

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void delete(String address_id) {
        if (mList.size() > 0 && !TextUtils.isEmpty(address_id)) {
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).address_id.equalsIgnoreCase(address_id)) {
                    mList.remove(i);
                    notifyItemRemoved(i);
                    break;
                }
            }
        }
    }

    public void delete(AddressModel addressModel) {
        if (mList.size() > 0 && mList.contains(addressModel)) {
            mList.remove(addressModel);
            notifyDataSetChanged();
        }
    }

    public void updateItem(AddressModel addressModel) {
        if (mList.size() > 0 && addressModel != null && !TextUtils.isEmpty(addressModel.address_id)) {
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).address_id.equalsIgnoreCase(addressModel.address_id)) {
                    mList.set(i, addressModel);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
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

    /**
     * Used to select current row based on address id, we are checking the position where address is stored.
     *
     * @param selectedAddressId
     */
    public void setSelectedAddressId(String selectedAddressId) {
        if (!TextUtils.isEmpty(selectedAddressId)) {
            selected = selectedAddressId;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowAddressItemBinding mRowAddressBinding;

        public ViewHolder(RowAddressItemBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowAddressBinding = binding;
        }
    }


}