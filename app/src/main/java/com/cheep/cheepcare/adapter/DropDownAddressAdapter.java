package com.cheep.cheepcare.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.RowAddressDropDownBinding;
import com.cheep.model.AddressModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;

/**
 * Created by giteeka on 1/8/17.
 * This adapter is for custom drop down menu strategic partner phase 2
 */

class DropDownAddressAdapter extends RecyclerView.Adapter<DropDownAddressAdapter.MyViewHolder> {

    /**
     * Listener for click item of menu
     */
    interface ClickItem {
        void clickItem(int pos);

        void dismissDialog();
    }

    private ArrayList<AddressModel> mList;
    private ClickItem mListener;

    public void setListener(ClickItem listener) {
        mListener = listener;
    }

    DropDownAddressAdapter(ArrayList<AddressModel> list) {
        mList = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RowAddressDropDownBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_address_drop_down, parent, false);
        return new MyViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AddressModel model = mList.get(position);
        if (!TextUtils.isEmpty(model.nickname))
            holder.binding.tvAddressNickname.setText(model.nickname);
        else
            holder.binding.tvAddressNickname.setText(Utility.getAddressCategoryString(model.category));
        holder.binding.ivAddressIcon.setImageResource(Utility.getAddressCategoryBlueIcon(model.category));
        holder.binding.llAddressContainer.setVisibility(View.VISIBLE);
        holder.binding.ivUpArrow.setVisibility(position == 0 ? View.VISIBLE : View.GONE);

        holder.binding.tvAddress.setText(model.getAddressWithInitials());

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RowAddressDropDownBinding binding;

        MyViewHolder(RowAddressDropDownBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
            itemView.getRoot().setOnClickListener(this);
            binding.ivUpArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.dismissDialog();

                }
            });
        }


        @Override
        public void onClick(View view) {
            mListener.clickItem(getAdapterPosition());
        }
    }
}
