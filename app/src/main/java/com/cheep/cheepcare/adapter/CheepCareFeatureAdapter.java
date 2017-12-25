package com.cheep.cheepcare.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcare.model.CheepCareFeatureModel;
import com.cheep.databinding.LeftImageCheepCareFeatureListItemBinding;
import com.cheep.databinding.RightImageCheepCareFeatureListItemBinding;
import com.cheep.utils.LoadMoreRecyclerAdapter;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 12/21/17.
 */

public class CheepCareFeatureAdapter extends LoadMoreRecyclerAdapter<CheepCareFeatureAdapter.CheepCareFeatureBaseViewHolder> {

    public static final int ODD_POSITION = 1;
    public static final int EVEN_POSITION = 2;
    private List<CheepCareFeatureModel> mList = new ArrayList<>();

    @Override
    public int getItemViewType(int position) {
        if (position % 2 != 0) {
            return ODD_POSITION;
        } else {
            return EVEN_POSITION;
        }
    }

    @Override
    public CheepCareFeatureBaseViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == EVEN_POSITION) {
            View view = layoutInflater.inflate(R.layout.left_image_cheep_care_feature_list_item, parent, false);
            return new LeftSideImageViewHolder(view);
        } else {
            View view = layoutInflater.inflate(R.layout.right_image_cheep_care_feature_list_item, parent, false);
            return new RightSideImageViewHolder(view);
        }
    }

    @Override
    public void onActualBindViewHolder(CheepCareFeatureBaseViewHolder holder, int position) {
        CheepCareFeatureModel model = mList.get(position);
        if (holder instanceof LeftSideImageViewHolder) {
            LeftImageCheepCareFeatureListItemBinding leftBinding = holder.getBinding();
            Utility.loadImageView(leftBinding.getRoot().getContext()
                    , leftBinding.ivNoHiddenCharges
                    , Integer.parseInt(model.featureImage)
                    , R.drawable.hotline_ic_image_loading_placeholder);
            leftBinding.tvTitle.setText(model.featureTitle);
            leftBinding.tvDescription.setText(model.featureDescription);
        } else if (holder instanceof RightSideImageViewHolder) {
            RightImageCheepCareFeatureListItemBinding rightBinding = holder.getBinding();
            Utility.loadImageView(rightBinding.getRoot().getContext()
                    , rightBinding.ivNoHiddenCharges
                    , Integer.parseInt(model.featureImage)
                    , R.drawable.hotline_ic_image_loading_placeholder);
            rightBinding.tvTitle.setText(model.featureTitle);
            rightBinding.tvDescription.setText(model.featureDescription);
        }
    }

    @Override
    public int onActualItemCount() {
        return mList.size();
    }

    public class LeftSideImageViewHolder extends CheepCareFeatureBaseViewHolder {
        public LeftSideImageViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class RightSideImageViewHolder extends CheepCareFeatureBaseViewHolder {
        public RightSideImageViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class CheepCareFeatureBaseViewHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding mBinding;

        public CheepCareFeatureBaseViewHolder(View itemView) {
            super(itemView);
            setBinding(itemView);
        }

        public <T extends ViewDataBinding> T getBinding() {
            return (T) mBinding;
        }

        public void setBinding(View binding) {
            this.mBinding = DataBindingUtil.bind(binding);
        }
    }

    public void addFeatureList(List<CheepCareFeatureModel> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }
}
