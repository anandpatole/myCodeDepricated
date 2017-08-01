package com.cheep.strategicpartner;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cheep.R;
import com.cheep.utils.Utility;

import java.util.ArrayList;

/**
 * Created by pankaj on 9/30/16.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {
    ArrayList<MediaModel> mList = new ArrayList<>();

    public ImageAdapter() {
    }

    public void addImage(MediaModel path) {
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

    class MyViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        ImageView mImageView = null;

        public MyViewHolder(View binding) {
            super(binding);
            mView = binding;
            mImageView = mView.findViewById(R.id.imgMedia);
        }

        void bind(MediaModel mediaModel) {
            if (mediaModel.type == MediaModel.MediaType.IMAGE)
                Glide.with(mImageView.getContext())
                        .load(mediaModel.path).into(mImageView);
            else
                try {
                    mImageView.setImageBitmap(Utility.getVideoThumbnail(mediaModel.path));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
        }
    }

}
