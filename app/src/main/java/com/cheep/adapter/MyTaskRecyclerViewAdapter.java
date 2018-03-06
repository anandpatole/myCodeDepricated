package com.cheep.adapter;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cheep.R;
import com.cheep.activity.ProviderProfileActivity;
import com.cheep.databinding.RowPastTaskProProfileBinding;
import com.cheep.model.CoverImageModel;
import com.cheep.model.JobCategoryModel;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.Utility;

import java.util.ArrayList;

/**
 * Created by pankaj on 9/27/16.
 */

public class MyTaskRecyclerViewAdapter extends RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder> {

    ArrayList<CoverImageModel> mList;
    ProviderProfileActivity.CategoryRowInteractionListener mListener;
    Context mContext;
    Activity activity;
    int height;
    int width;
    int imgHeight;

    public MyTaskRecyclerViewAdapter(Activity activity,ArrayList<CoverImageModel> mList, ProviderProfileActivity.CategoryRowInteractionListener mListener) {
        this.mList = mList;
        this.mListener = mListener;
        this.activity = activity;
        int heightWidth [] = Utility.getDeviceWidthHeight(activity);
        height = heightWidth[1];
        width = heightWidth[0];

        imgHeight = (height * 12) /100;
    }

    public void addItem(ArrayList<JobCategoryModel> mList) {

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        RowPastTaskProProfileBinding rowPastTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_past_task_pro_profile, parent, false);
        return new ViewHolder(rowPastTaskBinding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final CoverImageModel model = mList.get(holder.getAdapterPosition());
        LinearLayout.LayoutParams paramImage = (LinearLayout.LayoutParams) holder.rowPastTaskBinding.imgpastwork.getLayoutParams();
        paramImage.height = imgHeight;
        paramImage.width = imgHeight;
        holder.rowPastTaskBinding.imgpastwork.requestLayout();

        //Background image
        GlideUtility.loadImageView(mContext, holder.rowPastTaskBinding.imgpastwork, model.imgUrl, R.drawable.gradient_black);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onCategoryRowClicked(model, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowPastTaskProProfileBinding rowPastTaskBinding;

        public ViewHolder(RowPastTaskProProfileBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            rowPastTaskBinding = binding;
        }
    }

    public ArrayList<CoverImageModel> getmList() {
        return mList;
    }
}
