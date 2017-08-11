package com.cheep.strategicpartner;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cheep.R;

import java.util.ArrayList;

/**
 * Created by giteeka on 1/8/17.
 * This adapter is for custom drop down menu strategic partner phase 2
 */

class DropDownAdapter extends RecyclerView.Adapter<DropDownAdapter.MyViewHolder> {

    /**
     * Listener for click item of menu
     */
    interface ClickItem {
        void clickItem(int pos);
    }

    private ArrayList<QueAnsModel.DropDownModel> mList;
    private ClickItem mListener;
    public void setListener(ClickItem listener) {
        mListener = listener;
    }

    DropDownAdapter(ArrayList<QueAnsModel.DropDownModel> list) {
        mList = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_drop_down, parent, false);
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


    public void setSelected(int i) {
        for (int i1 = 0; i1 < mList.size(); i1++) {
            QueAnsModel.DropDownModel dropDownModel = mList.get(i1);
            dropDownModel.isSelected = i1 == i;
        }
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTextView;

        MyViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.txt_drop_down);
            itemView.setOnClickListener(this);
        }


        void bind(QueAnsModel.DropDownModel dropDownModel) {
            mTextView.setText(dropDownModel.dropdown_answer);
            mTextView.setSelected(dropDownModel.isSelected);

            // to set proper background of menu
            if (getAdapterPosition() == 0)
                mTextView.setBackgroundResource(R.drawable.bg_drop_down_item_top);
            else if (getAdapterPosition() == mList.size() - 1)
                mTextView.setBackgroundResource(R.drawable.bg_drop_down_item_last);
            else
                mTextView.setBackgroundResource(R.drawable.bg_drop_down_item_middle);

            // dynamically setting text color for selected item
            if (dropDownModel.isSelected) {
                mTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            } else {
                mTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.black));
            }
        }

        @Override
        public void onClick(View view) {
            mListener.clickItem(getAdapterPosition());
        }
    }
}
