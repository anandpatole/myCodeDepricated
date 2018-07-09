package com.cheep.cheepcarenew.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowCityNameBinding;
import com.cheep.model.CityModel;

import java.util.ArrayList;

/**
 * Created by giteeka on 1/8/17.
 * This adapter is for custom drop down menu strategic partner phase 2
 */

public class DropDownCityAdapter extends RecyclerView.Adapter<DropDownCityAdapter.MyViewHolder> {

    /**
     * Listener for click item of menu
     */
    public interface ClickItem {
        void clickItem(int pos);

        void dismissDialog();
    }

    private ArrayList<CityModel> mList;
    private ClickItem mListener;

    public void setListener(ClickItem listener) {
        mListener = listener;
    }

    public DropDownCityAdapter(ArrayList<CityModel> list) {
        mList = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RowCityNameBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_city_name, parent, false);
        return new MyViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        CityModel model = mList.get(position);

        holder.binding.tvCityName.setText(model.city);

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RowCityNameBinding binding;

        MyViewHolder(RowCityNameBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
            itemView.getRoot().setOnClickListener(this);
            binding.tvCityName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.clickItem(getAdapterPosition());

                }
            });
        }


        @Override
        public void onClick(View view) {
            mListener.clickItem(getAdapterPosition());
        }
    }
}
