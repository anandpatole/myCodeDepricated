package com.cheep.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.LeadingMarginSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowFavouriteBinding;
import com.cheep.model.ProviderModel;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by pankaj on 9/27/16.
 */

public class FavouriteRecyclerViewAdapter extends RecyclerView.Adapter<FavouriteRecyclerViewAdapter.ViewHolder> {

    private FavouriteRowInteractionListener listener;
    Context context;

    ArrayList<ProviderModel> mList;

    public FavouriteRecyclerViewAdapter(FavouriteRowInteractionListener listener) {
        this.mList = new ArrayList<>();
        this.listener = listener;
    }

    public FavouriteRecyclerViewAdapter(ArrayList<ProviderModel> mList, FavouriteRowInteractionListener listener) {
        this.mList = mList;
        this.listener = listener;
    }

    public void setItem(ArrayList<ProviderModel> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void addItem(ArrayList<ProviderModel> mList) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        this.mList.addAll(mList);
        notifyDataSetChanged();
    }

    public void updateFavStatus(String id, String isFav) {
        if (mList != null) {
            for (ProviderModel model : mList) {
                if (model.providerId.equalsIgnoreCase(id)) {
                    model.isFavourite = isFav;
                    mList.remove(model);
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        RowFavouriteBinding mRowTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_favourite, parent, false);
        return new ViewHolder(mRowTaskBinding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final ProviderModel model = mList.get(holder.getAdapterPosition());
        GlideUtility.showCircularImageView(holder.mRowFavouriteBinding.imgProfile.getContext(), TAG, holder.mRowFavouriteBinding.imgProfile, model.profileUrl,Utility.DEFAULT_CHEEP_LOGO);
        holder.mRowFavouriteBinding.textName.setText(model.userName);
        //holder.mRowFavouriteBinding.textTotalReviews.setText(context.getString(R.string.label_x_reviews, model.reviews));
        holder.mRowFavouriteBinding.textVerified.setText(Utility.BOOLEAN.YES.equalsIgnoreCase(model.isVerified) ? context.getString(R.string.label_verified) : context.getString(R.string.label_pending));
        final int liveFeedCounter = model.offerList != null ? model.offerList.size() : 0;

       // final int liveFeedCounter= 3;
        final Map<String, Integer> mOfferIndexMap = new HashMap<>();
        if (liveFeedCounter > 0) {
            holder.mRowFavouriteBinding.ivLiveAnimated.setVisibility(View.VISIBLE);
            holder.mRowFavouriteBinding.tvLiveFeed.setVisibility(View.VISIBLE);

            // Live Icon offset
            int offset = holder.mView.getResources().getDimensionPixelSize(R.dimen.scale_5dp);
            final int mLiveIconOffset = holder.mView.getResources().getDimensionPixelSize(R.dimen.icon_live_width) + offset;

            // Start live image animations
            holder.mRowFavouriteBinding.ivLiveAnimated.setBackgroundResource(R.drawable.ic_live);
            ((AnimationDrawable) holder.mRowFavouriteBinding.ivLiveAnimated.getBackground()).start();

            AnimatorSet offerAnimation = loadBannerScrollAnimation(holder.mRowFavouriteBinding.tvLiveFeed, 2000, 100, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    int offerIndex = mOfferIndexMap.containsKey(model.providerId) ? mOfferIndexMap.get(model.providerId) : 0;
                    SpannableString labelOffer = new SpannableString(model.offerList.get(offerIndex));
                    labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    holder.mRowFavouriteBinding.tvLiveFeed.setText(labelOffer);
                    offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
                    mOfferIndexMap.put(model.providerId, offerIndex);
                }
            });
            offerAnimation.start();

        }
        else
        {
            holder.mRowFavouriteBinding.ivLiveAnimated.setVisibility(View.GONE);
            holder.mRowFavouriteBinding.tvLiveFeed.setVisibility(View.GONE);
        }
        int bagResId = Utility.getProLevelBadge(model.pro_level);
        if (bagResId != -1)
            holder.mRowFavouriteBinding.imgBadge.setImageResource(bagResId);
        String temp="";
        for(int i=0;i<model.categories.size();i++)
        {
            if(i==0)
            {
                temp= temp +model.categories.get(i);
            }
            else
            {
                temp = temp+ model.categories.get(i)+" , ";
            }
        }

        holder.mRowFavouriteBinding.categoryName.setText(temp);
        // holder.mRowFavouriteBinding.textTotalJobs.setText(Utility.getJobs(context, model.jobsCount));
        holder.mRowFavouriteBinding.textTotalTasks.setText(Utility.getTasks(context, model.taskCount));

        if (Utility.BOOLEAN.YES.equalsIgnoreCase(model.isVerified)) {
            //holder.mRowFavouriteBinding.textVerified.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.mRowFavouriteBinding.textVerified.setVisibility(View.VISIBLE);
        } else {
            //holder.mRowFavouriteBinding.textVerified.setTextColor(ContextCompat.getColor(context, R.color.yellow));
            holder.mRowFavouriteBinding.textVerified.setVisibility(View.INVISIBLE);
        }
holder.mRowFavouriteBinding.textAddressKmAway.setText(model.sp_locality);
        holder.mRowFavouriteBinding.textAddressKmAway1.setText(model.distance);
        holder.mRowFavouriteBinding.textMinToArrive.setText(model.distance);
        holder.mRowFavouriteBinding.imgFav.setSelected(Utility.BOOLEAN.YES.equalsIgnoreCase(model.isFavourite));

        holder.mRowFavouriteBinding.imgFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.mRowFavouriteBinding.imgFav.setSelected(!holder.mRowFavouriteBinding.imgFav.isSelected());
                model.isFavourite = holder.mRowFavouriteBinding.imgFav.isSelected() ? Utility.BOOLEAN.YES : Utility.BOOLEAN.NO;

                if (listener != null) {
                    int position = holder.getAdapterPosition();
                    if (holder.mRowFavouriteBinding.imgFav.isSelected() == false) {
                        mList.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                    }
                    listener.onFavClicked(model, holder.mRowFavouriteBinding.imgFav.isSelected(), position);
                }
            }
        });
        if (TextUtils.isEmpty(model.experience)
                || Utility.ZERO_STRING.equals(model.experience)) {
            holder.mRowFavouriteBinding.labelExperience.setText(Utility.checkNonNullAndSet(holder.mRowFavouriteBinding.labelExperience.getContext().getString(R.string.label_experience_zero)));
        } else {
//            holder.tvExperience.setText(holder.mView.getContext().getResources().getQuantityString(R.plurals.getExperienceString, Integer.parseInt(provider.experience), provider.experience));
            holder.mRowFavouriteBinding.labelExperience.setText(Utility.getExperienceString(model.experience, "\n"));
        }
        Utility.showRating(model.experience, holder.mRowFavouriteBinding.ratingBar);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onFavouriteRowClicked(model, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
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
    public ArrayList<ProviderModel> getmList() {
        return mList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public RowFavouriteBinding mRowFavouriteBinding;

        public ViewHolder(RowFavouriteBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowFavouriteBinding = binding;
        }
    }

    public interface FavouriteRowInteractionListener {
        void onFavouriteRowClicked(ProviderModel providerModel, int position);

        void onFavClicked(ProviderModel providerModel, boolean isAddToFav, int position);
    }
}
