package com.cheep.cheepcare.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowSelectedSubServicePriceBinding;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.utils.Utility;

import java.util.List;

public class SelectedSubServicePriceAdapter extends RecyclerView.Adapter<SelectedSubServicePriceAdapter.SelectedSubServiceViewHolder> {

    private final List<SubServiceDetailModel> mList;
    private String mServiceType;

    public SelectedSubServicePriceAdapter(List<SubServiceDetailModel> list, String serviceType) {
        mList = list;
        mServiceType = serviceType;
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
        SubServiceDetailModel model = mList.get(holder.getAdapterPosition());
        Context context = holder.mBinding.getRoot().getContext();

        if (holder.getAdapterPosition() == (mList.size() - 1)) {
            holder.mBinding.txtVerticalLine.setVisibility(View.GONE);
        }

        holder.mBinding.tvSubService.setText(model.name);
        if (/*Double.parseDouble(model.unitPrice) == 0 ||*/ mServiceType.equalsIgnoreCase(Utility.SERVICE_TYPE.FREE)) {
            holder.mBinding.tvSubServicePrice.setText(context.getString(R.string.label_free));
        } else {
            holder.mBinding.tvSubServicePrice.setText(context.getString(R.string.rupee_symbol_x,
                    String.valueOf(model.selected_unit * Double.parseDouble(model.unitPriceWithGST))));
        }
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
