package com.cheep.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowProviderBinding;
import com.cheep.model.ProviderModel;
import com.cheep.utils.LoadMoreRecyclerAdapter;
import com.cheep.utils.Utility;

import java.util.ArrayList;

/**
 * Created by pankaj on 9/27/16.
 */

public class ProviderRecyclerViewAdapter extends LoadMoreRecyclerAdapter<ProviderRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "ProviderRecyclerViewAda";
    private ProviderRowInteractionListener listener;
    Context context;

    ArrayList<ProviderModel> mList;
    String currentAction;

    public ProviderRecyclerViewAdapter(ProviderRowInteractionListener listener, String currentAction) {
        this.mList = new ArrayList<>();
        this.listener = listener;
        this.currentAction = currentAction;
    }

    public ProviderRecyclerViewAdapter(ArrayList<ProviderModel> mList, ProviderRowInteractionListener listener) {
        this.mList = mList;
        this.listener = listener;
    }

    public void setList(ArrayList<ProviderModel> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void addToList(ArrayList<ProviderModel> mList) {
        if (this.mList == null) {
            this.mList = new ArrayList<>();
        }
        this.mList.addAll(mList);
        notifyDataSetChanged();
    }

    /*@Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewDataBinding mRowTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_provider, parent, false);
        return new ViewHolder(mRowTaskBinding);
    }*/

    @Override
    public ViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewDataBinding mRowTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_provider, parent, false);
        return new ViewHolder(mRowTaskBinding);
    }


    @Override
    public void onActualBindViewHolder(final ViewHolder holder, int position) {
        final ProviderModel model = mList.get(holder.getAdapterPosition());

        if (model.low_price != null && model.low_price.equalsIgnoreCase("1")) {
            holder.mView.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_varient_5));
            holder.mRowProviderBinding.textCheapest.setVisibility(View.VISIBLE);
            holder.mRowProviderBinding.textCheapest.setText(holder.mView.getContext().getString(R.string.label_cheepest_strip));
            holder.mRowProviderBinding.textCheapest.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.icon_cheapest_quote), null, null, null);
        } else if (model.high_rating != null && model.high_rating.equalsIgnoreCase("1")) {
            holder.mRowProviderBinding.textCheapest.setText(holder.mView.getContext().getString(R.string.label_highest_rated_strip));
            holder.mView.setBackgroundColor(ContextCompat.getColor(context, R.color.yellow_varient_1));
            holder.mRowProviderBinding.textCheapest.setVisibility(View.VISIBLE);
            holder.mRowProviderBinding.textCheapest.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.icon_highest_rating), null, null, null);
        } else {
            holder.mView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            holder.mRowProviderBinding.textCheapest.setVisibility(View.GONE);
        }

        Utility.showCircularImageView(holder.mRowProviderBinding.imgProfile.getContext(), TAG, holder.mRowProviderBinding.imgProfile, model.profileUrl, Utility.DEFAULT_CHEEP_LOGO);
        holder.mRowProviderBinding.textName.setText(model.userName);
        holder.mRowProviderBinding.textTotalJobs.setText(Utility.getJobs(context, model.jobsCount));
