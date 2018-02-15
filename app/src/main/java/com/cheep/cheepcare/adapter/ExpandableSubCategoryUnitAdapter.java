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
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcare.model.PackageSubOption;
import com.cheep.custom_view.expandablerecycleview.ChildViewHolder;
import com.cheep.custom_view.expandablerecycleview.ExpandableRecyclerAdapter;
import com.cheep.custom_view.expandablerecycleview.ParentViewHolder;
import com.cheep.databinding.RowPackageCareItemBinding;
import com.cheep.databinding.RowPackageCareSubItemBinding;
import com.cheep.databinding.RowSubCategoryUnitTickPriceBinding;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by kruti on 9/2/18.
 */

public class ExpandableSubCategoryUnitAdapter extends ExpandableRecyclerAdapter<SubServiceDetailModel
        , SubServiceDetailModel
        , ExpandableSubCategoryUnitAdapter.ParentCategoryViewHolder
        , ExpandableSubCategoryUnitAdapter.ChildCategoryViewHolder> {

    private static final String TAG = ExpandableSubCategoryUnitAdapter.class.getSimpleName();
    private List<SubServiceDetailModel> mParentList;

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
    public ExpandableSubCategoryUnitAdapter(@NonNull List<SubServiceDetailModel> parentList) {
        super(parentList);
        mParentList = parentList;
    }

    @NonNull
    @Override
    public ParentCategoryViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        RowSubCategoryUnitTickPriceBinding binding =
                DataBindingUtil.inflate(LayoutInflater.from(parentViewGroup.getContext())
                        , R.layout.row_package_sub_service_unit
                        , parentViewGroup
                        , false);
        return new ParentCategoryViewHolder(binding);
    }

    @NonNull
    @Override
    public ChildCategoryViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        RowSubCategoryUnitTickPriceBinding binding =
                DataBindingUtil.inflate(LayoutInflater.from(childViewGroup.getContext())
                        , R.layout.row_sub_category_unit_tick_price
                        , childViewGroup
                        , false);
        return new ChildCategoryViewHolder(binding);
    }

    @Override
    public void onBindParentViewHolder(@NonNull ParentCategoryViewHolder parentViewHolder, int parentPosition
            , @NonNull SubServiceDetailModel parent) {
        parentViewHolder.bind(parent, parentViewHolder);
    }

    @Override
    public void onBindChildViewHolder(@NonNull ChildCategoryViewHolder childViewHolder, int parentPosition, int childPosition, @NonNull SubServiceDetailModel child) {
        childViewHolder.bind(child, childViewHolder, parentPosition);
    }

    /**
     * View Holder for Parent row
     */
    class ParentCategoryViewHolder extends ParentViewHolder {

        RowSubCategoryUnitTickPriceBinding mBinding;

        ParentCategoryViewHolder(@NonNull RowSubCategoryUnitTickPriceBinding binding) {
            // init views
            super(binding.getRoot());
            mBinding = binding;

        }

        // bind data with view parent row
        public void bind(@NonNull final SubServiceDetailModel subServicesModel, final ParentCategoryViewHolder holder) {
            mBinding.tvSubServiceName.setText(subServicesModel.name);
            Context context = holder.mBinding.getRoot().getContext();

            if (subServicesModel.subServiceList.size() == 0) {
                mBinding.clCenter.setVisibility(View.GONE);
                mBinding.tvSubServicePrice.setVisibility(View.GONE);
                mBinding.ivIsExpanded.setVisibility(View.GONE);
            } else {
                mBinding.clCenter.setVisibility(View.GONE);
                mBinding.tvSubServicePrice.setVisibility(View.GONE);
                mBinding.ivIsExpanded.setVisibility(View.VISIBLE);
            }

            mBinding.ivIsExpanded.setSelected(subServicesModel.isSelected);
            mBinding.imgIconCorrect.setSelected(subServicesModel.isSelected);

            mBinding.tvSubServicePrice.setText(Utility.getCheepCarePackageMonthlyPrice(
                    context
                    , R.string.rupee_symbol_x_package_price_with_star
                    , subServicesModel.monthlyPrice));

            /*mBinding.tvSubServicePrice.setText(itemView.getContext().getString(R.string.rupee_symbol_x
                    , *//*Utility.getQuotePriceFormatter(String.valueOf(*//*subServicesModel.price)*//*))*//*);*/
            mBinding.imgIconCorrect.setSelected(subServicesModel.isSelected);
            mBinding.tvSubServicePrice.setSelected(subServicesModel.isSelected);

            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (subServicesModel.subServiceList.size() == 0) {
                        if (subServicesModel.isSelected) {
                            mBinding.clCenter.setVisibility(View.GONE);
                            mBinding.tvSubServicePrice.setVisibility(View.GONE);
                            mBinding.ivIsExpanded.setVisibility(View.GONE);
                        } else {
                            mBinding.clCenter.setVisibility(View.VISIBLE);
                            mBinding.tvSubServicePrice.setVisibility(View.VISIBLE);
                            mBinding.ivIsExpanded.setVisibility(View.GONE);
                        }
                    } else {
                        if (subServicesModel.isSelected) {
                            // doing reverse as this is based on previous selection value
                            mBinding.ivIsExpanded.setSelected(false);
                        } else {
                            mBinding.ivIsExpanded.setSelected(true);
                        }
                    }
                    subServicesModel.isSelected = !subServicesModel.isSelected;
                }
            });
        }
    }

    /**
     * View Holder for Child row
     */
    class ChildCategoryViewHolder extends ChildViewHolder {

        RowSubCategoryUnitTickPriceBinding mBinding;
        private boolean areAllDeselected = true;

        ChildCategoryViewHolder(@NonNull RowSubCategoryUnitTickPriceBinding binding) {
            super(binding.getRoot());
            // init views
            mBinding = binding;

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
        public void bind(@NonNull final SubServiceDetailModel subServicesModel, final ChildCategoryViewHolder holder
                , final int parentPosition) {
            areAllDeselected = true;
            mBinding.tvSubServiceName.setText(subServicesModel.name);
            Context context = holder.mBinding.getRoot().getContext();
            //commented as, as of now there is no description
            /*if (subServicesModel.package_description != null && !subServicesModel.package_description.isEmpty()) {
                textPackageDescription.setText(subServicesModel.package_description);
                textPackageDescription.setVisibility(View.VISIBLE);
            } else {
                textPackageDescription.setVisibility(View.GONE);
            }*/
//                SpannableString spannableString = new SpannableString(context.getString(R.string.rupee_symbol_x_package_price, subServicesModel.monthlyPrice));
//                spannableString = Utility.getCheepCarePackageMonthlyPrice(spannableString, spannableString.length() - 2, spannableString.length());
//                mSelectionBinding.tvSubServicePrice.setText(spannableString);
            mBinding.tvSubServicePrice.setText(Utility.getCheepCarePackageMonthlyPrice(
                    context
                    , R.string.rupee_symbol_x_package_price_with_star
                    , subServicesModel.monthlyPrice));

            /*mBinding.tvSubServicePrice.setText(itemView.getContext().getString(R.string.rupee_symbol_x
                    , *//*Utility.getQuotePriceFormatter(String.valueOf(*//*subServicesModel.price)*//*))*//*);*/
            mBinding.imgIconCorrect.setSelected(subServicesModel.isSelected);
            mBinding.tvSubServicePrice.setSelected(subServicesModel.isSelected);

            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<SubServiceDetailModel> subServiceList = mParentList.get(parentPosition).subServiceList;
                    if (subServiceList.size() == 0) {

                        if (subServicesModel.isSelected) {
                            for (int i = 0; i < subServiceList.size(); i++) {
                                if ((subServicesModel != subServiceList.get(i)) && subServiceList.get(i).isSelected) {
                                    areAllDeselected = false;
                                }
                            }

                            if (areAllDeselected) {
                                mParentList.get(parentPosition).isSelected = false;
                                notifyParentChanged(holder.getParentAdapterPosition());
                            }

                            mBinding.clCenter.setVisibility(View.GONE);
                            mBinding.tvSubServicePrice.setVisibility(View.GONE);
                            mBinding.ivIsExpanded.setVisibility(View.GONE);
                        } else {
                            mBinding.clCenter.setVisibility(View.VISIBLE);
                            mBinding.tvSubServicePrice.setVisibility(View.VISIBLE);
                            mBinding.ivIsExpanded.setVisibility(View.GONE);
                        }
                    }
                    subServicesModel.isSelected = !subServicesModel.isSelected;
                }
            });
        }
    }

    public void addList(ArrayList<SubServiceDetailModel> list/*, String otherSubService*/) {
        Log.d(TAG, "addList() called with: list = [" + list.size() + "]");
        if (list == null) {
            list = new ArrayList<>();
        }
        this.mParentList = list;
        //Add other as Subservice
        /*SubServiceDetailModel subServiceDetailModel = new SubServiceDetailModel();
        subServiceDetailModel.sub_cat_id = -1;
        subServiceDetailModel.catId = -1;
        subServiceDetailModel.name = otherSubService;
        this.mParentList.add(subServiceDetailModel);*/
        notifyDataSetChanged();
    }

    public List<SubServiceDetailModel> getSelectedList() {
        ArrayList<SubServiceDetailModel> list = new ArrayList<>();
        for (int i = 0; i < mParentList.size(); i++) {
            if (mParentList.get(i).isSelected) {
                list.add(mParentList.get(i));
            }
        }
        return list;
    }
}