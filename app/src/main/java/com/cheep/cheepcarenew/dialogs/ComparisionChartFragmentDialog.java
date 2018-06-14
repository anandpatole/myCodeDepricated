package com.cheep.cheepcarenew.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cheep.R;
<<<<<<< HEAD
import com.cheep.cheepcarenew.activities.PaymentSummaryActivityCheepCare;
import com.cheep.databinding.FragmentComparsionChartFragmentDialogBinding;
import com.cheep.model.ComparisionChart.ComparisionChartModel;
import com.cheep.model.ComparisionChart.FeatureList;
=======
import com.cheep.cheepcarenew.activities.PaymentSummaryCheepCareActivity;
import com.cheep.model.ComparisionChartModel;
>>>>>>> 1fb3d424cc477adc5b97a5ab4fe4a6ea1a631cf8
import com.cheep.network.NetworkUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.Utility;



public class ComparisionChartFragmentDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = ComparisionChartFragmentDialog.class.getSimpleName();

    private AcknowledgementPopupDialog acknowledgementPopupDialog;
    private FragmentComparsionChartFragmentDialogBinding mBinding;
    private ComparisionChartModel comparisionChartModel;

    public static ComparisionChartFragmentDialog newInstance(ComparisionChartModel comparisionChartModel) {
        ComparisionChartFragmentDialog fragment = new ComparisionChartFragmentDialog();
        Bundle args = new Bundle();
        args.putString(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(comparisionChartModel));
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

    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimationZoomInOut;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_comparsion_chart_fragment_dialog, container, false);

        if (getArguments() != null) {

            comparisionChartModel = (ComparisionChartModel) GsonUtility.getObjectFromJsonString(getArguments().getString(Utility.Extra.DATA), ComparisionChartModel.class);

        }
        loadData();
        setListener();

        return mBinding.getRoot();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

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

    private void setListener() {
        mBinding.tvBookKnowPremimu.setOnClickListener(this);
        mBinding.tvBookKnowCare.setOnClickListener(this);
    }

    private void loadData() {
        mBinding.recyclerView.setHasFixedSize(true);
        final ComparisionChatAdapter adapter = new ComparisionChatAdapter();
        mBinding.recyclerView.setAdapter(adapter);
        setPrice();
    }

    private void setPrice() {
        for (int i = 0; comparisionChartModel.priceLists.size() > i; i++) {

            String TYPE = comparisionChartModel.priceLists.get(i).type;

            if (TYPE.equalsIgnoreCase(NetworkUtility.PACKAGE_DETAIL_TYPE.premium)) {
                mBinding.tvPremiumNewPrice.setText(comparisionChartModel.priceLists.get(i).newPrice);
                mBinding.tvPremiumOldPrice.setText(comparisionChartModel.priceLists.get(i).oldPrice);
            } else if (TYPE.equalsIgnoreCase(NetworkUtility.PACKAGE_DETAIL_TYPE.normal)) {
                mBinding.tvNormalNewPrice.setText(comparisionChartModel.priceLists.get(i).newPrice);
                mBinding.tvNormalOldPrice.setText(comparisionChartModel.priceLists.get(i).oldPrice);
            }

        }
    }

    // open show Comparision Chart Fragment Dialog
    private void showAcknowledgementPopupDialog() {
        if (acknowledgementPopupDialog != null) {
            acknowledgementPopupDialog.dismissAllowingStateLoss();
            acknowledgementPopupDialog = null;
        }
        acknowledgementPopupDialog = AcknowledgementPopupDialog.newInstance("", "");
        acknowledgementPopupDialog.show(getActivity().getSupportFragmentManager(), TAG);
    }

    //View.OnClickListener
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_book_know_premimu:
<<<<<<< HEAD
                PaymentSummaryActivityCheepCare.newInstance(getContext(),comparisionChartModel,Utility.TYPE.PREMIUM);
                break;
            case R.id.tv_book_know_care:
                PaymentSummaryActivityCheepCare.newInstance(getContext(),comparisionChartModel,Utility.TYPE.NORMAL);
=======
                PaymentSummaryCheepCareActivity.newInstance(getContext());
                break;
            case R.id.tv_book_know_care:
                PaymentSummaryCheepCareActivity.newInstance(getContext());
>>>>>>> 1fb3d424cc477adc5b97a5ab4fe4a6ea1a631cf8
                break;
        }
    }

    public class ComparisionChatAdapter extends RecyclerView.Adapter<ComparisionChatAdapter.ComparisionChatViewHolder> {

        @NonNull
        @Override
        public ComparisionChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.cell_comparision_chart_dialog, parent, false);
            return new ComparisionChatViewHolder(view);
        }

        @SuppressLint("ResourceType")
        @Override
        public void onBindViewHolder(@NonNull ComparisionChatViewHolder holder, int position) {

            FeatureList featureList = comparisionChartModel.featureLists.get(position);

            holder.tvBenefitsFeatures.setText(featureList.feature);
            holder.tvCheepCarePackage.setText(featureList.normal);
            if (position % 2 == 1) {
                holder.linear.setBackgroundColor(Color.parseColor("#06FFCC01"));
            } else {
                holder.linear.setBackgroundColor(Color.parseColor("#20646460"));
            }

<<<<<<< HEAD
            if (featureList.premium.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                holder.imagePremium.setVisibility(View.VISIBLE);
                holder.tvPremium.setVisibility(View.GONE);
                holder.imagePremium.setBackgroundResource(R.drawable.verified_icon);
            } else if (featureList.premium.equalsIgnoreCase(Utility.BOOLEAN.NO)) {
=======
            if (model.premium.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                holder.imagePremium.setVisibility(View.VISIBLE);
                holder.tvPremium.setVisibility(View.GONE);
                holder.imagePremium.setBackgroundResource(R.drawable.verified_icon);
            } else if (model.premium.equalsIgnoreCase(Utility.BOOLEAN.NO)) {
>>>>>>> 1fb3d424cc477adc5b97a5ab4fe4a6ea1a631cf8
                holder.imagePremium.setVisibility(View.VISIBLE);
                holder.tvPremium.setVisibility(View.GONE);
                holder.imagePremium.setBackgroundResource(R.drawable.cancelled);
            } else {
                holder.tvPremium.setVisibility(View.VISIBLE);
                holder.imagePremium.setVisibility(View.GONE);
                holder.tvPremium.setText(featureList.premium);

            }
            if (featureList.normal.equalsIgnoreCase(Utility.BOOLEAN.NA)) {
                holder.tvCheepCarePackage.setTextColor(Color.RED);
            } else {
                holder.tvCheepCarePackage.setTextColor(Color.parseColor(getResources().getString(R.color.cheep_tips_black)));
            }

        }

        @Override
        public int getItemCount() {

            return comparisionChartModel.featureLists.size();

        }

        class ComparisionChatViewHolder extends RecyclerView.ViewHolder {


            private TextView tvBenefitsFeatures;
            private TextView tvCheepCarePackage;
            private LinearLayout linear;
            private ImageView imagePremium;
            private TextView tvPremium;


            ComparisionChatViewHolder(View itemView) {
                super(itemView);
                tvBenefitsFeatures = itemView.findViewById(R.id.tv_benefits_features);
                tvCheepCarePackage = itemView.findViewById(R.id.tv_cheep_care_package);
                linear = itemView.findViewById(R.id.linear);
                imagePremium = itemView.findViewById(R.id.imagePremium);
                tvPremium = itemView.findViewById(R.id.tvPremium);

            }
        }
    }
}
