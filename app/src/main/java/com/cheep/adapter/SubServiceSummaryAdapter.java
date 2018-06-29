package com.cheep.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowSelectedSubServiceBinding;
import com.cheep.databinding.RowSelectedSubServicePriceBinding;
import com.cheep.model.SubServiceDetailModel;

import java.util.ArrayList;

public class SubServiceSummaryAdapter extends RecyclerView.Adapter<SubServiceSummaryAdapter.ViewHolder> {

    ArrayList<SubServiceDetailModel> mList1;
    Context mContext;


    public SubServiceSummaryAdapter(ArrayList<SubServiceDetailModel> mList) {
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
    public SubServiceSummaryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        RowSelectedSubServiceBinding rowPastTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_selected_sub_service, parent, false);
        return new SubServiceSummaryAdapter.ViewHolder(rowPastTaskBinding);
    }



    @Override
    public void onBindViewHolder(final SubServiceSummaryAdapter.ViewHolder holder, int position) {


        SubServiceDetailModel model = mList1.get(holder.getAdapterPosition());
holder.rowPastTaskBinding.tvSubService.setText(model.name);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public RowSelectedSubServiceBinding rowPastTaskBinding;

        public ViewHolder(RowSelectedSubServiceBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            rowPastTaskBinding = binding;

        }
    }

}