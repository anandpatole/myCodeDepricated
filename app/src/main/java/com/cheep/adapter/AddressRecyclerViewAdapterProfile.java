package com.cheep.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.cheep.R;

import com.cheep.databinding.RowAddressABinding;
import com.cheep.model.AddressModel;
import com.cheep.utils.Utility;

import java.util.ArrayList;

/**
 * Created by pankaj on 9/29/16.
 */

public class AddressRecyclerViewAdapterProfile extends RecyclerView.Adapter<AddressRecyclerViewAdapterProfile.ViewHolder> {

    private ArrayList<AddressModel> mList;
    private String selected = "";
int flag;
    private AddressItemInteractionListener listener;

    public AddressRecyclerViewAdapterProfile(ArrayList<AddressModel> mList, AddressItemInteractionListener listener,int flag) {
        if (mList != null)
            this.mList = mList;
        else
            this.mList = new ArrayList<>();
        this.listener = listener;
        this.flag=flag;
    }

    @Override
    public AddressRecyclerViewAdapterProfile.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowAddressABinding mRowAddressBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_address_a, parent, false);
        return new AddressRecyclerViewAdapterProfile.ViewHolder(mRowAddressBinding);
    }

    @Override
    public void onBindViewHolder(final AddressRecyclerViewAdapterProfile.ViewHolder holder, int position) {

        final AddressModel model = mList.get(holder.getAdapterPosition());

      // holder.mRowAddressBinding.textAddressNickname.setText(model.name);

           // holder.mRowAddressBinding.textFullAddress.setText(model.getAddressWithInitials());

//        if (TextUtils.isEmpty(selected) && position == 0) {
//            selected = model.address_id;
//            holder.mRowAddressBinding.radioButton.setChecked(true);
//           // selectedRadioBtn = holder.mRowAddressBinding.radioButton;
//        } else if (selected.equalsIgnoreCase(model.address_id)) {
//            holder.mRowAddressBinding.radioButton.setChecked(true);
//            selectedRadioBtn = holder.mRowAddressBinding.radioButton;
//        } else {
//            holder.mRowAddressBinding.radioButton.setChecked(false);
//        }

        //holder.mRowAddressBinding.textAddressCategory.setText(Utility.getAddressCategoryString(model.category));
       // holder.mRowAddressBinding.textAddressCategory.setCompoundDrawablesWithIntrinsicBounds(Utility.getAddressCategoryBlueIcon(model.category), 0, 0, 0);

//       holder.mRowAddressBinding.radioButton.setOnClickListener(new View.OnClickListener() {
//           @Override
//           public void onClick(View view) {
//               RadioButton radioButton = (RadioButton) view;
//               if (radioButton.isChecked()) {
//                    if (selectedRadioBtn != null && !selected.equalsIgnoreCase(model.address_id)) {
//                       selectedRadioBtn.setChecked(false);
//                   }
//                    selectedRadioBtn = radioButton;
//                    selected = model.address_id;
//                }
//            }
//        });
        /*holder.mRowAddressBinding.imgEdit.setOnClickListener(new View.OnClickListener() {
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
        });*/

       /* holder.mView.setOnClickListener(new View.OnClickListener() {
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
              //  RadioButton radioButton = holder.mRowAddressBinding.radioButton;
              //  radioButton.setChecked(true);
//                if (radioButton.isChecked()) {
//                    if (selectedRadioBtn != null && !selected.equalsIgnoreCase(model.address_id)) {
//                        selectedRadioBtn.setChecked(false);
//                    }
//                    selectedRadioBtn = radioButton;
//
//                }
                selected = model.address_id;
                if (listener != null) {
                    listener.onRowClicked(model, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(flag==0)
        {
            if(mList.size()>0) {
                return 1;
            }
            else
            {
                return mList.size();
            }
        }
        else {
            return mList.size();
        }
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

    public void delete(AddressModel addressModel) {
        if (mList.size() > 0 && mList.contains(addressModel)) {
            mList.remove(addressModel);
            notifyDataSetChanged();
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
        public final RowAddressABinding mRowAddressBinding;

        public ViewHolder(RowAddressABinding binding) {
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
        void onEditClicked(AddressModel model, int position);

        void onDeleteClicked(AddressModel model, int position);

        void onRowClicked(AddressModel model, int position);
    }
}