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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BootstrapConstant;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.adapter.AddressRecyclerViewAdapter;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.FragmentStrategicPartnerPhaseTwoBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.ProviderModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.strategicpartner.recordvideo.RecordVideoNewActivity;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.cheep.network.NetworkUtility.TAGS.CAT_ID;

/**
 * Created by bhavesh on 28/4/17.
 */

public class StrategicPartnerFragPhaseTwo extends BaseFragment {
    public static final String TAG = "StrategicPartnerFragPhaseThree";
    private FragmentStrategicPartnerPhaseTwoBinding mFragmentStrategicPartnerPhaseTwoBinding;
    private StrategicPartnerTaskCreationAct mStrategicPartnerTaskCreationAct;
    private boolean isVerified = false;
    private boolean isTaskDescriptionVerified;
    SuperCalendar startDateTimeSuperCalendar = SuperCalendar.getInstance();
    public SuperCalendar superCalendar;

    private String mCurrentPhotoPath = "";
    private ImageAdapter imageAdapter;
    int count = 1;
    private ArrayList<QueAnsModel> mList;

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

        // Task Description
        isTaskDescriptionVerified = mStrategicPartnerTaskCreationAct.getSelectedSubService().size() != 0;

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void initiateUI() {
        Log.d(TAG, "initiateUI() called");
        mFragmentStrategicPartnerPhaseTwoBinding.textContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (doValidation()) {
                    mStrategicPartnerTaskCreationAct.setSelectedQuestions(mList);
                    mStrategicPartnerTaskCreationAct.isAllQuestionAnswer = true;
                    Log.d(TAG, "onSubCategoryRowItemClicked() called with: subServiceDetailModel = [" + "]");
                    // Make the status Verified
                    isVerified = true;

                    //Alert The activity that step one is been varified.
                    mStrategicPartnerTaskCreationAct.setTaskState(StrategicPartnerTaskCreationAct.STEP_TWO_VERIFIED);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mStrategicPartnerTaskCreationAct.gotoStep(StrategicPartnerTaskCreationAct.STAGE_3);
                        }
                    }, 500);
                } else {
                    mStrategicPartnerTaskCreationAct.isAllQuestionAnswer = false;

                }
            }
        });

        fetchListOfSubCategory(mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id);

    }

    private void fetchListOfSubCategory(String catId) {
        Log.d(TAG, "fetchListOfSubCategory() called with: catId = [" + catId + "]");
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(CAT_ID, catId);

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.FETCH_SUB_CATEGORIES_QUESTIONNAIRE
                , mCallFetchAllSubCateStreParListingWSErrorListener
                , mCallFetchAllSubCateStreParListingWSResponseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.FETCH_SUB_CATEGORIES_QUESTIONNAIRE);
    }

    Response.Listener mCallFetchAllSubCateStreParListingWSResponseListener = new Response.Listener() {
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
                mCallFetchAllSubCateStreParListingWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };


    Response.ErrorListener mCallFetchAllSubCateStreParListingWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
        }
    };

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
    }

    public boolean isVerified() {
        return isVerified;
    }


    @Override
    public Object getSharedElementEnterTransition() {
        return super.getSharedElementEnterTransition();
    }

    private void inflateUI(String response) {
        // top layout
        // get question answer list
        mList = Utility.getObjectListFromJsonString(response, QueAnsModel[].class);        // load list
        for (QueAnsModel model : mList) {
            if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_UPLOAD)) {
                inflateAnsUploadTemplate(model);
            } else if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_DATE_PICKER)) {
                inflateAnsDatePickerTemplate(model);
            } else if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_TIME_PICKER)) {
                inflateAnsTimePickerTemplate(model);
            } else if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_LOCATION)) {
                inflateAnsLocationPickerTemplate(model);
            } else if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_TEXT_FIELD)) {
                inflateAnsTextTemplate(model);
            } else if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_DROPDOWN)) {
                inflateAnsDropDownTemplate(model);
            }
            count++;
        }
    }

    private void inflateAnsDropDownTemplate(final QueAnsModel model) {
        ViewGroup queView = (ViewGroup) LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_question, null, false);
        final ViewGroup ansView = queView.findViewById(R.id.relAnsView);
        TextView txtQueNo = queView.findViewById(R.id.txtQueNo);
        txtQueNo.setTag(model.questionId);
        TextView txtQueStr = queView.findViewById(R.id.txtQueStr);
        txtQueNo.setText(String.valueOf(count));
        txtQueStr.setText(String.valueOf(model.question));

        // add question view on top view
        mFragmentStrategicPartnerPhaseTwoBinding.linMain.addView(queView);

        final View viewInflated = LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_answer_multiple_choice, null, false);
        final TextView txtAnswer = viewInflated.findViewById(R.id.txtQueAnswer);
        txtAnswer.setText("Select an answer");
        txtAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFilterWindow(txtAnswer, model);
            }
        });
        ansView.addView(viewInflated);
    }

    private void inflateAnsLocationPickerTemplate(final QueAnsModel model) {
        ViewGroup queView = (ViewGroup) LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_question, null, false);
        ViewGroup ansView = queView.findViewById(R.id.relAnsView);
        final TextView txtQueNo = queView.findViewById(R.id.txtQueNo);
        TextView txtQueStr = queView.findViewById(R.id.txtQueStr);
        txtQueNo.setText(String.valueOf(count));
        txtQueStr.setText(String.valueOf(model.question));

        // add question view on top view
        mFragmentStrategicPartnerPhaseTwoBinding.linMain.addView(queView);

        View view = LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_place_picker, null, false);
        final TextView txtAnswer = view.findViewById(R.id.txtQueAnswer);
        txtAnswer.setText(getString(R.string.label_select_an_address));
        txtAnswer.setTag(Utility.TEMPLATE_LOCATION);
        txtAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressDialog(txtAnswer, txtQueNo, model);
            }
        });
        ansView.addView(view);
    }

    private void inflateAnsTimePickerTemplate(final QueAnsModel model) {
        ViewGroup queView = (ViewGroup) LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_question, null, false);
        ViewGroup ansView = queView.findViewById(R.id.relAnsView);
        TextView txtQueNo = queView.findViewById(R.id.txtQueNo);
        TextView txtQueStr = queView.findViewById(R.id.txtQueStr);
        txtQueNo.setText(String.valueOf(count));
        txtQueNo.setTag(model.questionId);
        txtQueStr.setText(String.valueOf(model.question));

        // add question view on top view
        mFragmentStrategicPartnerPhaseTwoBinding.linMain.addView(queView);


        View view = LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_time_picker, null, false);
        final TextView txtAnswer = view.findViewById(R.id.txtQueAnswer);
        txtAnswer.setText(getString(R.string.label_select_the_time));
        txtAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(txtAnswer, model);
            }
        });
        ansView.addView(view);
    }

    private void inflateAnsTextTemplate(final QueAnsModel model) {
        final ViewGroup queView = (ViewGroup) LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_question, null, false);
        ViewGroup ansView = queView.findViewById(R.id.relAnsView);
        final TextView txtQueNo = queView.findViewById(R.id.txtQueNo);
        TextView txtQueStr = queView.findViewById(R.id.txtQueStr);
        txtQueNo.setText(String.valueOf(count));
        txtQueStr.setText(String.valueOf(model.question));
        // add question view on top view
        mFragmentStrategicPartnerPhaseTwoBinding.linMain.addView(queView);
        View view = LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_answer_edit_text, null, false);
        final EditText edtQueAnswer = view.findViewById(R.id.edtQueAnswer);
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
                txtQueNo.setSelected(model.answer != null && model.answer.trim().length() > 0);
            }
        });
        ansView.addView(view);
    }

    private void inflateAnsDatePickerTemplate(final QueAnsModel model) {
// inflate question view
        ViewGroup queView = (ViewGroup) LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_question, null, false);
        ViewGroup ansView = queView.findViewById(R.id.relAnsView);
        final TextView txtQueNo = queView.findViewById(R.id.txtQueNo);
        TextView txtQueStr = queView.findViewById(R.id.txtQueStr);
        txtQueNo.setText(String.valueOf(count));
        txtQueStr.setText(String.valueOf(model.question));

        // add question view on top view
        mFragmentStrategicPartnerPhaseTwoBinding.linMain.addView(queView);

        View view = LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_date_picker, null, false);
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
                            startDateTimeSuperCalendar.set(Calendar.YEAR, year);
                            startDateTimeSuperCalendar.set(Calendar.MONTH, monthOfYear);
                            startDateTimeSuperCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            txtAnswer.setText(startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM));
                            model.answer = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM);
                            txtQueNo.setSelected(true);
                            txtAnswer.setSelected(false);

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

                            superCalendar = SuperCalendar.getInstance();
                            superCalendar.setTimeInMillis(startDateTimeSuperCalendar.getTimeInMillis());

                            superCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            superCalendar.set(Calendar.MINUTE, minute);

                            superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);


                            // Get date-time for next 3 hours
                            SuperCalendar calAfter3Hours = SuperCalendar.getInstance().getNext3HoursTime();

