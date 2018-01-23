package com.cheep.cheepcare.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowSubServiceUnitTickBinding;
import com.cheep.cheepcare.fragment.SelectSubCategoryFragment;
import com.cheep.model.SubServiceDetailModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bhavesh on 28/4/17.
 */

public class SubServiceUnitAdapter extends RecyclerView.Adapter<SubServiceUnitAdapter.ViewHolder> {
    private static final String TAG = SubServiceUnitAdapter.class.getSimpleName();
    ArrayList<SubServiceDetailModel> mList = new ArrayList<>();
//    private SelectSubCategoryFragment.SubServiceListInteractionListener mSubServiceListInteractionListener;

    public SubServiceUnitAdapter(/*SelectSubCategoryFragment.SubServiceListInteractionListener listener*/) {
//        this.mSubServiceListInteractionListener = listener;
    }

    @Override
    public SubServiceUnitAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding mRowTaskBinding =
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.getContext())
                        , R.layout.row_sub_service_unit_tick
                        , parent
                        , false
                );
        return new SubServiceUnitAdapter.ViewHolder(mRowTaskBinding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final SubServiceDetailModel mSubServiceDetailModel = mList.get(holder.getAdapterPosition());

        //Set SubService name
        holder.mRowSubserviceBinding.tvSubServiceName.setText(mSubServiceDetailModel.name);

        holder.mRowSubserviceBinding.tvSubServiceName.setSelected(mSubServiceDetailModel.isSelected);
        holder.mRowSubserviceBinding.lnRoot.setSelected(mSubServiceDetailModel.isSelected);

        if (mSubServiceDetailModel.isSelected) {
            holder.mRowSubserviceBinding.imgIconCorrect.setImageResource(R.drawable.ic_tick);
            holder.mRowSubserviceBinding.lnRoot.setBackgroundColor(ContextCompat.getColor(holder.mView.getContext(), R.color.white));
        } else {
            holder.mRowSubserviceBinding.imgIconCorrect.setImageResource(R.drawable.ic_tick_unselected);
            holder.mRowSubserviceBinding.lnRoot.setBackgroundColor(ContextCompat.getColor(holder.mView.getContext(), R.color.transparent));
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mSubServiceListInteractionListener.onSubCategoryRowItemClicked(mSubServiceDetailModel);

                // Manage Selection state
//                for (SubServiceDetailModel subServiceDetailModel : mList) {
                mSubServiceDetailModel.isSelected = !mSubServiceDetailModel.isSelected;
//                }
                notifyItemChanged(holder.getAdapterPosition());
            }
        };
        holder.mRowSubserviceBinding.tvSubServiceName.setOnClickListener(clickListener);
        holder.mRowSubserviceBinding.imgIconCorrect.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public RowSubServiceUnitTickBinding mRowSubserviceBinding;

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowSubserviceBinding = (RowSubServiceUnitTickBinding) binding;
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
        subServiceDetailModel.sub_cat_id = -1;
        subServiceDetailModel.catId = -1;
        subServiceDetailModel.name = otherSubService;
        this.mList.add(subServiceDetailModel);
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