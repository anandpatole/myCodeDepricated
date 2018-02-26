package com.cheep.cheepcare.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowSelectedSubServicePriceBinding;
import com.cheep.model.SubServiceDetailModel;

import java.util.List;

public class SelectedSubServicePriceAdapter extends RecyclerView.Adapter<SelectedSubServicePriceAdapter.SelectedSubServiceViewHolder> {

    private final List<SubServiceDetailModel> mList;

    public SelectedSubServicePriceAdapter(List<SubServiceDetailModel> list) {
        mList = list;
    }

    @Override
    public SelectedSubServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RowSelectedSubServicePriceBinding binding =
                DataBindingUtil.inflate(
                        layoutInflater
                        , R.layout.row_selected_sub_service_price
                        , parent
                        , false
                );
        return new SelectedSubServiceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(SelectedSubServiceViewHolder holder, int position) {
        if (holder.getAdapterPosition() == (mList.size() - 1)) {
            holder.mBinding.txtVerticalLine.setVisibility(View.GONE);
        }
        holder.mBinding.tvSubService.setText(mList.get(holder.getAdapterPosition()).name);
        holder.mBinding.tvSubServicePrice.setText(mList.get(holder.getAdapterPosition()).unitPrice);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class SelectedSubServiceViewHolder extends RecyclerView.ViewHolder {

        public RowSelectedSubServicePriceBinding mBinding;

        public SelectedSubServiceViewHolder(RowSelectedSubServicePriceBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }
}
