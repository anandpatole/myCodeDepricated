package com.cheep.strategicpartner;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cheep.R;
import com.cheep.custom_view.expandablerecycleview.ChildViewHolder;
import com.cheep.custom_view.expandablerecycleview.ExpandableRecyclerAdapter;
import com.cheep.custom_view.expandablerecycleview.ParentViewHolder;
import com.cheep.utils.Utility;

import java.util.List;

/**
 * created by Giteeka 24/7/2017
 * <p>
 * Expandable RecyclerView for service selection in Strategic partner Phase 1
 */
class ExpandableServicesRecycleAdapter extends ExpandableRecyclerAdapter<StrategicPartnerServiceModel, AllSubSubCat, ExpandableServicesRecycleAdapter.ParentSubCategoryViewHolder, ExpandableServicesRecycleAdapter.ChildSubCategoryViewHolder> {

    private List<StrategicPartnerServiceModel> mSubCategoriesList;
    private boolean isSingleSelection = false;

    ExpandableServicesRecycleAdapter(@NonNull List<StrategicPartnerServiceModel> subCategoriesList, boolean isSingleSelection) {
        super(subCategoriesList);
        mSubCategoriesList = subCategoriesList;
        this.isSingleSelection = isSingleSelection;
    }

    /**
     * onCreateViewHolder for Parent row
     */
    @UiThread
    @NonNull
    @Override
    public ParentSubCategoryViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        View recipeView;
        recipeView = LayoutInflater.from(parentViewGroup.getContext()).inflate(R.layout.row_sub_categories, parentViewGroup, false);
        return new ParentSubCategoryViewHolder(recipeView);
    }

    /**
     * onCreateViewHolder for child row
     */
    @UiThread
    @NonNull
    @Override
    public ChildSubCategoryViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        View ingredientView;
        ingredientView = LayoutInflater.from(childViewGroup.getContext()).inflate(R.layout.row_sub_sub_categories, childViewGroup, false);
        return new ChildSubCategoryViewHolder(ingredientView);
    }

    /**
     * bind method for Parent row
     */
    @UiThread
    @Override
    public void onBindParentViewHolder(@NonNull ParentSubCategoryViewHolder parentSubCategoryViewHolder,
                                       int parentPosition,
                                       @NonNull final StrategicPartnerServiceModel recipe) {
        parentSubCategoryViewHolder.bind(recipe);

    }

    /**
     * bind method for child row
     */
    @UiThread
    @Override
    public void onBindChildViewHolder(@NonNull final ChildSubCategoryViewHolder childSubCategoryViewHolder, int parentPosition, int childPosition, @NonNull final AllSubSubCat ingredient) {
        childSubCategoryViewHolder.bind(ingredient);

    }

    /**
     * View Holder for Parent row
     */
    class ParentSubCategoryViewHolder extends ParentViewHolder {

        ImageView imgIconCorrect;
        private TextView textSubCategoryName;

        ParentSubCategoryViewHolder(@NonNull View itemView) {
            // init views
            super(itemView);
            textSubCategoryName = itemView.findViewById(R.id.text_sub_category_name);
            imgIconCorrect = itemView.findViewById(R.id.img_icon_correct);


            //     on click of check box parent row
            // select/deselect all child row of parent if parent is select/deselect
//            imgIconCorrect.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    if (!isSingleSelection) {
//                        StrategicPartnerServiceModel recipe = mSubCategoriesList.get(getParentAdapterPosition());
//                        recipe.isSelected = !recipe.isSelected;
//                        setAllChildSelected(recipe);
//                        notifyDataSetChanged();
//                    }
//                }
//            });
        }

        // bind data with view parent row
        public void bind(@NonNull StrategicPartnerServiceModel subServiceDetailModel) {
            imgIconCorrect.setSelected(subServiceDetailModel.isSelected);
            textSubCategoryName.setText(subServiceDetailModel.name);
        }

    }

    /**
     * select/deselect all child row of parent if parent is select/deselect
     */
    private void setAllChildSelected(StrategicPartnerServiceModel recipe) {
        for (AllSubSubCat ingredient : recipe.allSubSubCats) {
            ingredient.isSelected = recipe.isSelected;
        }
        notifyDataSetChanged();
    }

    /**
     * View Holder for Child row
     */
    class ChildSubCategoryViewHolder extends ChildViewHolder {
        private TextView textSubCategoryName;
        private TextView textSubCategoryPrice;
        private TextView textPackageDescription;
        private ImageView imgIconCorrect;
        private LinearLayout ln_root;

        ChildSubCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // init views
            textSubCategoryName = itemView.findViewById(R.id.text_sub_category_name);
            imgIconCorrect = itemView.findViewById(R.id.img_icon_correct);
            textSubCategoryPrice = itemView.findViewById(R.id.text_sub_category_price);
            textPackageDescription = itemView.findViewById(R.id.text_package_description);
            ln_root = itemView.findViewById(R.id.ln_root);

            // on click of check box
            ln_root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // get click child row and its parent
                    int parentPos = getParentAdapterPosition();
                    int childPos = getChildAdapterPosition();

                    // if single child is selected then parent also should be selected
                    // if all children are deselected then parent should be deselected

                    if (!isSingleSelection) {
                        AllSubSubCat subSubCat = mSubCategoriesList.get(parentPos).allSubSubCats.get(childPos);
                        subSubCat.isSelected = !subSubCat.isSelected;
                        if (subSubCat.isSelected)
                            mSubCategoriesList.get(parentPos).isSelected = true;
                        else {
                            int flag = 0;
                            for (AllSubSubCat allSubSubCat : mSubCategoriesList.get(parentPos).allSubSubCats) {
                                if (!allSubSubCat.isSelected)
                                    flag++;
                            }
                            if (flag == mSubCategoriesList.get(parentPos).allSubSubCats.size())
                                mSubCategoriesList.get(parentPos).isSelected = false;
                        }
                        notifyDataSetChanged();
                    } else {
                        for (int i = 0; i < mSubCategoriesList.size(); i++) {

                            for (int j = 0; j < mSubCategoriesList.get(i).allSubSubCats.size(); j++) {

                                mSubCategoriesList.get(i).allSubSubCats.get(j).isSelected = (i == parentPos && j == childPos);
                                mSubCategoriesList.get(i).isSelected = i == parentPos;
                            }
                        }
                        notifyDataSetChanged();

                    }

                }
            });
        }

        // bind data with view for child row
        public void bind(@NonNull AllSubSubCat subSubCat) {

            textSubCategoryName.setText(subSubCat.subSubCatName);
            if (subSubCat.package_description != null && !subSubCat.package_description.isEmpty()) {
                textPackageDescription.setText(subSubCat.package_description);
                textPackageDescription.setVisibility(View.VISIBLE);
            } else {
                textPackageDescription.setVisibility(View.GONE);
            }
            textSubCategoryPrice.setText(itemView.getContext().getString(R.string.ruppe_symbol_x, Utility.getQuotePriceFormatter(String.valueOf(subSubCat.price))));
            imgIconCorrect.setSelected(subSubCat.isSelected);
            textSubCategoryPrice.setSelected(subSubCat.isSelected);
        }

    }

}
