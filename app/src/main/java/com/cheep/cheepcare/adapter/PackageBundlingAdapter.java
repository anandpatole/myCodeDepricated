package com.cheep.cheepcare.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.databinding.RowBundledPackageHeaderBinding;
import com.cheep.databinding.RowBundledPackageSelectedBinding;
import com.cheep.databinding.RowBundledPackagetNoSelectedBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.GuestUserDetails;
import com.cheep.model.UserDetails;
import com.cheep.utils.LoadMoreRecyclerAdapter;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pankaj on 12/22/17.
 */

public class PackageBundlingAdapter extends LoadMoreRecyclerAdapter<PackageBundlingAdapter.PackageViewHolder> {

    private static final String TAG = PackageBundlingAdapter.class.getSimpleName();
    private final PackageItemClickListener mListener;
    private List<PackageDetail> mList = new ArrayList<>();

    public static final int ROW_PACKAGE_SELECTED = 0;
    public static final int ROW_PACKAGE_HEADER = 1;
    public static final int ROW_PACKAGE_NOT_SELECTED = 2;
    private ArrayList list;

    public List<PackageDetail> getList() {
        return mList;
    }

    public interface PackageItemClickListener {
        void onPackageItemClick(int position, PackageDetail packageModel);

        void onUpdateOfAddress(int position, AddressModel addressModel);
    }

    public PackageBundlingAdapter(PackageItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public PackageViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ROW_PACKAGE_HEADER: {
                RowBundledPackageHeaderBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.row_bundled_package_header, parent, false);
                return new PackageViewHolder(binding);
            }
            case ROW_PACKAGE_NOT_SELECTED: {
                RowBundledPackagetNoSelectedBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.row_bundled_packaget_no_selected, parent, false);
                return new PackageViewHolder(binding);
            }
            default: {
                RowBundledPackageSelectedBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.row_bundled_package_selected, parent, false);
                return new PackageViewHolder(binding);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).rowType;
    }

    @Override
    public void onActualBindViewHolder(final PackageViewHolder holder, int position) {
        int viewType = getItemViewType(holder.getAdapterPosition());
        Context context;
        final PackageDetail model = mList.get(position);
        switch (viewType) {
            case ROW_PACKAGE_NOT_SELECTED:
                context = holder.mRowNotSelectedBinding.getRoot().getContext();
                Glide.with(holder.mRowNotSelectedBinding.getRoot().getContext())
                        .load(model.packageImage)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(holder.mRowNotSelectedBinding.ivItemBackground);
                SpannableString spannableString = new SpannableString(context.getString(R.string.rupee_symbol_x_package_price, model.price));
                spannableString = Utility.getCheepCarePackageMonthlyPrice(spannableString, spannableString.length() - 3, spannableString.length());
                holder.mRowNotSelectedBinding.tvPrice.setText(spannableString);
                holder.mRowNotSelectedBinding.ivIsAddressSelected.setSelected(model.isSelected);
                holder.mRowNotSelectedBinding.tvDescription.setText(model.subtitle);
                holder.mRowNotSelectedBinding.tvTitle.setText(model.title);


                holder.mRowNotSelectedBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onPackageItemClick(holder.getAdapterPosition(), model);
                    }
                });

                holder.bindLiveFeedForPackagedBundle(holder.mRowNotSelectedBinding);
                break;
            case ROW_PACKAGE_SELECTED:
                context = holder.mRowSelectedBinding.getRoot().getContext();
                Glide.with(holder.mRowSelectedBinding.getRoot().getContext())
                        .load(model.packageImage)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(holder.mRowSelectedBinding.ivItemBackground);

                SpannableString spannableString1 = new SpannableString(context.getString(R.string.rupee_symbol_x_package_price, model.price));
                spannableString1 = Utility.getCheepCarePackageMonthlyPrice(spannableString1, spannableString1.length() - 3, spannableString1.length());
                holder.mRowSelectedBinding.tvPrice.setText(spannableString1);
                holder.mRowSelectedBinding.ivIsAddressSelected.setSelected(model.isSelected);
                holder.mRowSelectedBinding.tvDescription.setText(model.subtitle);
                holder.mRowSelectedBinding.tvTitle.setText(model.title);

                if (model.mSelectedAddressList != null && !model.mSelectedAddressList.isEmpty()) {
                    if (!TextUtils.isEmpty(model.mSelectedAddressList.get(0).nickname))
                        holder.mRowSelectedBinding.tvAddressNickname.setText(model.mSelectedAddressList.get(0).nickname);
                    else
                        holder.mRowSelectedBinding.tvAddressNickname.setText(Utility.getAddressCategoryString(model.mSelectedAddressList.get(0).category));
                    holder.mRowSelectedBinding.ivAddressIcon.setImageResource(Utility.getAddressCategoryBlueIcon(model.mSelectedAddressList.get(0).category));
                    holder.mRowSelectedBinding.tvAddress.setText(model.mSelectedAddressList.get(0).address_initials + ", " + model.mSelectedAddressList.get(0).address);
                }

                holder.mRowSelectedBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
                holder.bindLiveFeedForPackagedBundle(holder.mRowSelectedBinding);
                break;
            case ROW_PACKAGE_HEADER:
