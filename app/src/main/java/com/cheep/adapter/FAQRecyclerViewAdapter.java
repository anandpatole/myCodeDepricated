package com.cheep.adapter;


import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowFaqBinding;
import com.cheep.model.FAQModel;

import java.util.ArrayList;

/**
 * Created by pankaj on 9/29/16.
 */

public class FAQRecyclerViewAdapter extends RecyclerView.Adapter<FAQRecyclerViewAdapter.ViewHolder> {

    ArrayList<FAQModel> mList;
    private FAQItemInteractionListener listener;

    public FAQRecyclerViewAdapter(ArrayList<FAQModel> mList, FAQItemInteractionListener listener) {
        this.mList = mList;
        this.listener = listener;
    }

    @Override
    public FAQRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowFaqBinding mRowFaqBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_faq, parent, false);
        return new FAQRecyclerViewAdapter.ViewHolder(mRowFaqBinding);
    }

    @Override
    public void onBindViewHolder(FAQRecyclerViewAdapter.ViewHolder holder, int position) {

        final FAQModel model = mList.get(holder.getAdapterPosition());
        holder.mRowFaqBinding.textFaqTitle.setText(model.faq_title);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onFAQRowClicked(model);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowFaqBinding mRowFaqBinding;

        public ViewHolder(RowFaqBinding mRowFaqBinding) {
            super(mRowFaqBinding.getRoot());
            mView = mRowFaqBinding.getRoot();
            this.mRowFaqBinding = mRowFaqBinding;
        }
    }

    public interface FAQItemInteractionListener {
        void onFAQRowClicked(FAQModel model);
    }
}
