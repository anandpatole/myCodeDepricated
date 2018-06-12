package com.cheep.adapter;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cheep.R;
import com.cheep.databinding.RowRelationshipScreenBinding;
import com.cheep.model.AddressModel;


import java.util.ArrayList;

public class RelationShipRecyclerViewAdapter  extends RecyclerView.Adapter<RelationShipRecyclerViewAdapter.ViewHolder>

{

    InteractionListener listener;
    Context mContext;
    ArrayList<String> mList;

    public RelationShipRecyclerViewAdapter(ArrayList<String> mList,InteractionListener listener) {
        if (mList != null)
            this.mList = mList;

        else
            this.mList = new ArrayList<>();
this.listener=listener;
    }

    @Override
    public int getItemCount()
    {
        return mList.size();

    }
    @Override
    public RelationShipRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowRelationshipScreenBinding mRowAddressBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_relationship_screen, parent, false);
        return new RelationShipRecyclerViewAdapter.ViewHolder(mRowAddressBinding);
    }

    @Override
    public void onBindViewHolder(final RelationShipRecyclerViewAdapter.ViewHolder holder, int position)
    {
holder.mRowAddressBinding.textRelation.setText(mList.get(position));
holder.mRowAddressBinding.textRelation.setOnClickListener(new View.OnClickListener()
{
    @Override
    public void onClick(View v) {

     listener.onClicked(holder.mRowAddressBinding.textRelation.getText().toString());
    }
});
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        public final RowRelationshipScreenBinding mRowAddressBinding;

        public ViewHolder(RowRelationshipScreenBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowAddressBinding = binding;
        }
    }

    public interface InteractionListener {
        void onClicked( String s);


    }



}
