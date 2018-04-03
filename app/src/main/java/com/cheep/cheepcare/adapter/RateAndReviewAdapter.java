package com.cheep.cheepcare.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowReviewsByMeBinding;
import com.cheep.model.RateAndReviewModel;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.LoadMoreRecyclerAdapter;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by meet on 27/3/18.
 */

public class RateAndReviewAdapter extends LoadMoreRecyclerAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = "RateAndReviewAdapter";
    List<RateAndReviewModel.ReviewData> mList = new ArrayList<>();
    Boolean isReviewsOfme;


    public RateAndReviewAdapter(List<RateAndReviewModel.ReviewData> rateAndReviewByMeModelList,Boolean reviewsOfme) {
        this.mList = rateAndReviewByMeModelList;
        this.isReviewsOfme = reviewsOfme;
    }

    @Override
    public RecyclerView.ViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
      /*  RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_REVIEW_ADD:
                RowReviewsOfMeAddBinding rowReviewsOfMeAddBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_reviews_by_me, parent, false);
                viewHolder = new RateAndReviewAddViewHolder(rowReviewsOfMeAddBinding);
            case VIEW_TYPE_REVIEW_ADDED:
                RowReviewsOfMeBinding rowReviewsOfMeBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_reviews_of_me, parent, false);
                viewHolder = new RateAndReviewAddedViewHolder(rowReviewsOfMeBinding);
        }
        return viewHolder;*/
        RowReviewsByMeBinding binding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext())
                        , R.layout.row_reviews_by_me
                        , parent
                        , false);
        return new RateAndReviewAddViewHolder(binding);
    }

    @Override
    public void onActualBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final RateAndReviewModel.ReviewData rateAndReviewByMeModel = mList.get(holder.getAdapterPosition());
        if (holder instanceof RateAndReviewAddViewHolder) {
            RateAndReviewAddViewHolder viewHolder = (RateAndReviewAddViewHolder) holder;
            viewHolder.rowReviewsByMeBinding.textReviewedTaskDetail.setText(viewHolder.mView.getContext().getString(R.string.label_rating_and_review_number, rateAndReviewByMeModel.taskCategory, rateAndReviewByMeModel.taskDate));
            viewHolder.rowReviewsByMeBinding.textName.setText(rateAndReviewByMeModel.spUserName);
            GlideUtility.showCircularImageView(viewHolder.mView.getContext(), TAG, viewHolder.rowReviewsByMeBinding.imgProfile, rateAndReviewByMeModel.spProfileImage, R.drawable.icon_profile_img_solid);
            if(!TextUtils.isEmpty(rateAndReviewByMeModel.ratings))
            viewHolder.rowReviewsByMeBinding.ratingBar.setRating(Float.parseFloat(rateAndReviewByMeModel.ratings));
            if(rateAndReviewByMeModel.spFavourite.equalsIgnoreCase(viewHolder.mView.getContext().getString(R.string.label_yes))){
                viewHolder.rowReviewsByMeBinding.imgFav.setSelected(true);
            }
            else {
                viewHolder.rowReviewsByMeBinding.imgFav.setSelected(false);
            }
            if(!rateAndReviewByMeModel.message.equalsIgnoreCase(Utility.EMPTY_STRING)){
                viewHolder.rowReviewsByMeBinding.rlReviewMessage.setVisibility(View.VISIBLE);
                viewHolder.rowReviewsByMeBinding.textAddReview.setVisibility(View.GONE);
                viewHolder.rowReviewsByMeBinding.textReviewMessage.setText(rateAndReviewByMeModel.message);
            }


            if(isReviewsOfme){
                viewHolder.rowReviewsByMeBinding.textYouRated.setText(viewHolder.mView.getContext().getString(R.string.label_pro_rated_you));
                viewHolder.rowReviewsByMeBinding.textYouReviewed.setText(viewHolder.mView.getContext().getString(R.string.label_pro_reviewed_you));
                if(rateAndReviewByMeModel.message.equalsIgnoreCase(Utility.EMPTY_STRING)){
                    viewHolder.rowReviewsByMeBinding.flReview.setVisibility(View.GONE);
                    viewHolder.rowReviewsByMeBinding.separator.setVisibility(View.GONE);
                }
            }
            if(rateAndReviewByMeModel.isVerified.equalsIgnoreCase(viewHolder.mView.getContext().getString(R.string.label_yes))){
                viewHolder.rowReviewsByMeBinding.textVerified.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.rowReviewsByMeBinding.textVerified.setVisibility(View.GONE);
            }




        }
    }


    @Override
    public int onActualItemCount() {
        return mList.size();
    }





    private class RateAndReviewAddViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowReviewsByMeBinding rowReviewsByMeBinding;

        public RateAndReviewAddViewHolder(RowReviewsByMeBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            rowReviewsByMeBinding = binding;
        }
    }

    /*private class RateAndReviewAddedViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowReviewsOfMeBinding reviewsOfMeBinding;

        public RateAndReviewAddedViewHolder(RowReviewsOfMeBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            reviewsOfMeBinding = binding;
        }
    }
*/
    public void setItem(List<RateAndReviewModel.ReviewData> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void addItem(List<RateAndReviewModel.ReviewData> mList) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        if(!mList.isEmpty())
        this.mList.addAll(mList);
        notifyDataSetChanged();
    }

    public List<RateAndReviewModel.ReviewData> getmList() {
        return mList;
    }
}
