package com.cheep.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.cheep.R;
import com.cheep.databinding.RowAddressBinding;
import com.cheep.model.AddressModel;
import com.cheep.network.NetworkUtility;

import java.util.ArrayList;

/**
 * Created by pankaj on 9/29/16.
 */

public class AddressRecyclerViewAdapter extends RecyclerView.Adapter<AddressRecyclerViewAdapter.ViewHolder> {

    private ArrayList<AddressModel> mList;
    private String selected = "";
    private RadioButton selectedRadioBtn;
    private AddressItemInteractionListener listener;

    public AddressRecyclerViewAdapter(ArrayList<AddressModel> mList, AddressItemInteractionListener listener) {
        if (mList != null)
            this.mList = mList;
        else
            this.mList = new ArrayList<>();
        this.listener = listener;
    }

    @Override
    public AddressRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowAddressBinding mRowAddressBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_address, parent, false);
        return new AddressRecyclerViewAdapter.ViewHolder(mRowAddressBinding);
    }

    @Override
    public void onBindViewHolder(final AddressRecyclerViewAdapter.ViewHolder holder, int position) {

        final AddressModel model = mList.get(holder.getAdapterPosition());

//        holder.mRowAddressBinding.textAddressNickname.setText(model.name);

        if ((!TextUtils.isEmpty(model.address_initials))
                && model.address_initials.length() > 0) {
            holder.mRowAddressBinding.textFullAddress.setText(model.address_initials + ", " + model.address);
        } else {
            holder.mRowAddressBinding.textFullAddress.setText(model.address);
        }

        if (TextUtils.isEmpty(selected) && position == 0) {
            selected = model.address_id;
            holder.mRowAddressBinding.radioButton.setChecked(true);
            selectedRadioBtn = holder.mRowAddressBinding.radioButton;
        } else if (selected.equalsIgnoreCase(model.address_id)) {
            holder.mRowAddressBinding.radioButton.setChecked(true);
            selectedRadioBtn = holder.mRowAddressBinding.radioButton;
        } else {
            holder.mRowAddressBinding.radioButton.setChecked(false);
        }

        if (NetworkUtility.TAGS.ADDRESS_TYPE.HOME.equalsIgnoreCase(model.category)) {
            holder.mRowAddressBinding.textAddressNickname.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_address_home_active, 0, 0, 0);
            holder.mRowAddressBinding.textAddressNickname.setText(holder.mRowAddressBinding.getRoot().getContext().getString(R.string.label_home));
        } else if (NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE.equalsIgnoreCase(model.category)) {
            holder.mRowAddressBinding.textAddressNickname.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_address_office_active, 0, 0, 0);
            holder.mRowAddressBinding.textAddressNickname.setText(holder.mRowAddressBinding.getRoot().getContext().getString(R.string.label_office));
        } else {
            holder.mRowAddressBinding.textAddressNickname.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_address_other_active, 0, 0, 0);
            holder.mRowAddressBinding.textAddressNickname.setText(holder.mRowAddressBinding.getRoot().getContext().getString(R.string.label_other));
        }

        holder.mRowAddressBinding.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton radioButton = (RadioButton) view;
                if (radioButton.isChecked()) {
                    if (selectedRadioBtn != null && !selected.equalsIgnoreCase(model.address_id)) {
                        selectedRadioBtn.setChecked(false);
                    }
                    selectedRadioBtn = radioButton;
                    selected = model.address_id;
                }
            }
        });
        holder.mRowAddressBinding.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onEditClicked(model, holder.getAdapterPosition());
                }
            }
        });
        holder.mRowAddressBinding.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onDeleteClicked(model, holder.getAdapterPosition());
                }
            }
        });

        /*holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton radioButton = (RadioButton) holder.mRowAddressBinding.radioButton;
                radioButton.setChecked(true);
                if (radioButton.isChecked()) {
                    if (selectedRadioBtn != null && selected != holder.getAdapterPosition()) {
                        selectedRadioBtn.setChecked(false);
                    }
                    selectedRadioBtn = radioButton;
                    selected = holder.getAdapterPosition();
                }
            }
        });*/

        //Setting whole row click listener to update selected address(Radio button)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton radioButton = holder.mRowAddressBinding.radioButton;
                radioButton.setChecked(true);
                if (radioButton.isChecked()) {
                    if (selectedRadioBtn != null && !selected.equalsIgnoreCase(model.address_id)) {
                        selectedRadioBtn.setChecked(false);
                    }
                    selectedRadioBtn = radioButton;
                    selected = model.address_id;
                }
                if (listener != null) {
                    listener.onRowClicked(model, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void delete(String address_id) {
        if (mList.size() > 0 && !TextUtils.isEmpty(address_id)) {
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).address_id.equalsIgnoreCase(address_id)) {
                    mList.remove(i);
                    notifyItemRemoved(i);
                    break;
                }
            }
        }
    }

    public void updateItem(AddressModel addressModel) {
        if (mList.size() > 0 && addressModel != null && !TextUtils.isEmpty(addressModel.address_id)) {
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).address_id.equalsIgnoreCase(addressModel.address_id)) {
                    mList.set(i, addressModel);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    public ArrayList<AddressModel> getmList() {
        return mList;
    }

    public void add(AddressModel addressModel) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        mList.add(addressModel);
        notifyDataSetChanged();
    }

    /**
     * Used to select current row based on address id, we are checking the position where address is stored.
     *
     * @param selectedAddressId
     */
    public void setSelectedAddressId(String selectedAddressId) {
        if (!TextUtils.isEmpty(selectedAddressId)) {
            selected = selectedAddressId;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowAddressBinding mRowAddressBinding;

        public ViewHolder(RowAddressBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowAddressBinding = binding;
        }
    }

    public AddressModel getSelectedAddress() {
        if (mList.size() > 0 && !TextUtils.isEmpty(selected)) {
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).address_id.equalsIgnoreCase(selected)) {
                    return mList.get(i);
                }
            }
        }
        return null;
    }

    public interface AddressItemInteractionListener {
        public void onEditClicked(AddressModel model, int position);

        public void onDeleteClicked(AddressModel model, int position);

        public void onRowClicked(AddressModel model, int position);
    }
}