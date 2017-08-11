package com.cheep.strategicpartner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cheep.R;
import com.cheep.utils.Utility;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Giteeka on 31/7/17.
 * Adapter to show thumbnails of selected image and video in Strategic partner Phase 2(Questionnary screen)
 */

class MediaRecycleAdapter extends RecyclerView.Adapter<MediaRecycleAdapter.MyViewHolder> {
    private static final String TAG = "MediaRecycleAdapter";
    private ArrayList<MediaModel> mList = new ArrayList<>();
    private ItemClick mItemClick;

    MediaRecycleAdapter(ItemClick itemClick) {
        mItemClick = itemClick;
    }

    void addImage(MediaModel path) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        mList.add(path);
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

    interface ItemClick {
        void removeMedia();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        ImageView mImgThumb = null;
        ImageView mImgRemove = null;

        MyViewHolder(View binding) {
            super(binding);
            mView = binding;
            mImgThumb = mView.findViewById(R.id.imgThumb);
            mImgRemove = mView.findViewById(R.id.imgRemove);
            mImgRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mList.remove(getAdapterPosition());
                    notifyDataSetChanged();
                    mItemClick.removeMedia();
                }
            });
        }

        void bind(final MediaModel mediaModel) {
            Log.i(TAG, "bind:>>  " + mediaModel.path);
            // set image thumbnails with rounder grey border around image view
            if (mediaModel.type == MediaModel.MediaType.IMAGE) {
                mImgThumb.setImageBitmap(BitmapFactory.decodeFile(mediaModel.path));
                Glide.with(mView.getContext()).load(mediaModel.path).asBitmap().thumbnail(0.2f).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<String, Bitmap>() {

                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Log.e("SARMAD_GLIDE", "onResourceReadyCalled");
                        Log.e("SARMAD_GLIDE", "Is Loaded from Cache = " + isFromMemoryCache);
                        Log.e("SARMAD_GLIDE", "Is First Time loaded = " + isFirstResource);
                        mImgThumb.setImageBitmap(Utility.getRoundedCornerBitmap(resource, mImgThumb.getContext()));
                        // how to tell if the Bitmap resource is Thumbnail or actually the large size image
                        return false;
                    }
                }).into(mImgThumb);
            } else {
                try {
                    mImgThumb.setImageBitmap(Utility.getRoundedCornerBitmap(Utility.getVideoThumbnail(mediaModel.path), mImgThumb.getContext()));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

}
