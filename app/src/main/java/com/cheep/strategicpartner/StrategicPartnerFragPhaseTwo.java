package com.cheep.strategicpartner;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.adapter.AddressRecyclerViewAdapter;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.FragmentStrategicPartnerPhaseTwoBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.GuestUserDetails;
import com.cheep.model.LocationInfo;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.strategicpartner.model.AllSubSubCat;
import com.cheep.strategicpartner.model.MediaModel;
import com.cheep.strategicpartner.model.QueAnsModel;
import com.cheep.strategicpartner.model.StrategicPartnerServiceModel;
import com.cheep.strategicpartner.recordvideo.RecordVideoNewActivity;
import com.cheep.utils.FetchLocationInfoUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.cheep.network.NetworkUtility.TAGS.CAT_ID;
import static com.cheep.network.NetworkUtility.TAGS.SP_USER_ID;

/**
 * Created by Giteeka on 25/7/17.
 * This Fragment is second step of Strategic partner screen.
 * Questionnary screen
 * partner specific question will be inflated run time
 */

public class StrategicPartnerFragPhaseTwo extends BaseFragment {
    public static final String TAG = "StracPartnerFragThree";
    private FragmentStrategicPartnerPhaseTwoBinding mFragmentStrategicPartnerPhaseTwoBinding;
    private StrategicPartnerTaskCreationAct mStrategicPartnerTaskCreationAct;
    private SuperCalendar startDateTimeSuperCalendar = SuperCalendar.getInstance();
    private String mCurrentPhotoPath = "";
    private MediaRecycleAdapter mMediaRecycleAdapter;
    private int count = 1;
    private ArrayList<QueAnsModel> mList;
    private boolean isVerified = false;

    @SuppressWarnings("unused")
    public static StrategicPartnerFragPhaseTwo newInstance() {
        return new StrategicPartnerFragPhaseTwo();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentStrategicPartnerPhaseTwoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_strategic_partner_phase_two, container, false);
        return mFragmentStrategicPartnerPhaseTwoBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
        if (!isVisibleToUser || mStrategicPartnerTaskCreationAct == null) {
            return;
        }
        // calculation of total amount of selected services
        double total = 0;
        double totalOfBasePrice = 0;
        for (StrategicPartnerServiceModel model : mStrategicPartnerTaskCreationAct.getSelectedSubService()) {
            List<AllSubSubCat> allSubSubCats = model.allSubSubCats;
            for (AllSubSubCat allSubSubCat : allSubSubCats) {
                try {
                    total += Double.parseDouble(allSubSubCat.price);
                    totalOfBasePrice += Double.parseDouble(allSubSubCat.basePrice);
                } catch (NumberFormatException e) {
                    total += 0;
                    totalOfBasePrice += 0;
                }
            }
        }
        mFragmentStrategicPartnerPhaseTwoBinding.textContinue.setText(getString(R.string.book_and_pay_x, "" + Utility.getQuotePriceFormatter(String.valueOf(total))));
        mStrategicPartnerTaskCreationAct.total = String.valueOf(total);
        mStrategicPartnerTaskCreationAct.totalOfBasePrice = String.valueOf(totalOfBasePrice);
        // Task Description

