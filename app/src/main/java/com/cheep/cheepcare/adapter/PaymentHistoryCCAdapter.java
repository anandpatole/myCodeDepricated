package com.cheep.cheepcare.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowPaymentHistoryCcBinding;
import com.cheep.model.HistoryModel;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.LoadMoreRecyclerAdapter;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;

import java.util.ArrayList;

/**
 * Created by kruti on 20/3/18.
 */

public class PaymentHistoryCCAdapter extends LoadMoreRecyclerAdapter<PaymentHistoryCCAdapter.PaymentHistoryViewHolder> {

    private static final String TAG = LogUtils.makeLogTag(PaymentHistoryCCAdapter.class);
    private final HistoryItemInteractionListener mListener;
    private ArrayList<HistoryModel> mList = new ArrayList<>();

    public interface HistoryItemInteractionListener {
        void onHistoryRowClicked(HistoryModel model);
    }

    public PaymentHistoryCCAdapter(HistoryItemInteractionListener listener) {
        mListener = listener;
    }

    @Override
    public PaymentHistoryViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
        RowPaymentHistoryCcBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext())
                , R.layout.row_payment_history_cc
                , parent
                , false
        );
        return new PaymentHistoryViewHolder(binding);
    }

    @Override
    public void onActualBindViewHolder(PaymentHistoryViewHolder holder, int position) {
        final HistoryModel model = mList.get(holder.getAdapterPosition());
        Context context = holder.mBinding.getRoot().getContext();

        GlideUtility.showCircularImageView(context, TAG, holder.mBinding.imgProfile, model.sp_profile_image, Utility.DEFAULT_CHEEP_LOGO, true);
        holder.mBinding.textProviderName.setText(model.sp_user_name);
        holder.mBinding.tvProCategory.setText(model.task_category);

        holder.mBinding.tvDate.setText(model.getPaymentDate());

        holder.mBinding.tvPrice.setText(context.getString(R.string.rupee_symbol_x_space, Utility.getQuotePriceFormatter(model.paid_amount)));

    }

    @Override
    public int onActualItemCount() {
        return mList.size();
    }

    public void setItems(ArrayList<HistoryModel> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public class PaymentHistoryViewHolder extends RecyclerView.ViewHolder {

        private final RowPaymentHistoryCcBinding mBinding;

        public PaymentHistoryViewHolder(RowPaymentHistoryCcBinding binding) {
            super(binding.getRoot());
            mBinding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onHistoryRowClicked(mList.get(getAdapterPosition()));
                    }
                }
            });
        }
    }
}
