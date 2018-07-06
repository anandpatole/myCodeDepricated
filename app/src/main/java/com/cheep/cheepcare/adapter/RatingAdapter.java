package com.cheep.cheepcare.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RatingBar;

import com.cheep.R;
import com.cheep.cheepcarenew.model.RatingModel;
import com.cheep.databinding.RowRatingBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bhavesh on 22/2/18.
 */

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.ViewHolder> {

    private final List<RatingModel> mList = new ArrayList<>();
    private boolean isIndicator = false;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowRatingBinding binding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext())
                        , R.layout.row_rating
                        , parent
                        , false);
        return new ViewHolder(binding);
    }

    public void setIsIndicator(boolean isIndicator) {
        this.isIndicator = isIndicator;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        RatingModel ratingModel = mList.get(position);
        holder.mBinding.tvPresentation.setText(ratingModel.reviewType);
        try {
            holder.mBinding.ratingPresentation.setRating(Float.parseFloat(ratingModel.reviewTypeRating));
        } catch (Exception e) {
            holder.mBinding.ratingPresentation.setRating(0);
        }
        holder.mBinding.ratingPresentation.setIsIndicator(isIndicator);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setList(ArrayList<RatingModel> ratingList) {
        mList.clear();
        mList.addAll(ratingList);
        notifyDataSetChanged();
    }

    public List<RatingModel> getList() {
        return mList;
    }

    public void addAll(List<RatingModel> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RowRatingBinding mBinding;

        public ViewHolder(RowRatingBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.ratingPresentation.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    mList.get(getAdapterPosition()).reviewTypeRating = String.valueOf(rating);
                }
            });

        }
    }

}
