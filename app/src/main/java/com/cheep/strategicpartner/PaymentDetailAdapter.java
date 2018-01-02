package com.cheep.strategicpartner;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowPaymentSummaryBinding;
import com.cheep.strategicpartner.model.AllSubSubCat;
import com.cheep.strategicpartner.model.StrategicPartnerServiceModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Giteeka on 20 july 2017.
 * Adapter for Strategic partner Phase 3 selected services.
 * Row item :- selected Service name, sub services name and total of all sub services
 * amount
 */

class PaymentDetailAdapter extends RecyclerView.Adapter<PaymentDetailAdapter.MyViewHolder> {
    private ArrayList<StrategicPartnerServiceModel> mList;

    PaymentDetailAdapter(ArrayList<StrategicPartnerServiceModel> mList) {
        this.mList = mList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowPaymentSummaryBinding rowPaymentSummaryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_payment_summary, parent, false);
        return new PaymentDetailAdapter.MyViewHolder(rowPaymentSummaryBinding);

    }

    @Override
    public void onBindViewHolder(PaymentDetailAdapter.MyViewHolder holder, int position) {
        holder.rowPastTaskBinding.textServiceName.setText(mList.get(position).name);
        double total = 0;
        StringBuilder subscription = new StringBuilder("");

        // calculate selected sub services amount and set total
        List<AllSubSubCat> allSubSubCats = mList.get(position).allSubSubCats;
        for (int i = 0; i < allSubSubCats.size(); i++) {
            AllSubSubCat allSubSubCat = allSubSubCats.get(i);
            if (subscription.length() == 0)
                subscription.append(allSubSubCat.subSubCatName);
            else
                subscription.append(",").append(allSubSubCat.subSubCatName);
            try {
                total += Double.parseDouble(allSubSubCat.price);
            } catch (NumberFormatException e) {
                total += 0;
            }
        }

        holder.rowPastTaskBinding.textServiceSubService.setSelected(true);
        holder.rowPastTaskBinding.textServiceSubService.setText(subscription.toString());
        holder.rowPastTaskBinding.textServiceRate.setText(
                holder.rowPastTaskBinding.textServiceRate.getContext().getString(R.string.rupee_symbol_x, String.valueOf(Utility.getQuotePriceFormatter(String.valueOf(total)))));
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
