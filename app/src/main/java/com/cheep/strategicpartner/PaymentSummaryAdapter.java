package com.cheep.strategicpartner;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowPaymentSummaryBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Giteeka on 20 july 2017.
 * Adapter for Strategic partner Phase 3 selected services.
 * Row item :- selected Service name, sub services name and total of all sub services
 * amount
 */

class PaymentSummaryAdapter extends RecyclerView.Adapter<PaymentSummaryAdapter.MyViewHolder> {
    private ArrayList<StrategicPartnerServiceModel> mList;

    PaymentSummaryAdapter(ArrayList<StrategicPartnerServiceModel> mList) {
        this.mList = mList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowPaymentSummaryBinding rowSelectedServiceAmoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_payment_summary, parent, false);
        return new PaymentSummaryAdapter.MyViewHolder(rowSelectedServiceAmoutBinding);

    }

    @Override
    public void onBindViewHolder(PaymentSummaryAdapter.MyViewHolder holder, int position) {
        holder.rowPastTaskBinding.textServiceName.setText(mList.get(position).name);
        int total = 0;
        StringBuilder subscription = new StringBuilder("");

        // calculate selected sub services amount and set total
        List<StrategicPartnerServiceModel.AllSubSubCat> allSubSubCats = mList.get(position).allSubSubCats;
        for (int i = 0; i < allSubSubCats.size(); i++) {
            StrategicPartnerServiceModel.AllSubSubCat allSubSubCat = allSubSubCats.get(i);
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
        holder.rowPastTaskBinding.textServiceRate.setText(
                holder.rowPastTaskBinding.textServiceRate.getContext().getString(R.string.ruppe_symbol_x, String.valueOf(total)));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        final RowPaymentSummaryBinding rowPastTaskBinding;

        MyViewHolder(RowPaymentSummaryBinding binding) {
            super(binding.getRoot());
            rowPastTaskBinding = binding;
        }
    }

}
