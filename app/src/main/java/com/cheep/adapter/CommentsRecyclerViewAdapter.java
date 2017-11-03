package com.cheep.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowCommentsBinding;
import com.cheep.model.CommentsModel;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by pankaj on 9/29/16.
 */

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<CommentsRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = CommentsRecyclerViewAdapter.class.getSimpleName();
    private ArrayList<CommentsModel> mList;
    private SuperCalendar superCalendar;

    public CommentsRecyclerViewAdapter() {
        this.mList = new ArrayList<>();
    }

    public CommentsRecyclerViewAdapter(ArrayList<CommentsModel> mList) {
        this.mList = mList;
    }

    public void setList(ArrayList<CommentsModel> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void addToList(ArrayList<CommentsModel> mList) {
        if (this.mList == null) {
            this.mList = new ArrayList<>();
        }
        this.mList.addAll(mList);
        notifyDataSetChanged();
    }

    public void addToList(CommentsModel model) {
        if (this.mList == null) {
            this.mList = new ArrayList<>();
        }
        this.mList.add(model);
        notifyItemInserted(this.mList.size());
    }

    @Override
    public CommentsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        superCalendar = SuperCalendar.getInstance();
        RowCommentsBinding mRowCommentsBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_comments, parent, false);
        return new CommentsRecyclerViewAdapter.ViewHolder(mRowCommentsBinding);
    }

    @Override
    public void onBindViewHolder(final CommentsRecyclerViewAdapter.ViewHolder holder, int position) {

        final CommentsModel model = mList.get(holder.getAdapterPosition());

        holder.mRowCommentsBinding.textName.setText(model.commenter_name);
        holder.mRowCommentsBinding.textMessage.setText(model.comment);


        try {
            superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            superCalendar.setTimeInString(model.comment_date, Utility.DATE_TIME_FORMAT_SERVICE_YEAR);
            superCalendar.setLocaleTimeZone();
            holder.mRowCommentsBinding.textDate.setText(superCalendar.format(Utility.DATE_FORMAT_DD_MMM));
        } catch (ParseException e) {
            e.printStackTrace();
            holder.mRowCommentsBinding.textDate.setText(model.comment_date);
        }

        Utility.showCircularImageView(holder.mRowCommentsBinding.textMessage.getContext(), TAG, holder.mRowCommentsBinding.imgProfile, model.commenter_profile_image, Utility.DEFAULT_CHEEP_LOGO);

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public ArrayList<CommentsModel> getmList() {
        return mList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowCommentsBinding mRowCommentsBinding;

        public ViewHolder(RowCommentsBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowCommentsBinding = binding;
        }
    }
}
