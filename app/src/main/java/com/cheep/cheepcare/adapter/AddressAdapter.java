package com.cheep.cheepcare.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.cheep.R;
import com.cheep.databinding.RowAddressPackageCustomizationBinding;
import com.cheep.model.AddressModel;
import com.cheep.utils.Utility;

import java.util.List;

/**
 * Created by pankaj on 12/25/17.
 */

public class AddressAdapter<T> extends ArrayAdapter<T> {

    private final List<AddressModel> mList;

    public AddressAdapter(@NonNull Context context, int resource, List<AddressModel> list) {
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
        final AddressAdapter.ViewHolder mHolder;
        if (convertView == null) {
            RowAddressPackageCustomizationBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext())
                    , R.layout.row_address_package_customization
                    , parent, false);
            convertView = binding.getRoot();
            mHolder = new ViewHolder(binding);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        if (position == 0) {
            mHolder.mBinding.llAddressContainer.setVisibility(View.GONE);
        } else {
            if (!TextUtils.isEmpty(mList.get(position).nickname))
                mHolder.mBinding.tvAddressNickname.setText(mList.get(position).nickname);
            else
                mHolder.mBinding.tvAddressNickname.setText(Utility.getAddressCategoryString(mList.get(position).category));
            mHolder.mBinding.llAddressContainer.setVisibility(View.VISIBLE);
        }
        mHolder.mBinding.tvAddress.setText(mList.get(position).address);

        return convertView;
    }

    private static class ViewHolder {
        private RowAddressPackageCustomizationBinding mBinding;

        ViewHolder(RowAddressPackageCustomizationBinding binding) {
            mBinding = binding;
        }
    }
}