        mStrategicPartnerTaskCreationAct.setTaskState(
                isVerified ?
                        StrategicPartnerTaskCreationAct.STEP_TWO_VERIFIED :
                        StrategicPartnerTaskCreationAct.STEP_TWO_NORMAL);


    }

    @Override
    public void initiateUI() {
        Log.d(TAG, "initiateUI() called");

        // handle click of bottom button (book and pay)
        mFragmentStrategicPartnerPhaseTwoBinding.textContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = validateAllQueAndAns();
                if (message.equalsIgnoreCase(Utility.EMPTY_STRING)) {
                    for (QueAnsModel model : mList)
                        if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_UPLOAD)) {
                            model.medialList = mMediaRecycleAdapter.getList();
                            break;
                        }
                    mStrategicPartnerTaskCreationAct.setQuestionsList(mList);
                    isVerified = true;
                    // Make the status Verified

                    //Alert The activity that step one is been verified.
                    mStrategicPartnerTaskCreationAct.setTaskState(StrategicPartnerTaskCreationAct.STEP_TWO_VERIFIED);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mStrategicPartnerTaskCreationAct.gotoStep(StrategicPartnerTaskCreationAct.STAGE_3);
                        }
                    }, 500);
                } else {
                    Utility.showSnackBar(message, mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
                }
            }
        });

        fetchListOfQuestions(mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id);

    }

    /////////////////////////////////////// WEB CALL FOR QUESTIONNARY ////////////////////////////////
    private void fetchListOfQuestions(String catId) {
        Log.d(TAG, "fetchListOfQuestions() called with: catId = [" + catId + "]");
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);
        }

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(CAT_ID, catId);

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.FETCH_SUB_CATEGORIES_QUESTIONNAIRE
                , mCallFetchAllSubCateSPListingWSErrorListener
                , mCallFetchAllSubCateSPListingWSResponseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.FETCH_SUB_CATEGORIES_QUESTIONNAIRE);
    }

    private Response.Listener mCallFetchAllSubCateSPListingWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        // inflate all question ui
                        mStrategicPartnerTaskCreationAct.setQuestionsList(mList);
                        inflateUI(jsonObject.optString(NetworkUtility.TAGS.DATA));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        if (getActivity() != null)
                            getActivity().finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallFetchAllSubCateSPListingWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };


    private Response.ErrorListener mCallFetchAllSubCateSPListingWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
        }
    };
    /////////////////////////////////////// WEB CALL FOR QUESTIONNARY completed ////////////////////////////////

    @Override
    public void setListener() {
        Log.d(TAG, "setListener() called");
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof StrategicPartnerTaskCreationAct) {
            mStrategicPartnerTaskCreationAct = (StrategicPartnerTaskCreationAct) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.FETCH_SUB_CATEGORIES_QUESTIONNAIRE);

    }


    private void inflateUI(String response) {
        // top layout
        // get question answer list
        mList = Utility.getObjectListFromJsonString(response, QueAnsModel[].class);        // load list

        mStrategicPartnerTaskCreationAct.setQuestionsList(mList);
        for (QueAnsModel model : mList) {
            // image/video selection UI
            if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_UPLOAD)) {
                inflateMediaTemplate(model);
            }
            // date  selection UI
            else if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_DATE_PICKER)) {
                inflateDatePickerTemplate(model);
            }
            // time selection UI
            else if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_TIME_PICKER)) {
                inflateAnsTimePickerTemplate(model);
            }
            // location selection UI
            else if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_LOCATION)) {
                inflateAnsLocationPickerTemplate(model);
            }
            // enter free text UI
            else if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_TEXT_FIELD)) {
                inflateEditTextTemplate(model);
            }
            // multiple choice UI
            else if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_DROPDOWN)) {
                inflateDropDownTemplate(model);
            }

            count++;
        }
    }

    private void inflateDropDownTemplate(final QueAnsModel model) {
        // question view
        ViewGroup queView = (ViewGroup) LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_question, null, false);
        // text question number
        TextView txtQueNo = queView.findViewById(R.id.txtQueNo);
        txtQueNo.setText(String.valueOf(count));
        txtQueNo.setTag(model.questionId);

        // text question
        TextView txtQueStr = queView.findViewById(R.id.txtQueStr);
        txtQueStr.setText(String.valueOf(model.question));

        // if question is last then don't show vertical line
        TextView txtVerticalLine = queView.findViewById(R.id.txtVerticalLine);
        if (count == mList.size()) {
            txtVerticalLine.setVisibility(View.GONE);
        }

        // add question view on top view
        mFragmentStrategicPartnerPhaseTwoBinding.linMain.addView(queView);

        // answer view
        final ViewGroup ansView = queView.findViewById(R.id.relAnsView);
        final View viewInflated = LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_answer_drop_down, null, false);

        // answer text view
        final TextView txtAnswer = viewInflated.findViewById(R.id.txtQueAnswer);

        // by default first option will be selected
        txtAnswer.setText(model.dropDownList.get(0).dropdown_answer);
        model.dropDownList.get(0).isSelected = true;
        model.answer = model.dropDownList.get(0).dropdown_answer;
        txtQueNo.setSelected(true);
        // open menu onClick of ans text
        txtAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDropDownMenu(txtAnswer, model);
            }
        });

        ansView.addView(viewInflated);
    }

    private void showDropDownMenu(final TextView textView, final QueAnsModel model) {
        Log.i(TAG, "showDropDownMenu: ");
        final View mFilterPopupWindow = View.inflate(mStrategicPartnerTaskCreationAct, R.layout.layout_drop_down_menu, null);

        final PopupWindow mPopupWindow = new PopupWindow(mStrategicPartnerTaskCreationAct);
        RecyclerView recyclerview = mFilterPopupWindow.findViewById(R.id.listMultipleChoice);
        recyclerview.setLayoutManager(new LinearLayoutManager(mStrategicPartnerTaskCreationAct));

        Collections.sort(model.dropDownList, new Comparator<QueAnsModel.DropDownModel>() {
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
        });

        final DropDownAdapter dropDownAdapter = new DropDownAdapter(model.dropDownList);
        recyclerview.setAdapter(dropDownAdapter);
        DropDownAdapter.ClickItem clickListener = new DropDownAdapter.ClickItem() {
            @Override
            public void clickItem(int i) {
                for (int j = 0; j < model.dropDownList.size(); j++) {
                    QueAnsModel.DropDownModel dropDownModel = model.dropDownList.get(j);
                    dropDownModel.isSelected = i == j;
                }
                dropDownAdapter.setSelected(i);
                textView.setText(model.dropDownList.get(i).dropdown_answer);
                textView.setSelected(true);
                model.answer = model.dropDownList.get(i).dropdown_answer;
                mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag(model.questionId).setSelected(true);
                mPopupWindow.dismiss();

            }
        };
        dropDownAdapter.setListener(clickListener);

        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setContentView(mFilterPopupWindow);
        mPopupWindow.setWidth(ListView.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(ListView.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);

        // No animation at present
        mPopupWindow.setAnimationStyle(0);

        // Displaying the popup at the specified location, + offsets.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mPopupWindow.showAsDropDown(textView, 0, -textView.getHeight(), Gravity.NO_GRAVITY);
        } else {
            mPopupWindow.showAsDropDown(textView, 0, -textView.getHeight());
        }
    }


    private void inflateDatePickerTemplate(final QueAnsModel model) {
        // question view
        ViewGroup queView = (ViewGroup) LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_question, null, false);

        // text question number
        final TextView txtQueNo = queView.findViewById(R.id.txtQueNo);
        txtQueNo.setText(String.valueOf(count));

        // text question
        TextView txtQueStr = queView.findViewById(R.id.txtQueStr);
        txtQueStr.setText(String.valueOf(model.question));

        // if question is last then don't show vertical line
        TextView txtVerticalLine = queView.findViewById(R.id.txtVerticalLine);
        if (count == mList.size()) {
            txtVerticalLine.setVisibility(View.GONE);
        }

        // add question view on top view
        mFragmentStrategicPartnerPhaseTwoBinding.linMain.addView(queView);

        // answer view
        ViewGroup ansView = queView.findViewById(R.id.relAnsView);
        View view = LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_answer_date_picker, null, false);
        final TextView txtAnswer = view.findViewById(R.id.txtQueAnswer);
        txtAnswer.setText(getString(R.string.label_select_the_date));
        txtAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                txtAnswer.setSelected(true);
                DatePickerDialog datePickerDialog = new DatePickerDialog(mStrategicPartnerTaskCreationAct, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        if (view.isShown()) {
                            Log.d(TAG, "onDateSet() called with: view = [" + view + "], year = [" + year + "], monthOfYear = [" + monthOfYear + "], dayOfMonth = [" + dayOfMonth + "]");
                            startDateTimeSuperCalendar = SuperCalendar.getInstance();
                            startDateTimeSuperCalendar.set(Calendar.YEAR, year);
                            startDateTimeSuperCalendar.set(Calendar.MONTH, monthOfYear);
                            startDateTimeSuperCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            txtAnswer.setText(startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM_YYYY));
                            model.answer = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM_YYYY);
                            txtQueNo.setSelected(true);
                            txtAnswer.setSelected(false);


                            for (int i = 0; i < mList.size(); i++) {
                                QueAnsModel queAnsModel = mList.get(i);
                                if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_TIME_PICKER)) {
                                    queAnsModel.answer = "";

                                    TextView txtTime = mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag(Utility.TEMPLATE_TIME_PICKER);
                                    if (txtTime != null)
                                        txtTime.setText(getString(R.string.label_select_the_time));
                                }
                            }
                            //                            mStrategicPartnerTaskCreationAct.date = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM_YYYY);
                            setBtnBookAndPayBgState(validateAllQueAndAns().isEmpty());
                        }
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        txtAnswer.setSelected(false);
                    }
                });
                datePickerDialog.show();

            }
        });
        ansView.addView(view);
    }

    private void inflateAnsTimePickerTemplate(final QueAnsModel model) {

        // question view
        ViewGroup queView = (ViewGroup) LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_question, null, false);

        // text question number
        TextView txtQueNo = queView.findViewById(R.id.txtQueNo);
        txtQueNo.setText(String.valueOf(count));
        txtQueNo.setTag(model.questionId);

        // text question
        TextView txtQueStr = queView.findViewById(R.id.txtQueStr);
        txtQueStr.setText(String.valueOf(model.question));

        // if question is last then don't show vertical line
        TextView txtVerticalLine = queView.findViewById(R.id.txtVerticalLine);
        if (count == mList.size()) {
            txtVerticalLine.setVisibility(View.GONE);
        }

        // add question view on top view
        mFragmentStrategicPartnerPhaseTwoBinding.linMain.addView(queView);


        // answer view
        ViewGroup ansView = queView.findViewById(R.id.relAnsView);
        View view = LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_answer_date_picker, null, false);

        // answer text view
        final TextView txtAnswer = view.findViewById(R.id.txtQueAnswer);
        txtAnswer.setTag(Utility.TEMPLATE_TIME_PICKER);
        txtAnswer.setText(getString(R.string.label_select_the_time));

        // handle click - time selection
        txtAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (QueAnsModel queAnsModel : mList) {
                    // check if date is selected
                    if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_DATE_PICKER)) {
                        if (queAnsModel.answer != null && !queAnsModel.answer.equalsIgnoreCase("")) {
                            txtAnswer.setSelected(true);
                            showTimePickerDialog(txtAnswer, model);
                        } else {
                            txtAnswer.setSelected(false);
                            Utility.showSnackBar(getString(R.string.alert_select_the_date), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
                        }
                        break;
                    }
                }
            }
        });
        ansView.addView(view);
    }

    /**
     * add que and ans view for free text question type
     *
     * @param model free text Question model
     */
    private void inflateEditTextTemplate(final QueAnsModel model) {

        // question view
        final ViewGroup queView = (ViewGroup) LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_question, null, false);

        // text question number
        final TextView txtQueNo = queView.findViewById(R.id.txtQueNo);
        txtQueNo.setText(String.valueOf(count));

        // text question
        TextView txtQueStr = queView.findViewById(R.id.txtQueStr);
        txtQueStr.setText(String.valueOf(model.question));

        // if question is last then don't show vertical line
        TextView txtVerticalLine = queView.findViewById(R.id.txtVerticalLine);
        if (count == mList.size()) {
            txtVerticalLine.setVisibility(View.GONE);
        }

        // add question view on top view
        mFragmentStrategicPartnerPhaseTwoBinding.linMain.addView(queView);

        // answer view
        ViewGroup ansView = queView.findViewById(R.id.relAnsView);
        View view = LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_answer_edit_text, null, false);
        final EditText edtQueAnswer = view.findViewById(R.id.edtQueAnswer);
        edtQueAnswer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                edtQueAnswer.setCursorVisible(true);
                return false;
            }
        });
        // text watcher set typed text as answer in models
        edtQueAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                model.answer = edtQueAnswer.getText().toString();
                // set background of question no as answered
                txtQueNo.setSelected(!model.answer.trim().isEmpty());
            }
        });
        ansView.addView(view);
    }

    /**
     * @param textView this is answer text view to set time
     * @param model    question model for time picker
     */
    private void showTimePickerDialog(final TextView textView, final QueAnsModel model) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(mStrategicPartnerTaskCreationAct,
                new TimePickerDialog.OnTimeSetListener() {


                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (view.isShown()) {

                            Log.d(TAG, "onTimeSet() called with: view = [" + view + "], hourOfDay = [" + hourOfDay + "], minute = [" + minute + "]");


                            startDateTimeSuperCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            startDateTimeSuperCalendar.set(Calendar.MINUTE, minute);
                            SuperCalendar calAfter3Hours = SuperCalendar.getInstance().getNext3HoursTime();


                            if (startDateTimeSuperCalendar.getTimeInMillis() < calAfter3Hours.getTimeInMillis()) {
                                textView.setText(getString(R.string.label_select_the_time));
                                textView.setSelected(false);
                                model.answer = "";
                                Utility.showSnackBar(getString(R.string.alert_time_must_be_after_3_hour), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
                            } else {
                                String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);

                                // set selected time to text view
                                textView.setText(selectedDateTime);
                                textView.setSelected(false);


                                // this var is for payment summary screen for user task details
//                            mStrategicPartnerTaskCreationAct.time = selectedDateTime;

                                // set time zone for start date time
//                                startDateTimeSuperCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

                                // set timestamp as answer for web api
                                model.answer = String.valueOf(startDateTimeSuperCalendar.getTimeInMillis());

                                // set background of ques no as answered
                                mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag(model.questionId).setSelected(true);
                                setBtnBookAndPayBgState(validateAllQueAndAns().isEmpty());


                                // enable
                                TextView txtLocation = mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag(Utility.TEMPLATE_LOCATION);
                                txtLocation.setSelected(true);
                                txtLocation.setBackground(ContextCompat.getDrawable(mStrategicPartnerTaskCreationAct, R.drawable.background_ans_normal));

                            }
                        }
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
        timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                textView.setSelected(false);
            }
        });
        timePickerDialog.show();

    }


    /**
     * @param model media image/video selection question model
     *              inflate ui for media image/video selection question
     */
    private void inflateMediaTemplate(QueAnsModel model) {

        // question view
        ViewGroup queView = (ViewGroup) LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_question, null, false);

        // text question number
        TextView txtQueNo = queView.findViewById(R.id.txtQueNo);
        txtQueNo.setTag(model.answerType);
        txtQueNo.setText(String.valueOf(count));

        // text question
        TextView txtQueStr = queView.findViewById(R.id.txtQueStr);
        txtQueStr.setText(String.valueOf(model.question));

        // if question is last then don't show vertical line
        TextView txtVerticalLine = queView.findViewById(R.id.txtVerticalLine);
        if (count == mList.size()) {
            txtVerticalLine.setVisibility(View.GONE);
        }

        // add question view on top view
        mFragmentStrategicPartnerPhaseTwoBinding.linMain.addView(queView);

        // answer view
        ViewGroup ansView = queView.findViewById(R.id.relAnsView);
        View view = LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_answer_media, null, false);

        final ImageView imgAdd = view.findViewById(R.id.imgAdd);
        // this tag is set for onActivityResult to find image view by tag
        imgAdd.setTag("AddImage");

        // handles click for adding image/video chooser flow
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMediaChooserDialog();
            }
        });

        // recycle view for thumbnails for media files
        RecyclerView recycleImg = view.findViewById(R.id.recycleImg);
        recycleImg.setNestedScrollingEnabled(false);
        recycleImg.setLayoutManager(new LinearLayoutManager(mStrategicPartnerTaskCreationAct, LinearLayoutManager.HORIZONTAL, false));

        mMediaRecycleAdapter = new MediaRecycleAdapter(new MediaRecycleAdapter.ItemClick() {
            @Override
            public void removeMedia() {
                // after uploading 3 media file if any one is deleted then add image view again
                if (mMediaRecycleAdapter.getItemCount() < 3)
                    imgAdd.setVisibility(View.VISIBLE);
                if (mMediaRecycleAdapter.getItemCount() < 1)
                    mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag(Utility.TEMPLATE_UPLOAD).setSelected(false);

            }
        });

        recycleImg.setAdapter(mMediaRecycleAdapter);

        ansView.addView(view);

    }

    private void showMediaChooserDialog() {
        Log.d(TAG, "showPictureChooserDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(mStrategicPartnerTaskCreationAct);
        builder.setTitle(getString(R.string.choose_media))
                .setItems(R.array.choose_media_type, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0) {
                            showVideoChooserDialog();
                        } else {
                            //Select Gallery
                            // In case Choose File from Gallery
                            showPictureChooserDialog();
                        }
                    }
                });
        builder.create();

        //Show the dialog
        builder.show();
    }

    private void showVideoChooserDialog() {
        Log.d(TAG, "showPictureChooserDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(mStrategicPartnerTaskCreationAct);
        builder.setTitle(getString(R.string.choose_video))
                .setItems(R.array.choose_video_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0) {
                            takeVideoIntent(Utility.REQUEST_CODE_WRITE_EXTERNAL_STORAGE_ADD_PROFILE_CAMERA);
                        } else {
                            //Select Gallery
                            // In case Choose File from Gallery
                            chooseVideoFromGallery(Utility.REQUEST_CODE_GET_VIDEO_GALLERY, Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY);
                        }
                    }
                });
        builder.create();

        //Show the dialog
        builder.show();
    }


    private void takeVideoIntent(int requestPermissionCode) {
        //Go ahead with Camera capturing
        if (ContextCompat.checkSelfPermission(mStrategicPartnerTaskCreationAct, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mStrategicPartnerTaskCreationAct, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mStrategicPartnerTaskCreationAct, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mStrategicPartnerTaskCreationAct, Manifest.permission.CAMERA)
                    && ActivityCompat.shouldShowRequestPermissionRationale(mStrategicPartnerTaskCreationAct, Manifest.permission.RECORD_AUDIO)) {
                ActivityCompat.requestPermissions(mStrategicPartnerTaskCreationAct, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            } else {
                ActivityCompat.requestPermissions(mStrategicPartnerTaskCreationAct, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            }
        } else {
            //Go ahead with Camera capturing


            Intent takePictureIntent = new Intent(mStrategicPartnerTaskCreationAct, RecordVideoNewActivity.class);
            if (takePictureIntent.resolveActivity(mStrategicPartnerTaskCreationAct.getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile;
                photoFile = createVideoFile();
                mCurrentPhotoPath = photoFile.getAbsolutePath();
                if (photoFile.exists()) {
                    photoFile.delete();
                } else {
                    photoFile.getParentFile().mkdirs();
                }

                // Continue only if the File was successfully created
                Uri photoURI = FileProvider.getUriForFile(mStrategicPartnerTaskCreationAct,
                        BuildConfig.FILE_PROVIDER_URL,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Grant URI permission START
                // Enabling the permission at runtime
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip =
                            ClipData.newUri(mStrategicPartnerTaskCreationAct.getContentResolver(), "A photo", photoURI);
                    takePictureIntent.setClipData(clip);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    List<ResolveInfo> resInfoList =
                            mStrategicPartnerTaskCreationAct.getPackageManager()
                                    .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        mStrategicPartnerTaskCreationAct.grantUriPermission(packageName, photoURI,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                }
                //Grant URI permission END
//                startActivityForResult(takePictureIntent, requestCode);
                startActivityForResult(takePictureIntent, Utility.REQUEST_CODE_VIDEO_CAPTURE);
            }

        }
    }

    private void chooseVideoFromGallery(int requestFileChooserCode, int requestPermissionCode) {
        Log.d(TAG, "choosePictureFromGallery() called with: requestFileChooserCode = [" + requestFileChooserCode + "], requestPermissionCode = [" + requestPermissionCode + "]");
        if (ContextCompat.checkSelfPermission(mStrategicPartnerTaskCreationAct, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mStrategicPartnerTaskCreationAct, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(mStrategicPartnerTaskCreationAct, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            } else {
                ActivityCompat.requestPermissions(mStrategicPartnerTaskCreationAct, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            }
        } else {
            //Go ahead with file choosing
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            if (intent.resolveActivity(mStrategicPartnerTaskCreationAct.getPackageManager()) != null) {
                startActivityForResult(intent, requestFileChooserCode);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////    IMAGE CAPTURE - CHOOSER   /////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    private void showPictureChooserDialog() {
        Log.d(TAG, "showPictureChooserDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(mStrategicPartnerTaskCreationAct);
        builder.setTitle(getString(R.string.choose_image))
                .setItems(R.array.choose_image_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0) {
                            dispatchTakePictureIntent(Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE, Utility.REQUEST_CODE_WRITE_EXTERNAL_STORAGE_ADD_PROFILE_CAMERA);
                        } else {
                            //Select Gallery
                            // In case Choose File from Gallery
                            choosePictureFromGallery(Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY, Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY);
                        }
                    }
                });
        builder.create();

        //Show the dialog
        builder.show();
    }

    private void dispatchTakePictureIntent(int requestCode, int requestPermissionCode) {
        //Go ahead with Camera capturing
        if (ContextCompat.checkSelfPermission(mStrategicPartnerTaskCreationAct, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mStrategicPartnerTaskCreationAct, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(mStrategicPartnerTaskCreationAct, new String[]{Manifest.permission.CAMERA}, requestPermissionCode);
            } else {
                ActivityCompat.requestPermissions(mStrategicPartnerTaskCreationAct, new String[]{Manifest.permission.CAMERA}, requestPermissionCode);
            }
        } else {
            //Go ahead with Camera capturing
            startCameraCaptureChooser(requestCode);
        }
    }

    private void startCameraCaptureChooser(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(mStrategicPartnerTaskCreationAct.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile;
            photoFile = createImageFile();
            mCurrentPhotoPath = photoFile.getAbsolutePath();
            if (photoFile.exists()) {
                photoFile.delete();
            } else {
                photoFile.getParentFile().mkdirs();
            }

            // Continue only if the File was successfully created
            Uri photoURI = FileProvider.getUriForFile(mStrategicPartnerTaskCreationAct,
                    BuildConfig.FILE_PROVIDER_URL,
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            // Grant URI permission START
            // Enabling the permission at runtime
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip =
                        ClipData.newUri(mStrategicPartnerTaskCreationAct.getContentResolver(), "A photo", photoURI);
                takePictureIntent.setClipData(clip);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                List<ResolveInfo> resInfoList =
                        mStrategicPartnerTaskCreationAct.getPackageManager()
                                .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    mStrategicPartnerTaskCreationAct.grantUriPermission(packageName, photoURI,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
            }
            //Grant URI permission END
            startActivityForResult(takePictureIntent, requestCode);
        }
    }

    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss"/*, Locale.US*/).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        /*File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );*/

        File photoFile = new File(new File(mStrategicPartnerTaskCreationAct.getFilesDir(), "CheepImages"), imageFileName);
        mCurrentPhotoPath = photoFile.getAbsolutePath();
        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = photoFile.getAbsolutePath();
        return photoFile;
    }

    private File createVideoFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss"/*, Locale.US*/).format(new Date());
        String imageFileName = "VID_" + timeStamp + ".mp4";
        /*File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );*/

        File photoFile = new File(new File(mStrategicPartnerTaskCreationAct.getFilesDir(), "CheepImages"), imageFileName);
        mCurrentPhotoPath = photoFile.getAbsolutePath();
        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = photoFile.getAbsolutePath();
        return photoFile;
    }


    //// Gallery /////
    private void choosePictureFromGallery(int requestFileChooserCode, int requestPermissionCode) {
        Log.d(TAG, "choosePictureFromGallery() called with: requestFileChooserCode = [" + requestFileChooserCode + "], requestPermissionCode = [" + requestPermissionCode + "]");
        if (ContextCompat.checkSelfPermission(mStrategicPartnerTaskCreationAct, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mStrategicPartnerTaskCreationAct, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(mStrategicPartnerTaskCreationAct, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            } else {
                ActivityCompat.requestPermissions(mStrategicPartnerTaskCreationAct, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            }
        } else {
            //Go ahead with file choosing
            startIntentFileChooser(requestFileChooserCode);
        }
    }

    private void startIntentFileChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(mStrategicPartnerTaskCreationAct.getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// LOCATION //////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////


    private void inflateAnsLocationPickerTemplate(final QueAnsModel model) {
        // question view
        ViewGroup queView = (ViewGroup) LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_question, null, false);

        // text question number
        final TextView txtQueNo = queView.findViewById(R.id.txtQueNo);
        txtQueNo.setText(String.valueOf(count));

        // text question
        TextView txtQueStr = queView.findViewById(R.id.txtQueStr);
        txtQueStr.setText(String.valueOf(model.question));

        // if question is last then don't show vertical line
        TextView txtVerticalLine = queView.findViewById(R.id.txtVerticalLine);
        if (count == mList.size()) {
            txtVerticalLine.setVisibility(View.GONE);
        }

        // add question view on top view
        mFragmentStrategicPartnerPhaseTwoBinding.linMain.addView(queView);

        // answer view
        ViewGroup ansView = queView.findViewById(R.id.relAnsView);
        View view = LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_answer_location_picker, null, false);

        // answer text view
        final TextView txtAnswer = view.findViewById(R.id.txtQueAnswer);
        txtAnswer.setText(getString(R.string.label_select_an_address));
        txtAnswer.setTag(Utility.TEMPLATE_LOCATION);
        txtAnswer.setSelected(false);

        // handle click - open bottom sheet address dialog for location
        txtAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtAnswer.isSelected())
                    showAddressDialog(txtAnswer, txtQueNo, model);
//                else {
//                    for (int i = 0; i < mList.size(); i++) {
//                        QueAnsModel queAnsModel = mList.get(i);
//                        // alert date is not selected
//                        if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_DATE_PICKER)) {
//                            if (TextUtils.isEmpty(queAnsModel.answer)) {
//                                Utility.showSnackBar(getString(R.string.alert_select_the_date), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
//                                break;
//                            }
//                        }
//                        // alert time is not selected
//                        else if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_TIME_PICKER)) {
//                            if (TextUtils.isEmpty(queAnsModel.answer)) {
//                                Utility.showSnackBar(getString(R.string.alert_select_the_time), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
//                                break;
//                            }
//                        }
//                    }
//                }
            }
        });
        ansView.addView(view);
    }

    private BottomAlertDialog addressDialog;
    private AddressRecyclerViewAdapter addressRecyclerViewAdapter;
    //    private String addressId = "";
//    private AddressModel mSelectedAddressModel;

    private void showAddressDialog(final TextView textAns, final TextView textQueNo,
                                   final QueAnsModel queAnsModel) {
        View view = View.inflate(mContext, R.layout.dialog_choose_address_new_task, null);
        boolean shouldOpenAddAddress = fillAddressRecyclerView((RecyclerView) view.findViewById(R.id.recycler_view));
        addressDialog = new BottomAlertDialog(mContext);
        view.findViewById(R.id.btn_add_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddAddressDialog(null);
//                addAddressDialog.dismiss();
            }
        });
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addressRecyclerViewAdapter != null && !addressRecyclerViewAdapter.getmList().isEmpty()) {
                    AddressModel model = addressRecyclerViewAdapter.getSelectedAddress();
                    if (model != null) {
//                        String address;
                        checkStrategicPartnerProAvailability(model, textAns, queAnsModel, textQueNo);
                    }

                }
            }
        });
        addressDialog.setTitle(getString(R.string.label_address));
        addressDialog.setCustomView(view);
        addressDialog.setExpandedInitially(true);
        addressDialog.showDialog();

        if (shouldOpenAddAddress) {
            showAddAddressDialog(null);
            addressDialog.view.findViewById(R.id.btn_submit).setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("unchecked")
    private void checkStrategicPartnerProAvailability(final AddressModel model, final TextView textAns, final QueAnsModel queAnsModel, final TextView textQueNo) {

        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null)
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.CAT_ID, mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id);


        int addressId;
        try {
            addressId = Integer.parseInt(model.address_id);
        } catch (Exception e) {
            addressId = 0;
        }
        if (addressId <= 0) {
            // In case its nagative then provide other address information
            /*
             public String address_initials;
             public String address;
             public String category; //comes from NetworkUtility.TAGS.ADDRESS_TYPE.
             public String lat;
             public String lng;
             */
            mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, model.address_initials);
            mParams.put(NetworkUtility.TAGS.ADDRESS, model.address);
            mParams.put(NetworkUtility.TAGS.CATEGORY, model.category);
            mParams.put(NetworkUtility.TAGS.LAT, model.lat);
            mParams.put(NetworkUtility.TAGS.LNG, model.lng);
            mParams.put(NetworkUtility.TAGS.CITY_NAME, model.cityName);
            mParams.put(NetworkUtility.TAGS.COUNTRY, model.countryName);
            mParams.put(NetworkUtility.TAGS.STATE, model.stateName);
        } else {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, model.address_id);
        }

        Utility.hideKeyboard(mContext);
        @SuppressWarnings("unchecked")
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.CHECK_PRO_AVAILABILITY_FOR_STRATEGIC_TASK
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                // Close Progressbar
                hideProgressDialog();
                // Show Toast
                Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());

            }
        }
                , new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                hideProgressDialog();
                Log.i(TAG, "onResponse: " + response);

                String strResponse = (String) response;
                try {
                    JSONObject jsonObject = new JSONObject(strResponse);
                    Log.i(TAG, "onResponse: " + jsonObject.toString());
                    int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                    String error_message;

                    switch (statusCode) {
                        case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                            JSONObject jsonData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
                            // strategic partner pro id for given location
                            mStrategicPartnerTaskCreationAct.spUserId = jsonData.optString(SP_USER_ID);

                            if (mStrategicPartnerTaskCreationAct.spUserId.equalsIgnoreCase(Utility.EMPTY_STRING)) {
                                Utility.showToast(mStrategicPartnerTaskCreationAct, getString(R.string.alert_strategic_partner_unavailable_in_location));
                            } else {
                                mStrategicPartnerTaskCreationAct.mSelectedAddressModel = model;
                        /*if (!model.address_initials.isEmpty()) {
                            mStrategicPartnerTaskCreationAct.mSelectedAddressModel.address = model.address_initials + ", " + model.address;
                        } else {
                            mStrategicPartnerTaskCreationAct.mSelectedAddressModel.address = model.address;
                        }
//                        addressId = model.address_id;
                        mSelectedAddressModel = model;*/
                                Log.e(TAG, "category detail >> " + model.category + "");
                                textAns.setBackground(ContextCompat.getDrawable(mStrategicPartnerTaskCreationAct, R.drawable.background_ans_normal));
                                if (model.category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.HOME))
                                    textAns.setText(getString(R.string.label_home));
                                else if (model.category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE))
                                    textAns.setText(getString(R.string.label_office));
                                else
                                    textAns.setText(getString(R.string.label_other));
                                textAns.setSelected(true);
                                queAnsModel.answer = model.address_id;
                                textQueNo.setSelected(true);
                                setBtnBookAndPayBgState(validateAllQueAndAns().isEmpty());
                                addressDialog.dismiss();
                            }
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                            // Show Toast
                            Utility.showToast(mStrategicPartnerTaskCreationAct, getString(R.string.label_something_went_wrong));
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                            error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                            // Show message
                            Utility.showToast(mStrategicPartnerTaskCreationAct, error_message);
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                        case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                            //Logout and finish the current activity
                            Utility.logout(mContext, true, statusCode);
                            mStrategicPartnerTaskCreationAct.finish();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mCallDeleteAddressWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
                }
                hideProgressDialog();
            }
        }
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.CHECK_PRO_AVAILABILITY_FOR_STRATEGIC_TASK);


    }

    /**
     * Loads address in choose address dialog box in recycler view
     */
    private String TEMP_ADDRESS_ID = "";

    private boolean fillAddressRecyclerView(RecyclerView recyclerView) {
        ArrayList<AddressModel> addressList;

        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            addressList = PreferenceUtility.getInstance(mContext).getUserDetails().addressList;
        } else {
            addressList = PreferenceUtility.getInstance(mContext).getGuestUserDetails().addressList;
        }

        //Setting RecyclerView Adapter
        addressRecyclerViewAdapter = new AddressRecyclerViewAdapter(addressList, new AddressRecyclerViewAdapter.AddressItemInteractionListener() {
            @Override
            public void onEditClicked(AddressModel model, int position) {
                TEMP_ADDRESS_ID = model.address_id;
                showAddAddressDialog(model);
            }

            @Override
            public void onDeleteClicked(AddressModel model, int position) {
//                TEMP_ADDRESS_ID = model.address_id;
//                callDeleteAddressWS(model.address_id);
                showAddressDeletionConfirmationDialog(model);

            }

            @Override
            public void onRowClicked(AddressModel model, int position) {

            }
        });
        addressRecyclerViewAdapter.setSelectedAddressId(mStrategicPartnerTaskCreationAct.mSelectedAddressModel == null ? Utility.EMPTY_STRING : mStrategicPartnerTaskCreationAct.mSelectedAddressModel.address_id);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(addressRecyclerViewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_16dp)));

        //Here we are checking if address is not there then open add address dialog immediately
        return addressList == null || addressList.isEmpty();
    }
