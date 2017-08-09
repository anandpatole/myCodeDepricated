package com.cheep.strategicpartner;

import android.content.Context;
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
 */

public class DropDownAdapter extends RecyclerView.Adapter<DropDownAdapter.MyViewHolder> {
    Context mContext;
    ArrayList<QueAnsModel.DropDownModel> mList;
    private ClickItem mListener;

    public void setListener(ClickItem listener) {
        mListener = listener;
    }

    interface ClickItem {
        void clickItem(int pos);
    }

    DropDownAdapter() {

    }

    public DropDownAdapter(Context context, ArrayList<QueAnsModel.DropDownModel> list) {
        mContext = context;
        mList = list;
    }

//    @Override
//
//    public int getCount() {
//        return mList.size();
//    }
//
//    @Override
//    public QueAnsModel.DropDownModel getItem(int i) {
//        return mList.get(i);
//    }
//
//    @Override
//    public long getItemId(int i) {
//        return 0;
//    }
//
//    @Override
//    public View getView(int i, View view, ViewGroup viewGroup) {
//        ViewDataHolder viewDataHolder;
//        if (view == null) {
//            view = LayoutInflater.from(mContext).inflate(R.layout.row_drop_down, null);
//            viewDataHolder = new ViewDataHolder();
//            view.setTag(viewDataHolder);
//            viewDataHolder.mTextView = view.findViewById(R.id.txt_drop_down);
//        } else {
//            viewDataHolder = (ViewDataHolder) view.getTag();
//        }
//        viewDataHolder.mTextView.setText(mediaModel.dropdown_answer);
//        viewDataHolder.mTextView.setSelected(mediaModel.isSelected);
//
//        if (mediaModel.isSelected) {
//            viewDataHolder.mTextView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.dark_blue_variant_1));
//            viewDataHolder.mTextView.setTextColor(ContextCompat.getColor(mContext, R.color.white));
//        } else {
//            viewDataHolder.mTextView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
//            viewDataHolder.mTextView.setTextColor(ContextCompat.getColor(mContext, R.color.dark_blue_variant_1));
//        }
//        return view;
//    }

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

        public MyViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.txt_drop_down);
            itemView.setOnClickListener(this);
        }


        void bind(QueAnsModel.DropDownModel mediaModel) {
            mTextView.setText(mediaModel.dropdown_answer);
            mTextView.setSelected(mediaModel.isSelected);
            if (getAdapterPosition() == 0)
                mTextView.setBackgroundResource(R.drawable.bg_item_top);
            else if (getAdapterPosition() == mList.size() - 1)
                mTextView.setBackgroundResource(R.drawable.bg_item_last);
            else
                mTextView.setBackgroundResource(R.drawable.bg_item_middle);

            if (mediaModel.isSelected) {
//                mTextView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.dark_blue_variant_1));
                mTextView.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            } else {
//                mTextView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                mTextView.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            }
        }

        @Override
        public void onClick(View view) {
            mListener.clickItem(getAdapterPosition());
        }
    }
}
