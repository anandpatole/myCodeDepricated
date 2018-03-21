package com.cheep.cheepcare.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowMediaFileCcBinding;
import com.cheep.utils.AmazonUtils;
import com.cheep.model.MediaModel;
import com.cheep.utils.GlideUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bhavesh on 1/3/18.
 */

public class MediaFullScreenAdapter extends RecyclerView.Adapter<MediaFullScreenAdapter.MediaViewHolder> {

    private static final String TAG = MediaFullScreenAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_PADDING = 1;
    private static final int VIEW_TYPE_ITEM = 2;
    private final MediaInteractionListener mListener;

    private List<MediaModel> mList = new ArrayList<>();

    public interface MediaInteractionListener {

        void onItemClick(int position, MediaModel model);

        void onRemoveClick(int position, MediaModel model);

    }

    public MediaFullScreenAdapter(MediaInteractionListener listener) {
        mListener = listener;
    }

    /*@Override
    public int getItemViewType(int position) {
        if (position == 0 || position == getItemCount() - 1) {
            return VIEW_TYPE_PADDING;
        }
        return VIEW_TYPE_ITEM;
    }*/

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RowMediaFileCcBinding binding = DataBindingUtil.inflate(
                layoutInflater
                , R.layout.row_media_file_cc
                , parent
                , false
        );
        return new MediaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final MediaViewHolder holder, final int position) {
//        if (getItemViewType(holder.getAdapterPosition()) == VIEW_TYPE_PADDING) {
//            holder.mBinding.imgRemove.setVisibility(View.GONE);
//            holder.mBinding.imgThumb.setVisibility(View.GONE);
//        } else {
        MediaModel model = mList.get(holder.getAdapterPosition()/* - 1*/);
        Context context = holder.mBinding.getRoot().getContext();
        holder.mBinding.imgRemove.setVisibility(View.VISIBLE);
        holder.mBinding.imgThumb.setVisibility(View.VISIBLE);
        GlideUtility.loadImageView(context
                , holder.mBinding.imgThumb
                , model.localFilePath);
//        }
    }

    @Override
    public int getItemCount() {
        return mList.size()/* + 2*/;// We have to add 2 paddings
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {

        public RowMediaFileCcBinding mBinding;

        public MediaViewHolder(RowMediaFileCcBinding binding) {
            super(binding.getRoot());
            mBinding = binding;


            mBinding.flContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick() called with: v = [" + v + "]");
                    mListener.onItemClick(getAdapterPosition()/* - 1*/, mList.get(getAdapterPosition()/* - 1*/));
                }
            });
            mBinding.imgRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick() called with: v = [" + v + "]");
                    mListener.onRemoveClick(getAdapterPosition()/* - 1*/, mList.get(getAdapterPosition()/* - 1*/));
                }
            });
        }

    }

    public int getListSize() {
        return mList.size();
    }

    public void addAll(ArrayList<MediaModel> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void add(MediaModel media) {
        mList.add(media);
//        notifyDataSetChanged();
        notifyItemInserted(mList.size()/* + 1*/);
    }

    public void removeItem(Context context, int position, MediaModel model) {
        if (mList.contains(model)) {
            AmazonUtils.deleteFiles(context, model.mediaName, model.mediaThumbName);
            mList.remove(model);
            notifyItemRemoved(position);
        }
    }
}