///////////////////////////// DELETE CONFIRMATION DIALOG//////////////////////////////////////

    private void showAddressDeletionConfirmationDialog(final AddressModel model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.MyAlertDialogStyle);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.label_address_delete_title));
        builder.setMessage(getString(R.string.label_address_delete_message));
        builder.setPositiveButton(getString(R.string.label_Ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick() called with: dialogInterface = [" + dialogInterface + "], i = [" + i + "]");
                TEMP_ADDRESS_ID = model.address_id;
                callDeleteAddressWS(model);
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

    private BottomAlertDialog addAddressDialog;
    private TextView edtAddress;
    private EditText edtAddressInitials;
    private LinearLayout ln_pick_your_location;
    private LinearLayout ln_address_row;
    private Button btnAdd;
    private boolean isAddressNameVerified = false;
    private boolean isAddressPickYouLocationVerified = false;

    private void showAddAddressDialog(final AddressModel addressModel) {
        if (addressModel == null) {
            isAddressPickYouLocationVerified = false;
            isAddressNameVerified = false;
        } else {
            isAddressPickYouLocationVerified = true;
            isAddressNameVerified = true;
        }
        View view = View.inflate(mContext, R.layout.dialog_add_address, null);
        final RadioButton radioHome = view.findViewById(R.id.radio_home);
        final RadioButton radio_office = view.findViewById(R.id.radio_office);
        final RadioButton radioOther = view.findViewById(R.id.radio_other);
//        final EditText edtName = (EditText) view.findViewById(R.id.edit_name);
        edtAddress = view.findViewById(R.id.edit_address);
        edtAddressInitials = view.findViewById(R.id.edit_address_initials);
        ln_pick_your_location = view.findViewById(R.id.ln_pick_your_location);
        ln_address_row = view.findViewById(R.id.ln_address_row);


        edtAddressInitials.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!edtAddressInitials.getText().toString().trim().isEmpty()) {
                    checkAddAddressVerified();
                } else {
                    checkAddAddressVerified();
                }
            }
        });

        ln_pick_your_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlacePickerDialog(false);
            }
        });

        edtAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlacePickerDialog(false);
            }
        });

        if (addressModel != null) {
            ln_address_row.setVisibility(View.VISIBLE);
            ln_pick_your_location.setVisibility(View.GONE);
        } else {
            ln_address_row.setVisibility(View.GONE);
            ln_pick_your_location.setVisibility(View.VISIBLE);
        }
        btnAdd = view.findViewById(R.id.btn_add);

        if (addressModel != null) {
            if (NetworkUtility.TAGS.ADDRESS_TYPE.HOME.equalsIgnoreCase(addressModel.category)) {
                radioHome.setChecked(true);
//                radioHome.setSelected(true);
            } else if (NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE.equalsIgnoreCase(addressModel.category)) {
                radio_office.setChecked(true);
//                radioOther.setSelected(true);
            } else {
                radioOther.setChecked(true);
            }
            edtAddress.setTag(addressModel.getLatLng());
//            edtName.setText(addressModel.name);
            edtAddress.setText(addressModel.address);
            edtAddressInitials.setText(addressModel.address_initials);
            btnAdd.setText(getString(R.string.label_update));

            // Initialize the verification tags accordingly.
           /* isAddressNameVerified = true;
            isAddressPickYouLocationVerified = addressModel.address_initials.trim().length() > 0;*/
            checkAddAddressVerified();

        } else {
            btnAdd.setText(getString(R.string.label_add));
            radioHome.setChecked(true);
           /* edtAddress.setFocusable(false);
            edtAddress.setFocusableInTouchMode(false);*/
        }

        addAddressDialog = new BottomAlertDialog(mContext);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               /* if (TextUtils.isEmpty(edtName.getText().toString().trim())) {
                    Utility.showToast(mContext, getString(R.string.validate_address_nickname));
                } else*/
                if (TextUtils.isEmpty(edtAddress.getText().toString().trim())) {
                    Utility.showToast(mContext, getString(R.string.validate_address));
                } /*else if (TextUtils.isEmpty(edtAddressInitials.getText().toString().trim())) {
                    Utility.showToast(mContext, getString(R.string.validate_address_initials));
                } */ else {
                    if (addressModel != null) {
                        callUpdateAddressWS(addressModel.address_id,
                                (radioHome.isChecked()
                                        ? NetworkUtility.TAGS.ADDRESS_TYPE.HOME
                                        : radio_office.isChecked() ? NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE : NetworkUtility.TAGS.ADDRESS_TYPE.OTHERS)
                                /*, edtName.getText().toString().trim()*/
                                , edtAddress.getText().toString().trim()
                                , edtAddressInitials.getText().toString().trim()
                                , (LatLng) edtAddress.getTag());
                    } else {
                        callAddAddressWS(
                                (radioHome.isChecked()
                                        ? NetworkUtility.TAGS.ADDRESS_TYPE.HOME
                                        : radio_office.isChecked() ? NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE : NetworkUtility.TAGS.ADDRESS_TYPE.OTHERS)
                                /*, edtName.getText().toString().trim()*/
                                , edtAddress.getText().toString().trim()
                                , edtAddressInitials.getText().toString().trim()
                                , (LatLng) edtAddress.getTag());
                    }
                }
            }
        });
        if (addressModel == null) {
            addAddressDialog.setTitle(getString(R.string.label_add_address));
        } else {
            addAddressDialog.setTitle(getString(R.string.label_update_address));
        }
        addAddressDialog.setCustomView(view);
        addAddressDialog.showDialog();

        checkAddAddressVerified();
    }

    private void checkAddAddressVerified() {
        if (isAddressPickYouLocationVerified
                && isAddressNameVerified) {
            btnAdd.setBackgroundColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
        } else {
            btnAdd.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_varient_14));
        }
    }


    public void showPlacePickerDialog(boolean isForceShow) {

        /*if (!isForceShow) {
            if (mStrategicPartnerTaskCreationAct.mLocationTrackService != null) {
                mStrategicPartnerTaskCreationAct.mLocationTrackService.requestLocationUpdate();
                return;
            }
        }*/
        try {
            Utility.hideKeyboard(mContext);
            showProgressDialog();
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(mStrategicPartnerTaskCreationAct);
            startActivityForResult(intent, Utility.PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {

            //TODO: Adding dummy place when play service is not there
            if (edtAddress != null) {
                edtAddress.setText(getString(R.string.label_dummy_address , Utility.STATIC_LAT,Utility.STATIC_LNG));
                edtAddress.setFocusable(true);
                edtAddress.setFocusableInTouchMode(true);
                try {
                    edtAddress.setTag(new LatLng(Double.parseDouble(Utility.STATIC_LAT), Double.parseDouble(Utility.STATIC_LNG)));
                } catch (Exception exe) {
                    exe.printStackTrace();
                    edtAddress.setTag(new LatLng(0, 0));
                }
            }

            e.printStackTrace();
            Utility.showToast(mContext, getString(R.string.label_playservice_not_available));
        }
    }

    /**
     * Calling delete address Web service
     *
     * @param addressModel selected addressModel
     */
    private void callDeleteAddressWS(AddressModel addressModel) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
            if (addressRecyclerViewAdapter != null) {
                addressRecyclerViewAdapter.delete(addressModel);
                // Saving information in sharedpreference
                guestUserDetails.addressList = addressRecyclerViewAdapter.getmList();
                PreferenceUtility.getInstance(mContext).saveGuestUserDetails(guestUserDetails);

                // hide That's me button when list is empty
                if (addressRecyclerViewAdapter.getItemCount() == 0)
                    addressDialog.view.findViewById(R.id.btn_submit).setVisibility(View.GONE);

                // if selected adress is deleted then reset select an address field  again
                if (mStrategicPartnerTaskCreationAct.mSelectedAddressModel != null && addressModel.address_id.equalsIgnoreCase(mStrategicPartnerTaskCreationAct.mSelectedAddressModel.address_id)) {
                    mStrategicPartnerTaskCreationAct.mSelectedAddressModel = null;
                    ((TextView) mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag(Utility.TEMPLATE_LOCATION)).setText(getString(R.string.label_select_an_address));
                    for (QueAnsModel queAnsModel : mList) {
                        if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_LOCATION)) {
                            queAnsModel.answer = "";
                            break;
                        }
                    }
                    validateAllQueAndAns();
                }

            }

            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, String.valueOf(addressModel.address_id));

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.DELETE_ADDRESS
                , mCallDeleteAddressWSErrorListener
                , mCallDeleteAddressResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.DELETE_ADDRESS);
    }

    private Response.Listener mCallDeleteAddressResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;

                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        if (addressRecyclerViewAdapter != null) {
                            addressRecyclerViewAdapter.delete(TEMP_ADDRESS_ID);

                            //Saving information in shared preference
                            UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                            userDetails.addressList = addressRecyclerViewAdapter.getmList();
                            PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);
                            if (addressRecyclerViewAdapter.getItemCount() == 0)
                                addressDialog.view.findViewById(R.id.btn_submit).setVisibility(View.GONE);

                            if (mStrategicPartnerTaskCreationAct.mSelectedAddressModel != null && TEMP_ADDRESS_ID.equalsIgnoreCase(mStrategicPartnerTaskCreationAct.mSelectedAddressModel.address_id)) {
                                mStrategicPartnerTaskCreationAct.mSelectedAddressModel = null;
                                ((TextView) mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag(Utility.TEMPLATE_LOCATION)).setText(getString(R.string.label_select_an_address));
                                for (QueAnsModel queAnsModel : mList) {
                                    if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_LOCATION)) {
                                        queAnsModel.answer = "";
                                        break;
                                    }
                                }
                                validateAllQueAndAns();
                            }

                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        mStrategicPartnerTaskCreationAct.finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallDeleteAddressWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    private Response.ErrorListener mCallDeleteAddressWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
        }
    };

    /**
     * Calling Update Address WS
     *
     * @param addressType home/office/other
     * @param address     address string
     */
    private void callUpdateAddressWS(String addressId, String
            addressType,/* String addressName,*/ String address, String addressInitials, LatLng latLng) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
            if (addressRecyclerViewAdapter != null) {
                AddressModel mAddressModel = addressRecyclerViewAdapter.getSelectedAddress();
                mAddressModel.address_id = addressId;
                mAddressModel.category = addressType;
                mAddressModel.address = address;
                mAddressModel.address_initials = addressInitials;
                mAddressModel.lat = String.valueOf(latLng.latitude);
                mAddressModel.lng = String.valueOf(latLng.longitude);
                addressRecyclerViewAdapter.updateItem(mAddressModel);
                // Saving information in sharedpreference
                guestUserDetails.addressList = addressRecyclerViewAdapter.getmList();
            }
            PreferenceUtility.getInstance(mContext).saveGuestUserDetails(guestUserDetails);

            if (addAddressDialog != null) {
                addAddressDialog.dismiss();
            }
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.CATEGORY, addressType);
//        mParams.put(NetworkUtility.TAGS.NAME, addressName);
        mParams.put(NetworkUtility.TAGS.ADDRESS, address);
        mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, addressInitials);

        if (latLng != null) {
            mParams.put(NetworkUtility.TAGS.LAT, latLng.latitude + "");
            mParams.put(NetworkUtility.TAGS.LNG, latLng.longitude + "");
        }


        //if address id is greater then 0 then it means we need to update the existing address so sending addressId as parameter also
        if (!"0".equalsIgnoreCase(addressId)) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, String.valueOf(addressId));
        }

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest((!"0".equalsIgnoreCase(addressId) ? NetworkUtility.WS.EDIT_ADDRESS : NetworkUtility.WS.ADD_ADDRESS)
                , mCallUpdateAddressWSErrorListener
                , mCallUpdateAddressResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, (!"0".equalsIgnoreCase(addressId) ? NetworkUtility.WS.EDIT_ADDRESS : NetworkUtility.WS.ADD_ADDRESS));
    }


    private Response.Listener mCallUpdateAddressResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                if (addAddressDialog != null) {
                    addAddressDialog.dismiss();
                }
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        AddressModel addressModel = (AddressModel) Utility.getObjectFromJsonString(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).toString(), AddressModel.class);

                        if (!TextUtils.isEmpty(TEMP_ADDRESS_ID)) {
                            if (addressRecyclerViewAdapter != null) {
                                addressRecyclerViewAdapter.updateItem(addressModel);
                            }
                        }

                        //Saving information in shared preference
                        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                        userDetails.addressList = addressRecyclerViewAdapter.getmList();
                        PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);


