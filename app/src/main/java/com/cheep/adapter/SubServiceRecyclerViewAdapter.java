package com.cheep.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
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

        holder.mRowSubserviceBinding.tvSubServiceName.setSelected(mSubServiceDetailModel.isSelected);
        if (mSubServiceDetailModel.isSelected) {
            holder.mRowSubserviceBinding.imgIconCorrect.setImageResource(R.drawable.ic_tick);
        } else {
            holder.mRowSubserviceBinding.imgIconCorrect.setImageResource(R.drawable.ic_tick_unselected);
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

            mRowSubserviceBinding.lnRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubServiceDetailModel subServicesModel = mList.get(getAdapterPosition());
                    subServicesModel.isSelected = !subServicesModel.isSelected;
                    notifyItemChanged(getAdapterPosition());
                    mSubServiceListInteractionListener.onSubCategoryRowItemClicked(subServicesModel);
                }
            });

        }
    }

    public void addList(ArrayList<SubServiceDetailModel> list) {
        Log.d(TAG, "addList() called with: list = [" + list.size() + "]");
        this.mList = list;
        notifyDataSetChanged();
    }
}

