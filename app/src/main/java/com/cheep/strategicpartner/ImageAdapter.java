package com.cheep.strategicpartner;

import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cheep.R;
import com.cheep.utils.Utility;

import java.util.ArrayList;

/**
 * Created by pankaj on 9/30/16.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {
    ArrayList<MediaModel> mList = new ArrayList<>();
    ItemClick mItemClick;

    public ImageAdapter(ItemClick itemClick) {
        mItemClick = itemClick;
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

    interface ItemClick {
        void removeMedia(int pos);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        ImageView mImgThumb = null;
        ImageView mImgRemove = null;

        public MyViewHolder(View binding) {
            super(binding);
            mView = binding;
            mImgThumb = mView.findViewById(R.id.imgThumb);
            mImgRemove = mView.findViewById(R.id.imgRemove);
            mImgRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mList.remove(getAdapterPosition());
                    notifyDataSetChanged();
                    mItemClick.removeMedia(getAdapterPosition());
                }
            });
        }

        void bind(MediaModel mediaModel) {
            if (mediaModel.type == MediaModel.MediaType.IMAGE)
//                Utility.showCircularImageView(mImgThumb.getContext(), "Image", mImgThumb, mediaModel.path, R.drawable.ic_camera, true);
//                Glide.with(mImgThumb.getContext()).load(mediaModel.path).into(mImgThumb);
                mImgThumb.setImageBitmap(Utility.getRoundedCornerBitmap(BitmapFactory.decodeFile(mediaModel.path), mImgThumb.getContext()));
            else
                try {
                    mImgThumb.setImageBitmap(Utility.getVideoThumbnail(mediaModel.path));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
        }
    }

}
