package com.cheep.cheepcare.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowSubCategoryUnitPaidBinding;
import com.cheep.model.SubServiceDetailModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bhavesh on 22/2/18.
 */

public class PaidServicesAdapter extends RecyclerView.Adapter<PaidServicesAdapter.ViewHolder> {

    private final List<SubServiceDetailModel> mList = new ArrayList<>();
    private final ItemInteractionListener mListener;

    public PaidServicesAdapter(ItemInteractionListener listener) {
        mListener = listener;
    }

    public interface ItemInteractionListener {
//        void onItemClick(SubServiceDetailModel subServiceDetailModel, int position);

        void onLimitExceeded(SubServiceDetailModel subServiceDetailModel, int position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowSubCategoryUnitPaidBinding binding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext())
                        , R.layout.row_sub_category_unit_paid
                        , parent
                        , false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final SubServiceDetailModel subServicesModel = mList.get(holder.getAdapterPosition());

        holder.mBinding.tvSubServiceName.setText(subServicesModel.name);
        final Context context = holder.mBinding.getRoot().getContext();

        holder.mBinding.tvDigit.setText(String.valueOf(subServicesModel.qty));

        if (subServicesModel.isSelected) {
            holder.mBinding.imgIconCorrect.setSelected(true);
            holder.mBinding.clCenter.setVisibility(View.VISIBLE);
            holder.mBinding.tvSubServicePrice.setVisibility(View.VISIBLE);
            holder.mBinding.tvSubServicePrice.setText(context.getString(R.string.rupee_symbol_x, "152"));
        } else {
            holder.mBinding.imgIconCorrect.setSelected(false);
            holder.mBinding.clCenter.setVisibility(View.INVISIBLE);
            holder.mBinding.tvSubServicePrice.setVisibility(View.INVISIBLE);
        }

        holder.mBinding.tvSubServicePrice.setSelected(subServicesModel.isSelected);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RowSubCategoryUnitPaidBinding mBinding;

        public ViewHolder(RowSubCategoryUnitPaidBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.tvSubServiceName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubServiceDetailModel subServicesModel = mList.get(getAdapterPosition());
                    subServicesModel.isSelected = !subServicesModel.isSelected;
                    notifyItemChanged(getAdapterPosition());
                }
            });

            mBinding.imgIconCorrect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubServiceDetailModel subServicesModel = mList.get(getAdapterPosition());
                    subServicesModel.isSelected = !subServicesModel.isSelected;
                    notifyItemChanged(getAdapterPosition());
                }
            });

            mBinding.tvPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    SubServiceDetailModel subServicesModel = mList.get(getAdapterPosition());
                    int maxQty = Integer.valueOf(subServicesModel.maxUnit);
                    if (subServicesModel.qty < maxQty) {
                        subServicesModel.qty++;
                        notifyItemChanged(getAdapterPosition());
                    } else {
                        mListener.onLimitExceeded(subServicesModel, getAdapterPosition());
                    }
                }
            });

            mBinding.tvMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    SubServiceDetailModel subServicesModel = mList.get(getAdapterPosition());
                    int minQty = Integer.valueOf(subServicesModel.minUnit);
                    if (subServicesModel.qty > minQty) {
                        subServicesModel.qty--;
                        notifyItemChanged(getAdapterPosition());
                    }
                }
            });
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
