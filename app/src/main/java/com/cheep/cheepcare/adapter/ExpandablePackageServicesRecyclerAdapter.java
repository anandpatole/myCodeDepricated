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
import com.cheep.databinding.RowPackageSubServicesBinding;
import com.cheep.utils.Utility;

import java.util.List;

/**
 * Created by pankaj on 12/26/17.
 */

public class ExpandablePackageServicesRecyclerAdapter extends ExpandableRecyclerAdapter<CheepCarePackageServicesModel
        , CheepCarePackageSubServicesModel
        , ExpandablePackageServicesRecyclerAdapter.ParentCategoryViewHolder
        , ExpandablePackageServicesRecyclerAdapter.ChildCategoryViewHolder> {

    private final boolean isSingleSelection;
    private final List<CheepCarePackageServicesModel> mList;

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
        RowPackageSubServicesBinding binding = DataBindingUtil.inflate(LayoutInflater.from(childViewGroup.getContext())
                , R.layout.row_package_sub_services
                , childViewGroup
                , false);
        return new ExpandablePackageServicesRecyclerAdapter.ChildCategoryViewHolder(binding);
    }

    @Override
    public void onBindParentViewHolder(@NonNull ParentCategoryViewHolder parentViewHolder
            , int parentPosition, @NonNull CheepCarePackageServicesModel parent) {
        parentViewHolder.bind(parent);
    }

    @Override
    public void onBindChildViewHolder(@NonNull ChildCategoryViewHolder childViewHolder
            , int parentPosition, int childPosition, @NonNull CheepCarePackageSubServicesModel child) {
        childViewHolder.bind(child, childViewHolder.mBinding.getRoot().getContext());
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
            mBinding.tvServiceDescription.setText(servicesModel.description);
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

        RowPackageSubServicesBinding mBinding;

        ChildCategoryViewHolder(@NonNull RowPackageSubServicesBinding binding) {
            super(binding.getRoot());
            // init views
            mBinding = binding;
            // on click of check box
            mBinding.lnRoot.setOnClickListener(new View.OnClickListener() {
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

        // bind data with view for child row
        public void bind(@NonNull CheepCarePackageSubServicesModel subServicesModel, Context context) {

            mBinding.tvSubServiceName.setText(subServicesModel.subSubCatName);
            //commented as, as of now there is no description
            /*if (subServicesModel.package_description != null && !subServicesModel.package_description.isEmpty()) {
                textPackageDescription.setText(subServicesModel.package_description);
                textPackageDescription.setVisibility(View.VISIBLE);
            } else {
                textPackageDescription.setVisibility(View.GONE);
            }*/
            SpannableString spannableString = new SpannableString(context.getString(R.string.rupee_symbol_x_package_price
                    , subServicesModel.price));
            spannableString = Utility.getCheepCarePackageMonthlyPrice(spannableString, spannableString.length() - 2, spannableString.length());
            mBinding.tvSubServicePrice.setText(spannableString);
            /*mBinding.tvSubServicePrice.setText(itemView.getContext().getString(R.string.rupee_symbol_x
                    , *//*Utility.getQuotePriceFormatter(String.valueOf(*//*subServicesModel.price)*//*))*//*);*/
            mBinding.imgIconCorrect.setSelected(subServicesModel.isSelected);
            mBinding.tvSubServicePrice.setSelected(subServicesModel.isSelected);
        }

    }
}
