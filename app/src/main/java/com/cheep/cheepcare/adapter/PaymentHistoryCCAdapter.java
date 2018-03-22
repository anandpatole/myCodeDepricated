package com.cheep.cheepcare.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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

        //for managing the case of pro not assigned
        if (TextUtils.isEmpty(model.sp_user_id)) {
            if (TextUtils.isEmpty(model.sp_user_name)) {
                holder.mBinding.imgProfile.setImageResource(R.drawable.placeholder_cheep_pro_not_assigned);
                holder.mBinding.textProviderName.setText(context.getString(R.string.label_pro_will_be_assigned_shortly));
                holder.mBinding.textProviderName.setTextColor(ContextCompat.getColor(context, R.color.grey_varient_16));
            } else {
                Glide.with(context)
                        .load(R.drawable.ic_home_with_heart_text)
                        .asGif()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(holder.mBinding.imgProfile);
                holder.mBinding.textProviderName.setTextColor(ContextCompat.getColor(context, R.color.splash_gradient_end));
                holder.mBinding.textProviderName.setText(model.sp_user_name);
            }

        } else {
            GlideUtility.showCircularImageView(context, TAG, holder.mBinding.imgProfile, model.sp_profile_image
                    , Utility.DEFAULT_CHEEP_LOGO, true);
            holder.mBinding.textProviderName.setTextColor(ContextCompat.getColor(context, R.color.splash_gradient_end));
            holder.mBinding.textProviderName.setText(model.sp_user_name);
        }

        //whether or not to show favorite image
        if (model.spFavourite.equalsIgnoreCase(context.getString(R.string.label_no))) {
            holder.mBinding.imgFav.setVisibility(View.GONE);
        } else {
            holder.mBinding.imgFav.setVisibility(View.VISIBLE);
        }

        /////////////////////manage amount, payment via and pending group visibility/////////////////////////

        //if task type is subscribed show in UI free with cheep care
        boolean isSubscribed = model.taskType.equalsIgnoreCase(context.getString(R.string.label_subscribed));
        boolean isPendingAmount = Double.parseDouble(model.pendingAmount) != 0;
        //Case when there is pending amount: if pending amount is there whether or not task is subscribed we have to show pending amount
        if (isPendingAmount) {
            //there is pending amount hence set price text as pending amount
            holder.mBinding.tvPrice.setText(context.getString(R.string.rupee_symbol_x_space
                    , Utility.getQuotePriceFormatter(model.pendingAmount)));

            //setting text of payment via as pending amount as there is pending amount
            holder.mBinding.tvPaymentVia.setTextColor(ContextCompat.getColor(context, R.color.c_FD7E28));
            holder.mBinding.tvPaymentVia.setText(context.getString(R.string.label_payment_pending));

            //pending amount so pending amount group visible
            holder.mBinding.groupPaymentPending.setVisibility(View.VISIBLE);

        }
        //Case when there is no pending amount and task is subscribed
        else if (isSubscribed) {
            //task is subscribed and there is no pending amount hence set price text as free
            holder.mBinding.tvPrice.setText(context.getString(R.string.label_free_small));

            //setting text of payment via as with Cheep Care
            holder.mBinding.tvPaymentVia.setTextColor(ContextCompat.getColor(context, R.color.grey_varient_13));
            holder.mBinding.tvPaymentVia.setText(context.getString(R.string.label_with_cheep_care));

            //no pending amount so pending amount group gone
            holder.mBinding.groupPaymentPending.setVisibility(View.GONE);
        }
        //Case when there is no pending amount and task is not subscribed
        else {
            //set text of price as paid amount
            holder.mBinding.tvPrice.setText(context.getString(R.string.rupee_symbol_x_space
                    , Utility.getQuotePriceFormatter(model.paid_amount)));

            //setting text of payment via as with Cheep Care
            holder.mBinding.tvPaymentVia.setTextColor(ContextCompat.getColor(context, R.color.grey_varient_13));
            holder.mBinding.tvPaymentVia.setText(model.paymentMethod);

            //no pending amount so pending amount group gone
            holder.mBinding.groupPaymentPending.setVisibility(View.GONE);
        }
        /////////////////////manage amount, payment via and pending group visibility/////////////////////////

        holder.mBinding.tvProCategory.setText(model.task_category);

        holder.mBinding.tvDate.setText(model.getPaymentDate());

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
