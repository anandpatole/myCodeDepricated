package com.cheep.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowFavouriteBinding;
import com.cheep.model.ProviderModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by pankaj on 9/27/16.
 */

public class FavouriteRecyclerViewAdapter extends RecyclerView.Adapter<FavouriteRecyclerViewAdapter.ViewHolder> {

    private FavouriteRowInteractionListener listener;
    Context context;

    ArrayList<ProviderModel> mList;

    public FavouriteRecyclerViewAdapter(FavouriteRowInteractionListener listener) {
        this.mList = new ArrayList<>();
        this.listener = listener;
    }

    public FavouriteRecyclerViewAdapter(ArrayList<ProviderModel> mList, FavouriteRowInteractionListener listener) {
        this.mList = mList;
        this.listener = listener;
    }

    public void setItem(ArrayList<ProviderModel> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void addItem(ArrayList<ProviderModel> mList) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        this.mList.addAll(mList);
        notifyDataSetChanged();
    }

    public void updateFavStatus(String id, String isFav) {
        if (mList != null) {
            for (ProviderModel model : mList) {
                if (model.providerId.equalsIgnoreCase(id)) {
                    model.isFavourite = isFav;
                    mList.remove(model);
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        RowFavouriteBinding mRowTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_favourite, parent, false);
        return new ViewHolder(mRowTaskBinding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final ProviderModel model = mList.get(holder.getAdapterPosition());
        Utility.showCircularImageView(holder.mRowFavouriteBinding.imgProfile.getContext(), TAG, holder.mRowFavouriteBinding.imgProfile, model.profileUrl,Utility.DEFAULT_CHEEP_LOGO);
        holder.mRowFavouriteBinding.textName.setText(model.userName);

        holder.mRowFavouriteBinding.textTotalReviews.setText(context.getString(R.string.label_x_reviews, model.reviews));
        holder.mRowFavouriteBinding.textVerified.setText(Utility.BOOLEAN.YES.equalsIgnoreCase(model.isVerified) ? context.getString(R.string.label_verified) : context.getString(R.string.label_pending));

        holder.mRowFavouriteBinding.textTotalJobs.setText(Utility.getJobs(context, model.jobsCount));
        holder.mRowFavouriteBinding.textTotalTasks.setText(Utility.getTasks(context, model.taskCount));

        if (Utility.BOOLEAN.YES.equalsIgnoreCase(model.isVerified)) {
            holder.mRowFavouriteBinding.textVerified.setTextColor(ContextCompat.getColor(context, R.color.black));
        } else {
            holder.mRowFavouriteBinding.textVerified.setTextColor(ContextCompat.getColor(context, R.color.yellow));
        }
        holder.mRowFavouriteBinding.textMinToArrive.setText(model.distance);
        holder.mRowFavouriteBinding.imgFav.setSelected(Utility.BOOLEAN.YES.equalsIgnoreCase(model.isFavourite));

        holder.mRowFavouriteBinding.imgFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.mRowFavouriteBinding.imgFav.setSelected(!holder.mRowFavouriteBinding.imgFav.isSelected());
                model.isFavourite = holder.mRowFavouriteBinding.imgFav.isSelected() ? Utility.BOOLEAN.YES : Utility.BOOLEAN.NO;

                if (listener != null) {
                    int position = holder.getAdapterPosition();
                    if (holder.mRowFavouriteBinding.imgFav.isSelected() == false) {
                        mList.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                    }
                    listener.onFavClicked(model, holder.mRowFavouriteBinding.imgFav.isSelected(), position);
                }
            }
        });
        Utility.showRating(model.rating, holder.mRowFavouriteBinding.ratingBar);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onFavouriteRowClicked(model, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public ArrayList<ProviderModel> getmList() {
        return mList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public RowFavouriteBinding mRowFavouriteBinding;

        public ViewHolder(RowFavouriteBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowFavouriteBinding = binding;
        }
    }

    public interface FavouriteRowInteractionListener {
        void onFavouriteRowClicked(ProviderModel providerModel, int position);

        void onFavClicked(ProviderModel providerModel, boolean isAddToFav, int position);
    }
}
