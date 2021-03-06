package com.cheep.adapter;

import android.content.Context;

import android.databinding.DataBindingUtil;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowRateCardBinding;
import com.cheep.model.RateCardModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;

public class RateRecyclerViewAdapter extends RecyclerView.Adapter<RateRecyclerViewAdapter.ViewHolder> {

    ArrayList<RateCardModel> mList1;
    Context mContext;


    public RateRecyclerViewAdapter(ArrayList<RateCardModel> mList) {
        mList1 = mList;

    }

    @Override
    public int getItemCount() {

        return mList1.size();
    }

    public ArrayList<RateCardModel> getmList() {
        return mList1;
    }

    @Override
    public RateRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        RowRateCardBinding rowPastTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_rate_card, parent, false);
        return new RateRecyclerViewAdapter.ViewHolder(rowPastTaskBinding);
    }

    @Override
    public void onBindViewHolder(final RateRecyclerViewAdapter.ViewHolder holder, int position) {
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.grey_color));
        } else {
            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.white_variant_3));
        }
        RateCardModel model = mList1.get(holder.getAdapterPosition());
        holder.rowPastTaskBinding.rateCardProduct.setText(model.description);

        if(model.labourRate.equalsIgnoreCase(""))
        {
            holder.rowPastTaskBinding.rateCardRates.setText("-");
        }
        else
        {
            holder.rowPastTaskBinding.rateCardRates.setText(model.labourRate);
        }
       if(model.addUnit.equalsIgnoreCase(""))
       {
           holder.rowPastTaskBinding.rateCardRatesPerUnit.setText("-");
       }
       else {
           holder.rowPastTaskBinding.rateCardRatesPerUnit.setText(model.addUnit);
       }

        //holder.rowPastTaskBinding.rateCardRates.setText(model.labourRate);
        if(model.labourRate.equalsIgnoreCase("")) {
            holder.rowPastTaskBinding.rateCardRates.setText(mContext.getString(R.string.hyphen));
        }
        else
        {
            holder.rowPastTaskBinding.rateCardRates.setText(mContext.getString(R.string.rupee_symbol_x, model.labourRate));
        }
        //holder.rowPastTaskBinding.rateCardRatesPerUnit.setText(model.addUnit);
        if(model.addUnit.equalsIgnoreCase(""))
        {
            holder.rowPastTaskBinding.rateCardRatesPerUnit.setText(mContext.getString(R.string.hyphen));
        }
        else
        {
            holder.rowPastTaskBinding.rateCardRatesPerUnit.setText(mContext.getString(R.string.rupee_symbol_x, model.addUnit));
        }




    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public RowRateCardBinding rowPastTaskBinding;

        public ViewHolder(RowRateCardBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            rowPastTaskBinding = binding;

        }
    }


}
