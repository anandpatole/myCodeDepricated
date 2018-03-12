package com.cheep.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowNotificationBinding;
import com.cheep.model.NotificationModel;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.LoadMoreRecyclerAdapter;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by pankaj on 9/29/16.
 */

public class NotificationRecyclerViewAdapter extends LoadMoreRecyclerAdapter<NotificationRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = NotificationRecyclerViewAdapter.class.getSimpleName();
    private ArrayList<NotificationModel> mList;
    private NotificationItemInteractionListener mListener;
    private SuperCalendar superCalendar;

    public NotificationRecyclerViewAdapter(NotificationItemInteractionListener listener) {
        this.mList = new ArrayList<>();
        this.mListener = listener;
    }

    public NotificationRecyclerViewAdapter(ArrayList<NotificationModel> mList, NotificationItemInteractionListener listener) {
        this.mList = mList;
        this.mListener = listener;
    }

    @Override
    public ViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
        superCalendar = SuperCalendar.getInstance();
        RowNotificationBinding mRowNotificationBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_notification, parent, false);
        return new NotificationRecyclerViewAdapter.ViewHolder(mRowNotificationBinding);
    }


    @Override
    public void onActualBindViewHolder(final ViewHolder holder, int position) {

        final NotificationModel model = mList.get(holder.getAdapterPosition());

        holder.mRowNotificationBinding.textDesc.setText(model.message);
        holder.mRowNotificationBinding.textDate.setText(model.datetime);

        try {
            superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            superCalendar.setTimeInString(model.datetime, Utility.DATE_TIME_FORMAT_SERVICE_YEAR);
            superCalendar.setLocaleTimeZone();
            holder.mRowNotificationBinding.textDate.setText(superCalendar.format(Utility.DATE_FORMAT_DD_MMM));
        } catch (ParseException e) {
            e.printStackTrace();
            holder.mRowNotificationBinding.textDate.setText(model.datetime);
        }

        /**
         * @Changes by Bhavesh :-7th July 2017
         * If case we are showing Web Notification, we need to show cheep Logo.
         */
        if (Utility.NOTIFICATION_TYPE.WEB_CUSTOM_NOTIFICATION.equalsIgnoreCase(model.notificationType)) {
            holder.mRowNotificationBinding.imgPhoto.setImageResource(Utility.DEFAULT_CHEEP_LOGO);
        } else {
            // Show Circular image
            if (model.task_type.equalsIgnoreCase(Utility.TASK_TYPE.NORMAL)) {
                GlideUtility.showCircularImageView(holder.mRowNotificationBinding.imgPhoto.getContext(), TAG, holder.mRowNotificationBinding.imgPhoto, model.sp_profile_image, Utility.DEFAULT_CHEEP_LOGO);
            } else {
                GlideUtility.showCircularImageViewWithColorBorder(holder.mRowNotificationBinding.imgPhoto.getContext(), TAG, holder.mRowNotificationBinding.imgPhoto, model.sp_profile_image, Utility.DEFAULT_CHEEP_LOGO, R.color.grey_dark_color, true);
            }
        }

        // Set OnClicklistener
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mList != null) {
                    mListener.onNotificationRowClicked(model, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int onActualItemCount() {
        return mList.size();
    }

    public void setItem(ArrayList<NotificationModel> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void addItem(ArrayList<NotificationModel> mList) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        this.mList.addAll(mList);
        notifyDataSetChanged();
    }


    public ArrayList<NotificationModel> getmList() {
        return mList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowNotificationBinding mRowNotificationBinding;

        public ViewHolder(RowNotificationBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowNotificationBinding = binding;
        }
    }

    public interface NotificationItemInteractionListener {
        void onNotificationRowClicked(NotificationModel model, int position);
    }
}
