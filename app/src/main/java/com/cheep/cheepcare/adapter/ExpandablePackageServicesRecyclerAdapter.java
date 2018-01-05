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
import com.cheep.cheepcare.model.CheepCarePackageSubServicesModel;
import com.cheep.custom_view.expandablerecycleview.ChildViewHolder;
import com.cheep.custom_view.expandablerecycleview.ExpandableRecyclerAdapter;
import com.cheep.custom_view.expandablerecycleview.ParentViewHolder;
import com.cheep.databinding.RowPackageServiceBinding;
import com.cheep.databinding.RowPackageSubServiceUnitBinding;
import com.cheep.databinding.RowPackageSubServicesBinding;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;

import java.util.List;

/**
 * Created by pankaj on 12/26/17.
 */

public class ExpandablePackageServicesRecyclerAdapter extends ExpandableRecyclerAdapter<CheepCarePackageServicesModel
        , CheepCarePackageSubServicesModel
        , ExpandablePackageServicesRecyclerAdapter.ParentCategoryViewHolder
        , ExpandablePackageServicesRecyclerAdapter.ChildCategoryViewHolder> {

    private static final String TAG = "ExpandablePackageServic";
    private final boolean isSingleSelection;
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
        this.isSingleSelection = isSingleSelection;
    }

    @Override
    public int getChildViewType(int parentPosition, int childPosition) {
        if (mList.get(parentPosition).type.equalsIgnoreCase(CheepCarePackageServicesModel.SERVICE_TYPE.SIMPLE))
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
            , int parentPosition, int childPosition, @NonNull CheepCarePackageSubServicesModel child) {
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


            //     on click of check box parent row
            // select/deselect all child row of parent if parent is select/deselect
            mBinding.imgIconCorrect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!isSingleSelection) {
                        CheepCarePackageServicesModel parent = mList.get(getParentAdapterPosition());
                        parent.isSelected = !parent.isSelected;
                        setAllChildSelected(parent);
                        notifyDataSetChanged();
                    }
                }
            });
        }

        // bind data with view parent row
        public void bind(@NonNull CheepCarePackageServicesModel servicesModel) {
            mBinding.imgIconCorrect.setSelected(servicesModel.isSelected);
            if (mList.get(getParentAdapterPosition()).type.equalsIgnoreCase(CheepCarePackageServicesModel.SERVICE_TYPE.SIMPLE))
                mBinding.imgDownArrow.setVisibility(View.VISIBLE);
            else
                mBinding.imgDownArrow.setVisibility(View.GONE);

            mBinding.tvServiceName.setText(servicesModel.name);
        }

    }

    /**
     * select/deselect all child row of parent if parent is select/deselect
     */
    private void setAllChildSelected(CheepCarePackageServicesModel parent) {
        for (CheepCarePackageSubServicesModel child : parent.subServices) {
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

                    if (!isSingleSelection) {
                        CheepCarePackageSubServicesModel subService = mList.get(parentPos).subServices.get(childPos);
                        subService.isSelected = !subService.isSelected;
                        if (subService.isSelected)
                            mList.get(parentPos).isSelected = true;
                        else {
                            int flag = 0;
                            for (CheepCarePackageSubServicesModel subServiceEach : mList.get(parentPos).subServices) {
                                if (!subServiceEach.isSelected)
                                    flag++;
                            }
                            if (flag == mList.get(parentPos).subServices.size())
                                mList.get(parentPos).isSelected = false;
                        }
                        notifyDataSetChanged();
                    } else {
                        for (int i = 0; i < mList.size(); i++) {

                            for (int j = 0; j < mList.get(i).subServices.size(); j++) {

                                mList.get(i).subServices.get(j).isSelected = (i == parentPos && j == childPos);
                                mList.get(i).isSelected = i == parentPos;
                            }
                        }
                        notifyDataSetChanged();

                    }

                }
            });
        }

        ChildCategoryViewHolder(@NonNull RowPackageSubServiceUnitBinding unitBinding) {
            super(unitBinding.getRoot());
            mUnitBinding = unitBinding;
            mUnitBinding.tvMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheepCarePackageSubServicesModel model = mList.get(getParentAdapterPosition()).getChildList().get(getChildAdapterPosition());
                    LogUtils.LOGE(TAG, "onClick() called with: minus = " + model.qty);
                    if (model.qty > 0) {
                        model.qty--;
                    }
                    LogUtils.LOGE(TAG, "onClick() called with: minus = " + model.qty);
                    notifyChildChanged(getParentAdapterPosition(), getChildAdapterPosition());
                }
            });

            mUnitBinding.tvPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheepCarePackageSubServicesModel model = mList.get(getParentAdapterPosition()).getChildList().get(getChildAdapterPosition());
                    LogUtils.LOGE(TAG, "onClick() called with: plus = " + model.qty);
                    if (model.qty < model.maxQty) {
                        model.qty++;
                    }
                    LogUtils.LOGE(TAG, "onClick() called with: plus = " + model.qty);
                    notifyChildChanged(getParentAdapterPosition(), getChildAdapterPosition());
                }
            });
        }

        // bind data with view for child row
        public void bind(@NonNull CheepCarePackageSubServicesModel subServicesModel, ChildCategoryViewHolder holder, int viewType) {
            if (viewType == VIEW_TYPE_SINGLE_SELECTION) {
                mSelectionBinding.tvSubServiceName.setText(subServicesModel.subSubCatName);
                Context context = holder.mSelectionBinding.getRoot().getContext();
                //commented as, as of now there is no description
            /*if (subServicesModel.package_description != null && !subServicesModel.package_description.isEmpty()) {
                textPackageDescription.setText(subServicesModel.package_description);
                textPackageDescription.setVisibility(View.VISIBLE);
            } else {
                textPackageDescription.setVisibility(View.GONE);
            }*/
                SpannableString spannableString = new SpannableString(context.getString(R.string.rupee_symbol_x_package_price, subServicesModel.price));
                spannableString = Utility.getCheepCarePackageMonthlyPrice(spannableString, spannableString.length() - 2, spannableString.length());
                mSelectionBinding.tvSubServicePrice.setText(spannableString);
            /*mBinding.tvSubServicePrice.setText(itemView.getContext().getString(R.string.rupee_symbol_x
                    , *//*Utility.getQuotePriceFormatter(String.valueOf(*//*subServicesModel.price)*//*))*//*);*/
                mSelectionBinding.imgIconCorrect.setSelected(subServicesModel.isSelected);
                mSelectionBinding.tvSubServicePrice.setSelected(subServicesModel.isSelected);

            } else {
                Context context = holder.mUnitBinding.getRoot().getContext();
                mUnitBinding.tvSubServiceName.setText(subServicesModel.subSubCatName);
                mUnitBinding.tvDigit.setText(String.valueOf(subServicesModel.qty));
                SpannableString spannableString = new SpannableString(context.getString(R.string.rupee_symbol_x_package_price, subServicesModel.price));
                spannableString = Utility.getCheepCarePackageMonthlyPrice(spannableString, spannableString.length() - 2, spannableString.length());
                mUnitBinding.tvSubServicePrice.setText(spannableString);
                mUnitBinding.tvSubServicePrice.setSelected(true);
            }


        }
    }
}
