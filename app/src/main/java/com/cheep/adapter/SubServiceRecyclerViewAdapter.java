package com.cheep.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowSubCategoryUnitFreeBinding;
import com.cheep.fragment.SelectSubCategoryFragment;
import com.cheep.model.SubServiceDetailModel;

import java.util.ArrayList;

/**
 * Created by bhavesh on 28/4/17.
 */

public class SubServiceRecyclerViewAdapter extends RecyclerView.Adapter<SubServiceRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = SubServiceRecyclerViewAdapter.class.getSimpleName();
    ArrayList<SubServiceDetailModel> mList = new ArrayList<>();
    private SelectSubCategoryFragment.SubServiceListInteractionListener mSubServiceListInteractionListener;

    public SubServiceRecyclerViewAdapter(SelectSubCategoryFragment.SubServiceListInteractionListener listener) {
        this.mSubServiceListInteractionListener = listener;
    }

    @Override
    public SubServiceRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding mRowTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_sub_category_unit_free, parent, false);
        return new SubServiceRecyclerViewAdapter.ViewHolder(mRowTaskBinding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final SubServiceDetailModel mSubServiceDetailModel = mList.get(holder.getAdapterPosition());

        //Set SubService name
        holder.mRowSubserviceBinding.tvSubServiceName.setText(mSubServiceDetailModel.name);

        holder.mRowSubserviceBinding.tvDigit.setText(String.valueOf(mSubServiceDetailModel.selected_unit));
        holder.mRowSubserviceBinding.tvSubServiceName.setSelected(mSubServiceDetailModel.isSelected);
        holder.mRowSubserviceBinding.lnRoot.setSelected(mSubServiceDetailModel.isSelected);

        if (mSubServiceDetailModel.isSelected) {
            holder.mRowSubserviceBinding.clCenter.setVisibility(View.VISIBLE);
            holder.mRowSubserviceBinding.imgIconCorrect.setImageResource(R.drawable.ic_tick);
            holder.mRowSubserviceBinding.lnRoot.setBackgroundColor(ContextCompat.getColor(holder.mView.getContext(), R.color.white));
        } else {
            holder.mRowSubserviceBinding.clCenter.setVisibility(View.GONE);
            holder.mRowSubserviceBinding.imgIconCorrect.setImageResource(R.drawable.ic_tick_unselected);
            holder.mRowSubserviceBinding.lnRoot.setBackgroundColor(ContextCompat.getColor(holder.mView.getContext(), R.color.transparent));
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public ArrayList<SubServiceDetailModel> getList() {
        return mList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public RowSubCategoryUnitFreeBinding mRowSubserviceBinding;

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowSubserviceBinding = (RowSubCategoryUnitFreeBinding) binding;

            mRowSubserviceBinding.tvSubServiceName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubServiceDetailModel subServicesModel = mList.get(getAdapterPosition());
                    subServicesModel.isSelected = !subServicesModel.isSelected;
                    notifyItemChanged(getAdapterPosition());
                    mSubServiceListInteractionListener.onSubCategoryRowItemClicked(subServicesModel);

                    // Manage Selection state //TODO new flow uncomments this code
//                for (SubServiceDetailModel subServiceDetailModel : mList) {
//                    subServiceDetailModel.isSelected = subServiceDetailModel.equals(mSubServiceDetailModel);
//                }
                    notifyDataSetChanged();
                }
            });

            mRowSubserviceBinding.imgIconCorrect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubServiceDetailModel subServicesModel = mList.get(getAdapterPosition());
                    subServicesModel.isSelected = !subServicesModel.isSelected;
                    notifyItemChanged(getAdapterPosition());
                }
            });

            mRowSubserviceBinding.tvPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    SubServiceDetailModel subServicesModel = mList.get(getAdapterPosition());
                    int maxQty = Integer.valueOf(subServicesModel.maxUnit);
                    if (subServicesModel.selected_unit < maxQty) {
                        subServicesModel.selected_unit++;
                        notifyItemChanged(getAdapterPosition());
                    } else {
//                        mListener.onLimitExceeded(subServicesModel, getAdapterPosition());
                    }
                }
            });

            mRowSubserviceBinding.tvMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    SubServiceDetailModel subServicesModel = mList.get(getAdapterPosition());
                    int minQty = Integer.valueOf(subServicesModel.minUnit);
                    if (subServicesModel.selected_unit > minQty) {
                        subServicesModel.selected_unit--;
                        notifyItemChanged(getAdapterPosition());
                    }
                }
            });
        }
    }

    public void addList(ArrayList<SubServiceDetailModel> list, String otherSubService) {
        Log.d(TAG, "addList() called with: list = [" + list.size() + "]");
        if (list == null) {
            list = new ArrayList<>();
        }
        this.mList = list;
        //Add other as Subservice
        SubServiceDetailModel subServiceDetailModel = new SubServiceDetailModel();
        subServiceDetailModel.sub_cat_id = "-1";
        subServiceDetailModel.catId = "-1";
        subServiceDetailModel.name = otherSubService;
        this.mList.add(subServiceDetailModel);
        notifyDataSetChanged();
    }
}

