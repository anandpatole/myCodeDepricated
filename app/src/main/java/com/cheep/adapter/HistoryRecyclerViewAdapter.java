package com.cheep.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowHistoryBinding;
import com.cheep.model.HistoryModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;

/**
 * Created by pankaj on 9/29/16.
 */

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = HistoryRecyclerViewAdapter.class.getSimpleName();
    ArrayList<HistoryModel> mList;
    private HistoryItemInteractionListener listener;
    private Context mContext;

    public HistoryRecyclerViewAdapter(HistoryItemInteractionListener listener) {
        this.mList = new ArrayList<>();
        this.listener = listener;
    }

    public HistoryRecyclerViewAdapter(ArrayList<HistoryModel> mList, HistoryItemInteractionListener listener) {
        this.mList = mList;
        this.listener = listener;
    }

    public void setItems(ArrayList<HistoryModel> items) {
        this.mList = items;
        notifyDataSetChanged();
    }

    @Override
    public HistoryRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        RowHistoryBinding mRowHistoryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_history, parent, false);
        return new HistoryRecyclerViewAdapter.ViewHolder(mRowHistoryBinding);
    }

    @Override
    public void onBindViewHolder(HistoryRecyclerViewAdapter.ViewHolder holder, int position) {

        final HistoryModel model = mList.get(holder.getAdapterPosition());
//        Utility.showCircularImageView(holder.mRowHistoryBinding.imgProfile.getContext(), TAG, holder.mRowHistoryBinding.imgProfile, model.sp_profile_image, 0);
        Utility.showCircularImageView(holder.mRowHistoryBinding.imgProfile.getContext(), TAG, holder.mRowHistoryBinding.imgProfile, model.sp_profile_image, Utility.DEFAULT_CHEEP_LOGO, true);
        holder.mRowHistoryBinding.textName.setText(model.sp_user_name);
        holder.mRowHistoryBinding.textCategoryName.setText(model.task_category);

        holder.mRowHistoryBinding.textDate.setText(model.getPaymentDate());

        holder.mRowHistoryBinding.textSpent.setText(mContext.getString(R.string.rupee_symbol_x_space, Utility.getQuotePriceFormatter(model.paid_amount)));
        holder.mRowHistoryBinding.textSaved.setText(mContext.getString(R.string.rupee_symbol_x_space, Utility.getQuotePriceFormatter(model.saved_amount)));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onHistoryRowClicked(model);
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
        public final RowHistoryBinding mRowHistoryBinding;

        public ViewHolder(RowHistoryBinding mRowHistoryBinding) {
            super(mRowHistoryBinding.getRoot());
            mView = mRowHistoryBinding.getRoot();
            this.mRowHistoryBinding = mRowHistoryBinding;
        }
    }

    public interface HistoryItemInteractionListener {
        void onHistoryRowClicked(HistoryModel model);
    }
}
