package com.cheep.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import com.cheep.R;
import com.cheep.custom_view.CFTextViewRegular;
import com.cheep.model.JobCategoryModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by pankaj on 9/28/16.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    ArrayList<JobCategoryModel> mList;
    CategoryRowInteractionListener listener;

    public SearchAdapter() {

    }

    public SearchAdapter(CategoryRowInteractionListener listener) {
        this.mList = new ArrayList<>();
        this.listener = listener;
    }

    public SearchAdapter(ArrayList<JobCategoryModel> mList, CategoryRowInteractionListener listener) {
        this.mList = mList;
        this.listener = listener;
    }

    public void updateItems(ArrayList<JobCategoryModel> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SearchAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_search, parent, false));
    }

    @Override
    public void onBindViewHolder(SearchAdapter.ViewHolder holder, final int position) {
        final JobCategoryModel model = mList.get(holder.getAdapterPosition());
        holder.mTextResult.setText(model.catName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onCategoryRowClicked(model, position);
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
        public final CFTextViewRegular mTextResult;

        public ViewHolder(View rootView) {
            super(rootView);
            mView = rootView;
            mTextResult = (CFTextViewRegular) rootView;
        }
    }

    //Testing code for Filtering

    UserFilter userFilter;

    public UserFilter getUserFilter(List<JobCategoryModel> mList) {
        if (userFilter == null)
            userFilter = new UserFilter(this, mList);
        return userFilter;
    }

    public static class UserFilter extends Filter {

        private final SearchAdapter adapter;

        private final List<JobCategoryModel> originalList;

        private final List<JobCategoryModel> filteredList;

        private UserFilter(SearchAdapter adapter, List<JobCategoryModel> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = new LinkedList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        public FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (constraint.toString().trim().length() == 0) {
//                filteredList.addAll(originalList);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();

                for (final JobCategoryModel user : originalList) {
                    if (user.catName.toLowerCase().contains(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            publishResults(constraint, results);
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.mList = ((ArrayList<JobCategoryModel>) results.values);
            adapter.notifyDataSetChanged();
        }
    }

    public interface CategoryRowInteractionListener {
        void onCategoryRowClicked(JobCategoryModel model, int position);
    }
}
