package com.cheep.strategicpartner;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowSelectedServiceAmoutBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 9/30/16.
 */

public class StrategicPartnerPaymentAdapter extends RecyclerView.Adapter<StrategicPartnerPaymentAdapter.MyViewHolder> {
    ArrayList<StrategicPartnerSubCategoryModel> mList;
    Context mContext;
    Activity activity;

    public StrategicPartnerPaymentAdapter(Activity activity, ArrayList<StrategicPartnerSubCategoryModel> mList) {
        this.mList = mList;
        this.activity = activity;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        RowSelectedServiceAmoutBinding rowSelectedServiceAmoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_selected_service_amout, parent, false);
        return new StrategicPartnerPaymentAdapter.MyViewHolder(rowSelectedServiceAmoutBinding);

    }

    @Override
    public void onBindViewHolder(StrategicPartnerPaymentAdapter.MyViewHolder holder, int position) {
        holder.rowPastTaskBinding.textServiceName.setText(mList.get(position).name);
        int total = 0;
        StringBuilder subscription = new StringBuilder("");

        List<StrategicPartnerSubCategoryModel.AllSubSubCat> allSubSubCats = mList.get(position).allSubSubCats;
        for (int i = 0; i < allSubSubCats.size(); i++) {
            StrategicPartnerSubCategoryModel.AllSubSubCat allSubSubCat = allSubSubCats.get(i);
            if (subscription.length() == 0)
                subscription.append(allSubSubCat.subSubCatName);
            else
                subscription.append(",").append(allSubSubCat.subSubCatName);
            try {
                total += Integer.parseInt(allSubSubCat.price);
            } catch (NumberFormatException e) {
                total += 0;
            }
        }
        
        holder.rowPastTaskBinding.textServiceSubService.setSelected(true);
        holder.rowPastTaskBinding.textServiceSubService.append(subscription.toString());
        holder.rowPastTaskBinding.textServiceRate.setText(activity.getString(R.string.ruppe_symbol_x, String.valueOf(total)));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowSelectedServiceAmoutBinding rowPastTaskBinding;

        public MyViewHolder(RowSelectedServiceAmoutBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            rowPastTaskBinding = binding;
        }
    }

}
