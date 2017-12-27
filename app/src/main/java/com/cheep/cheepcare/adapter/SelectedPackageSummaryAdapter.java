package com.cheep.cheepcare.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.cheepcare.model.CheepCarePackageModel;
import com.cheep.databinding.RowSelectedPackagesBinding;
import com.cheep.utils.LoadMoreRecyclerAdapter;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 12/22/17.
 */

public class SelectedPackageSummaryAdapter extends LoadMoreRecyclerAdapter<SelectedPackageSummaryAdapter.CheepCarePackageViewHolder> {

    private final PackageItemClickListener mListener;
    private List<CheepCarePackageModel> mList = new ArrayList<>();

    public interface PackageItemClickListener {
        void onPackageItemClick(int position, CheepCarePackageModel packageModel);
    }

    public SelectedPackageSummaryAdapter(PackageItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public CheepCarePackageViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RowSelectedPackagesBinding binding
                = DataBindingUtil.inflate(layoutInflater, R.layout.row_selected_packages, parent, false);
        return new CheepCarePackageViewHolder(binding);
    }

    @Override
    public void onActualBindViewHolder(final CheepCarePackageViewHolder holder, int position) {
        final CheepCarePackageModel model = mList.get(position);
        Context context = holder.mBinding.getRoot().getContext();

        Glide.with(context)
                .load(R.drawable.banner_appliance_care_gif)
                .asGif()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.mBinding.ivItemBackground);

        holder.mBinding.tvTitle.setText(model.packageTitle);
        holder.mBinding.ivIsAddressSelected.setSelected(model.isSelected);
        holder.mBinding.tvDescription.setText(model.packageDescription);
        SpannableString spannableString1 = new SpannableString(context.getString(R.string.rupee_symbol_x_package_price, model.price));
        spannableString1 = Utility.getCheepCarePackageMonthlyPrice(spannableString1, spannableString1.length() - 2, spannableString1.length());
        holder.mBinding.tvPrice.setText(spannableString1);

        /*Glide.with(holder.mBinding.getRoot().getContext())
                .load(R.drawable.home_care_price_mumbai)
                .asGif()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.mBinding.ivCharge);*/

        holder.mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPackageItemClick(holder.getAdapterPosition(), model);
            }
        });
    }

    @Override
    public int onActualItemCount() {
        return mList.size();
    }

    public class CheepCarePackageViewHolder extends RecyclerView.ViewHolder {
        public RowSelectedPackagesBinding mBinding;

        public CheepCarePackageViewHolder(RowSelectedPackagesBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }

    public void addPakcageList(List<CheepCarePackageModel> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }
}