package com.cheep.cheepcarenew.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.addresspopupsfortask.AddressCategorySelectionDialog;
import com.cheep.addresspopupsfortask.AddressListDialog;
import com.cheep.cheepcarenew.fragments.ProfileDetailsFragmentnew;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.DialogAddressListProfileBinding;
import com.cheep.model.AddressModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddressListProfileDialog extends DialogFragment {

    public static final String TAG = AddressListDialog.class.getSimpleName();
    private DialogAddressListProfileBinding mBinding;
    private ArrayList<AddressModel> listOfAddress;
    private AddressListAdapter adapter;
    private ProgressDialog mProgressDialog;
    int position;
    static DismissDialog listners;
    public AddressListProfileDialog() {
        // Required empty public constructor
    }
    public interface DismissDialog
    {
        public void dismiss_Dialog();
    }
    public void   getDataFromEditAddressDialog(ArrayList<AddressModel> listOfAddress)
    {
        this.listOfAddress=listOfAddress;
        setAdapter();
    }
    public static AddressListProfileDialog newInstance(ArrayList<AddressModel> addressList,DismissDialog listner) {
        AddressListProfileDialog fragment = new AddressListProfileDialog();
        Bundle args = new Bundle();
        listners=listner;
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
    ///////////////////////////// DELETE CONFIRMATION DIALOG//////////////////////////////////////

    private void showAddressDeletionConfirmationDialog(final String address_id,final int getAdapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.cheep_all_caps));
        builder.setMessage(getString(R.string.label_address_delete_message));
        builder.setPositiveButton(getString(R.string.label_Ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick() called with: dialogInterface = [" + dialogInterface + "], i = [" + i + "]");
                callDeleteAddressWS(address_id);
                position = getAdapterPosition;
            }
        });
        builder.setNegativeButton(getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick() called with: dialogInterface = [" + dialogInterface + "], i = [" + i + "]");
            }
        });
        builder.show();
    }


    // adapter
    public class AddressListAdapter extends RecyclerView.Adapter<AddressListAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.row_address_a, parent, false);
            return new ViewHolder(view);
        }

        @SuppressLint("ResourceType")
        @Override
        public void onBindViewHolder(@NonNull AddressListAdapter.ViewHolder holder, final int position) {
            holder.imgAddress.setImageResource(Utility.getAddressCategoryBlueIcon(listOfAddress.get(position).category));
            holder.tvAddressCategory.setText(listOfAddress.get(position).category);
            holder.textFullAddress.setText(listOfAddress.get(position).address);
            holder.tvEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAddressCategorySelectionDialog(position);
                }
            });
            holder.tvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAddressDeletionConfirmationDialog(listOfAddress.get(position).address_id,position);
                }
            });

            hideAndShowView(listOfAddress.get(position).is_subscribe,holder.tvEdit,holder.tvDelete);
        }

        // show dialog for select home and office address
        private void showAddressCategorySelectionDialog(int position) {
            AddressCategorySelectionDialog addressCategorySelectionDialog = AddressCategorySelectionDialog.newInstance(Utility.EDIT_PROFILE_ACTIVITY, true, listOfAddress, position);
            addressCategorySelectionDialog.show(((BaseAppCompatActivity) getContext()).getSupportFragmentManager(),
                    AddressCategorySelectionDialog.TAG);
        }

        private void hideAndShowView(String isSubscribe,TextView tvEdit ,TextView tvDelete){
            if(isSubscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.NONE)){
                tvEdit.setVisibility(View.VISIBLE);
                tvDelete.setVisibility(View.VISIBLE);
            }else {
                tvEdit.setVisibility(View.INVISIBLE);
                tvDelete.setVisibility(View.INVISIBLE);
            }

        }
        @Override
        public int getItemCount() {

            return listOfAddress.size();

        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView textFullAddress, tvAddressCategory;
            private TextView tvDelete, tvEdit;
            private ImageView imgAddress;

            ViewHolder(View itemView) {
                super(itemView);
                textFullAddress = itemView.findViewById(R.id.text_full_address);
                imgAddress = itemView.findViewById(R.id.img_address);
                tvAddressCategory = itemView.findViewById(R.id.tv_address_category);

                tvEdit = itemView.findViewById(R.id.tv_edit);
                tvDelete = itemView.findViewById(R.id.tv_delete);

            }
        }
    }

    /************************************************************************************************
     **********************************Calling Webservice  delete Address********************************************
     ************************************************************************************************/

    protected void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mProgressDialog.show();
    }

    /**
     * Show Progress Dialog
     */
    public void showProgressDialog() {
        showProgressDialog(getString(R.string.label_please_wait));
    }

    /**
     * Close Progress Dialog
     */
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }


    private void callDeleteAddressWS(String addressId) {
        if (!Utility.isConnected(getContext())) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(getContext()).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(getContext()).getUserDetails().userID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, String.valueOf(addressId));

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.DELETE_ADDRESS
                , mCallDeleteAddressWSErrorListener
                , mCallDeleteAddressResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(getContext()).addToRequestQueue(mVolleyNetworkRequest);
    }

    /**
     * Listeners for get profile calls
     */
    Response.Listener mCallDeleteAddressResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
//                if (addAddressDialog != null) {
//                    addAddressDialog.dismiss();
//                }
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        listOfAddress.remove(position);
                        adapter.notifyDataSetChanged();
                        if(listOfAddress.size()==0)
                        {
                            dismiss();
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(getContext(), true, statusCode);

                        if (getActivity() != null)
                            getActivity().finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallDeleteAddressWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallDeleteAddressWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    @Override
    public void dismiss() {
        if(listners!=null)
        {
            listners.dismiss_Dialog();
        }
        super.dismiss();

    }
}
