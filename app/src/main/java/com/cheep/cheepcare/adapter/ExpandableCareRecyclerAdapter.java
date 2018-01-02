package com.cheep.cheepcare.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcare.model.CheepCarePackageModel;
import com.cheep.custom_view.expandablerecycleview.ChildViewHolder;
import com.cheep.custom_view.expandablerecycleview.ExpandableRecyclerAdapter;
import com.cheep.custom_view.expandablerecycleview.ParentViewHolder;
import com.cheep.databinding.RowPackageCareItemBinding;
import com.cheep.databinding.RowPackageCareSubItemBinding;
import com.cheep.model.JobCategoryModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pankaj on 1/1/18.
 */

public class ExpandableCareRecyclerAdapter extends ExpandableRecyclerAdapter<CheepCarePackageModel
        , JobCategoryModel
        , ExpandableCareRecyclerAdapter.ParentCategoryViewHolder
        , ExpandableCareRecyclerAdapter.ChildCategoryViewHolder> {

    private final boolean isSingleSelection;
    private final List<CheepCarePackageModel> mList;
    private static final String TAG = ExpandableCareRecyclerAdapter.class.getSimpleName();

    /**
     * Primary constructor. Sets up {@link #mParentList} and {@link #mFlatItemList}.
     * <p>
     * Any changes to {@link #mParentList} should be made on the original instance, and notified via
     * {@link #notifyParentInserted(int)}
     * {@link #notifyParentRemoved(int)}
     * {@link #notifyParentChanged(int)}
     * {@link #notifyParentRangeInserted(int, int)}
     * {@link #notifyChildInserted(int, int)}
     * {@link #notifyChildRemoved(int, int)}
     * {@link #notifyChildChanged(int, int)}
     * methods and not the notify methods of RecyclerView.Adapter.
     *
     * @param parentList List of all parents to be displayed in the RecyclerView that this
     *                   adapter is linked to
     */
    public ExpandableCareRecyclerAdapter(@NonNull List<CheepCarePackageModel> parentList
            , boolean isSingleSelection) {
        super(parentList);

        mList = parentList;
        this.isSingleSelection = isSingleSelection;
    }

    @NonNull
    @Override
    public ExpandableCareRecyclerAdapter.ParentCategoryViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        RowPackageCareItemBinding binding =
                DataBindingUtil.inflate(LayoutInflater.from(parentViewGroup.getContext())
                        , R.layout.row_package_care_item
                        , parentViewGroup
                        , false);
        return new ParentCategoryViewHolder(binding);
    }

    @NonNull
    @Override
    public ExpandableCareRecyclerAdapter.ChildCategoryViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        RowPackageCareSubItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(childViewGroup.getContext())
                , R.layout.row_package_care_sub_item
                , childViewGroup
                , false);
        return new ChildCategoryViewHolder(binding);
    }

    @Override
    public void onBindParentViewHolder(@NonNull ExpandableCareRecyclerAdapter.ParentCategoryViewHolder parentViewHolder, int parentPosition, @NonNull CheepCarePackageModel parent) {
        parentViewHolder.bind(parent);
    }

    @Override
    public void onBindChildViewHolder(@NonNull ExpandableCareRecyclerAdapter.ChildCategoryViewHolder childViewHolder, int parentPosition, int childPosition, @NonNull JobCategoryModel child) {
        childViewHolder.bind(child, childViewHolder.mBinding.getRoot().getContext());
    }

    /**
     * View Holder for Parent row
     */
    class ParentCategoryViewHolder extends ParentViewHolder {

        RowPackageCareItemBinding mBinding;

        ParentCategoryViewHolder(@NonNull RowPackageCareItemBinding binding) {
            // init views
            super(binding.getRoot());
            mBinding = binding;


            //     on click of check box parent row
            // select/deselect all child row of parent if parent is select/deselect
            /*mBinding.imgIconCorrect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!isSingleSelection) {
                        CheepCarePackageModel parent = mList.get(getParentAdapterPosition());
                        parent.isSelected = !parent.isSelected;
                        setAllChildSelected(parent);
                        notifyDataSetChanged();
                    }
                }
            });*/
        }

        // bind data with view parent row
        // bind data with view parent row
        public void bind(@NonNull CheepCarePackageModel model) {
            mBinding.tvCareName.setText(model.packageTitle);
            mBinding.tvDescription.setText(model.packageDescription);
            if (model.daysLeft != null /*&& model.daysLeft.length() != 0*/) {
                switch (model.daysLeft.length()) {
                    case 0:
                        mBinding.tvDaysLeft1.setVisibility(View.GONE);
                        mBinding.tvDaysLeft2.setVisibility(View.GONE);
                        mBinding.tvDaysLeft3.setVisibility(View.GONE);
                        mBinding.tvLeftDays.setVisibility(View.GONE);
                        break;
                    case 1:
                        mBinding.tvDaysLeft1.setVisibility(View.VISIBLE);
                        mBinding.tvDaysLeft2.setVisibility(View.VISIBLE);
                        mBinding.tvDaysLeft3.setVisibility(View.VISIBLE);

                        mBinding.tvDaysLeft1.setText("0");
                        mBinding.tvDaysLeft2.setText("0");
                        mBinding.tvDaysLeft3.setText(model.daysLeft);
                        break;
                    case 2:
                        mBinding.tvDaysLeft1.setVisibility(View.VISIBLE);
                        mBinding.tvDaysLeft2.setVisibility(View.VISIBLE);
                        mBinding.tvDaysLeft3.setVisibility(View.VISIBLE);

                        mBinding.tvDaysLeft1.setText("0");
                        mBinding.tvDaysLeft2.setText(model.daysLeft.subSequence(0, 1));
                        mBinding.tvDaysLeft3.setText(model.daysLeft.subSequence(1, 2));
                        break;
                    case 3:
                        mBinding.tvDaysLeft1.setVisibility(View.VISIBLE);
                        mBinding.tvDaysLeft2.setVisibility(View.VISIBLE);
                        mBinding.tvDaysLeft3.setVisibility(View.VISIBLE);

                        mBinding.tvDaysLeft1.setText(model.daysLeft.subSequence(0, 1));
                        mBinding.tvDaysLeft2.setText(model.daysLeft.subSequence(1, 2));
                        mBinding.tvDaysLeft3.setText(model.daysLeft.subSequence(2, 3));
                        break;
                }
            } else {
                mBinding.tvDaysLeft1.setVisibility(View.GONE);
                mBinding.tvDaysLeft2.setVisibility(View.GONE);
                mBinding.tvDaysLeft3.setVisibility(View.GONE);
                mBinding.tvLeftDays.setVisibility(View.GONE);
            }
        }
    }

    /**
     * View Holder for Child row
     */
    class ChildCategoryViewHolder extends ChildViewHolder {

        RowPackageCareSubItemBinding mBinding;
        private List<AnimatorSet> animators;

        ChildCategoryViewHolder(@NonNull RowPackageCareSubItemBinding binding) {
            super(binding.getRoot());
            // init views
            mBinding = binding;
            animators = new ArrayList<>();
            // on click of check box
            /*mBinding.lnRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // get click child row and its parent
                    int parentPos = getParentAdapterPosition();
                    int childPos = getChildAdapterPosition();

                    // if single child is selected then parent also should be selected
                    // if all children are deselected then parent should be deselected

                    if (!isSingleSelection) {
                        JobCategoryModel subService = mList.get(parentPos).subItems.get(childPos);
                        subService.isSelected = !subService.isSelected;
                        if (subService.isSelected)
                            mList.get(parentPos).isSelected = true;
                        else {
                            int flag = 0;
                            for (CheepCarePackageModel subServiceEach : mList.get(parentPos).subItems) {
                                if (!subServiceEach.isSelected)
                                    flag++;
                            }
                            if (flag == mList.get(parentPos).subItems.size())
                                mList.get(parentPos).isSelected = false;
                        }
                        notifyDataSetChanged();
                    } else {
                        for (int i = 0; i < mList.size(); i++) {

                            for (int j = 0; j < mList.get(i).subItems.size(); j++) {

                                mList.get(i).subItems.get(j).isSelected = (i == parentPos && j == childPos);
                                mList.get(i).isSelected = i == parentPos;
                            }
                        }
                        notifyDataSetChanged();

                    }

                }
            });*/
        }

        // bind data with view for child row
        public void bind(@NonNull final JobCategoryModel model, Context context) {
            //Background image
            Utility.loadImageView(context, mBinding.imgCategoryBackground, model.catImage, R.drawable.gradient_black);
            Utility.loadImageView(context, mBinding.imgCategoryBackground, model.catImageExtras.thumb, R.drawable.gradient_black);

            //Category Icon
            Utility.loadImageView(context, mBinding.imgCategoryIcon, model.catIcon, 0);
            mBinding.imgCategoryIcon.setVisibility(View.GONE);
//        mBinding.textJobsCount.setText(String.valueOf(model.spCount));
            mBinding.textCategoryName.setText(String.valueOf(model.catName));

            // Start LIVE tracking and Text changes
            final int liveFeedCounter = model.live_lable_arr != null ? model.live_lable_arr.size() : 0;
            final Map<String, Integer> mOfferIndexMap = new HashMap<>();
            if (liveFeedCounter > 0) {
                mBinding.ivLiveAnimated.setVisibility(View.VISIBLE);
                mBinding.tvLiveFeed.setVisibility(View.VISIBLE);

                // Live Icon offset
                int offset = mBinding.getRoot().getResources().getDimensionPixelSize(R.dimen.scale_5dp);
                final int mLiveIconOffset = mBinding.getRoot().getResources().getDimensionPixelSize(R.dimen.icon_live_width) + offset;

                // Start live image animations
                mBinding.ivLiveAnimated.setBackgroundResource(R.drawable.ic_live);
                ((AnimationDrawable) mBinding.ivLiveAnimated.getBackground()).start();

                AnimatorSet offerAnimation = loadBannerScrollAnimation(mBinding.tvLiveFeed, 2000, 100, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        int offerIndex = mOfferIndexMap.containsKey(model.catId) ? mOfferIndexMap.get(model.catId) : 0;
                        SpannableString labelOffer = new SpannableString(model.live_lable_arr.get(offerIndex));
                        labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        mBinding.tvLiveFeed.setText(labelOffer);
                        offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
                        mOfferIndexMap.put(model.catId, offerIndex);
                    }
                });
                offerAnimation.start();
                removeAnimations();
                addAnimator(offerAnimation);
                int offerIndex = mOfferIndexMap.containsKey(model.catId) ? mOfferIndexMap.get(model.catId) : 0;
                SpannableString labelOffer = new SpannableString(model.live_lable_arr.get(offerIndex));
                labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                mBinding.tvLiveFeed.setText(labelOffer);
                offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
                mOfferIndexMap.put(model.catId, offerIndex);
            } else {
                mBinding.ivLiveAnimated.setVisibility(View.GONE);
                mBinding.tvLiveFeed.setVisibility(View.GONE);
            }

            // LIVE Pro stacks
            updateLIVEProStackImages(mBinding, (ArrayList<String>) model.proImagesPerCategory, context);
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

        ///////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////Reload SP Listing based on Availability [START]///////////
        ///////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * This method would going to update the SP list of images
         *
         * @param list of Providers available for particular category
         */
        private void updateLIVEProStackImages(RowPackageCareSubItemBinding mBinding, ArrayList<String> list, Context context) {

            for (int i = 0; i < 5; i++) {
                switch (i) {
                    case 0:
                        if (list.size() > 0 && list.get(i) != null) {
                            Utility.showCircularImageView(context, TAG, mBinding.img1, list.get(i), R.drawable.ic_cheep_circular_icon, true, 0.5f);
                            mBinding.img1.setVisibility(View.VISIBLE);
                        } else {
                            mBinding.img1.setVisibility(View.GONE);
                        }
                        break;
                    case 1:
                        if (list.size() > 1 && list.get(i) != null) {
                            Utility.showCircularImageView(context, TAG, mBinding.img2, list.get(i), R.drawable.ic_cheep_circular_icon, true, 0.5f);
                            mBinding.img2.setVisibility(View.VISIBLE);
                        } else {
                            mBinding.img2.setVisibility(View.GONE);
                        }
                        break;
                    case 2:
                        if (list.size() > 2 && list.get(i) != null) {
                            Utility.showCircularImageView(context, TAG, mBinding.img3, list.get(i), R.drawable.ic_cheep_circular_icon, true, 0.5f);
                            mBinding.img3.setVisibility(View.VISIBLE);
                        } else {
                            mBinding.img3.setVisibility(View.GONE);
                        }
                        break;
                /*case 3:
                    if (list.size() > 3 && list.get(i) != null) {
                        Utility.showCircularImageView(context, TAG, mBinding.img4, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mBinding.img4.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.img4.setVisibility(View.GONE);
                    }
                    break;
                case 4:
                    if (list.size() > 4 && list.get(i) != null) {
                        Utility.showCircularImageView(context, TAG, mActivityTaskSummaryBinding.img5, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mBinding.img5.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.img5.setVisibility(View.GONE);
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
                switch (digit_length) {
                    case 1:
                        mBinding.extraProCount.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX,
                                context.getResources().getDimension(R.dimen.home_screen_extra_category_count_text_size_one_digits));
                        break;
                    case 2:
                        mBinding.extraProCount.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX,
                                context.getResources().getDimension(R.dimen.home_screen_extra_category_count_text_size_two_digits));
                        break;
                    default:
                        mBinding.extraProCount.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX,
                                context.getResources().getDimension(R.dimen.home_screen_extra_category_count_text_size_three_digits));
                        break;
                }

                mBinding.extraProCount.setVisibility(View.VISIBLE);
                mBinding.extraProCount.setText("+" + String.valueOf(extra_count));
            } else {
                mBinding.extraProCount.setVisibility(View.GONE);
            }

            // Awaiting Response
            if (list.size() == 0) {
                mBinding.lnProsAvailable.setVisibility(View.VISIBLE);
                // Set margin as Zero
                mBinding.textTaskResponseStatus.setPadding((int) Utility.convertDpToPixel(0, context), 0, 0, 0);
                mBinding.textTaskResponseStatus.setText(mBinding.lnProsAvailable.getResources().getString(R.string.no_pros_in_this_category));
            } else {
                // Set margin as Zero
                mBinding.textTaskResponseStatus.setPadding((int) Utility.convertDpToPixel(5, context), 0, 0, 0);
                mBinding.lnProsAvailable.setVisibility(View.VISIBLE);
                mBinding.textTaskResponseStatus.setText(mBinding.lnProsAvailable.getResources().getString(R.string.label_pros_available));
            }

        }


        ///////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////Reload SP Listing based on AddressID [END]//////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////

    }
}