//                holder.mBindingBundledPackageHeaderBinding.txtRibbon.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                holder.mRowHeaderBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
                break;
        }
    }

    @Override
    public int onActualItemCount() {
        return mList.size();
    }


    class PackageViewHolder extends RecyclerView.ViewHolder {
        RowBundledPackageSelectedBinding mRowSelectedBinding;
        RowBundledPackageHeaderBinding mRowHeaderBinding;
        RowBundledPackagetNoSelectedBinding mRowNotSelectedBinding;
        private List<AnimatorSet> animators;


        PackageViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            if (binding instanceof RowBundledPackageHeaderBinding)
                mRowHeaderBinding = (RowBundledPackageHeaderBinding) binding;
            else if (binding instanceof RowBundledPackagetNoSelectedBinding) {
                mRowNotSelectedBinding = (RowBundledPackagetNoSelectedBinding) binding;
            } else
                mRowSelectedBinding = (RowBundledPackageSelectedBinding) binding;

            if (mRowSelectedBinding != null)
                mRowSelectedBinding.lnAddressRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDropDownMenu(mRowSelectedBinding.lnAddressRow, getAdapterPosition());
                    }
                });
            animators = new ArrayList<>();

        }

        private void bindLiveFeedForPackagedBundle(final RowBundledPackagetNoSelectedBinding binding) {
            final PackageDetail model = mList.get(getAdapterPosition());
            final int liveFeedCounter = model.live_lable_arr != null ? model.live_lable_arr.size() : 0;
            final Map<String, Integer> mOfferIndexMap = new HashMap<>();
            if (liveFeedCounter > 0) {
                binding.ivLiveAnimated.setVisibility(View.VISIBLE);
                binding.tvLiveFeed.setVisibility(View.VISIBLE);

                // Live Icon offset
                int offset = binding.getRoot().getResources().getDimensionPixelSize(R.dimen.scale_5dp);
                final int mLiveIconOffset = binding.getRoot().getResources().getDimensionPixelSize(R.dimen.icon_live_width) + offset;

                // Start live image animations
                binding.ivLiveAnimated.setBackgroundResource(R.drawable.ic_live);
                ((AnimationDrawable) binding.ivLiveAnimated.getBackground()).start();

                AnimatorSet offerAnimation = loadBannerScrollAnimation(binding.tvLiveFeed, 2000, 100, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        int offerIndex = mOfferIndexMap.containsKey(model.id) ? mOfferIndexMap.get(model.id) : 0;
                        SpannableString labelOffer = new SpannableString(model.live_lable_arr.get(offerIndex));
                        labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        binding.tvLiveFeed.setText(labelOffer);
                        offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
                        mOfferIndexMap.put(model.id, offerIndex);
                    }
                });
                offerAnimation.start();
                removeAnimations();
                addAnimator(offerAnimation);
                int offerIndex = mOfferIndexMap.containsKey(model.id) ? mOfferIndexMap.get(model.id) : 0;
                SpannableString labelOffer = new SpannableString(model.live_lable_arr.get(offerIndex));
                labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                binding.tvLiveFeed.setText(labelOffer);
                offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
                mOfferIndexMap.put(model.id, offerIndex);
            } else {
                binding.ivLiveAnimated.setVisibility(View.GONE);
                binding.tvLiveFeed.setVisibility(View.GONE);
            }

        }

        private void bindLiveFeedForPackagedBundle(final RowBundledPackageSelectedBinding binding) {
            final PackageDetail model = mList.get(getAdapterPosition());
            final int liveFeedCounter = model.live_lable_arr != null ? model.live_lable_arr.size() : 0;
            final Map<String, Integer> mOfferIndexMap = new HashMap<>();
            if (liveFeedCounter > 0) {
                binding.ivLiveAnimated.setVisibility(View.VISIBLE);
                binding.tvLiveFeed.setVisibility(View.VISIBLE);

                // Live Icon offset
                int offset = binding.getRoot().getResources().getDimensionPixelSize(R.dimen.scale_5dp);
                final int mLiveIconOffset = binding.getRoot().getResources().getDimensionPixelSize(R.dimen.icon_live_width) + offset;

                // Start live image animations
                binding.ivLiveAnimated.setBackgroundResource(R.drawable.ic_live);
                ((AnimationDrawable) binding.ivLiveAnimated.getBackground()).start();

                AnimatorSet offerAnimation = loadBannerScrollAnimation(binding.tvLiveFeed, 2000, 100, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        int offerIndex = mOfferIndexMap.containsKey(model.id) ? mOfferIndexMap.get(model.id) : 0;
                        SpannableString labelOffer = new SpannableString(model.live_lable_arr.get(offerIndex));
                        labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        binding.tvLiveFeed.setText(labelOffer);
                        offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
                        mOfferIndexMap.put(model.id, offerIndex);
                    }
                });
                offerAnimation.start();
                removeAnimations();
                addAnimator(offerAnimation);
                int offerIndex = mOfferIndexMap.containsKey(model.id) ? mOfferIndexMap.get(model.id) : 0;
                SpannableString labelOffer = new SpannableString(model.live_lable_arr.get(offerIndex));
                labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                binding.tvLiveFeed.setText(labelOffer);
                offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
                mOfferIndexMap.put(model.id, offerIndex);
            } else {
                binding.ivLiveAnimated.setVisibility(View.GONE);
                binding.tvLiveFeed.setVisibility(View.GONE);
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
    }

    public void addPakcageList(List<PackageDetail> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    private void showDropDownMenu(final View view, final int adapterPosition) {
        Log.i(TAG, "showDropDownMenu: ");
        final View mFilterPopupWindow = View.inflate(view.getContext(), R.layout.layout_address_drop_down, null);

        final PopupWindow mPopupWindow = new PopupWindow(view.getContext());
        RecyclerView recyclerview = mFilterPopupWindow.findViewById(R.id.listMultipleChoice);
        recyclerview.setLayoutManager(new LinearLayoutManager(view.getContext()));

      /*  Collections.sort(model.dropDownList, new Comparator<QueAnsModel.DropDownModel>() {
            @Override
            public int compare(QueAnsModel.DropDownModel abc1, QueAnsModel.DropDownModel abc2) {

                boolean b1 = abc1.isSelected;
                boolean b2 = abc2.isSelected;

                if (b1 != b2) {

                    if (b1) {
                        return -1;
                    }

                    return 1;
                }
                return 0;

            }
        });*/
        final UserDetails userDetails = PreferenceUtility.getInstance(view.getContext()).getUserDetails();
        final GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(view.getContext()).getGuestUserDetails();
        final DropDownAddressAdapter dropDownAdapter;
        if (userDetails != null)
            dropDownAdapter = new DropDownAddressAdapter(userDetails.addressList);
        else {
            dropDownAdapter = new DropDownAddressAdapter(guestUserDetails.addressList);
        }
        recyclerview.setAdapter(dropDownAdapter);
        DropDownAddressAdapter.ClickItem clickListener = new DropDownAddressAdapter.ClickItem() {
            @Override
            public void clickItem(int i) {
//                for (int j = 0; j < userDetails.addressList.size(); j++) {
//                    AddressModel model1 = userDetails.addressList.get(j);
//                }
//                dropDownAdapter.setSelected(i);
//                textView.setText(model.dropDownList.get(i).dropdown_answer);
//                textView.setSelected(true);
//                model.answer = model.dropDownList.get(i).dropdown_answer;
//                mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag(model.questionId).setSelected(true);

                if (userDetails != null)
                    mListener.onUpdateOfAddress(adapterPosition, userDetails.addressList.get(i));
                else
                    mListener.onUpdateOfAddress(adapterPosition, guestUserDetails.addressList.get(i));


                mPopupWindow.dismiss();


            }

            @Override
            public void dismissDialog() {
                mPopupWindow.dismiss();
            }
        };
        dropDownAdapter.setListener(clickListener);

        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setContentView(mFilterPopupWindow);
        mPopupWindow.setWidth(view.getWidth());
        mPopupWindow.setHeight(ListView.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);

        // No animation at present
        mPopupWindow.setAnimationStyle(0);

        // Displaying the popup at the specified location, + offsets.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mPopupWindow.showAsDropDown(view, 0, -view.getHeight(), Gravity.NO_GRAVITY);
        } else {
            mPopupWindow.showAsDropDown(view, 0, -view.getHeight());
        }
    }


}