package com.cheep.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.custom_view.CFTextViewRegular;
import com.cheep.custom_view.TypeFaceProvider;
import com.cheep.model.ProviderModel;
import com.cheep.utils.CustomTypefaceSpan;
import com.cheep.utils.RoundedBackgroundSpan;
import com.cheep.utils.Utility;
import com.daimajia.swipe.SwipeLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.facebook.login.widget.ProfilePictureView.TAG;

/**
 * Created by Anurag on 06-06-2017.
 */

public class TaskQuotesRecyclerViewAdapter extends RecyclerView.Adapter<TaskQuotesRecyclerViewAdapter.QuoteViewHolder> {
    private static final int TIME = 1;
    private static final int DISTANCE = 2;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private OnInteractionListener mListener;

    private int mLiveIconOffset, mHomeIconOffset;
    private int mTagBackgroundColor, mTagTextColor;
    private List<ProviderModel> mQuotesList;
    private Typeface mSemiBoldTypeface;

    //saves the upcoming state for time/time_distance animation
    private Map<String, Integer> mTimeDistanceStateMap;
    //saves the index of the upcoming offer to display
    private Map<String, Integer> mOfferIndexMap;

    public TaskQuotesRecyclerViewAdapter(Context context, /*List<ProviderModel> quotesList,*/ OnInteractionListener listener) {
        this.mContext = context;
        this.mQuotesList = new ArrayList<>();
        this.mListener = listener;
        mLayoutInflater = LayoutInflater.from(context);

        int offset = context.getResources().getDimensionPixelSize(R.dimen.scale_4dp);
        mLiveIconOffset = context.getResources().getDimensionPixelSize(R.dimen.icon_live_width) + offset;
        mHomeIconOffset = context.getResources().getDimensionPixelSize(R.dimen.icon_home_width) + offset;
        mTagBackgroundColor = ContextCompat.getColor(context, R.color.splash_gradient_end);
        mTagTextColor = ContextCompat.getColor(context, R.color.white);

        mSemiBoldTypeface = TypeFaceProvider.get(mContext, mContext.getResources().getString(R.string.font_semi_bold));
        mTimeDistanceStateMap = new HashMap<>();
        mOfferIndexMap = new HashMap<>();
    }

    @Override
    public QuoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new QuoteViewHolder(mLayoutInflater.inflate(R.layout.row_task_quote, parent, false));
    }

    @Override
    public void onBindViewHolder(final QuoteViewHolder holder, final int position) {
        final ProviderModel provider = mQuotesList.get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(provider);
                }
            }
        });

        //remove all ongoing animations
        holder.removeAnimations();

        //image
        Utility.showCircularImageView(mContext, TAG, holder.ivAvatar, provider.profileUrl, Utility.DEFAULT_PROFILE_SRC);

        //basic info
        SpannableString sName = new SpannableString(checkNonNullAndSet(provider.userName));
        SpannableString sVerified = null;
        if (provider.isVerified.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
            sVerified = new SpannableString(mContext.getString(R.string.label_verified_pro));
            sVerified.setSpan(new RoundedBackgroundSpan(mTagBackgroundColor, mTagTextColor), 0, sVerified.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        holder.tvName.setText(sVerified != null ? TextUtils.concat(sName, " ", sVerified) : sName);

//        holder.tvDescription.setText(checkNonNullAndSet(provider.information));

        holder.tvDescription.setText(provider.information);
        holder.tvDescription.makeExpandable(3);
        holder.tvDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(provider);
            }
        });
