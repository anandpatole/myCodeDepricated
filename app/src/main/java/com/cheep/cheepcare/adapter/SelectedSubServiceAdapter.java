package com.cheep.cheepcare.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowSelectedSubServiceBinding;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.utils.Utility;

import java.util.List;

public class SelectedSubServiceAdapter extends RecyclerView.Adapter<SelectedSubServiceAdapter.SelectedSubServiceViewHolder> {

    private final List<SubServiceDetailModel> mList;

    public SelectedSubServiceAdapter(List<SubServiceDetailModel> list) {
        mList = list;
    }

    @Override
    public SelectedSubServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RowSelectedSubServiceBinding binding =
                DataBindingUtil.inflate(
                        layoutInflater
                        , R.layout.row_selected_sub_service
                        , parent
                        , false
                );
        SelectedSubServiceViewHolder holder = new SelectedSubServiceViewHolder(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(SelectedSubServiceViewHolder holder, int position) {
        SubServiceDetailModel model = mList.get(holder.getAdapterPosition());
        Context context = holder.mBinding.getRoot().getContext();

        if (holder.getAdapterPosition() == (mList.size() - 1)) {
            holder.mBinding.txtVerticalLine.setVisibility(View.GONE);
        }
        String subServiceText = model.name + Utility.ONE_CHARACTER_SPACE +
                context.getString(R.string.number_of_services, String.valueOf(model.qty));
        holder.mBinding.tvSubService.setText(subServiceText);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class SelectedSubServiceViewHolder extends RecyclerView.ViewHolder {

        public RowSelectedSubServiceBinding mBinding;

        public SelectedSubServiceViewHolder(RowSelectedSubServiceBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }
}
