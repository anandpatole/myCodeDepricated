package com.cheep.cheepcare.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.cheep.R;
import com.cheep.databinding.RowAddressTaskCreateBinding;
import com.cheep.model.AddressModel;
import com.cheep.utils.Utility;

import java.util.List;

/**
 * Created by pankaj on 12/25/17.
 */

public class AddressTaskCreateAdapter<T> extends ArrayAdapter<T> {

    private final List<AddressModel> mList;

    public AddressTaskCreateAdapter(@NonNull Context context, int resource, List<AddressModel> list) {
        super(context, resource);
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Nullable
    @Override
    public T getItem(int position) {
        return (T) mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final AddressViewHolder mHolder;


        if (convertView == null) {
            RowAddressTaskCreateBinding binding = DataBindingUtil.inflate(layoutInflater
                    , R.layout.row_address_task_create
                    , parent, false);
            convertView = binding.getRoot();
            mHolder = new AddressViewHolder(binding);
            convertView.setTag(mHolder);
        } else {
            mHolder = (AddressViewHolder) convertView.getTag();
        }

        if (position == mList.size() - 1) {
            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) mHolder.mBinding.llAddressMetaData.getLayoutParams();
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            mHolder.mBinding.llAddressContainer.setPadding(
                    (int) Utility.convertDpToPixel(12, context)
                    , (int) Utility.convertDpToPixel(12, context)
                    , (int) Utility.convertDpToPixel(12, context)
                    , 0
            );
            mHolder.mBinding.ivHome.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_white_plus_background_blue));
            mHolder.mBinding.tvAddressNickname.setText(context.getString(R.string.label_add_new));
            mHolder.mBinding.viewDot.setVisibility(View.GONE);
            mHolder.mBinding.tvLabelAddressSubscribed.setVisibility(View.GONE);
            mHolder.mBinding.tvAddress.setVisibility(View.GONE);
        } else {
            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) mHolder.mBinding.llAddressMetaData.getLayoutParams();
            layoutParams.gravity = Gravity.START;
            mHolder.mBinding.llAddressContainer.setPadding(
                    (int) Utility.convertDpToPixel(32, context)
                    , (int) Utility.convertDpToPixel(12, context)
                    , (int) Utility.convertDpToPixel(12, context)
                    , 0
            );
            mHolder.mBinding.ivHome.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_address_home_active));
            mHolder.mBinding.tvAddressNickname.setText(context.getString(R.string.label_home));
            mHolder.mBinding.viewDot.setVisibility(mList.get(position).isSubscribedAddress
                    ? View.VISIBLE
                    : View.GONE
            );
            mHolder.mBinding.tvLabelAddressSubscribed.setVisibility(mList.get(position).isSubscribedAddress
                    ? View.VISIBLE
                    : View.GONE
            );
            mHolder.mBinding.tvAddress.setVisibility(View.VISIBLE);
        }

       /* AddressModel model = mList.get(position);
        if (position == 0) {
            mHolder.mBinding.llAddressContainer.setVisibility(View.GONE);
        } else {
            if (!TextUtils.isEmpty(model.nickname) && !model.nickname.equalsIgnoreCase("null"))
                mHolder.mBinding.tvAddressNickname.setText(model.nickname);
            else
                mHolder.mBinding.tvAddressNickname.setText(Utility.getAddressCategoryString(model.category));
            mHolder.mBinding.ivAddressIcon.setImageResource(Utility.getAddressCategoryBlueIcon(model.category));
            mHolder.mBinding.llAddressContainer.setVisibility(View.VISIBLE);
        }
        mHolder.mBinding.tvAddress.setText(model.address_initials + ", " + model.address);*/

        return convertView;
    }

    private static class AddressViewHolder {
        private RowAddressTaskCreateBinding mBinding;

        AddressViewHolder(RowAddressTaskCreateBinding binding) {
            mBinding = binding;
        }
    }
}
