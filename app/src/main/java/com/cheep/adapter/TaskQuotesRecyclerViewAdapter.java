package com.cheep.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anurag on 06-06-2017.
 */

public class TaskQuotesRecyclerViewAdapter extends RecyclerView.Adapter<TaskQuotesRecyclerViewAdapter.QuoteViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private int mLiveIconOffset, mHomeIconOffset;

    public TaskQuotesRecyclerViewAdapter(Context context) {
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        int offset = context.getResources().getDimensionPixelSize(R.dimen.scale_4dp);
        mLiveIconOffset = context.getResources().getDimensionPixelSize(R.dimen.icon_live_width) + offset;
        mHomeIconOffset = context.getResources().getDimensionPixelSize(R.dimen.icon_home_width) + offset;
    }

    @Override
    public QuoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new QuoteViewHolder(mLayoutInflater.inflate(R.layout.row_task_quote, parent, false));
    }

    @Override
    public void onBindViewHolder(final QuoteViewHolder holder, int position) {
        holder.itemView.setBackgroundResource(position == 0 ? R.color.cheepest_bg_color : (position == 1 ? R.color.highest_rated_bg_color : R.color.white));
        holder.tvDiscount.setSelected(true);

        SpannableString labelOffer = new SpannableString("17 people trolled by Anurag Kulkarni today");
        labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        holder.tvOffer.setText(labelOffer);

        SpannableString labelHappyHomes = new SpannableString("60 Homes Made Happy So Far");
        labelHappyHomes.setSpan(new LeadingMarginSpan.Standard(mHomeIconOffset, 0), 0, labelHappyHomes.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        holder.tvHappyHomes.setText(labelHappyHomes);

        holder.ivLiveAnimated.setBackgroundResource(R.drawable.ic_live);
        ((AnimationDrawable) holder.ivLiveAnimated.getBackground()).start();

        holder.ivHomeAnimated.setBackgroundResource(R.drawable.ic_home);
        ((AnimationDrawable) holder.ivHomeAnimated.getBackground()).start();

//        Glide.with(mContext).load(R.drawable.ic_live_animated).asGif().dontAnimate().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.ivLiveAnimated);
//        Glide.with(mContext).load(R.drawable.ic_home_animated).asGif().dontAnimate().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.ivHomeAnimated);
        Glide.with(mContext).load(R.drawable.ic_chat_animated).asGif().dontAnimate().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.ivChat);

        holder.removeAnimations();

        holder.tvDiscount.setTag("time_distance");
        AnimatorSet distanceAnimation = loadBannerScrollAnimation(holder.tvDistance, 10000, 60, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (((String) holder.tvDiscount.getTag()).equals("distance")) {
                    holder.tvDiscount.setTag("time_distance");
                    holder.tvDistance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_time_distance, 0, 0, 0);
                    holder.tvDistance.setText("20 mins away");
                } else {
                    holder.tvDiscount.setTag("distance");
                    holder.tvDistance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_relative_distance, 0, 0, 0);
                    holder.tvDistance.setText("2.7 kms away");
                }
            }
        });
        distanceAnimation.start();
        holder.addAnimator(distanceAnimation);

        AnimatorSet offerAnimation = loadBannerScrollAnimation(holder.tvOffer, 2000, 100, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        offerAnimation.start();
        holder.addAnimator(offerAnimation);
    }

    private AnimatorSet loadBannerScrollAnimation(View view, int offset, int distance, AnimatorListenerAdapter midEndListener) {
        ObjectAnimator moveOut = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0, (-1 * distance));
        if (midEndListener != null) {
            moveOut.addListener(midEndListener);
        }

        ObjectAnimator moveIn = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, distance, 0);
        final AnimatorSet set = new AnimatorSet();
        set.setDuration(1000);
        set.setStartDelay(offset);
        set.playSequentially(moveOut, moveIn);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                set.start();
            }
        });
        return set;
    }

    @Override
    public void onViewDetachedFromWindow(QuoteViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

//        holder.removeAnimations();
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    static class QuoteViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvLocation;
        private TextView tvDistance;
        private TextView tvDescription;
        private TextView tvOffer;
        private TextView tvHappyHomes;
        private TextView tvDiscount;
        private TextView tvPrice;
        private TextView tvExperience;
        private ImageView ivBadge;
        private ImageView ivAvatar;
        private ImageView ivChat;
        private ImageView ivLiveAnimated;
        private ImageView ivHomeAnimated;
        private ImageView ivFavoriteQuote;
        private RatingBar ratingBar;

        private List<AnimatorSet> animators;

        public void addAnimator(AnimatorSet animator) {
            if (animator != null) {
                animators.add(animator);
            }
        }

        public void removeAnimations() {
            for (AnimatorSet animatorSet : animators) {
                for (Animator child : animatorSet.getChildAnimations()) {
                    child.removeAllListeners();
                }
                animatorSet.removeAllListeners();
                animatorSet.end();
                animatorSet.cancel();
            }
            animators.clear();
        }

        public QuoteViewHolder(View itemView) {
            super(itemView);
            initViews(itemView);
            animators = new ArrayList<>();
        }

        private void initViews(View itemView) {
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvLocation = (TextView) itemView.findViewById(R.id.tvLocation);
            tvDistance = (TextView) itemView.findViewById(R.id.tvDistance);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
            tvOffer = (TextView) itemView.findViewById(R.id.tvOffer);
            tvHappyHomes = (TextView) itemView.findViewById(R.id.tvHappyHomes);
            tvDiscount = (TextView) itemView.findViewById(R.id.tvDiscount);
            tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
            tvExperience = (TextView) itemView.findViewById(R.id.tvExperience);
            ivBadge = (ImageView) itemView.findViewById(R.id.ivBadge);
            ivAvatar = (ImageView) itemView.findViewById(R.id.ivAvatar);
            ivChat = (ImageView) itemView.findViewById(R.id.ivChat);
            ivLiveAnimated = (ImageView) itemView.findViewById(R.id.ivLiveAnimated);
            ivHomeAnimated = (ImageView) itemView.findViewById(R.id.ivHomeAnimated);
            ivFavoriteQuote = (ImageView) itemView.findViewById(R.id.ivFavoriteQuote);
            ratingBar = (RatingBar) itemView.findViewById(R.id.rating_bar);
        }
    }
}