//                            TODO: mStrategicPartnerTaskCreationAct needs to Be UNCOMMENTED DO NOT FORGET
//                            if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase(Utility.DEBUG)) {
                            if (superCalendar.getTimeInMillis() < calAfter3Hours.getTimeInMillis()) {
                                Utility.showSnackBar("Date Time must be after 3 hour.", mFragmentStrategicPartnerPhaseTwoBinding.linMain);
                                return;
                            }
//                            }

                            if (System.currentTimeMillis() < superCalendar.getTimeInMillis()) {
                                String selectedDateTime = superCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
                                textView.setText(selectedDateTime);
                                textView.setSelected(true);
                                model.answer = String.valueOf(superCalendar.getTimeInMillis());
                                mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag(model.questionId).setSelected(true);
                            } else {
                                textView.setText(getString(R.string.label_select_the_time));
                                textView.setSelected(false);
                                Utility.showSnackBar("Please enter future  time.", mFragmentStrategicPartnerPhaseTwoBinding.linMain);
                            }
                        }
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
        timePickerDialog.show();

    }

    private void inflateAnsUploadTemplate(QueAnsModel model) {

        ViewGroup queView = (ViewGroup) LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_question, null, false);
        ViewGroup ansView = queView.findViewById(R.id.relAnsView);
        TextView txtQueNo = queView.findViewById(R.id.txtQueNo);
        txtQueNo.setTag(model.answerType);
        TextView txtQueStr = queView.findViewById(R.id.txtQueStr);
        txtQueNo.setText(String.valueOf(count));
        txtQueStr.setText(String.valueOf(model.question));

        // add question view on top view
        mFragmentStrategicPartnerPhaseTwoBinding.linMain.addView(queView);


        txtQueStr.setText(String.valueOf(model.question));

        View view = LayoutInflater.from(mStrategicPartnerTaskCreationAct).inflate(R.layout.layout_template_answer_upload, null, false);
        RecyclerView recycleImg = view.findViewById(R.id.recycleImg);
        recycleImg.setNestedScrollingEnabled(false);
        recycleImg.setLayoutManager(new LinearLayoutManager(mStrategicPartnerTaskCreationAct, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter();
        recycleImg.setAdapter(imageAdapter);
        final ImageView imgAdd = view.findViewById(R.id.imgAdd);
        imgAdd.setTag("AddImage");
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageAdapter.getItemCount() < 3)
                    showMediaChooserDialog();
                else
                    Utility.showSnackBar("you can upload only 3 files", mFragmentStrategicPartnerPhaseTwoBinding.linMain);
            }
        });

        ansView.addView(view);

    }

    private void showVideoChooserDialog() {
        Log.d(TAG, "showPictureChooserDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(mStrategicPartnerTaskCreationAct);
        builder.setTitle("Choose video")
                .setItems(R.array.choose_video_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0) {
                            takeVideoIntent(Utility.REQUEST_CODE_VIDEO_CAPTURE, Utility.REQUEST_CODE_WRITE_EXTERNAL_STORAGE_ADD_PROFILE_CAMERA);
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

    private void takeVideoIntent(int requestCode, int requestPermissionCode) {
        //Go ahead with Camera capturing
        if (ContextCompat.checkSelfPermission(mStrategicPartnerTaskCreationAct, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(mStrategicPartnerTaskCreationAct
                , Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mStrategicPartnerTaskCreationAct, Manifest.permission.CAMERA) && ActivityCompat.shouldShowRequestPermissionRationale(mStrategicPartnerTaskCreationAct, Manifest.permission.RECORD_AUDIO)) {
                ActivityCompat.requestPermissions(mStrategicPartnerTaskCreationAct, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, requestPermissionCode);
            } else {
                ActivityCompat.requestPermissions(mStrategicPartnerTaskCreationAct, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, requestPermissionCode);
            }
        } else {
            //Go ahead with Camera capturing
            startActivityForResult(new Intent(mStrategicPartnerTaskCreationAct, RecordVideoNewActivity.class), requestCode);

        }
    }

    public void chooseVideoFromGallery(int requestFileChooserCode, int requestPermissionCode) {
        Log.d(TAG, "choosePictureFromGallery() called with: requestFileChooserCode = [" + requestFileChooserCode + "], requestPermissionCode = [" + requestPermissionCode + "]");
        if (ContextCompat.checkSelfPermission(mStrategicPartnerTaskCreationAct, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mStrategicPartnerTaskCreationAct, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(mStrategicPartnerTaskCreationAct, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            } else {
                ActivityCompat.requestPermissions(mStrategicPartnerTaskCreationAct, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestPermissionCode);
            }
        } else {
            //Go ahead with file choosing
            startIntentVideoFileChooser(requestFileChooserCode);
        }
    }

    public void startIntentVideoFileChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(mStrategicPartnerTaskCreationAct.getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////    IMAGE CAPTURE    /////////////////////
    ////////////////////////////////////////////////////////////////////////////////////

    private void showMediaChooserDialog() {
        Log.d(TAG, "showPictureChooserDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(mStrategicPartnerTaskCreationAct);
        builder.setTitle("Choose image")
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


    private void showPictureChooserDialog() {
        Log.d(TAG, "showPictureChooserDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(mStrategicPartnerTaskCreationAct);
        builder.setTitle("Choose image")
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

    public void startCameraCaptureChooser(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(mStrategicPartnerTaskCreationAct.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                mCurrentPhotoPath = photoFile.getAbsolutePath();
                if (photoFile.exists()) {
                    photoFile.delete();
                } else {
                    photoFile.getParentFile().mkdirs();
                }

            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(mStrategicPartnerTaskCreationAct,
                        BuildConfig.FILE_PROVIDER_URL,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Grant URI permission START
                // Enableing the permission at runtime
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
    }

    public File createImageFile() throws IOException {
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


    //// Gallery /////
    public void choosePictureFromGallery(int requestFileChooserCode, int requestPermissionCode) {
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

    public void startIntentFileChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(mStrategicPartnerTaskCreationAct.getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////// -- Menu multiple choice--- ////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void showFilterWindow(final TextView textView, final QueAnsModel model) {
        Log.i(TAG, "showFilterWindow: ");
        final View mFilterPopupWindow = View.inflate(mStrategicPartnerTaskCreationAct, R.layout.layout_template_drop_down_list, null);

        final PopupWindow mPopupWindow = new PopupWindow(mStrategicPartnerTaskCreationAct);
        ListView listView = mFilterPopupWindow.findViewById(R.id.listMultipleChoice);

        final DropDownAdapter dropDownAdapter = new DropDownAdapter(mStrategicPartnerTaskCreationAct, model.dropDownList);
        listView.setAdapter(dropDownAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
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
        });

        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setContentView(mFilterPopupWindow);
        mPopupWindow.setWidth(textView.getWidth());
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

    /////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// LOCATION //////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////
    private BottomAlertDialog addressDialog;
    private AddressRecyclerViewAdapter addressRecyclerViewAdapter;
    public String addressId = "";

    private void showAddressDialog(final TextView textAns, final TextView textQueNo, final QueAnsModel queAnsModel) {
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
                if (addressRecyclerViewAdapter != null && addressRecyclerViewAdapter.getmList().isEmpty() == false) {
                    AddressModel model = addressRecyclerViewAdapter.getSelectedAddress();
                    if (model != null) {
                        String address = "";
                        if (model.address_initials.length() > 0) {
                            address = model.address_initials + ", " + model.address;
                        } else {
                            address = model.address;
                        }
                        addressId = model.address_id;
                        Log.e(TAG, "category detail >> " + model.category + "");

                        if (model.category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.HOME))
                            textAns.setText(getString(R.string.label_home));
                        else if (model.category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE))
                            textAns.setText(getString(R.string.label_office));
                        else
                            textAns.setText(getString(R.string.label_other));
                        textAns.setSelected(true);
                        queAnsModel.answer = addressId;
                        textQueNo.setSelected(true);
                        addressDialog.dismiss();
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
        }
    }

    /**
     * Loads address in choose address dialog box in recycler view
     */
    private String TEMP_ADDRESS_ID = "";

    private boolean fillAddressRecyclerView(RecyclerView recyclerView) {
        ArrayList<AddressModel> addressList = PreferenceUtility.getInstance(mContext).getUserDetails().addressList;
        //Setting RecyclerView Adapter
        addressRecyclerViewAdapter = new AddressRecyclerViewAdapter(addressList, new AddressRecyclerViewAdapter.AddressItemInteractionListener() {
            @Override
            public void onEditClicked(AddressModel model, int position) {
                TEMP_ADDRESS_ID = model.address_id;
                showAddAddressDialog(model);
            }

            @Override
            public void onDeleteClicked(AddressModel model, int position) {
                TEMP_ADDRESS_ID = model.address_id;
                callDeleteAddressWS(model.address_id);
            }

            @Override
            public void onRowClicked(AddressModel model, int position) {

            }
        });
        addressRecyclerViewAdapter.setSelectedAddressId(addressId);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(addressRecyclerViewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_16dp)));

        //Here we are checking if address is not there then open add address dialog immediatly
        return addressList == null || (addressList != null && addressList.isEmpty());
    }

    private BottomAlertDialog addAddressDialog;
    private TextView edtAddress;
    private EditText edtAddressInitials;
    private LinearLayout ln_pick_your_location;
    private LinearLayout ln_address_row;
    private Button btnAdd;
    private boolean isAddressNameVerified = false;
    private boolean isAddressPickYouLocationVerified = false;
    private boolean isAddressFlatNoVerified = false;

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
                if (edtAddressInitials.getText().toString().trim().length() > 0) {
                    isAddressFlatNoVerified = true;
                    checkAddAddressVerified();
                } else {
                    isAddressFlatNoVerified = false;
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
       /* edtAddress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    final int DRAWABLE_LEFT = 0;
                    final int DRAWABLE_TOP = 1;
                    final int DRAWABLE_RIGHT = 2;
                    final int DRAWABLE_BOTTOM = 3;

                    if (edtAddress.getTag() != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (event.getRawX() >= (edtAddress.getRight() - edtAddress.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            // your action here
                            showPlacePickerDialog(false);
                            return true;
                        }
                    } else if (edtAddress.getTag() == null) {
                        showPlacePickerDialog(false);
                        return true;
                    }
                }
                return false;
            }
        });*/
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

            // Initiaze the varfication tags accordingly.
            isAddressFlatNoVerified = true;
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
        /*if (isAddressFlatNoVerified
                && isAddressPickYouLocationVerified
                && isAddressNameVerified) {
            btnAdd.setBackgroundColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
        } else {
            btnAdd.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_varient_14));
        }*/
        if (isAddressPickYouLocationVerified
                && isAddressNameVerified) {
            btnAdd.setBackgroundColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
        } else {
            btnAdd.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_varient_14));
        }
    }

    boolean isPlacePickerClicked = false;

    public void showPlacePickerDialog(boolean isForceShow) {

        if (!isForceShow) {
            if (mStrategicPartnerTaskCreationAct.mLocationTrackService != null) {
                isPlacePickerClicked = true;
                mStrategicPartnerTaskCreationAct.mLocationTrackService.requestLocationUpdate();
                return;
            }
            /*if (isLocationEnabled() == false) {
                if (isGPSEnabled() == false) {
                    showGPSEnableDialog();
                    return;
                }
            }*/

            /*String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (locationProviders == null || locationProviders.equals("")) {
                //show gps disabled and enable gps dialog here
                showGPSEnableDialog();
                return;
            }

            LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //show gps disabled and enable gps dialog here
                showGPSEnableDialog();
                return;
            }*/
        }

        try {
            Utility.hideKeyboard(mContext);
            showProgressDialog();
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(mStrategicPartnerTaskCreationAct);
            startActivityForResult(intent, Utility.PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {

            //TODO: Adding dummy place when playservice is not there
            if (edtAddress != null) {
                edtAddress.setText("Dummy Address with " + BootstrapConstant.LAT + "," + BootstrapConstant.LNG);
                edtAddress.setFocusable(true);
                edtAddress.setFocusableInTouchMode(true);
                try {
                    edtAddress.setTag(new LatLng(Double.parseDouble(BootstrapConstant.LAT), Double.parseDouble(BootstrapConstant.LNG)));
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
     * @param addressId
     */
    private void callDeleteAddressWS(String addressId) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
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
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, String.valueOf(addressId));

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.DELETE_ADDRESS
                , mCallDeleteAddressWSErrorListener
                , mCallDeleteAddressResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.DELETE_ADDRESS);
    }

    Response.Listener mCallDeleteAddressResponseListener = new Response.Listener() {
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

                            //Saving information in sharedpreference
                            UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                            userDetails.addressList = addressRecyclerViewAdapter.getmList();
                            PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);
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

    Response.ErrorListener mCallDeleteAddressWSErrorListener = new Response.ErrorListener() {
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
     * @param addressType
     * @param address
     */
    private void callUpdateAddressWS(String addressId, String addressType,/* String addressName,*/ String address, String addressInitials, LatLng latLng) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
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


        //if address id is greater then 0 then it means we need to update the existing address so sending address_id as parameter also
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


    Response.Listener mCallUpdateAddressResponseListener = new Response.Listener() {
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

                        //Saving information in sharedpreference
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

    Response.ErrorListener mCallUpdateAddressWSErrorListener = new Response.ErrorListener() {
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
     * @param addressType
     * @param address
     */
    private void callAddAddressWS(String addressType, /*String addressName,*/ String address, String addressInitials, LatLng latLng) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
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


    Response.Listener mCallAddAddressResponseListener = new Response.Listener() {
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

                        //Saving information in sharedpreference
                        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                        userDetails.addressList = addressRecyclerViewAdapter.getmList();
                        PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);

                        if (addAddressDialog != null) {
                            addAddressDialog.dismiss();
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

    Response.ErrorListener mCallAddAddressWSErrorListener = new Response.ErrorListener() {
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
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////WHERE Feature [START]/////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Reload SP Listing based on AddressID [START]//////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Calling Get SP list web service from server
     */
    private String mCategoryId, mCityId, mTaskId;

    private void callSPListWS(String categoryId, String cityId, String taskId) {
        this.mCategoryId = categoryId;
        this.mCityId = cityId;
        this.mTaskId = taskId;

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
            return;
        }

        //Show Progress
//        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        if (TextUtils.isEmpty(taskId)) {
            mParams.put(NetworkUtility.TAGS.CITY_ID, cityId);
            mParams.put(NetworkUtility.TAGS.CAT_ID, categoryId);
        } else {
            mParams.put(NetworkUtility.TAGS.TASK_ID, taskId);
        }

        //for pagination
        // Sending @pageNo Hard-Coded for now as it won't required here
        mParams.put(NetworkUtility.TAGS.PAGE_NUM, "0");

        if (!TextUtils.isEmpty(addressId)) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);
        }

        String url = NetworkUtility.WS.SP_LIST;

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(url
                , mCallSPListWSErrorListener
                , mCallSPListResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList, NetworkUtility.WS.SP_LIST);
    }

    Response.Listener mCallSPListResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        ArrayList<ProviderModel> list = Utility.getObjectListFromJsonString(jsonObject.getString(NetworkUtility.TAGS.DATA), ProviderModel[].class);
                        Log.i(TAG, "onResponse: size>>" + list.size());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
//                        errorLoadingHelper.failed(getString(R.string.label_something_went_wrong), 0, onRetryBtnClickListener);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
//                        errorLoadingHelper.failed(error_message, 0, onRetryBtnClickListener);
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
                mCallSPListWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };


    Response.ErrorListener mCallSPListWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseTwoBinding.getRoot());

            hideProgressDialog();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.PLACE_PICKER_REQUEST) {
            isPlacePickerClicked = false;
            if (resultCode == RESULT_OK) {
                isAddressPickYouLocationVerified = true;
                isAddressNameVerified = true;
                final Place place = PlacePicker.getPlace(mContext, data);
                final CharSequence name = place.getName();
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
        } else if (requestCode == Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "onActivityResult: CurrentPath" + mCurrentPhotoPath);
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mCurrentPhotoPath = Utility.getPath(mStrategicPartnerTaskCreationAct, contentUri);
            imageAdapter.addImage(new MediaModel(mCurrentPhotoPath, MediaModel.MediaType.IMAGE));
            mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag(Utility.TEMPLATE_UPLOAD).setSelected(true);
            checkMediaArraySize();
        } else if (requestCode == Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "onActivityResult: " + data.getData().toString());
            mCurrentPhotoPath = Utility.getPath(mStrategicPartnerTaskCreationAct, data.getData());
            imageAdapter.addImage(new MediaModel(mCurrentPhotoPath, MediaModel.MediaType.IMAGE));
            mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag(Utility.TEMPLATE_UPLOAD).setSelected(true);
            checkMediaArraySize();

        } else if (requestCode == Utility.REQUEST_CODE_VIDEO_CAPTURE && resultCode == RESULT_OK && data != null) {
            mCurrentPhotoPath = data.getStringExtra("path");
            Log.e(TAG, "path >> " + mCurrentPhotoPath);
            imageAdapter.addImage(new MediaModel(mCurrentPhotoPath, MediaModel.MediaType.VIDEO));
            checkMediaArraySize();
        } else if (requestCode == Utility.REQUEST_CODE_GET_VIDEO_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            mCurrentPhotoPath = Utility.getPath(mStrategicPartnerTaskCreationAct, selectedImageUri);
            Log.e(TAG, "path >> " + mCurrentPhotoPath);
            imageAdapter.addImage(new MediaModel(mCurrentPhotoPath, MediaModel.MediaType.VIDEO));
            checkMediaArraySize();
        }
    }

    private void checkMediaArraySize() {
        if (imageAdapter.getItemCount() == 3) {
            ImageView imageView = mFragmentStrategicPartnerPhaseTwoBinding.linMain.findViewWithTag("AddImage");
            if (imageView != null)
                imageView.setVisibility(View.GONE);
        }
    }


    private boolean doValidation() {
        String message = "";
        for (int i = 0; i < mList.size(); i++) {
            QueAnsModel queAnsModel = mList.get(i);
            if (queAnsModel.answer == null || queAnsModel.answer.equalsIgnoreCase("")) {
                if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_DATE_PICKER)) {
                    message = "Please select the date";
                    Utility.showSnackBar(message, mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
                    return false;
                } else if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_TIME_PICKER)) {
                    message = "Please select the time";
                    Utility.showSnackBar(message, mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
                    return false;
                } else if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_DROPDOWN)) {
                    message = "Please answer all the questions";
                    Utility.showSnackBar(message, mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
                    return false;
                }
            }
            if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_LOCATION) && addressId.equalsIgnoreCase("")) {
                message = "Please select an address";
                Utility.showSnackBar(message, mFragmentStrategicPartnerPhaseTwoBinding.getRoot());
                return false;
            }
        }
        return true;
    }


}
