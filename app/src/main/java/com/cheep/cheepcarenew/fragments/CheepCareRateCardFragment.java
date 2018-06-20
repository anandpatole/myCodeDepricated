package com.cheep.cheepcarenew.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.cheepcarenew.model.CheepCareRateCardModel;
import com.cheep.databinding.FragmentCheepcareRateCardBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.Utility;
import org.json.JSONObject;
import java.util.ArrayList;


public class CheepCareRateCardFragment extends BaseFragment {

    public static final String TAG = "CheepCareRateCardFragment";
    private FragmentCheepcareRateCardBinding mBinding;
    private DrawerLayoutInteractionListener mListener;
    private ArrayList<CheepCareRateCardModel> list;
    private CheepCareRateCardAdapter adapter;
    private LinearLayoutManager linearLayoutManager;

    public static CheepCareRateCardFragment newInstance() {
        Bundle args = new Bundle();
        CheepCareRateCardFragment fragment = new CheepCareRateCardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_cheepcare_rate_card, container, false);

        getAllCatsCategoryFromServer();

        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);
        if (context instanceof DrawerLayoutInteractionListener) {
            this.mListener = (DrawerLayoutInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }

    @Override
    public void initiateUI() {
        if (((AppCompatActivity) mContext).getSupportActionBar() != null) {
            //Setting up toolbar
            ((AppCompatActivity) mContext).setSupportActionBar(mBinding.toolbar);
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle(Utility.EMPTY_STRING);
        }

        //Provide callback to activity to link drawerlayout with toolbar
        mListener.setUpDrawerLayoutWithToolBar(mBinding.toolbar);
    }

    @Override
    public void setListener() {
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

              /*  case R.id.text_plumber:
                    CheepCareRateCardPricingFragment fragment4 = CheepCareRateCardPricingFragment.newInstance("Plumber", "");
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment4, CheepCareRateCardPricingFragment.TAG).commitAllowingStateLoss();
                    break;
                case R.id.text_repairs:
                    CheepCareRateCardPricingFragment fragment5 = CheepCareRateCardPricingFragment.newInstance("Repair", "");
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment5, CheepCareRateCardPricingFragment.TAG).commitAllowingStateLoss();
                    break;
                case R.id.text_electrician:
                    CheepCareRateCardPricingFragment fragment6 = CheepCareRateCardPricingFragment.newInstance("Electrician", "");
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment6, CheepCareRateCardPricingFragment.TAG).commitAllowingStateLoss();
                    break;
                case R.id.text_painter:
                    CheepCareRateCardSelectionFragment fragment = CheepCareRateCardSelectionFragment.newInstance("Painter");
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment, CheepCareRateCardSelectionFragment.TAG).commitAllowingStateLoss();
                    break;
                case R.id.text_application_repair:
                    CheepCareRateCardSelectionFragment fragment1 = CheepCareRateCardSelectionFragment.newInstance("Application Repair");
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment1, CheepCareRateCardSelectionFragment.TAG).commitAllowingStateLoss();
                    break;
                case R.id.text_tech_repair:
                    CheepCareRateCardSelectionFragment fragment2 = CheepCareRateCardSelectionFragment.newInstance("Tech Repair");
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment2, CheepCareRateCardSelectionFragment.TAG).commitAllowingStateLoss();
                    break;
                case R.id.text_pest_control:
                    CheepCareRateCardSelectionFragment fragment3 = CheepCareRateCardSelectionFragment.newInstance("Pest Control");
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment3, CheepCareRateCardSelectionFragment.TAG).commitAllowingStateLoss();
                    break;
                default:

                    // RateCardDialog.newInstance((AppCompatActivity) mContext);
                    break;*/


            }
        }
    };
    private void setAdapter(){
        mBinding.recyclerView.setHasFixedSize(true);
        mBinding.recyclerView.setNestedScrollingEnabled(false);
        linearLayoutManager = new GridLayoutManager(getContext(),2);
        mBinding.recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new CheepCareRateCardAdapter();
        mBinding.recyclerView.setAdapter(adapter);


    }


    public class CheepCareRateCardAdapter extends RecyclerView.Adapter<CheepCareRateCardAdapter.ViewHolder> {

        @NonNull
        @Override
        public CheepCareRateCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.cell_cheep_care_rate_card, parent, false);
            return new CheepCareRateCardAdapter.ViewHolder(view);
        }

        @SuppressLint("ResourceType")
        @Override
        public void onBindViewHolder(@NonNull CheepCareRateCardAdapter.ViewHolder holder, int position) {

            final CheepCareRateCardModel model = list.get(position);

            holder.tvCatName.setText(model.catName);
            //Background image
            GlideUtility.loadImageView(mContext, holder.imgBackground, model.catImage.original, R.drawable.gradient_black);
            GlideUtility.loadImageView(mContext, holder.imgBackground, model.catImage.thumb, R.drawable.gradient_black);
            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheepCareRateCardPricingFragment fragment4 = CheepCareRateCardPricingFragment.newInstance(model.catId,model.catName);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .add(R.id.content, fragment4, CheepCareRateCardPricingFragment.TAG)
                            .addToBackStack(CheepCareRateCardPricingFragment.TAG)
                            .commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvCatName;
            private ImageView imgBackground;
            private RelativeLayout relativeLayout;

            ViewHolder(View itemView) {
                super(itemView);
                tvCatName = itemView.findViewById(R.id.text_category_name);
                imgBackground = itemView.findViewById(R.id.img_category_background);
                relativeLayout = itemView.findViewById(R.id.rel_category_image);

            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Category AllCats [END]/////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void getAllCatsCategoryFromServer() {

        if (!Utility.isConnected(getContext())) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressDialog();
        // Sort Type Params
        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.CATEGORY_LIST
                , mCallCategoryListWSErrorListener
                , mCallCategoryListWSResponseListener
                , null
                , null
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.CATEGORY_LIST);
    }

    Response.Listener mCallCategoryListWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");

            String strResponse = (String) response;
            hideProgressDialog();
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        list = GsonUtility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), CheepCareRateCardModel[].class);
                        setAdapter();
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
                        Utility.logout(mContext, true, statusCode);
                        if (getActivity() != null)
                            getActivity().finish();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                mCallCategoryListWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };


    Response.ErrorListener mCallCategoryListWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());

            hideProgressDialog();
        }
    };
}
