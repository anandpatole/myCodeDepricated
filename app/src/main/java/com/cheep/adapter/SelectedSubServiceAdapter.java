package com.cheep.adapter;

import android.content.Context;


import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;


import com.cheep.databinding.RowSelectedSubServicePriceBinding;
import com.cheep.model.SubServiceDetailModel;

import java.util.ArrayList;

public class SelectedSubServiceAdapter extends RecyclerView.Adapter<SelectedSubServiceAdapter.ViewHolder> {

    ArrayList<SubServiceDetailModel> mList1;
    Context mContext;


    public SelectedSubServiceAdapter(ArrayList<SubServiceDetailModel> mList) {
        mList1 = mList;

    }

    @Override
    public int getItemCount() {

        return mList1.size();
    }

    public ArrayList<SubServiceDetailModel> getmList() {
        return mList1;
    }

    @Override
    public SelectedSubServiceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        RowSelectedSubServicePriceBinding rowPastTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_selected_sub_service_price, parent, false);
        return new SelectedSubServiceAdapter.ViewHolder(rowPastTaskBinding);
    }



    @Override
    public void onBindViewHolder(final SelectedSubServiceAdapter.ViewHolder holder, int position) {

        SubServiceDetailModel model = mList1.get(holder.getAdapterPosition());
holder.rowPastTaskBinding.tvSubService.setText(model.name);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public RowSelectedSubServicePriceBinding rowPastTaskBinding;

        public ViewHolder(RowSelectedSubServicePriceBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            rowPastTaskBinding = binding;

        }
    }

}