//        holder.tvDescription.makeExpandable(provider.information, 3);
//        manageReadMore(holder.tvDescription, provider.information);

        holder.tvLocation.setText(checkNonNullAndSet(provider.sp_locality));
        Utility.showRating(provider.rating, holder.ratingBar);

        //experience
        if (TextUtils.isEmpty(provider.experience)
                || Utility.ZERO_STRING.equals(provider.experience)) {
            holder.tvExperience.setText(checkNonNullAndSet(mContext.getString(R.string.label_experience_zero)));
        } else {
            holder.tvExperience.setText(
                    mContext.getResources().getQuantityString(R.plurals.getTotalExperianceString
                            , Integer.parseInt(provider.experience)
                            , Integer.parseInt(provider.experience)));
        }

        // price - Checking if amount present then show call and paid lables else hide
        if (provider.getQuotePriceInInteger() > 0) {
            holder.tvPrice.setVisibility(View.VISIBLE);
            holder.tvPrice.setText(mContext.getString(R.string.label_book_rs, provider.quotePrice));
            holder.tvPrice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onBookClick(provider);
                    }
                }
            });
        } else {
            holder.tvPrice.setOnClickListener(null);
            holder.tvPrice.setVisibility(View.GONE);
        }

        //low price-high rating
        if (provider.low_price != null && provider.low_price.equals("1")) {
            holder.tvBanner.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundResource(R.color.cheepest_bg_color);
            holder.tvBanner.setText(mContext.getString(R.string.label_cheepest_strip));
            holder.tvBanner.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_cheapest_quote, 0, 0, 0);
            holder.tvBanner.setTextColor(ContextCompat.getColor(holder.tvBanner.getContext(), R.color.cheepest_highlighted_text_color));
        } else if (provider.high_rating != null && provider.high_rating.equals("1")) {
            holder.tvBanner.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundResource(R.color.yellow_varient_1);
            holder.tvBanner.setText(mContext.getString(R.string.label_highest_rated_strip));
            holder.tvBanner.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_highest_rating, 0, 0, 0);
            holder.tvBanner.setTextColor(ContextCompat.getColor(holder.tvBanner.getContext(), R.color.highest_rated_highlighted_text_color));
        } else {
            holder.itemView.setBackgroundResource(R.color.white);
            holder.tvBanner.setVisibility(View.GONE);
        }

        //discount
        try {
            DecimalFormat df2 = new DecimalFormat(".##");
            double discount = Double.valueOf(provider.discount);
            if (discount > 0) {
                holder.tvDiscount.setVisibility(View.VISIBLE);
                holder.tvDiscount.setText(TextUtils.concat(/*"-",*/ df2.format(Double.valueOf(provider.discount)), mContext.getString(R.string.label_quote_discount)));
                holder.tvDiscount.setSelected(true);
            } else {
                holder.tvDiscount.setVisibility(View.GONE);
                holder.tvDiscount.setSelected(false);
            }
        } catch (Exception e) {
            holder.tvDiscount.setVisibility(View.GONE);
            holder.tvDiscount.setSelected(false);
        }

        //happy home
        int happyHomeLength = provider.happyHomeCount != null ? provider.happyHomeCount.trim().length() : 0;
        if (happyHomeLength > 0) {
            holder.ivHomeAnimated.setBackgroundResource(R.drawable.ic_home);
            ((AnimationDrawable) holder.ivHomeAnimated.getBackground()).start();

            holder.tvHappyHomes.setVisibility(View.VISIBLE);
            SpannableString labelHappyHomes = new SpannableString(mContext.getString(R.string.label_happy_homes, provider.happyHomeCount));
            labelHappyHomes.setSpan(new LeadingMarginSpan.Standard(mHomeIconOffset, 0), 0, labelHappyHomes.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            labelHappyHomes.setSpan(new CustomTypefaceSpan(mSemiBoldTypeface), 0, happyHomeLength, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            labelHappyHomes.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.happyHomeColor)), 0, happyHomeLength, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            labelHappyHomes.setSpan(new RelativeSizeSpan(1.5f), 0, happyHomeLength, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            holder.tvHappyHomes.setText(labelHappyHomes);
        } else {
            holder.tvHappyHomes.setVisibility(View.GONE);
            holder.ivHomeAnimated.setVisibility(View.GONE);
        }

        //offer
        final int offerCount = provider.offerList != null ? provider.offerList.size() : 0;
        if (offerCount > 0) {
            holder.ivLiveAnimated.setVisibility(View.VISIBLE);
            holder.tvOffer.setVisibility(View.VISIBLE);

            holder.ivLiveAnimated.setBackgroundResource(R.drawable.ic_live);
            ((AnimationDrawable) holder.ivLiveAnimated.getBackground()).start();

            AnimatorSet offerAnimation = loadBannerScrollAnimation(holder.tvOffer, 2000, 100, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    int offerIndex = mOfferIndexMap.containsKey(provider.providerId) ? mOfferIndexMap.get(provider.providerId) : 0;
                    SpannableString labelOffer = new SpannableString(provider.offerList.get(offerIndex));
                    labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    holder.tvOffer.setText(labelOffer);
                    offerIndex = (offerIndex == (offerCount - 1) ? 0 : offerIndex + 1);
                    mOfferIndexMap.put(provider.providerId, offerIndex);
                }
            });
            offerAnimation.start();
            holder.addAnimator(offerAnimation);


            int offerIndex = mOfferIndexMap.containsKey(provider.providerId) ? mOfferIndexMap.get(provider.providerId) : 0;
            SpannableString labelOffer = new SpannableString(provider.offerList.get(offerIndex));
            labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            holder.tvOffer.setText(labelOffer);
            offerIndex = (offerIndex == (offerCount - 1) ? 0 : offerIndex + 1);
            mOfferIndexMap.put(provider.providerId, offerIndex);
        } else {
            holder.ivLiveAnimated.setVisibility(View.GONE);
            holder.tvOffer.setVisibility(View.GONE);
        }


        if (Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ALREADY_REQUESTED.equalsIgnoreCase(provider.request_detail_status)) {
            holder.ivChat.setVisibility(View.VISIBLE);
            //chat icon
            Glide.with(mContext).load(R.drawable.ic_chat_animated).asGif().dontAnimate().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.ivChat);
        } else {
            // Hide Chat Icon
            holder.ivChat.setVisibility(View.GONE);
        }

        //time-distance
        boolean hasDistance = provider.distance != null && (provider.distance = provider.distance.trim()).length() > 0;
        boolean hasTime = provider.time != null && (provider.time = provider.time.trim()).length() > 0;

        if (hasDistance && hasTime) {
            holder.tvDistance.setVisibility(View.VISIBLE);

            // Manage default state which is TIME
            int state = mTimeDistanceStateMap.containsKey(provider.providerId) ? mTimeDistanceStateMap.get(provider.providerId) : TIME;
            if (state == TIME) {
                mTimeDistanceStateMap.put(provider.providerId, DISTANCE);
                holder.tvDistance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_time_distance, 0, 0, 0);
                holder.tvDistance.setText(provider.time);
            } else {
                mTimeDistanceStateMap.put(provider.providerId, TIME);
                holder.tvDistance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_relative_distance, 0, 0, 0);
                holder.tvDistance.setText(provider.distance);
            }

            AnimatorSet distanceAnimation = loadBannerScrollAnimation(holder.tvDistance, 10000, 60, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    int state = mTimeDistanceStateMap.containsKey(provider.providerId) ? mTimeDistanceStateMap.get(provider.providerId) : TIME;
                    if (state == TIME) {
                        mTimeDistanceStateMap.put(provider.providerId, DISTANCE);
                        holder.tvDistance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_time_distance, 0, 0, 0);
                        holder.tvDistance.setText(provider.time);
                    } else {
                        mTimeDistanceStateMap.put(provider.providerId, TIME);
                        holder.tvDistance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_relative_distance, 0, 0, 0);
                        holder.tvDistance.setText(provider.distance);
                    }
                }
            });
            distanceAnimation.start();
            holder.addAnimator(distanceAnimation);
        } else if (hasDistance) {
            holder.tvDistance.setVisibility(View.VISIBLE);
            holder.tvDistance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_relative_distance, 0, 0, 0);
            holder.tvDistance.setText(provider.distance);
        } else if (hasTime) {
            holder.tvDistance.setVisibility(View.VISIBLE);
            holder.tvDistance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_time_distance, 0, 0, 0);
            holder.tvDistance.setText(provider.time);
        } else {
            holder.tvDistance.setVisibility(View.GONE);
        }

        //badge
        holder.ivBadge.setImageResource(getBadgeResource(provider.pro_level));

        //favorite
        holder.ivFavoriteQuote.setSelected(provider.isFavourite.equals(Utility.BOOLEAN.YES));

        // Chat Image click event
        holder.ivChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onChatClicked(provider);
            }
        });
    }

    private void manageReadMore(TextView tvDescription, String information) {
        final String ELLIPSIZE = "... ";
        final String MORE = "Read More";

        int lineEndIndex = tvDescription.getLayout().getLineEnd(3 - 1);
        String newText = information.substring(0, lineEndIndex - (ELLIPSIZE.length() + MORE.length() + 1)) + ELLIPSIZE + MORE;
        SpannableStringBuilder builder = new SpannableStringBuilder(newText);
        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(tvDescription.getContext(), R.color.splash_gradient_end)), newText.length() - MORE.length(), newText.length(), 0);
