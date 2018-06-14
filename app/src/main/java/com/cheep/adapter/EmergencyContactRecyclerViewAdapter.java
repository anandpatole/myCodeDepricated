package com.cheep.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cheep.R;
import com.cheep.databinding.RowEmergencyContactBinding;
import com.cheep.network.NetworkUtility;
import org.json.JSONArray;
import java.util.ArrayList;

public class EmergencyContactRecyclerViewAdapter extends RecyclerView.Adapter<EmergencyContactRecyclerViewAdapter.ViewHolder>

{

    EmergencyInteractionListener listener;
    Context mContext;
    ArrayList<String> mList;
JSONArray emergency_contacts;
    public EmergencyContactRecyclerViewAdapter(EmergencyContactRecyclerViewAdapter.EmergencyInteractionListener listener, JSONArray emergency) {
        if (emergency != null)
            this.emergency_contacts = emergency;

        else
            this.emergency_contacts = new JSONArray();

this.listener=listener;

    }

    @Override
    public int getItemCount()
    {
        return emergency_contacts.length();

    }
    @Override
    public EmergencyContactRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowEmergencyContactBinding mRowAddressBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_emergency_contact, parent, false);
        return new EmergencyContactRecyclerViewAdapter.ViewHolder(mRowAddressBinding);
    }

    @Override
    public void onBindViewHolder(final EmergencyContactRecyclerViewAdapter.ViewHolder holder, final int position)
    {

holder.mRowAddressBinding.textEmergencyContactName.setText(emergency_contacts.optJSONObject(position).optString(NetworkUtility.TAGS.NAME)+"  ("+emergency_contacts.optJSONObject(position).optString(NetworkUtility.TAGS.TYPE)+")");
        holder.mRowAddressBinding.textEmergencyContactPhone.setText(emergency_contacts.optJSONObject(position).optString(NetworkUtility.TAGS.NUMBER));


holder.mRowAddressBinding.textEmergencyContactName.setOnClickListener(new View.OnClickListener()
{
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {

        emergency_contacts.remove(position);
        notifyDataSetChanged();
     listener.onClicked(emergency_contacts);
    }
});
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        public final RowEmergencyContactBinding mRowAddressBinding;

        public ViewHolder(RowEmergencyContactBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowAddressBinding = binding;
        }
    }

    public interface EmergencyInteractionListener {
        void onClicked(JSONArray s);


    }



}
