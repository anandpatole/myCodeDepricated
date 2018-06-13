package com.cheep.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowRateCardSelectionBinding;


import java.util.ArrayList;

public class RateCardSelectionRecyclerViewAdapter extends RecyclerView.Adapter<RateCardSelectionRecyclerViewAdapter.ViewHolder>

{

    InteractionListener listener;
    Context mContext;
    ArrayList<String> mList;

    public RateCardSelectionRecyclerViewAdapter(ArrayList<String> mList, InteractionListener listener) {
        if (mList != null)
            this.mList = mList;
        else
            this.mList = new ArrayList<>();
this.listener=listener;
    }

    @Override
    public int getItemCount()
    {
        return mList.size();

    }
    @Override
    public RateCardSelectionRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       RowRateCardSelectionBinding mRowAddressBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_rate_card_selection, parent, false);
        return new RateCardSelectionRecyclerViewAdapter.ViewHolder(mRowAddressBinding);
    }

    @Override
    public void onBindViewHolder(final RateCardSelectionRecyclerViewAdapter.ViewHolder holder, int position)
    {
holder.mRowAddressBinding.textSelection.setText(mList.get(position));
holder.mRowAddressBinding.textSelection.setOnClickListener(new View.OnClickListener()
{
    @Override
    public void onClick(View v) {

     listener.onClicked("s");
    }
});
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        public final RowRateCardSelectionBinding mRowAddressBinding;

        public ViewHolder(RowRateCardSelectionBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowAddressBinding = binding;
        }
    }

    public interface InteractionListener {
        void onClicked(String s);


    }



}
