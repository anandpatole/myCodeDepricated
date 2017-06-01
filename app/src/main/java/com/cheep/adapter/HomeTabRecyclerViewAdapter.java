package com.cheep.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowTabHomeBinding;
import com.cheep.fragment.HomeTabFragment;
import com.cheep.model.JobCategoryModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;

/**
 * Created by pankaj on 9/27/16.
 */

public class HomeTabRecyclerViewAdapter extends RecyclerView.Adapter<HomeTabRecyclerViewAdapter.ViewHolder> {

    ArrayList<JobCategoryModel> mList;
    HomeTabFragment.CategoryRowInteractionListener mListener;
    Context mContext;

    public HomeTabRecyclerViewAdapter(ArrayList<JobCategoryModel> mList, HomeTabFragment.CategoryRowInteractionListener mListener) {
        this.mList = mList;
        this.mListener = mListener;
    }

    public void addItem(ArrayList<JobCategoryModel> mList) {

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        RowTabHomeBinding mRowTabHomeBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_tab_home, parent, false);
        return new ViewHolder(mRowTabHomeBinding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final JobCategoryModel model = mList.get(holder.getAdapterPosition());

        //Background image
        Utility.loadImageView(mContext, holder.mRowTabHomeBinding.imgCategoryBackground, model.catImage, R.drawable.gradient_black);
        Utility.loadImageView(mContext, holder.mRowTabHomeBinding.imgCategoryBackground, model.catImageExtras.thumb, R.drawable.gradient_black);

        //Category Icon
        Utility.loadImageView(mContext, holder.mRowTabHomeBinding.imgCategoryIcon, model.catIcon, 0);
        holder.mRowTabHomeBinding.imgCategoryIcon.setVisibility(View.GONE);
        holder.mRowTabHomeBinding.textJobsCount.setText(String.valueOf(model.spCount));
        holder.mRowTabHomeBinding.textCategoryName.setText(String.valueOf(model.catName));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onCategoryRowClicked(model, holder.getAdapterPosition());
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
        public final RowTabHomeBinding mRowTabHomeBinding;

        public ViewHolder(RowTabHomeBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowTabHomeBinding = binding;
        }
    }

    public ArrayList<JobCategoryModel> getmList() {
        return mList;
    }
}
