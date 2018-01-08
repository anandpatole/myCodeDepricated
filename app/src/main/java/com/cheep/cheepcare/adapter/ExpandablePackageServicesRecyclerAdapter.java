package com.cheep.cheepcare.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcare.model.CheepCarePackageServicesModel;
import com.cheep.cheepcare.model.PackageOption;
import com.cheep.custom_view.expandablerecycleview.ChildViewHolder;
import com.cheep.custom_view.expandablerecycleview.ExpandableRecyclerAdapter;
import com.cheep.custom_view.expandablerecycleview.ParentViewHolder;
import com.cheep.databinding.RowPackageServiceBinding;
import com.cheep.databinding.RowPackageSubServiceUnitBinding;
import com.cheep.databinding.RowPackageSubServicesBinding;
import com.cheep.utils.Utility;

import java.util.List;

/**
 * Created by pankaj on 12/26/17.
 */

public class ExpandablePackageServicesRecyclerAdapter extends ExpandableRecyclerAdapter<CheepCarePackageServicesModel
        , PackageOption
        , ExpandablePackageServicesRecyclerAdapter.ParentCategoryViewHolder
        , ExpandablePackageServicesRecyclerAdapter.ChildCategoryViewHolder> {

    private static final String TAG = "ExpandablePackageServic";
    private final List<CheepCarePackageServicesModel> mList;

    public static final int VIEW_TYPE_SINGLE_SELECTION = 1;
    public static final int VIEW_TYPE_UNIT_SELECTION = 2;


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
    public ExpandablePackageServicesRecyclerAdapter(@NonNull List<CheepCarePackageServicesModel> parentList
            , boolean isSingleSelection) {
        super(parentList);
        mList = parentList;
    }

    @Override
    public int getChildViewType(int parentPosition, int childPosition) {
        if (mList.get(parentPosition).selectionType.equalsIgnoreCase(CheepCarePackageServicesModel.SERVICE_TYPE.RADIO))
            return VIEW_TYPE_SINGLE_SELECTION;
        else
            return VIEW_TYPE_UNIT_SELECTION;
    }

    @NonNull
    @Override
    public ParentCategoryViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        RowPackageServiceBinding binding =
                DataBindingUtil.inflate(LayoutInflater.from(parentViewGroup.getContext())
                        , R.layout.row_package_service
                        , parentViewGroup
                        , false);
        return new ExpandablePackageServicesRecyclerAdapter.ParentCategoryViewHolder(binding);
    }

    @NonNull
    @Override
    public ChildCategoryViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_UNIT_SELECTION:
                RowPackageSubServiceUnitBinding binding = DataBindingUtil.inflate(LayoutInflater.from(childViewGroup.getContext())
                        , R.layout.row_package_sub_service_unit
                        , childViewGroup
                        , false);
                return new ExpandablePackageServicesRecyclerAdapter.ChildCategoryViewHolder(binding);

            default:
                RowPackageSubServicesBinding binding1 = DataBindingUtil.inflate(LayoutInflater.from(childViewGroup.getContext())
                        , R.layout.row_package_sub_services
                        , childViewGroup
                        , false);
                return new ExpandablePackageServicesRecyclerAdapter.ChildCategoryViewHolder(binding1);
        }
    }

    @Override
    public void onBindParentViewHolder(@NonNull ParentCategoryViewHolder parentViewHolder
            , int parentPosition, @NonNull CheepCarePackageServicesModel parent) {
        parentViewHolder.bind(parent);
    }

    @Override
    public void onBindChildViewHolder(@NonNull ChildCategoryViewHolder childViewHolder
            , int parentPosition, int childPosition, @NonNull PackageOption child) {
        int viewType = getChildViewType(parentPosition, childPosition);
        childViewHolder.bind(child, childViewHolder, viewType);
    }

    /**
     * View Holder for Parent row
     */
    class ParentCategoryViewHolder extends ParentViewHolder {

        RowPackageServiceBinding mBinding;

        ParentCategoryViewHolder(@NonNull RowPackageServiceBinding binding) {
            // init views
            super(binding.getRoot());
            mBinding = binding;
            setExpanded(true);

            //     on click of check box parent row
            // select/deselect all child row of parent if parent is select/deselect
        }

        // bind data with view parent row
        public void bind(@NonNull CheepCarePackageServicesModel servicesModel) {
            mBinding.imgIconCorrect.setSelected(true);
            if (mList.get(getParentAdapterPosition()).selectionType.equalsIgnoreCase(CheepCarePackageServicesModel.SERVICE_TYPE.RADIO))
                mBinding.imgDownArrow.setVisibility(View.VISIBLE);
            else
                mBinding.imgDownArrow.setVisibility(View.GONE);

            mBinding.tvServiceName.setText(servicesModel.packageOptionTitle);
        }

    }

    /**
     * select/deselect all child row of parent if parent is select/deselect
     */
    private void setAllChildSelected(CheepCarePackageServicesModel parent) {
        for (PackageOption child : parent.packageOptionList) {
            child.isSelected = parent.isSelected;
        }
        notifyDataSetChanged();
    }

    /**
     * View Holder for Child row
     */
    class ChildCategoryViewHolder extends ChildViewHolder {

        RowPackageSubServicesBinding mSelectionBinding;
        RowPackageSubServiceUnitBinding mUnitBinding;


        ChildCategoryViewHolder(@NonNull RowPackageSubServicesBinding selectionBinding) {
            super(selectionBinding.getRoot());
            // init views
            mSelectionBinding = selectionBinding;

            // on click of check box
            mSelectionBinding.lnRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // get click child row and its parent
                    int parentPos = getParentAdapterPosition();
                    int childPos = getChildAdapterPosition();

                    // if single child is selected then parent also should be selected
                    // if all children are deselected then parent should be deselected
                    for (int i = 0; i < mList.size(); i++) {

                        for (int j = 0; j < mList.get(i).packageOptionList.size(); j++) {

                            mList.get(i).packageOptionList.get(j).isSelected = (i == parentPos && j == childPos);
                            mList.get(i).isSelected = i == parentPos;
                        }
                    }
                    notifyDataSetChanged();
                }
            });
        }

        ChildCategoryViewHolder(@NonNull RowPackageSubServiceUnitBinding unitBinding) {
            super(unitBinding.getRoot());
            mUnitBinding = unitBinding;
            mUnitBinding.tvMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PackageOption model = mList.get(getParentAdapterPosition()).getChildList().get(getChildAdapterPosition());
                    int minQty = Integer.valueOf(model.minUnit);
                    if (model.qty > minQty) {
                        model.qty--;
                    }
                    notifyChildChanged(getParentAdapterPosition(), getChildAdapterPosition());
                }
            });

            mUnitBinding.tvPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PackageOption model = mList.get(getParentAdapterPosition()).getChildList().get(getChildAdapterPosition());
                    int maxQty = Integer.valueOf(model.maxUnit);
                    if (model.qty < maxQty) {
                        model.qty++;
                    }
                    notifyChildChanged(getParentAdapterPosition(), getChildAdapterPosition());
                }
            });
        }

        // bind data with view for child row
        public void bind(@NonNull PackageOption subServicesModel, ChildCategoryViewHolder holder, int viewType) {
            if (viewType == VIEW_TYPE_SINGLE_SELECTION) {
                mSelectionBinding.tvSubServiceName.setText(subServicesModel.options);
                Context context = holder.mSelectionBinding.getRoot().getContext();
                //commented as, as of now there is no description
            /*if (subServicesModel.package_description != null && !subServicesModel.package_description.isEmpty()) {
                textPackageDescription.setText(subServicesModel.package_description);
                textPackageDescription.setVisibility(View.VISIBLE);
            } else {
                textPackageDescription.setVisibility(View.GONE);
            }*/
                SpannableString spannableString = new SpannableString(context.getString(R.string.rupee_symbol_x_package_price, subServicesModel.monthlyPrice));
                spannableString = Utility.getCheepCarePackageMonthlyPrice(spannableString, spannableString.length() - 2, spannableString.length());
                mSelectionBinding.tvSubServicePrice.setText(spannableString);
            /*mBinding.tvSubServicePrice.setText(itemView.getContext().getString(R.string.rupee_symbol_x
                    , *//*Utility.getQuotePriceFormatter(String.valueOf(*//*subServicesModel.price)*//*))*//*);*/
                mSelectionBinding.imgIconCorrect.setSelected(subServicesModel.isSelected);
                mSelectionBinding.tvSubServicePrice.setSelected(subServicesModel.isSelected);

            } else {
                Context context = holder.mUnitBinding.getRoot().getContext();
                mUnitBinding.tvSubServiceName.setText(subServicesModel.options);
                if (subServicesModel.qty == -1) {
                    subServicesModel.qty = Integer.valueOf(subServicesModel.minUnit);
                    mUnitBinding.tvDigit.setText(String.valueOf(subServicesModel.minUnit));
                } else
                    mUnitBinding.tvDigit.setText(String.valueOf(subServicesModel.qty));

                SpannableString spannableString = new SpannableString(context.getString(R.string.rupee_symbol_x_package_price, subServicesModel.unitPrice));
                spannableString = Utility.getCheepCarePackageMonthlyPrice(spannableString, spannableString.length() - 2, spannableString.length());
                mUnitBinding.tvSubServicePrice.setText(spannableString);
                mUnitBinding.tvSubServicePrice.setSelected(true);
            }


        }
    }
}
