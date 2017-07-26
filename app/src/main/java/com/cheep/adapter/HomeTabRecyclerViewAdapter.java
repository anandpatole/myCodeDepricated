package com.cheep.adapter;

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
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.cheep.R;
import com.cheep.activity.TaskQuotesActivity;
import com.cheep.databinding.RowTabHomeBinding;
import com.cheep.fragment.HomeTabFragment;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.ProviderModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pankaj on 9/27/16.
 */

public class HomeTabRecyclerViewAdapter extends RecyclerView.Adapter<HomeTabRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "HomeTabRecyclerViewAdap";
    ArrayList<JobCategoryModel> mList;
    HomeTabFragment.CategoryRowInteractionListener mListener;
    Context mContext;
    //saves the index of the upcoming offer to display
    private Map<String, Integer> mOfferIndexMap;

    public HomeTabRecyclerViewAdapter(ArrayList<JobCategoryModel> mList, HomeTabFragment.CategoryRowInteractionListener mListener) {
        this.mList = mList != null ? mList : new ArrayList<JobCategoryModel>();
        this.mListener = mListener;
        mOfferIndexMap = new HashMap<>();

    }

    /**
     * This would going to add list of category model for Home screen view.
     *
     * @param list
     */
    public void addItems(ArrayList<JobCategoryModel> list) {
        this.mList = list;
        notifyDataSetChanged();
//        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        final RowTabHomeBinding mRowTabHomeBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_tab_home, parent, false);

        // TODO: This can be enabled in orderto make the category images in Listing according to 16:9 ratio.
        // TODO: Currently Its not needed so commenting it.
        ViewTreeObserver mViewTreeObserver = mRowTabHomeBinding.relCategoryImage.getViewTreeObserver();
        mViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mRowTabHomeBinding.relCategoryImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // Manage height of Cover Image
                int width = mRowTabHomeBinding.relCategoryImage.getMeasuredWidth();
                ViewGroup.LayoutParams params = mRowTabHomeBinding.relCategoryImage.getLayoutParams();
                params.height = Utility.getHeightCategoryImageBasedOnRatio(width);
                mRowTabHomeBinding.relCategoryImage.setLayoutParams(params);
            }
        });

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
//        holder.mRowTabHomeBinding.textJobsCount.setText(String.valueOf(model.spCount));
        holder.mRowTabHomeBinding.textCategoryName.setText(String.valueOf(model.catName));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onCategoryRowClicked(model, holder.getAdapterPosition());
                }
            }
        });

        //Favourite Icon
        holder.mRowTabHomeBinding.imgCategoryFavourite.setImageResource(model.isFavourite.equalsIgnoreCase(Utility.BOOLEAN.YES)
                ? R.drawable.heart_icon_selected
                : R.drawable.heart_icon_deselected
        );

        // On Click of favourite icon
        holder.mRowTabHomeBinding.imgCategoryFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCategoryFavouriteClicked(model, holder.getAdapterPosition());
                }
            }
        });

        // Start LIVE tracking and Text changes
        final int liveFeedCounter = model.live_lable_arr != null ? model.live_lable_arr.size() : 0;
        if (liveFeedCounter > 0) {
            holder.mRowTabHomeBinding.ivLiveAnimated.setVisibility(View.VISIBLE);
            holder.mRowTabHomeBinding.tvLiveFeed.setVisibility(View.VISIBLE);

            // Live Icon offset
            int offset = holder.mView.getResources().getDimensionPixelSize(R.dimen.scale_5dp);
            final int mLiveIconOffset = holder.mView.getResources().getDimensionPixelSize(R.dimen.icon_live_width) + offset;

            // Start live image animations
            holder.mRowTabHomeBinding.ivLiveAnimated.setBackgroundResource(R.drawable.ic_live);
            ((AnimationDrawable) holder.mRowTabHomeBinding.ivLiveAnimated.getBackground()).start();

            AnimatorSet offerAnimation = loadBannerScrollAnimation(holder.mRowTabHomeBinding.tvLiveFeed, 2000, 100, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    int offerIndex = mOfferIndexMap.containsKey(model.catId) ? mOfferIndexMap.get(model.catId) : 0;
                    SpannableString labelOffer = new SpannableString(model.live_lable_arr.get(offerIndex));
                    labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    holder.mRowTabHomeBinding.tvLiveFeed.setText(labelOffer);
                    offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
                    mOfferIndexMap.put(model.catId, offerIndex);
                }
            });
            offerAnimation.start();
            holder.removeAnimations();
            holder.addAnimator(offerAnimation);
            int offerIndex = mOfferIndexMap.containsKey(model.catId) ? mOfferIndexMap.get(model.catId) : 0;
            SpannableString labelOffer = new SpannableString(model.live_lable_arr.get(offerIndex));
            labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            holder.mRowTabHomeBinding.tvLiveFeed.setText(labelOffer);
            offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
            mOfferIndexMap.put(model.catId, offerIndex);
        } else {
            holder.mRowTabHomeBinding.ivLiveAnimated.setVisibility(View.GONE);
            holder.mRowTabHomeBinding.tvLiveFeed.setVisibility(View.GONE);
        }

        // LIVE Pro stacks
        updateLIVEProStackImages(holder.mRowTabHomeBinding, (ArrayList<String>) model.proImagesPerCategory);
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
    public int getItemCount() {
        return mList.size();
    }


    /**
     * This would invoked when category favourited by User to refresh the contents.
     *
     * @param cat_id
     * @param isFavourite
     * @param mCurrentFilterType
     */
    public void updateOnCategoryFavourited(String cat_id, String isFavourite, String mCurrentFilterType) {
        Log.d(TAG, "updateOnCategoryFavourited() called with: cat_id = [" + cat_id + "], isFavourite = [" + isFavourite + "], mCurrentFilterType = [" + mCurrentFilterType + "]");
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).catId.equalsIgnoreCase(cat_id)) {
                mList.get(i).isFavourite = isFavourite;
                // In Current Filter type is Favourited, we need to remove this from listing.
                if (mCurrentFilterType.equalsIgnoreCase(Utility.FILTER_TYPES.FILTER_TYPE_FAVOURITES)
                        && isFavourite.equalsIgnoreCase(Utility.BOOLEAN.NO)) {
                    mList.remove(i);
                    notifyItemRemoved(i);
                    if (mList.size() <= 0)
                        mListener.onListCategoryListGetsEmpty();
                } else {
                    notifyItemChanged(i);
                }
                break;
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowTabHomeBinding mRowTabHomeBinding;
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

        public ViewHolder(RowTabHomeBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowTabHomeBinding = binding;
            animators = new ArrayList<>();
        }
    }

    public ArrayList<JobCategoryModel> getmList() {
        return mList;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Reload SP Listing based on Availability [START]///////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This method would going to update the SP list of images
     *
     * @param list of Providers available for particular category
     */
    private void updateLIVEProStackImages(RowTabHomeBinding mRowTabHomeBinding, ArrayList<String> list) {

        for (int i = 0; i < 5; i++) {
            switch (i) {
                case 0:
                    if (list.size() > 0 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mRowTabHomeBinding.img1, list.get(i), R.drawable.ic_cheep_circular_icon, true, 0.5f);
                        mRowTabHomeBinding.img1.setVisibility(View.VISIBLE);
                    } else {
                        mRowTabHomeBinding.img1.setVisibility(View.GONE);
                    }
                    break;
                case 1:
                    if (list.size() > 1 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mRowTabHomeBinding.img2, list.get(i), R.drawable.ic_cheep_circular_icon, true, 0.5f);
                        mRowTabHomeBinding.img2.setVisibility(View.VISIBLE);
                    } else {
                        mRowTabHomeBinding.img2.setVisibility(View.GONE);
                    }
                    break;
                case 2:
                    if (list.size() > 2 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mRowTabHomeBinding.img3, list.get(i), R.drawable.ic_cheep_circular_icon, true, 0.5f);
                        mRowTabHomeBinding.img3.setVisibility(View.VISIBLE);
                    } else {
                        mRowTabHomeBinding.img3.setVisibility(View.GONE);
                    }
                    break;
                /*case 3:
                    if (list.size() > 3 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mRowTabHomeBinding.img4, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mRowTabHomeBinding.img4.setVisibility(View.VISIBLE);
                    } else {
                        mRowTabHomeBinding.img4.setVisibility(View.GONE);
                    }
                    break;
                case 4:
                    if (list.size() > 4 && list.get(i) != null) {
                        Utility.showCircularImageView(mContext, TAG, mActivityTaskSummaryBinding.img5, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mRowTabHomeBinding.img5.setVisibility(View.VISIBLE);
                    } else {
                        mRowTabHomeBinding.img5.setVisibility(View.GONE);
                    }
                    break;*/
            }
        }

        // Check if list size is more than 3
        if (list.size() > 3) {
            int extra_count = list.size() - 3;
            // Set size of textview based on number of digits, to make sure it looks proper in
            // In all devices(specifically for hdpi) devices.
            int digit_length = String.valueOf(extra_count).length();
            if (digit_length == 1) {
                mRowTabHomeBinding.extraProCount.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        mContext.getResources().getDimension(R.dimen.home_screen_extra_category_count_text_size_one_digits));
            } else if (digit_length == 2) {
                mRowTabHomeBinding.extraProCount.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        mContext.getResources().getDimension(R.dimen.home_screen_extra_category_count_text_size_two_digits));
            } else {
                mRowTabHomeBinding.extraProCount.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        mContext.getResources().getDimension(R.dimen.home_screen_extra_category_count_text_size_three_digits));
            }
            
            mRowTabHomeBinding.extraProCount.setVisibility(View.VISIBLE);
            mRowTabHomeBinding.extraProCount.setText("+" + String.valueOf(extra_count));
        } else {
            mRowTabHomeBinding.extraProCount.setVisibility(View.GONE);
        }

        // Awaiting Response
        if (list.size() == 0) {
            mRowTabHomeBinding.lnProsAvailable.setVisibility(View.VISIBLE);
            // Set margin as Zero
            mRowTabHomeBinding.textTaskResponseStatus.setPadding((int) Utility.convertDpToPixel(0, mContext), 0, 0, 0);
            mRowTabHomeBinding.textTaskResponseStatus.setText(mRowTabHomeBinding.lnProsAvailable.getResources().getString(R.string.no_pros_in_this_category));
        } else {
            // Set margin as Zero
            mRowTabHomeBinding.textTaskResponseStatus.setPadding((int) Utility.convertDpToPixel(5, mContext), 0, 0, 0);
            mRowTabHomeBinding.lnProsAvailable.setVisibility(View.VISIBLE);
            mRowTabHomeBinding.textTaskResponseStatus.setText(mRowTabHomeBinding.lnProsAvailable.getResources().getString(R.string.label_pros_available));
        }

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Reload SP Listing based on AddressID [END]//////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
}