//        builder.setSpan(new StyleSpan(Typeface.BOLD), newText.length() - MORE.length(), newText.length(), 0);
        tvDescription.setText(builder, TextView.BufferType.SPANNABLE);
    }

    private String checkNonNullAndSet(String text) {
        return text != null ? text.trim() : "";
    }

    private int getBadgeResource(String badgeIndex) {
        if (badgeIndex != null) {
            switch (badgeIndex) {
                case "1"://platinum
                    return R.drawable.ic_badge_platinum;
                case "2"://gold
                    return R.drawable.ic_badge_gold;
                case "3"://silver
                    return R.drawable.ic_badge_silver;
                case "4"://bronze
                    return R.drawable.ic_badge_bronze;
                default:
                    return 0;
            }
        }
        return 0;
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
        return mQuotesList != null ? mQuotesList.size() : 0;
    }

    public void updateModelForRequestDetailStatus(String spUserID, String requestDatailStatus, String quoted_sp_image_url) {
        for (int i = 0; i < mQuotesList.size(); i++) {
            if (mQuotesList.get(i).providerId.equalsIgnoreCase(spUserID)) {
                mQuotesList.get(i).request_detail_status = requestDatailStatus;
                if (Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.REJECTED.equals(requestDatailStatus)) {
                    // Check if already quoted any any other amount else remove it from list.
                    if (!(mQuotesList.get(i).getQuotePriceInInteger() > 0)) {
                        mQuotesList.remove(i);
                        notifyItemRemoved(i);
                        break;
                    }
                }

                notifyItemChanged(i);
                break;
            }
        }
    }

    public void addAll(List<ProviderModel> providerModels) {
        this.mQuotesList.clear();
        this.mQuotesList.addAll(providerModels);
        notifyDataSetChanged();
    }

    public ArrayList<ProviderModel> getData() {
        return (ArrayList<ProviderModel>) mQuotesList;
    }

    static class QuoteViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBanner;
        private TextView tvName;
        private TextView tvLocation;
        private TextView tvDistance;
        private CFTextViewRegular tvDescription;
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
            tvBanner = (TextView) itemView.findViewById(R.id.tvBanner);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvLocation = (TextView) itemView.findViewById(R.id.tvLocation);
            tvDistance = (TextView) itemView.findViewById(R.id.tvDistance);
            tvDescription = (CFTextViewRegular) itemView.findViewById(R.id.tvDescription);
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

    public interface OnInteractionListener {
        void onBookClick(ProviderModel provider);

        void onItemClick(ProviderModel provider);

        void onChatClicked(ProviderModel provider);
    }
}
