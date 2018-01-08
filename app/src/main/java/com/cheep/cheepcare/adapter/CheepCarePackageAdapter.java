package com.cheep.cheepcare.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.databinding.RowCheepCarePackageBinding;
import com.cheep.utils.LoadMoreRecyclerAdapter;

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
                .load(R.drawable.banner_appliance_care_gif)
                .asGif()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.mBinding.ivItemBackground);

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