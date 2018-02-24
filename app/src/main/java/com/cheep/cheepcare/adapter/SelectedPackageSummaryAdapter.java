package com.cheep.cheepcare.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcare.model.PackageDetail;
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
    private List<PackageDetail> packageDetailList = new ArrayList<>();

    public interface PackageItemClickListener {
        void onPackageItemClick(int position, PackageDetail packageModel);

        void onRemovePackage(int position, PackageDetail packageModel);
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
        final PackageDetail packageDetail = packageDetailList.get(position);
        Context context = holder.mBinding.getRoot().getContext();

        // set title of package
        holder.mBinding.tvTitle.setText(packageDetail.title);

        // set description of package
        holder.mBinding.tvDescription.setText(packageDetail.subtitle);

        // set montly price of package


        // set address category icon
        if (packageDetail.mSelectedAddressList != null && !packageDetail.mSelectedAddressList.isEmpty()) {
            holder.mBinding.ivAddressIcon.setImageResource(Utility.getAddressCategoryBlueIcon(packageDetail.mSelectedAddressList.get(0).category));
            holder.mBinding.tvAddress.setText(packageDetail.mSelectedAddressList.get(0).getAddressWithInitials());
        }

        holder.mBinding.ivIsAddressSelected.setSelected(packageDetail.isSelected);


        holder.mBinding.tvPrice.setText(Utility.getCheepCarePackageMonthlyPrice(context, R.string.rupee_symbol_x_package_price, String.valueOf(packageDetail.monthlyPrice)));

        /*Glide.with(holder.mBinding.getRoot().getContext())
                .load(R.drawable.home_care_price_mumbai)
                .asGif()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.mBinding.ivCharge);*/

        holder.mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPackageItemClick(holder.getAdapterPosition(), packageDetail);
            }
        });
    }

    @Override
    public int onActualItemCount() {
        return packageDetailList.size();
    }

    public class CheepCarePackageViewHolder extends RecyclerView.ViewHolder {
        public RowSelectedPackagesBinding mBinding;

        public CheepCarePackageViewHolder(RowSelectedPackagesBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.ivCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onRemovePackage(getAdapterPosition(), packageDetailList.get(getAdapterPosition()));
                }
            });
        }
    }

    public List<PackageDetail> getList() {
        return packageDetailList;
    }

    public void addPakcageList(List<PackageDetail> list) {
        packageDetailList.addAll(list);
        notifyDataSetChanged();
    }
}