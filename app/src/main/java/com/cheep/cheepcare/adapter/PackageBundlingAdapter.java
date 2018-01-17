package com.cheep.cheepcare.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.cheep.R;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.databinding.RowBundledPackageHeaderBinding;
import com.cheep.databinding.RowBundledPackageSelectedBinding;
import com.cheep.databinding.RowBundledPackagetNoSelectedBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.UserDetails;
import com.cheep.utils.LoadMoreRecyclerAdapter;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 12/22/17.
 */

public class PackageBundlingAdapter extends LoadMoreRecyclerAdapter<PackageBundlingAdapter.PackageViewHolder> {

    private static final String TAG = "PackageBundlingAdapter";
    private final PackageItemClickListener mListener;
    private List<PackageDetail> mList = new ArrayList<>();

    public static final int ROW_PACKAGE_SELECTED = 0;
    public static final int ROW_PACKAGE_HEADER = 1;
    public static final int ROW_PACKAGE_NOT_SELECTED = 2;
    private ArrayList list;

    public List<PackageDetail> getList() {
        return mList;
    }

    public interface PackageItemClickListener {
        void onPackageItemClick(int position, PackageDetail packageModel);

        void onUpdateOfAddress(int position, AddressModel addressModel);
    }

    public PackageBundlingAdapter(PackageItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public PackageViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ROW_PACKAGE_HEADER: {
                RowBundledPackageHeaderBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.row_bundled_package_header, parent, false);
                return new PackageViewHolder(binding);
            }
            case ROW_PACKAGE_NOT_SELECTED: {
                RowBundledPackagetNoSelectedBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.row_bundled_packaget_no_selected, parent, false);
                return new PackageViewHolder(binding);
            }
            default: {
                RowBundledPackageSelectedBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.row_bundled_package_selected, parent, false);
                return new PackageViewHolder(binding);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).rowType;
    }

    @Override
    public void onActualBindViewHolder(final PackageViewHolder holder, int position) {
        int viewType = getItemViewType(holder.getAdapterPosition());
        Context context;
        final PackageDetail model = mList.get(position);
        switch (viewType) {
            case ROW_PACKAGE_NOT_SELECTED:
                context = holder.mRowNotSelectedBinding.getRoot().getContext();
                Utility.loadImageView(context, holder.mRowNotSelectedBinding.ivItemBackground, "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/medium/Untitled.jpg");
                SpannableString spannableString = new SpannableString(context.getString(R.string.rupee_symbol_x_package_price, model.price));
                spannableString = Utility.getCheepCarePackageMonthlyPrice(spannableString, spannableString.length() - 3, spannableString.length());
                holder.mRowNotSelectedBinding.tvPrice.setText(spannableString);
                holder.mRowNotSelectedBinding.ivIsAddressSelected.setSelected(model.isSelected);
                holder.mRowNotSelectedBinding.tvDescription.setText(model.subtitle);
                holder.mRowNotSelectedBinding.tvTitle.setText(model.title);


                holder.mRowNotSelectedBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onPackageItemClick(holder.getAdapterPosition(), model);
                    }
                });
                break;
            case ROW_PACKAGE_SELECTED:
                context = holder.mRowSelectedBinding.getRoot().getContext();
                Utility.loadImageView(context, holder.mRowSelectedBinding.ivItemBackground, "https://s3.ap-south-1.amazonaws.com/cheepapp/category/banner_image/medium/Untitled.jpg");
                SpannableString spannableString1 = new SpannableString(context.getString(R.string.rupee_symbol_x_package_price, model.price));
                spannableString1 = Utility.getCheepCarePackageMonthlyPrice(spannableString1, spannableString1.length() - 3, spannableString1.length());
                holder.mRowSelectedBinding.tvPrice.setText(spannableString1);
                holder.mRowSelectedBinding.ivIsAddressSelected.setSelected(model.isSelected);
                holder.mRowSelectedBinding.tvDescription.setText(model.subtitle);
                holder.mRowSelectedBinding.tvTitle.setText(model.title);

                if (model.mSelectedAddress != null) {
                    if (!TextUtils.isEmpty(model.mSelectedAddress.nickname))
                        holder.mRowSelectedBinding.tvAddressNickname.setText(model.mSelectedAddress.nickname);
                    else
                        holder.mRowSelectedBinding.tvAddressNickname.setText(Utility.getAddressCategoryString(model.mSelectedAddress.category));
                    holder.mRowSelectedBinding.ivAddressIcon.setImageResource(Utility.getAddressCategoryBlueIcon(model.mSelectedAddress.category));
                }
                holder.mRowSelectedBinding.tvAddress.setText(model.mSelectedAddress.address_initials + ", " + model.mSelectedAddress.address);


                holder.mRowSelectedBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
                break;
            case ROW_PACKAGE_HEADER:
