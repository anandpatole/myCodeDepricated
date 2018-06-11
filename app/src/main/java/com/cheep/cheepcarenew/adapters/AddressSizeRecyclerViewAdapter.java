package com.cheep.cheepcarenew.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowAddressSizeItemBinding;
import com.cheep.model.AddressSizeModel;

import java.util.ArrayList;

/**
 * Created by pankaj on 9/29/16.
 */

public class AddressSizeRecyclerViewAdapter extends RecyclerView.Adapter<AddressSizeRecyclerViewAdapter.ViewHolder> {

    private ArrayList<AddressSizeModel> mList;
    AddressSizeClickListener listener;

    public interface AddressSizeClickListener {
        void onClickAddressSize(AddressSizeModel model);
    }

    public AddressSizeRecyclerViewAdapter(ArrayList<AddressSizeModel> mList, AddressSizeClickListener listener) {
        this.listener = listener;
        if (mList != null)
            this.mList = mList;
        else
            this.mList = new ArrayList<>();
    }

    @Override
    public AddressSizeRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowAddressSizeItemBinding mRowAddressBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_address_size_item, parent, false);
        return new AddressSizeRecyclerViewAdapter.ViewHolder(mRowAddressBinding);
    }

    @Override
    public void onBindViewHolder(final AddressSizeRecyclerViewAdapter.ViewHolder holder, int position) {

        final AddressSizeModel model = mList.get(holder.getAdapterPosition());
        holder.mRowAddressBinding.tvOption.setText(model.value);
        holder.mRowAddressBinding.cvOption.setSelected(model.isSelected);

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public ArrayList<AddressSizeModel> getmList() {
        return mList;
    }

    public void add(AddressSizeModel AddressSizeModel) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        mList.add(AddressSizeModel);
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        final RowAddressSizeItemBinding mRowAddressBinding;

        public ViewHolder(RowAddressSizeItemBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowAddressBinding = binding;
            mRowAddressBinding.cvOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddressSizeModel model = mList.get(getAdapterPosition());
                    listener.onClickAddressSize(mList.get(getAdapterPosition()));
                    for (AddressSizeModel addressSizeModel : mList) {
                        addressSizeModel.isSelected = addressSizeModel.id.equalsIgnoreCase(model.id);
                    }
                    notifyDataSetChanged();
                }
            });
        }
    }


}