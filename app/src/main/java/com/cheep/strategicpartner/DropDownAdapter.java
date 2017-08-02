package com.cheep.strategicpartner;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cheep.R;

import java.util.ArrayList;

/**
 * Created by giteeka on 1/8/17.
 */

public class DropDownAdapter extends BaseAdapter {
    Context mContext;
    ArrayList<QueAnsModel.DropDownModel> mList;

    DropDownAdapter() {

    }

    public DropDownAdapter(Context context, ArrayList<QueAnsModel.DropDownModel> list) {
        mContext = context;
        mList = list;
    }

    @Override

    public int getCount() {
        return mList.size();
    }

    @Override
    public QueAnsModel.DropDownModel getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewDataHolder viewDataHolder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.row_drop_down, null);
            viewDataHolder = new ViewDataHolder();
            view.setTag(viewDataHolder);
            viewDataHolder.mTextView = view.findViewById(R.id.txt_drop_down);
        } else {
            viewDataHolder = (ViewDataHolder) view.getTag();
        }
        viewDataHolder.mTextView.setText(getItem(i).dropdown_answer);
        viewDataHolder.mTextView.setSelected(getItem(i).isSelected);

        if (getItem(i).isSelected) {
            viewDataHolder.mTextView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.dark_blue_variant_1));
            viewDataHolder.mTextView.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        } else {
            viewDataHolder.mTextView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
            viewDataHolder.mTextView.setTextColor(ContextCompat.getColor(mContext, R.color.dark_blue_variant_1));
        }
        return view;
    }

    public void setSelected(int i) {
        for (int i1 = 0; i1 < mList.size(); i1++) {
            QueAnsModel.DropDownModel dropDownModel = mList.get(i1);
            dropDownModel.isSelected = i1 == i;
        }
        notifyDataSetChanged();
    }

    private class ViewDataHolder {
        TextView mTextView;
    }
}
