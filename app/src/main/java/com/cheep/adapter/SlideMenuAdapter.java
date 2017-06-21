package com.cheep.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cheep.R;
import com.cheep.model.SlideMenuListModel;

import java.util.ArrayList;

public class SlideMenuAdapter extends BaseAdapter {
    private ArrayList<SlideMenuListModel> list;
    private SlideMenuListItemInterface mSlideMenuListItemInterface;

    public SlideMenuAdapter(ArrayList<SlideMenuListModel> list, SlideMenuListItemInterface mSlideMenuListItemInterface) {
        this.list = list;
        this.mSlideMenuListItemInterface = mSlideMenuListItemInterface;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder mHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_slide_menu_listing, parent, false);
            mHolder = new ViewHolder(convertView);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }


        mHolder.mIcon.setImageResource(list.get(position).image_id);
        mHolder.mName.setText(list.get(position).title);


        mHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSelectionAtIndex(position);
                mSlideMenuListItemInterface.onSlideMenuListItemClicked(list.get(position), position);
            }
        });

        // There is NO Separator for any of the section so, anyways we will hide them out.
        mHolder.separator.setVisibility(list.get(position).separatorEnabled ? View.VISIBLE : View.GONE);
        return convertView;
    }

    private void changeSelectionAtIndex(int index) {
        for (int i = 0; i < list.size(); i++) {
            if (i == index) {
                list.get(i).isSelected = true;
            } else {
                list.get(i).isSelected = false;
            }
        }
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        View mView;
        ImageView mIcon;
        TextView mName;
        View separator;

        ViewHolder(View convertView) {
            mView = convertView;
            mIcon = (ImageView) convertView.findViewById(R.id.icon);
            mName = (TextView) convertView.findViewById(R.id.name);
            separator = (View) convertView.findViewById(R.id.separator);
        }
    }


    public interface SlideMenuListItemInterface {
        void onSlideMenuListItemClicked(SlideMenuListModel slideMenuListModel, int position);
    }
}