//                holder.mBindingBundledPackageHeaderBinding.txtRibbon.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                holder.mRowHeaderBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
                break;
        }
    }

    @Override
    public int onActualItemCount() {
        return mList.size();
    }


    class PackageViewHolder extends RecyclerView.ViewHolder {
        RowBundledPackageSelectedBinding mRowSelectedBinding;
        RowBundledPackageHeaderBinding mRowHeaderBinding;
        RowBundledPackagetNoSelectedBinding mRowNotSelectedBinding;


        PackageViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            if (binding instanceof RowBundledPackageHeaderBinding)
                mRowHeaderBinding = (RowBundledPackageHeaderBinding) binding;
            else if (binding instanceof RowBundledPackagetNoSelectedBinding)
                mRowNotSelectedBinding = (RowBundledPackagetNoSelectedBinding) binding;
            else
                mRowSelectedBinding = (RowBundledPackageSelectedBinding) binding;

            if (mRowSelectedBinding != null)
                mRowSelectedBinding.lnAddressRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDropDownMenu(mRowSelectedBinding.lnAddressRow, getAdapterPosition());
                    }
                });
        }
    }

    public void addPakcageList(List<PackageDetail> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    private void showDropDownMenu(final View view, final int adapterPosition) {
        Log.i(TAG, "showDropDownMenu: ");
        final View mFilterPopupWindow = View.inflate(view.getContext(), R.layout.layout_address_drop_down, null);

        final PopupWindow mPopupWindow = new PopupWindow(view.getContext());
        RecyclerView recyclerview = mFilterPopupWindow.findViewById(R.id.listMultipleChoice);
        recyclerview.setLayoutManager(new LinearLayoutManager(view.getContext()));

      /*  Collections.sort(model.dropDownList, new Comparator<QueAnsModel.DropDownModel>() {
            @Override
            public int compare(QueAnsModel.DropDownModel abc1, QueAnsModel.DropDownModel abc2) {

                boolean b1 = abc1.isSelected;
                boolean b2 = abc2.isSelected;

                if (b1 != b2) {

                    if (b1) {
                        return -1;
                    }

                    return 1;
                }
                return 0;

            }
        });*/
        final UserDetails userDetails = PreferenceUtility.getInstance(view.getContext()).getUserDetails();
        final DropDownAddressAdapter dropDownAdapter = new DropDownAddressAdapter(userDetails.addressList);
        recyclerview.setAdapter(dropDownAdapter);
        DropDownAddressAdapter.ClickItem clickListener = new DropDownAddressAdapter.ClickItem() {
            @Override
            public void clickItem(int i) {
//                for (int j = 0; j < userDetails.addressList.size(); j++) {
//                    AddressModel model1 = userDetails.addressList.get(j);
//                }
//                dropDownAdapter.setSelected(i);
//                textView.setText(model.dropDownList.get(i).dropdown_answer);
//                textView.setSelected(true);
//                model.answer = model.dropDownList.get(i).dropdown_answer;
//                mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag(model.questionId).setSelected(true);

                mListener.onUpdateOfAddress(adapterPosition,userDetails.addressList.get(i));

                mPopupWindow.dismiss();


            }

            @Override
            public void dismissDialog() {
                mPopupWindow.dismiss();
            }
        };
        dropDownAdapter.setListener(clickListener);

        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setContentView(mFilterPopupWindow);
        mPopupWindow.setWidth(view.getWidth());
        mPopupWindow.setHeight(ListView.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);

        // No animation at present
        mPopupWindow.setAnimationStyle(0);

        // Displaying the popup at the specified location, + offsets.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mPopupWindow.showAsDropDown(view, 0, -view.getHeight(), Gravity.NO_GRAVITY);
        } else {
            mPopupWindow.showAsDropDown(view, 0, -view.getHeight());
        }
    }


}