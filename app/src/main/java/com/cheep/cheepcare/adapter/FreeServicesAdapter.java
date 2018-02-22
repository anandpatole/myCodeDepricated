package com.cheep.cheepcare.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowPackageSubServiceUnitBinding;
import com.cheep.databinding.RowSubCategoryUnitTickPriceBinding;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kruti on 22/2/18.
 */

public class FreeServicesAdapter extends RecyclerView.Adapter<FreeServicesAdapter.ViewHolder> {

    private final List<SubServiceDetailModel> mList = new ArrayList<>();
    private final ItemInteractionListener mListener;

    public FreeServicesAdapter(ItemInteractionListener listener) {
        mListener = listener;
    }

    public interface ItemInteractionListener {
//        void onItemClick(SubServiceDetailModel subServiceDetailModel, int position);

        void onLimitExceeded(SubServiceDetailModel subServiceDetailModel, int position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowSubCategoryUnitTickPriceBinding binding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext())
                        , R.layout.row_sub_category_unit_tick_price
                        , parent
                        , false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final SubServiceDetailModel subServicesModel = mList.get(holder.getAdapterPosition());

        holder.mBinding.tvSubServiceName.setText(subServicesModel.name);
        Context context = holder.mBinding.getRoot().getContext();

        holder.mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mListener.onItemClick(subServicesModel, holder.getAdapterPosition());
                holder.mBinding.imgIconCorrect.setSelected(true);
                holder.mBinding.clCenter.setVisibility(View.VISIBLE);
            }
        });

        holder.mBinding.tvSubServicePrice.setSelected(subServicesModel.isSelected);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RowSubCategoryUnitTickPriceBinding mBinding;

        public ViewHolder(RowSubCategoryUnitTickPriceBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }

    public void addAll(List<SubServiceDetailModel> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public List<SubServiceDetailModel> getSelectedList() {
        ArrayList<SubServiceDetailModel> list = new ArrayList<>();
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).isSelected) {
                list.add(mList.get(i));
            }
        }
        return list;
    }
}