//                        String message = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.OTP_CODE);
//                        VerificationActivity.newInstance(mContext, PreferenceUtility.getInstance(mContext).getUserDetails(), TEMP_PHONE_NUMBER, message);

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        mStrategicPartnerTaskCreationAct.finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallUpdateAddressWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    private Response.ErrorListener mCallUpdateAddressWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
        }
    };


    /**
     * Calling Add Address WS
     *
     * @param addressType home/office/other
     * @param address     address string
     */
    private void callAddAddressWS(final String addressType, /*String addressName,*/ final String
            address, final String addressInitials, final LatLng latLng) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            showProgressDialog();
            FetchLocationInfoUtility mFetchLocationInfoUtility = new FetchLocationInfoUtility(
                    mContext,
                    new FetchLocationInfoUtility.FetchLocationInfoCallBack() {
                        @Override
                        public void onLocationInfoAvailable(LocationInfo mLocationIno) {
                            hideProgressDialog();

                            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();

                            /**
                             * In case og Guest User we want to save it locally.
                             */
                            AddressModel addressModel = new AddressModel();
                            // Creating Dynamic AddressID but it would be nagative values always to differentiate with logged in users address.
                            addressModel.address_id = "-" + (guestUserDetails.addressList == null ? "1" : String.valueOf(guestUserDetails.addressList.size() + 1));
                            addressModel.address = address;
                            addressModel.cityName = mLocationIno.City;
                            addressModel.countryName = mLocationIno.Country;
                            addressModel.stateName = mLocationIno.State;
                            addressModel.address_initials = addressInitials;
                            addressModel.category = addressType;
                            addressModel.lat = String.valueOf(latLng.latitude);
                            addressModel.lng = String.valueOf(latLng.longitude);

                            if (addressRecyclerViewAdapter != null) {
                                addressRecyclerViewAdapter.add(addressModel);
                                addressDialog.view.findViewById(R.id.btn_submit).setVisibility(View.VISIBLE);
                            }

                            //Saving information in sharedpreference
                            guestUserDetails.addressList = addressRecyclerViewAdapter.getmList();
                            PreferenceUtility.getInstance(mContext).saveGuestUserDetails(guestUserDetails);

                            if (addAddressDialog != null) {
                                addAddressDialog.dismiss();
                            }
                        }
                    },
                    false
            );
            mFetchLocationInfoUtility.getLocationInfo(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.CATEGORY, addressType);
