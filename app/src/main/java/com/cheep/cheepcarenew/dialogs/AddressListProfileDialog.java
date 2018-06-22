package com.cheep.cheepcarenew.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.cheep.R;
import com.cheep.addresspopupsfortask.AddressListDialog;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.DialogAddressListProfileBinding;
import com.cheep.model.AddressModel;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.Utility;

import java.util.ArrayList;

public class AddressListProfileDialog extends DialogFragment {

    public static final String TAG = AddressListDialog.class.getSimpleName();
    private DialogAddressListProfileBinding mBinding;
    private ArrayList<AddressModel> listOfAddress;
    private AddressListAdapter adapter;

    public AddressListProfileDialog() {
        // Required empty public constructor
    }

    public static AddressListProfileDialog newInstance(ArrayList<AddressModel> addressList) {
        AddressListProfileDialog fragment = new AddressListProfileDialog();
        Bundle args = new Bundle();
        args.putString(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(addressList));
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listOfAddress = GsonUtility.getObjectListFromJsonString(getArguments().getString(Utility.Extra.DATA), AddressModel[].class);
        }
    }
    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimationZoomInOut;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_address_list_profile, container, false);
        setAdapter();
        return mBinding.getRoot();
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setCanceledOnTouchOutside(true);
        this.setCancelable(true);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.AlertAnimation;
        return dialog;

    }

    private void setAdapter() {
        mBinding.recyclerView.setHasFixedSize(true);
        adapter = new AddressListAdapter();
        mBinding.recyclerView.setAdapter(adapter);
        mBinding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_0dp)));
    }


    // adapter
    public class AddressListAdapter extends RecyclerView.Adapter<AddressListAdapter.ViewHolder> {

        @NonNull
        @Override
        public AddressListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.row_address_a, parent, false);
            return new ViewHolder(view);
        }

        @SuppressLint("ResourceType")
        @Override
        public void onBindViewHolder(@NonNull AddressListAdapter.ViewHolder holder, int position) {
            holder.imgAddress.setImageResource(Utility.getAddressCategoryBlueIcon(listOfAddress.get(0).category));
            holder.tvAddressCategory.setText(listOfAddress.get(0).category);
            holder.textFullAddress.setText(listOfAddress.get(position).getAddressWithInitials());
        }

        @Override
        public int getItemCount() {

            return listOfAddress.size();

        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView textFullAddress,tvAddressCategory;
            private ImageView imgAddress;

            ViewHolder(View itemView) {
                super(itemView);
                textFullAddress = itemView.findViewById(R.id.text_full_address);
                imgAddress = itemView.findViewById(R.id.img_address);
                tvAddressCategory = itemView.findViewById(R.id.tv_address_category);

            }
        }
    }
}
