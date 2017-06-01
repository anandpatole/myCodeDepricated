package com.cheep.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowReviewsBinding;
import com.cheep.model.ReviewModel;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by pankaj on 9/27/16.
 */

public class ReviewsRecyclerViewAdapter extends RecyclerView.Adapter<ReviewsRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "ReviewsRecyclerViewAdap";
    private ReviewRowInteractionListener listener;
    private Context context;
    private ArrayList<ReviewModel> mList;
    private SuperCalendar superCalendar;

    public ReviewsRecyclerViewAdapter(ReviewRowInteractionListener listener) {
        this.mList = new ArrayList<>();
        this.listener = listener;
    }

    public ReviewsRecyclerViewAdapter(ArrayList<ReviewModel> mList, ReviewRowInteractionListener listener) {
        this.mList = mList;
        this.listener = listener;
    }

    public void setList(ArrayList<ReviewModel> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void addToList(ArrayList<ReviewModel> mList) {
        if (this.mList == null) {
            this.mList = new ArrayList<>();
        }
        this.mList.addAll(mList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        superCalendar = SuperCalendar.getInstance();
        RowReviewsBinding mRowReviewsBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_reviews, parent, false);
        return new ViewHolder(mRowReviewsBinding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final ReviewModel model = mList.get(holder.getAdapterPosition());
        Utility.showCircularImageView(holder.mView.getContext(), TAG, holder.mRowReviewsBinding.imgProfile, model.reviewerProfileImage, Utility.DEFAULT_PROFILE_SRC);
        holder.mRowReviewsBinding.textName.setText(model.reviewerName);

        try {
            superCalendar.setTimeInString(model.reviewDate, Utility.DATE_TIME_FORMAT_SERVICE_YEAR);
            holder.mRowReviewsBinding.textDate.setText(superCalendar.format(Utility.DATE_FORMAT_DD_MMM));
        } catch (ParseException e) {
            e.printStackTrace();
            holder.mRowReviewsBinding.textDate.setText(model.reviewDate);
        }

        holder.mRowReviewsBinding.textMessage.setText(model.reviewerMessage);

        Utility.showRating(model.reviewerRatings, holder.mRowReviewsBinding.ratingBar);

        holder.mRowReviewsBinding.textTotalComments.setText(Utility.getComments(holder.mView.getContext(), model.commentCount));

        /*holder.mRowReviewsBinding.textTotalJobs.setText(context.getString(R.string.label_x_jobs, model.totalJobs + ""));
        holder.mRowReviewsBinding.textTotalReviews.setText(context.getString(R.string.label_x_reviews, model.totalReviews + ""));
        holder.mRowReviewsBinding.textVerified.setText(model.isVerified ? context.getString(R.string.label_verified) : context.getString(R.string.label_pending));
        if (model.isVerified) {
            holder.mRowReviewsBinding.textVerified.setTextColor(ContextCompat.getColor(context, R.color.black));
        } else {
            holder.mRowReviewsBinding.textVerified.setTextColor(ContextCompat.getColor(context, R.color.yellow));
        }
        holder.mRowReviewsBinding.textMinToArrive.setText(model.minToArrive);
        holder.mRowReviewsBinding.imgFav.setSelected(model.isFav);*/


        holder.mRowReviewsBinding.textTotalComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onReviewRowClicked(model, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public ArrayList<ReviewModel> getmList() {
        return mList;
    }

    public void updateCommentCounter(String id, String commentCount) {
        int i = 0;
        for (ReviewModel reviewModel : mList) {
            if (id.equalsIgnoreCase(reviewModel.reviewId)) {
                reviewModel.commentCount = commentCount;
                notifyItemChanged(i);
                break;
            }
            i++;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public RowReviewsBinding mRowReviewsBinding;

        public ViewHolder(RowReviewsBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowReviewsBinding = binding;
        }
    }

    public interface ReviewRowInteractionListener {
        void onReviewRowClicked(ReviewModel ReviewModel, int position);
    }
}