//        mParams.put(NetworkUtility.TAGS.NAME, addressName);
        mParams.put(NetworkUtility.TAGS.ADDRESS, address);
        mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, addressInitials);

        if (latLng != null) {
            mParams.put(NetworkUtility.TAGS.LAT, latLng.latitude + "");
            mParams.put(NetworkUtility.TAGS.LNG, latLng.longitude + "");
        }
        Utility.hideKeyboard(mContext);
        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.ADD_ADDRESS
                , mCallAddAddressWSErrorListener
                , mCallAddAddressResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.ADD_ADDRESS);
    }


    private Response.Listener mCallAddAddressResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;

                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        AddressModel addressModel = (AddressModel) Utility.getObjectFromJsonString(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).toString(), AddressModel.class);

                        if (addressRecyclerViewAdapter != null) {
                            addressRecyclerViewAdapter.add(addressModel);
                        }

                        //Saving information in shared preference
                        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                        userDetails.addressList = addressRecyclerViewAdapter.getmList();
                        PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);

                        if (addAddressDialog != null) {
                            addAddressDialog.dismiss();
                            addressDialog.view.findViewById(R.id.btn_submit).setVisibility(View.VISIBLE);
                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mActivityHireNewJobBinding.getRoot());
                        Utility.showToast(mContext, error_message);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        mStrategicPartnerTaskCreationAct.finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallAddAddressWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    private Response.ErrorListener mCallAddAddressWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
        }
    };

    /////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// LOCATION [END]//////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // place picker result
        if (requestCode == Utility.PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                isAddressPickYouLocationVerified = true;
                isAddressNameVerified = true;
                final Place place = PlacePicker.getPlace(mContext, data);
                final CharSequence address = place.getAddress();
                ln_pick_your_location.setVisibility(View.GONE);
                ln_address_row.setVisibility(View.VISIBLE);
                if (edtAddress != null) {
                    edtAddress.setText(address);
                   /* edtAddress.setFocusable(true);
                    edtAddress.setFocusableInTouchMode(true);*/
                    edtAddress.setTag(place.getLatLng());
                }
            } else {
                if (TextUtils.isEmpty(edtAddress.getText().toString().trim())) {
                    isAddressPickYouLocationVerified = false;
                } else {
                    isAddressPickYouLocationVerified = true;
                    isAddressNameVerified = true;
                }
            }
            checkAddAddressVerified();
            hideProgressDialog();
        }


        // image capture from camera result
        else if (requestCode == Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "onActivityResult: CurrentPath" + mCurrentPhotoPath);
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mCurrentPhotoPath = Utility.getPath(mStrategicPartnerTaskCreationAct, contentUri);
            uploadFile(mCurrentPhotoPath, MediaModel.MediaType.TYPE_IMAGE);