//        holder.mRowProviderBinding.textTotalReviews.setText(context.getString(R.string.label_x_reviews, model.reviews));
//        holder.mRowProviderBinding.textTotalReviews.setText(context.getString(R.string.label_x_reviews, model.reviews));
        holder.mRowProviderBinding.textDesc.setText(model.information);

        holder.mRowProviderBinding.textVerified.setText(Utility.BOOLEAN.YES.equalsIgnoreCase(model.isVerified) ? context.getString(R.string.label_verified) : context.getString(R.string.label_pending));
        /*if (Utility.BOOLEAN.YES.equalsIgnoreCase(model.isVerified)) {
            holder.mRowProviderBinding.textVerified.setTextColor(ContextCompat.getColor(context, R.color.black));
        } else {
            holder.mRowProviderBinding.textVerified.setTextColor(ContextCompat.getColor(context, R.color.yellow));
        }*/

        /*if (!TextUtils.isEmpty(model.experience)) {
            holder.mRowProviderBinding.textExperience.setVisibility(View.VISIBLE);
            holder.mRowProviderBinding.textExperience.setText(context.getString(R.string.label_experience_zero));
        } else {
            holder.mRowProviderBinding.textExperience.setVisibility(View.GONE);
        }*/
        //experience
        if (TextUtils.isEmpty(model.experience)
                || Utility.ZERO_STRING.equals(model.experience)) {
            holder.mRowProviderBinding.textExperience.setText(context.getString(R.string.label_experience_zero));
        } else {
            holder.mRowProviderBinding.textExperience.setText(Utility.getExperienceString(model.experience));
        }


        holder.mRowProviderBinding.textMinToArrive.setSelected(true);
        if (model.distance != null) {
            holder.mRowProviderBinding.textMinToArrive.setVisibility(View.VISIBLE);
            if (model.sp_locality != null) {
                holder.mRowProviderBinding.textMinToArrive.setText(model.sp_locality + ", " + model.distance + " away");
            } else {
                holder.mRowProviderBinding.textMinToArrive.setText(model.distance + " away");
            }
        } else {
            holder.mRowProviderBinding.textMinToArrive.setVisibility(View.GONE);
        }

      /*  holder.mRowProviderBinding.layoutPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onProviderPayClicked(model, holder.getAdapterPosition());
                }
            }
        });*/


        Utility.showRating(model.rating, holder.mRowProviderBinding.ratingBar);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onProviderRowClicked(model, holder.getAdapterPosition());
                }
            }
        });

        // Set Marquee forever
        holder.mRowProviderBinding.textPrice.setSelected(true);
        holder.mRowProviderBinding.textPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProviderPayClicked(model, holder.getAdapterPosition());
            }
        });

        // Change the background logic that first one would have different color
        /*if (holder.getAdapterPosition() == 0 && currentAction.equals(Utility.ACTION_HIRE_PROVIDER)) {
            holder.mView.setBackgroundColor(ContextCompat.getColor(context, R.color.cheepest_bg_color));
        } else {
            holder.mView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }*/

        // Checking if amount present then show call and paid lables else hide
        if (model.getQuotePriceInInteger() > 0) {
            holder.mRowProviderBinding.textPrice.setVisibility(View.VISIBLE);
            holder.mRowProviderBinding.textPrice.setText(context.getString(R.string.label_book_rs, model.quotePrice));
        } else {
            holder.mRowProviderBinding.textPrice.setVisibility(View.GONE);
        }

        // Check for DetailAction Request Type
        if (Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.INITIAL.equalsIgnoreCase(model.request_detail_status)) {
           /* holder.mRowProviderBinding.imgChat.setVisibility(View.VISIBLE);
            holder.mRowProviderBinding.imgCall.setVisibility(View.VISIBLE);*/
            //holder.mRowProviderBinding.frameAction.setVisibility(View.GONE);
            holder.mRowProviderBinding.textContactRequest.setVisibility(View.GONE);
        } else if (Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ALREADY_REQUESTED.equalsIgnoreCase(model.request_detail_status)) {
          /*  holder.mRowProviderBinding.imgChat.setVisibility(View.VISIBLE);
            holder.mRowProviderBinding.imgCall.setVisibility(View.VISIBLE);*/
            //holder.mRowProviderBinding.frameAction.setVisibility(View.GONE);
            holder.mRowProviderBinding.textContactRequest.setVisibility(View.VISIBLE);

        } else if (Utility.SEND_TASK_DETAIL_REQUESTED_STATUS.ACCEPTED.equalsIgnoreCase(model.request_detail_status)) {
            //holder.mRowProviderBinding.frameAction.setVisibility(View.GONE);
            holder.mRowProviderBinding.textContactRequest.setVisibility(View.GONE);
        } else {
            holder.mRowProviderBinding.textContactRequest.setVisibility(View.GONE);
            // If in case its null we should make it as GONE

            //holder.mRowProviderBinding.frameAction.setVisibility(View.GONE);

        }

    }

    // holder.mRowProviderBinding.textChat.setVisibility(View.GONE);

    public ArrayList<ProviderModel> getmList() {
        return mList;
    }

    //For commenting first method for loadmore

    /*@Override
    public int getItemCount() {
        return mList.size();
    }*/

    @Override
    public int onActualItemCount() {
        return mList.size();
    }

    public void updateFavStatus(String id, String isFav) {
        if (mList != null) {
            for (ProviderModel providerModel : mList) {
                if (providerModel.providerId.equalsIgnoreCase(id)) {
                    providerModel.isFavourite = isFav;
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    public void updateModelForRequestDetailStatus(String SPUserID, String request_detail_status) {
        if (mList != null) {
            for (ProviderModel model : mList) {
                if (SPUserID.equals(model.providerId)) {
                    model.request_detail_status = request_detail_status;
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    public void removeModelForRequestDetailStatus(String SPUserID, String request_detail_status) {
        if (mList != null) {
            for (ProviderModel model : mList) {
                if (SPUserID.equals(model.providerId)) {
                    model.request_detail_status = request_detail_status;
                    if (model.getQuotePriceInInteger() <= 0) {
                        mList.remove(model);
                    }
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public RowProviderBinding mRowProviderBinding;

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowProviderBinding = (RowProviderBinding) binding;
        }
    }


    public interface ProviderRowInteractionListener {
        void onProviderRowClicked(ProviderModel providerModel, int position);

        void onProviderPayClicked(ProviderModel providerModel, int position);

        void onActionButtonClicked(ProviderModel providerModel, int position);

        void onChatClicked(ProviderModel providerModel, int position);

        void onCallClicked(ProviderModel providerModel, int position);

        void onFavClicked(ProviderModel providerModel, boolean isAddToFav, int position);
    }
}
