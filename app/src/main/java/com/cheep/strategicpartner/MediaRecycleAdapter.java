package com.cheep.strategicpartner;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cheep.R;
import com.cheep.strategicpartner.model.MediaModel;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;

import java.util.ArrayList;

/**
 * Created by Giteeka on 31/7/17.
 * Adapter to show thumbnails of selected image and video in Strategic partner Phase 2(Questionnary screen)
 */

public class MediaRecycleAdapter extends RecyclerView.Adapter<MediaRecycleAdapter.MyViewHolder> {
    private static final String TAG = MediaRecycleAdapter.class.getSimpleName();
    private ArrayList<MediaModel> mList = new ArrayList<>();
    private ItemClick mItemClick;
    private boolean mIsStrategicPartner;

    public MediaRecycleAdapter(ItemClick itemClick, boolean isStrategicPartner) {
        mItemClick = itemClick;
        mIsStrategicPartner = isStrategicPartner;
    }

    public void addImage(MediaModel mediaModel) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        mList.add(mediaModel);
        notifyDataSetChanged();
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_media_file, parent, false);
        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bind(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public ArrayList<MediaModel> getList() {
        return mList;
    }

    public interface ItemClick {
        void removeMedia();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        ImageView mImgThumb = null;
        ImageView mImgRemove = null;
        ImageView mImgPlay = null;

        MyViewHolder(final View binding) {
            super(binding);
            mView = binding;
            mImgThumb = mView.findViewById(R.id.imgThumb);
            mImgRemove = mView.findViewById(R.id.imgRemove);
            mImgPlay = mView.findViewById(R.id.imgPlay);
            mImgRemove.setImageResource(mIsStrategicPartner ? R.drawable.ic_remove : R.drawable.ic_remove_blue);
            mImgRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MediaModel mediaModel = mList.get(getAdapterPosition());
                    AmazonUtils.deleteFiles(mView.getContext(), mediaModel.mediaName, mediaModel.mediaThumbName);
                    mList.remove(getAdapterPosition());
                    notifyDataSetChanged();
                    mItemClick.removeMedia();
                }
            });
        }

        void bind(final MediaModel mediaModel) {
            LogUtils.LOGI(TAG, "bind:>>  " + mediaModel.mediaName);

            mImgPlay.setVisibility(
                    !mIsStrategicPartner && mediaModel.mediaType.equalsIgnoreCase(MediaModel.MediaType.TYPE_VIDEO) ?
                            View.VISIBLE:
                            View.GONE);

            Glide.with(mView.getContext()).load(mediaModel.localFilePath).asBitmap().thumbnail(0.2f).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<String, Bitmap>() {

                @Override
                public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    if (mIsStrategicPartner) {
                        mImgThumb.setImageBitmap(Utility.getRoundedCornerBitmap(resource, mImgThumb.getContext()));
                    } else {
                        mImgThumb.setImageBitmap(resource);
                    }
                    return true;
                }
            }).into(mImgThumb);
        }
    }

}