//            mMediaRecycleAdapter.addImage(new MediaModel(mCurrentPhotoPath, MediaModel.MediaType.TYPE_IMAGE));
//            checkMediaArraySize();
        }

        // image chosen from gallery result
        else if (requestCode == Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            Log.i(TAG, "onActivityResult: " + data.getData().toString());
            mCurrentPhotoPath = Utility.getPath(mStrategicPartnerTaskCreationAct, data.getData());
            uploadFile(mCurrentPhotoPath, MediaModel.MediaType.TYPE_IMAGE);
//            mMediaRecycleAdapter.addImage(new MediaModel(mCurrentPhotoPath, MediaModel.MediaType.TYPE_IMAGE));
//            checkMediaArraySize();
        }

        // video captured from camera result
        else if (requestCode == Utility.REQUEST_CODE_VIDEO_CAPTURE && resultCode == RESULT_OK && data != null) {
            mCurrentPhotoPath = data.getStringExtra("path");
            Log.e(TAG, "path >> " + mCurrentPhotoPath);
            uploadFile(mCurrentPhotoPath, MediaModel.MediaType.TYPE_VIDEO);
//            mMediaRecycleAdapter.addImage(new MediaModel(mCurrentPhotoPath, MediaModel.MediaType.TYPE_VIDEO));
//            checkMediaArraySize();
        }

        // video chosen from gallery result
        else if (requestCode == Utility.REQUEST_CODE_GET_VIDEO_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            mCurrentPhotoPath = Utility.getPath(mStrategicPartnerTaskCreationAct, selectedImageUri);

            if (mCurrentPhotoPath != null && !mCurrentPhotoPath.equals("")) {
                if (getDuration(mCurrentPhotoPath) > 10) {
                    Utility.showToast(mContext, getString(R.string.message_file_heavy));
                } else if (getDuration(mCurrentPhotoPath) <= 0) {
                    Utility.showToast(mContext, getString(R.string.message_file_something_wrong));
                } else {
                    try {
                        Log.e(TAG, "path >> " + mCurrentPhotoPath);
//                        mMediaRecycleAdapter.addImage(new MediaModel(mCurrentPhotoPath, MediaModel.MediaType.TYPE_VIDEO));
                        uploadFile(mCurrentPhotoPath, MediaModel.MediaType.TYPE_VIDEO);
//                        checkMediaArraySize();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            }

        }
    }

    private long getDuration(String selectedImagePath) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        use one of overloaded setDataSource() functions to set your data source
            retriever.setDataSource(selectedImagePath);

            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInSec = 0;
            try {
                timeInSec = Long.parseLong(time) / 1000;
            } catch (NumberFormatException e) {
                timeInSec = 0;
            }
            retriever.release();
            return timeInSec;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * check if 3 image/video is added then hide image add view
     * to preventing from adding more media files
     */

    private void checkMediaArraySize() {
        if (mMediaRecycleAdapter.getItemCount() > 0)
            mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag(Utility.TEMPLATE_UPLOAD).setSelected(true);
        if (mMediaRecycleAdapter.getItemCount() == 3) {
            ImageView imageView = mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag("AddImage");
            if (imageView != null)
                imageView.setVisibility(View.GONE);
        }
    }


    private void resetTime(TextView txtAnswer, QueAnsModel model, TextView txtQueNo) {
        SuperCalendar calAfter3Hours = SuperCalendar.getInstance().getNext3HoursTime();

    }


    /**
     * @return true if all questions are answered
     * or false is something is missing
     */
    private String validateAllQueAndAns() {
        String message;
        // Get date-time for next 3 hours
        SuperCalendar calAfter3Hours = SuperCalendar.getInstance().getNext3HoursTime();
        for (int i = 0; i < mList.size(); i++) {
            QueAnsModel queAnsModel = mList.get(i);
            // alert date is not selected
            if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_DATE_PICKER)) {
                if (TextUtils.isEmpty(queAnsModel.answer)) {
                    message = getString(R.string.alert_select_date_and_time);
                    return message;
                }
//                else if (startDateTimeSuperCalendar.getTimeInMillis() < calAfter3Hours.getTimeInMillis()) {
//                    message = "Date Time must be after 3 hour.";
//                    return message;
//                }

            }

            // alert time is not selected
            else if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_TIME_PICKER)) {
                if (TextUtils.isEmpty(queAnsModel.answer)) {
                    message = getString(R.string.alert_select_the_time);
                    return message;
                } else if (startDateTimeSuperCalendar.getTimeInMillis() < calAfter3Hours.getTimeInMillis()) {
                    message = getString(R.string.alert_time_must_be_after_3_hour);
                    return message;
                }
            }
            // alert multiple choices option is not selected
            else if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_DROPDOWN)) {
                if (TextUtils.isEmpty(queAnsModel.answer)) {
                    message = getString(R.string.alert_please_answer_all_the_questions);
                    return message;

                }
            }
            // location is not selected
            else if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_LOCATION)
                    && mStrategicPartnerTaskCreationAct.mSelectedAddressModel == null) {
                message = getString(R.string.alert_select_an_address);
                return message;

            }
        }
        // all necessary questions are answered
        return "";
    }

    private void setBtnBookAndPayBgState(boolean isEnable) {
        mFragmentStrategicPartnerPhaseTwoBinding.textContinue.
                setBackgroundColor(ContextCompat.getColor(mStrategicPartnerTaskCreationAct, isEnable ? R.color.dark_blue_variant_1 : R.color.grey_varient_12));
    }


    ///////////////////////// ********* Amazon code start here*********** //////////////////////////////////

    private void uploadFile(final String path, final String type) {
        if (path == null || path.equalsIgnoreCase("")) {
            return;
        }

        // async task for uploading file on amazon

//        new AsyncTask<Void, Void, Void>() {

        // thumb folder path for s3 amazon
        String s3PathThumb;
        // original file folder path for s3 amazon
        String s3pathOriginal;

        // to show thumbnail in recycler view in after uploading file on s3
        String localFilePath;

        showProgressDialog();

        File fileOriginal = new File(path);

        String thumbPath = "";
        // create thumbnail for uploading
        if (type.equalsIgnoreCase(MediaModel.MediaType.TYPE_VIDEO))
            thumbPath = AmazonUtils.getVideoThumbPath(mStrategicPartnerTaskCreationAct, path);
        else
            thumbPath = AmazonUtils.getImageThumbPath(mStrategicPartnerTaskCreationAct, path);
        final File fileThumb = new File(thumbPath);

//                this name is for creating s3 url for original and file file
        String name;
        String thumbName;
        String timeStamp = System.currentTimeMillis() + "";
        if (type.equalsIgnoreCase(MediaModel.MediaType.TYPE_IMAGE)) {
            name = "AND_IMG_" + timeStamp + AmazonUtils.getExtension(path);
            thumbName = "AND_IMG_" + timeStamp + ".jpg";
        } else {
            name = "AND_VID_" + timeStamp + AmazonUtils.getExtension(path);
            thumbName = "AND_VID_" + timeStamp + ".jpg";
        }

        localFilePath = thumbPath;
        s3pathOriginal = BuildConfig.TASK_ORIGINAL_FOLDER + File.separator + name;
        s3PathThumb = BuildConfig.TASK_THUMB_FOLDER + File.separator + thumbName;
        UploadListener listener = new UploadListener(s3PathThumb, s3pathOriginal, type, localFilePath);
        TransferObserver observer;
        TransferObserver observer1;
        observer = AmazonUtils.uploadMedia(mStrategicPartnerTaskCreationAct, fileOriginal, s3pathOriginal, listener);
        observer1 = AmazonUtils.uploadMedia(mStrategicPartnerTaskCreationAct, fileThumb, s3PathThumb, listener);
        listener.observer = observer;
        listener.observer1 = observer1;

    }

    private class UploadListener implements TransferListener {
        String s3PathThumb, s3pathOriginal, type, localFilePath;

        UploadListener(String s3PathThumb, String s3pathOriginal, String type, String localFilePath) {
            this.s3PathThumb = s3PathThumb;
            this.s3pathOriginal = s3pathOriginal;
            this.type = type;
            this.localFilePath = localFilePath;
        }

        // Keep tracks for media uploading
        TransferObserver observer;
        TransferObserver observer1;


        @Override
        public void onError(int id, Exception e) {
            Log.e(TAG, "Error during upload: " + id, e);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d(TAG, "onStateChanged: " + id + ", " + newState);
            Log.d(TAG, "observer: " + observer.getId() + ", " + observer.getState());
            Log.d(TAG, "observer1: " + observer1.getId() + ", " + observer.getState());
            if (observer != null && observer1 != null) {
                if (observer.getState() == TransferState.COMPLETED && observer1.getState() == TransferState.COMPLETED) {
                    // get s3 urls
                    String thumbUrl = AmazonUtils.getThumbURL(s3PathThumb);
                    String originalUrl = AmazonUtils.getOriginalURL(s3pathOriginal);

                    // add image/video model to recycle view
                    MediaModel mediaModel = new MediaModel();
                    mediaModel.mediaName = originalUrl;
                    mediaModel.mediaThumbName = thumbUrl;
                    Log.e(TAG, "mediaName: " + originalUrl);
                    Log.e(TAG, "mediaThumbName: " + thumbUrl);
                    mediaModel.mediaType = type;
                    mediaModel.localFilePath = localFilePath;
                    mMediaRecycleAdapter.addImage(mediaModel);

                    // set update media list for strategic partner activity
                    // for when  user is not creating tasks but he press back buttons
                    // and goes to home screen that time all uploaded media will be deleted.

                    for (QueAnsModel model : mList)
                        if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_UPLOAD)) {
                            model.medialList = mMediaRecycleAdapter.getList();
                            break;
                        }

                    checkMediaArraySize();
                    // close progress
                    hideProgressDialog();
                }
            }
        }
    }


    ///////////////////////// ********* Amazon code ends here*********** //////////////////////////////////
}