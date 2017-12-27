package com.cheep.cheepcare.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcare.model.CheepCarePackageModel;
import com.cheep.databinding.RowBundledPackageHeaderBinding;
import com.cheep.databinding.RowBundledPackageSelectedBinding;
import com.cheep.databinding.RowBundledPackagetNoSelectedBinding;
import com.cheep.utils.LoadMoreRecyclerAdapter;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 12/22/17.
 */

public class PackageBundlingAdapter extends LoadMoreRecyclerAdapter<PackageBundlingAdapter.PackageViewHolder> {

    private final PackageItemClickListener mListener;
    private List<CheepCarePackageModel> mList = new ArrayList<>();

    public static final int ROW_PACKAGE_SELECTED = 0;
    public static final int ROW_PACKAGE_HEADER = 1;
    public static final int ROW_PACKAGE_NOT_SELECTED = 2;

    public interface PackageItemClickListener {
        void onPackageItemClick(int position, CheepCarePackageModel packageModel);
    }

    public PackageBundlingAdapter(PackageItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public PackageViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ROW_PACKAGE_HEADER: {
                RowBundledPackageHeaderBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.row_bundled_package_header, parent, false);
                return new PackageViewHolder(binding);
            }
            case ROW_PACKAGE_NOT_SELECTED: {
                RowBundledPackagetNoSelectedBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.row_bundled_packaget_no_selected, parent, false);
                return new PackageViewHolder(binding);
            }
            default: {
                RowBundledPackageSelectedBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.row_bundled_package_selected, parent, false);
                return new PackageViewHolder(binding);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).rowType;
    }

    @Override
    public void onActualBindViewHolder(final PackageViewHolder holder, int position) {
        int viewType = getItemViewType(holder.getAdapterPosition());
        Context context;
        final CheepCarePackageModel model = mList.get(position);
        switch (viewType) {
            case ROW_PACKAGE_NOT_SELECTED:
                context = holder.mRowNotSelectedBinding.getRoot().getContext();
                Utility.loadImageView(context, holder.mRowNotSelectedBinding.ivItemBackground, "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/medium/Untitled.jpg");
                SpannableString spannableString = new SpannableString(context.getString(R.string.rupee_symbol_x_package_price, model.price));
                spannableString = Utility.getCheepCarePackageMonthlyPrice(spannableString, spannableString.length() - 2, spannableString.length());
                holder.mRowNotSelectedBinding.tvPrice.setText(spannableString);
                holder.mRowNotSelectedBinding.ivIsAddressSelected.setSelected(model.isSelected);
                holder.mRowNotSelectedBinding.tvDescription.setText(model.packageDescription);
                holder.mRowNotSelectedBinding.tvTitle.setText(model.packageTitle);


                holder.mRowNotSelectedBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onPackageItemClick(holder.getAdapterPosition(), model);
                    }
                });
                break;
            case ROW_PACKAGE_SELECTED:
                context = holder.mRowSelectedBinding.getRoot().getContext();
                Utility.loadImageView(context, holder.mRowSelectedBinding.ivItemBackground, "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/medium/Untitled.jpg");
                SpannableString spannableString1 = new SpannableString(context.getString(R.string.rupee_symbol_x_package_price, model.price));
                spannableString1 = Utility.getCheepCarePackageMonthlyPrice(spannableString1, spannableString1.length() - 2, spannableString1.length());
                holder.mRowSelectedBinding.tvPrice.setText(spannableString1);
                holder.mRowSelectedBinding.ivIsAddressSelected.setSelected(model.isSelected);
                holder.mRowSelectedBinding.tvDescription.setText(model.packageDescription);
                holder.mRowSelectedBinding.tvTitle.setText(model.packageTitle);


                holder.mRowSelectedBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onPackageItemClick(holder.getAdapterPosition(), model);
                    }
                });
                break;
            case ROW_PACKAGE_HEADER:
//                holder.mBindingBundledPackageHeaderBinding.txtRibbon.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                holder.mRowHeaderBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
                break;
        }
    }

    @Override
    public int onActualItemCount() {
        return mList.size();
    }


    class PackageViewHolder extends RecyclerView.ViewHolder {
        RowBundledPackageSelectedBinding mRowSelectedBinding;
        RowBundledPackageHeaderBinding mRowHeaderBinding;
        RowBundledPackagetNoSelectedBinding mRowNotSelectedBinding;


        PackageViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            if (binding instanceof RowBundledPackageHeaderBinding)
                mRowHeaderBinding = (RowBundledPackageHeaderBinding) binding;
            else if (binding instanceof RowBundledPackagetNoSelectedBinding)
                mRowNotSelectedBinding = (RowBundledPackagetNoSelectedBinding) binding;
            else
                mRowSelectedBinding = (RowBundledPackageSelectedBinding) binding;
        }
    }

    public void addPakcageList(List<CheepCarePackageModel> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }


}