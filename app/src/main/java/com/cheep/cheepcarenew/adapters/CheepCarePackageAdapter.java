package com.cheep.cheepcarenew.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.cheepcarenew.model.PackageDetail;
import com.cheep.databinding.RowCheepCarePackageBinding;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.LoadMoreRecyclerAdapter;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 12/22/17.
 */

public class CheepCarePackageAdapter extends LoadMoreRecyclerAdapter<CheepCarePackageAdapter.CheepCarePackageViewHolder> {

    private final PackageItemClickListener mListener;
    private List<PackageDetail> mList = new ArrayList<>();

    public interface PackageItemClickListener {
        void onPackageItemClick(int position, PackageDetail packageModel);
    }

    public CheepCarePackageAdapter(PackageItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public CheepCarePackageViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RowCheepCarePackageBinding binding
                = DataBindingUtil.inflate(layoutInflater, R.layout.row_cheep_care_package, parent, false);
        return new CheepCarePackageViewHolder(binding);
    }

    @Override
    public void onActualBindViewHolder(final CheepCarePackageViewHolder holder, int position) {
        final PackageDetail model = mList.get(position);


        Glide.with(holder.mBinding.getRoot().getContext())
                .load(model.packageImage)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.mBinding.ivItemBackground);

        int resId = R.drawable.gif_ballon_home_price;
        /*switch (model.packageSlug) {
            case NetworkUtility.CARE_PACKAGE_SLUG.HOME_CARE:
                resId = R.drawable.gif_ballon_home_price;
                break;
            case NetworkUtility.CARE_PACKAGE_SLUG.APPLIANCE_CARE:
                resId = R.drawable.gif_ballon_app_tec_price;
                break;
            case NetworkUtility.CARE_PACKAGE_SLUG.TECH_CARE:
                resId = R.drawable.gif_ballon_app_tec_price;
                break;
            case NetworkUtility.CARE_PACKAGE_SLUG.BIZ_CARE:
                resId = R.drawable.gif_ballon_biz_price;
                break;
            case NetworkUtility.CARE_PACKAGE_SLUG.SOCI_CARE:
                resId = R.drawable.gif_ballon_soci_price;
                break;
        }*/
        switch (model.type){
            case NetworkUtility.PACKAGE_DETAIL_TYPE.premium:
                holder.mBinding.tvPremiumAndNormal.setText(Utility.EARLY_BIRD_OFFER);
                resId = R.drawable.gif_ballon_biz_price;
                holder.mBinding.tvVip.setVisibility(View.VISIBLE);
                break;
            case NetworkUtility.PACKAGE_DETAIL_TYPE.normal:
                holder.mBinding.tvPremiumAndNormal.setText(Utility.VALID_FOR_3_MONTH);
                resId = R.drawable.gif_ballon_home_price;
                holder.mBinding.tvVip.setVisibility(View.GONE);
                break;
        }
        Glide.with(holder.mBinding.getRoot().getContext())
                .load(resId)
                .asGif()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.mBinding.ivCharge);

        holder.mBinding.tvTitle.setText(model.title);
        holder.mBinding.tvDescription.setText(model.subtitle);

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
        public RowCheepCarePackageBinding mBinding;

        public CheepCarePackageViewHolder(RowCheepCarePackageBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }

    public void addPackageList(List<PackageDetail> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }
}