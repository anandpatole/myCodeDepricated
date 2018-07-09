package com.cheep.cheepcarenew.fragments;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.adapter.RelationShipRecyclerViewAdapter;
import com.cheep.databinding.FragmnetRelationshipScreenBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class RelationShipScreenFragment extends BaseFragment implements WebCallClass.GetRelationShipResponseListener, WebCallClass.CommonResponseListener,
        RelationShipRecyclerViewAdapter.InteractionListener,
        WebCallClass.UpdateEmergencyContactResponseListener {
    public static final String TAG = "RelationShipScreenFragment";
    FragmnetRelationshipScreenBinding mBinding;
    private static final int RESULT_PICK_CONTACT = 85;
    String type;

    public static RelationShipScreenFragment newInstance() {
        Bundle args = new Bundle();
        RelationShipScreenFragment fragment = new RelationShipScreenFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragmnet_relationship_screen, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }

    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach: ");


        /*
          Cancel the request as it no longer available
         */
       /* Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.UPDATE_LOCATION);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.UPDATE_PROFILE);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.DELETE_ADDRESS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.EDIT_ADDRESS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.ADD_ADDRESS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.PROFILE);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.UPDATE_EMERGENCY_CONTACTS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.EDIT_PHONE_NUMBER);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.CHANGE_PASSWORD);*/

        super.onDetach();
    }

    @Override
    public void initiateUI() {
        if (((AppCompatActivity) mContext).getSupportActionBar() != null) {
            //Setting up toolbar
            ((AppCompatActivity) mContext).setSupportActionBar(mBinding.toolbar);
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle(Utility.EMPTY_STRING);
        }
        showProgressDialog();
        WebCallClass.getRelationShipListDetail(mContext, RelationShipScreenFragment.this, RelationShipScreenFragment.this);
    }

    @Override
    public void setListener() {
        mBinding.relationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, ProfileTabFragment.newInstance(), ProfileTabFragment.TAG).commitAllowingStateLoss();
            }
        });
    }

    @Override
    public void getRelationShipList(JSONArray relationshipList) {
        try {
            hideProgressDialog();

            ArrayList<String> listdata = new ArrayList<String>();

            if (relationshipList != null) {
                for (int i = 0; i < relationshipList.length(); i++) {
                    JSONObject temp = relationshipList.getJSONObject(i);
                    listdata.add(temp.optString(NetworkUtility.TAGS.TYPE));
                }
            }

            RelationShipRecyclerViewAdapter adapter = new RelationShipRecyclerViewAdapter(listdata, RelationShipScreenFragment.this);
            mBinding.recyclerRelationshipList.setAdapter(adapter);
            mBinding.recyclerRelationshipList.setLayoutManager(new LinearLayoutManager(getActivity()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void volleyError(VolleyError error) {
        hideProgressDialog();
        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
//        Utility.showSnackBar(error.toString(), mBinding.getRoot());
    }

    @Override
    public void showSpecificMessage(String message) {
        hideProgressDialog();
        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        // Utility.showSnackBar(message, mBinding.getRoot());
    }

    @Override
    public void forceLogout() {

    }

    @Override
    public void onClicked(String s) {
        type = s;
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }
    }

    private void contactPicked(Intent data) {
        Cursor cursor = null;
        try {
            String phoneNo = null;
            String name = null;
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            // column index of the phone number
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            // column index of the contact name
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            phoneNo = cursor.getString(phoneIndex);
            name = cursor.getString(nameIndex);

            JSONArray jsonArray = ProfileDetailsFragmentnew.jsonEmergencyContacts;
            JSONObject jsonObject;


            jsonObject = new JSONObject();
            jsonObject.put(NetworkUtility.TAGS.NAME, name);
            jsonObject.put(NetworkUtility.TAGS.NUMBER, phoneNo);
            jsonObject.put(NetworkUtility.TAGS.TYPE, type);
            jsonArray.put(jsonObject);

            WebCallClass.updateEmergencyContactDetail(mContext, RelationShipScreenFragment.this, RelationShipScreenFragment.this, jsonArray);
            //getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, ProfileTabFragment.newInstance(), ProfileTabFragment.TAG).commitAllowingStateLoss();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getUpdateEmergencyContactResponse(JSONArray emergency_contact) {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, ProfileTabFragment.newInstance(), ProfileTabFragment.TAG).commitAllowingStateLoss();
    }
}
