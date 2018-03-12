package com.cheep.cheepcare.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.databinding.RowManageSubscriptionAddPackagesBinding;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pankaj on 1/10/18.
 */

public class ManageSubscriptionAddPackageAdapter extends
        RecyclerView.Adapter<ManageSubscriptionAddPackageAdapter.AddPackageViewHolder> {

    private final List<PackageDetail> mList;
    private final AddPackageInteractionListener mListener;

    public interface AddPackageInteractionListener {
        void onPackageItemClick(PackageDetail model);
    }

    public ManageSubscriptionAddPackageAdapter(AddPackageInteractionListener listener, List<PackageDetail> list) {
        mListener = listener;
        mList = list;
    }

    @Override
    public AddPackageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowManageSubscriptionAddPackagesBinding binding =
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.getContext())
                        , R.layout.row_manage_subscription_add_packages
                        , parent
                        , false
                );
        return new AddPackageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final AddPackageViewHolder holder, int position) {
        final PackageDetail model = mList.get(holder.getAdapterPosition());
        Context context = holder.mBinding.getRoot().getContext();

        GlideUtility.loadImageView(context, holder.mBinding.ivItemBackground
                , model.packageImage, R.drawable.gradient_black);

        holder.mBinding.tvTitle.setText(model.title);
        holder.mBinding.tvDescription.setText(model.subtitle);

        holder.mBinding.tvPrice.setText(Utility.getCheepCarePackageMonthlyPrice(context, R.string.rupee_symbol_x_package_price
                , model.price));

        // Start LIVE tracking and Text changes
        final int liveFeedCounter = model.live_lable_arr != null ? model.live_lable_arr.size() : 0;
        final Map<String, Integer> mOfferIndexMap = new HashMap<>();
        if (liveFeedCounter > 0) {
            holder.mBinding.ivLiveAnimated.setVisibility(View.VISIBLE);
            holder.mBinding.tvLiveFeed.setVisibility(View.VISIBLE);

            // Live Icon offset
            int offset = holder.mBinding.getRoot().getResources().getDimensionPixelSize(R.dimen.scale_5dp);
            final int mLiveIconOffset = holder.mBinding.getRoot().getResources().getDimensionPixelSize(R.dimen.icon_live_width) + offset;

            // Start live image animations
            holder.mBinding.ivLiveAnimated.setBackgroundResource(R.drawable.ic_live);
            ((AnimationDrawable) holder.mBinding.ivLiveAnimated.getBackground()).start();

            AnimatorSet offerAnimation = loadBannerScrollAnimation(holder.mBinding.tvLiveFeed, 2000, 100, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    int offerIndex = mOfferIndexMap.containsKey(model.id) ? mOfferIndexMap.get(model.id) : 0;
                    SpannableString labelOffer = new SpannableString(model.live_lable_arr.get(offerIndex));
                    labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    holder.mBinding.tvLiveFeed.setText(labelOffer);
                    offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
                    mOfferIndexMap.put(model.id, offerIndex);
                }
            });
            offerAnimation.start();
            removeAnimations(holder.animators);
            addAnimator(holder.animators, offerAnimation);
            int offerIndex = mOfferIndexMap.containsKey(model.id) ? mOfferIndexMap.get(model.id) : 0;
            SpannableString labelOffer = new SpannableString(model.live_lable_arr.get(offerIndex));
            labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            holder.mBinding.tvLiveFeed.setText(labelOffer);
            offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
            mOfferIndexMap.put(model.id, offerIndex);
        } else {
            holder.mBinding.ivLiveAnimated.setVisibility(View.GONE);
            holder.mBinding.tvLiveFeed.setVisibility(View.GONE);
        }
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

    public void addAnimator(List<AnimatorSet> animators, AnimatorSet animator) {
        if (animator != null) {
            animators.add(animator);
        }
    }

    public void removeAnimations(List<AnimatorSet> animators) {
        if (animators != null) {
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
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class AddPackageViewHolder extends RecyclerView.ViewHolder {

        public RowManageSubscriptionAddPackagesBinding mBinding;
        List<AnimatorSet> animators = new ArrayList<>();

        public AddPackageViewHolder(RowManageSubscriptionAddPackagesBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onPackageItemClick(mList.get(getAdapterPosition()));
                }
            });
        }
    }
}
