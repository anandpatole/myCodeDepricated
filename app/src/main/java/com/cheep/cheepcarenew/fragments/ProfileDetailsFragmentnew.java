package com.cheep.cheepcarenew.fragments;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.HomeActivity;
import com.cheep.activity.VerificationActivity;
import com.cheep.adapter.AddressRecyclerViewAdapterProfile;
import com.cheep.adapter.EmergencyContactRecyclerViewAdapter;
import com.cheep.addresspopupsfortask.AddressCategorySelectionDialog;
import com.cheep.addresspopupsfortask.AddressSelectionListener;
import com.cheep.cheepcarenew.dialogs.BottomAddAddressDialog;
import com.cheep.cheepcarenew.dialogs.AddressListProfileDialog;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.DividerItemDecoration;
import com.cheep.databinding.DialogChangePhoneNumberBinding;
import com.cheep.databinding.FragmentProfileDetailsNewBinding;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.ChatUserModel;
import com.cheep.fragment.BaseFragment;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.model.AddressModel;
import com.cheep.model.GuestUserDetails;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.MediaUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.cheep.R.id.edit_username;
import static com.cheep.utils.Utility.REQUEST_CODE_CROP_GET_FILE_ADD_PROFILE;
import static com.cheep.utils.Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE;

/**
 * Created by pankaj on 9/27/16.
 */

public class ProfileDetailsFragmentnew extends BaseFragment implements
        EmergencyContactRecyclerViewAdapter.EmergencyInteractionListener,
        WebCallClass.UpdateEmergencyContactResponseListener,
        WebCallClass.CommonResponseListener,
        View.OnClickListener {


    public static final String TAG = "ProfileDetailsFragmentnew";
    private String TEMP_PHONE_NUMBER;
    private FragmentProfileDetailsNewBinding mBinding;
    public static JSONArray jsonEmergencyContacts;
    private ArrayList<AddressModel> addressList;
    private DrawerLayoutInteractionListener mListener;
    String rating;
    private RelationShipScreenFragment relationShipScreenFragment;
    private ManageSubscriptionFragment manageSubscriptionFragment;

    // dialog
    private AddressCategorySelectionDialog addressCategorySelectionDialog;
    private AddressListProfileDialog addressListDialog;
    private UserDetails userDetails;
    private AddressRecyclerViewAdapterProfile adapterAddressRecyclerView;
    boolean isPlacePickerClicked = false;

    //    private String mCurrentPhotoPath;
    private File photoFile;

    public static ProfileDetailsFragmentnew newInstance() {
        Bundle args = new Bundle();
        ProfileDetailsFragmentnew fragment = new ProfileDetailsFragmentnew();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_details_new, container, false);
        callGetProfileWS();
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();

    }

    @Override
    public void onResume() {
        super.onResume();
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
    public void onDetach() {
        Log.i(TAG, "onDetach: ");
        mListener = null;
        /*
          Cancel the request as it no longer available
         */
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.UPDATE_LOCATION);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.UPDATE_PROFILE);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.DELETE_ADDRESS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.EDIT_ADDRESS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.ADD_ADDRESS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.PROFILE);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.UPDATE_EMERGENCY_CONTACTS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.EDIT_PHONE_NUMBER);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.CHANGE_PASSWORD);

        super.onDetach();
    }


    @Override
    public void initiateUI() {


        //Fetch User Details from Preference
        userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        mBinding.recyclerEmergencyContact.setNestedScrollingEnabled(false);

        fillFields(userDetails);
    }

    private void fillFields(UserDetails userDetails) {
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            //loading rounded image on profile
            loadImage(userDetails.profileImg);
            //  loadCoverImage(userDetails.profileBanner);

            //Update Name
            if (rating != null) {
                mBinding.textRating.setText(rating);
                mBinding.userRating.setRating(Integer.parseInt(rating.toString().trim()));
            }
            mBinding.userName.setText(userDetails.userName);
            if (addressList != null) {
                //fillAddressRecyclerView(mBinding.textAddressRecyclerNew);
                // fillAddressRecyclerView1(mBinding.textAddressRecyclerNew);
            }
            if (jsonEmergencyContacts != null) {
                if (jsonEmergencyContacts.length() >= 3) {
                    mBinding.textAddContact.setVisibility(View.GONE);
                } else {
                    mBinding.textAddContact.setVisibility(View.VISIBLE);
                }
                EmergencyContactRecyclerViewAdapter adapter = new EmergencyContactRecyclerViewAdapter(ProfileDetailsFragmentnew.this, jsonEmergencyContacts);
                mBinding.recyclerEmergencyContact.setLayoutManager(new LinearLayoutManager(mContext));
                mBinding.recyclerEmergencyContact.setAdapter(adapter);
            }
            mBinding.textPhoneNo.setText(userDetails.phoneNumber);
            mBinding.textEmail.setText(userDetails.email);

        } else {
            // Static details for Guest Users
            //  mBinding.userName.setText(Utility.GUEST_STATIC_INFO.USERNAME);
        }

    }


    @Override
    public void setListener() {

        mBinding.imgProfilePhotoEdit.setOnClickListener(onClickListener);
        mBinding.textManageCheepCareSubscription.setOnClickListener(onClickListener);
        mBinding.textAddContact.setOnClickListener(onClickListener);
        mBinding.textViewMore.setOnClickListener(onClickListener);
        mBinding.labelAddNewAddress.setOnClickListener(onClickListener);
        mBinding.textViewLess.setOnClickListener(onClickListener);
        mBinding.usernameProfileEdit.setOnClickListener(onClickListener);
        mBinding.emailProfileEdit.setOnClickListener(onClickListener);
        mBinding.mainProfileEdit.setOnClickListener(onClickListener);


    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
            switch (view.getId()) {
              /*  case R.id.text_phone_number:
                    showChangePhoneNumberDialog();
                    break;*/
                case R.id.text_manage_cheep_care_subscription:
                    manageSubscriptionFragment = ManageSubscriptionFragment.newInstance(addressList);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, manageSubscriptionFragment, ManageSubscriptionFragment.TAG).commitAllowingStateLoss();
                    break;
                case R.id.main_profile_edit:
                    mBinding.mainProfileEdit.setVisibility(View.GONE);
                    mBinding.usernameProfileEdit.setVisibility(View.VISIBLE);
                    mBinding.imgProfilePhotoEdit.setVisibility(View.VISIBLE);
                    mBinding.emailProfileEdit.setVisibility(View.VISIBLE);
                    break;
                case R.id.username_profile_edit:
                    if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
                        UserDetails details = PreferenceUtility.getInstance(mContext).getUserDetails();
                        showChangeUsernameDialog(details.userName);
                    }
                    break;
                case R.id.email_profile_edit:
                    if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
                        UserDetails details = PreferenceUtility.getInstance(mContext).getUserDetails();
                        showChangeEmailDialog(details.email);
                    }
                    break;
                case R.id.text_add_contact:
                    relationShipScreenFragment = RelationShipScreenFragment.newInstance();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, relationShipScreenFragment, RelationShipScreenFragment.TAG).commitAllowingStateLoss();
                    //showProgressDialog();
                    // WebCallClass.getRelationShipListDetail(mContext,ProfileDetailsFragmentnew.this,ProfileDetailsFragmentnew.this);
                    //showChangeEmergencyContactDialog();
                    break;
                case R.id.text_view_less:
                    //fillAddressRecyclerView(mBinding.textAddressRecyclerNew);

                   /* fillAddressRecyclerView1(mBinding.textAddressRecyclerNew);
                    mBinding.textViewLess.setVisibility(View.GONE);
                    mBinding.textViewMore.setVisibility(View.VISIBLE);*/
                    break;
                case R.id.text_view_more:
                    showAddressListDialog();

                   /* fillAddressRecyclerView(mBinding.textAddressRecyclerNew);
                    mBinding.textViewMore.setVisibility(View.GONE);
                    mBinding.textViewLess.setVisibility(View.VISIBLE);*/
                    //showAddressDialog();
                    break;
                case R.id.label_add_new_address:
//                    showBottomAddressDialog(null);
                    AddressCategorySelectionDialog addressCategorySelectionDialog = AddressCategorySelectionDialog.newInstance(true, new AddressSelectionListener() {
                        @Override
                        public void onAddressSelection(AddressModel addressModel) {
                            addressList.add(addressModel);
                        }
                    });
                    addressCategorySelectionDialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), AddressCategorySelectionDialog.TAG);
                    break;
                case R.id.text_emergency_contact:
                    showChangeEmergencyContactDialog();
                    break;
                case R.id.text_manage_address:
                    showAddressDialog();

                    break;
                /*case R.id.text_change_password:
                    showChangePasswordDialog();
                    break;*/
                case R.id.img_edit_username:
                    showChangeUsernameDialog(userDetails.userName);
                    break;
                case R.id.img_edit_email:
                    showChangeEmailDialog(userDetails.email);
                    break;
                case R.id.img_profile_photo_edit:
                    if (Utility.isConnected(mContext)) {
//                        CropImage.activity()
//                                .setGuidelines(CropImageView.Guidelines.ON)
//                                .start(getContext(), ProfileDetailsFragment.this);
//
                        // showPictureChooserDialog(false);
                        showPictureChooserDialog(false);
                        // showChooserDialog();
                    } else {
                        Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
                    }
                    break;
                case R.id.img_cover_photo_edit:
                    if (Utility.isConnected(mContext)) {
                        showPictureChooserDialog(true);
                    } else {
                        Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
                    }
                    break;
            }
        }
    };

    private void showPictureChooserDialog(final boolean isForBanner) {
        Log.d(TAG, "showPictureChooserDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.choose_image)
                .setItems(R.array.choose_image_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0) {
                            /* if (!isForBanner) {*/
                            dispatchTakePictureIntent(REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE, Utility.REQUEST_CODE_WRITE_EXTERNAL_STORAGE_ADD_PROFILE_CAMERA);
                         /*   } else {
                                dispatchTakePictureIntent(Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_COVER, Utility.REQUEST_CODE_WRITE_EXTERNAL_STORAGE_ADD_PROFILE_CAMERA);

                            }*/
                        } else {
                            /* if (!isForBanner) {*/
                            //Select Gallery
                            // In case Choose File from Gallery
                            choosePictureFromGallery(Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY, Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY);
                           /* } else {
                                //Cover
                                choosePictureFromGallery(Utility.REQUEST_CODE_GET_FILE_ADD_COVER_GALLERY, Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_COVER);
                            }*/
                        }
                    }
                });
        builder.create();

        //Show the dialog
        builder.show();
    }

    private void dispatchTakePictureIntent(int requestCode, int requestPermissionCode) {
        //Go ahead with Camera capturing
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, requestPermissionCode);
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, requestPermissionCode);
            }
        } else {
            //Go ahead with Camera capturing
            startCameraCaptureChooser(requestCode);
        }
    }

    public void startCameraCaptureChooser(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
//                mCurrentPhotoPath = photoFile.getAbsolutePath();
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

                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        BuildConfig.FILE_PROVIDER_URL,
                        photoFile);
                /*   Uri  photoURI= Uri.parse(photoFile.toString());*/
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Grant URI permission START
                // Enableing the permission at runtime
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip =
                            ClipData.newUri(getActivity().getContentResolver(), "A photo", photoURI);
                    takePictureIntent.setClipData(clip);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    List<ResolveInfo> resInfoList =
                            getActivity().getPackageManager()
                                    .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        getActivity().grantUriPermission(packageName, photoURI,
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

        return new File(new File(getActivity().getFilesDir(), "CheepImages"), imageFileName);
    }

    private BottomAlertDialog changeUsernameOrEmail;

    private void showChangeUsernameDialog(final String username) {

        View view = View.inflate(mContext, R.layout.dialog_change_username, null);
        final EditText edtUsername = (EditText) view.findViewById(edit_username);
        edtUsername.setText(username);
        changeUsernameOrEmail = new BottomAlertDialog(mContext);
        view.findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (username.equalsIgnoreCase(edtUsername.getText().toString().trim())) {
                    changeUsernameOrEmail.dismiss();
                    return;
                } else if (username.trim().length() < 3) {
                    Utility.showToast(mContext, getString(R.string.validate_username_length));
                    return;
                }

                HashMap<String, String> mParams = new HashMap<>();
                mParams.put(NetworkUtility.TAGS.USERNAME, edtUsername.getText().toString().trim());
                callUpdateProfileWS(mParams, null);
            }
        });
        changeUsernameOrEmail.setTitle(getString(R.string.label_username));
        changeUsernameOrEmail.setCustomView(view);
        changeUsernameOrEmail.showDialog();
    }

    private void showChangeEmailDialog(final String email) {

        View view = View.inflate(mContext, R.layout.dialog_change_username, null);
        final EditText edtUsername = (EditText) view.findViewById(R.id.edit_username);
        edtUsername.setText(email);
        final BottomAlertDialog dialog = new BottomAlertDialog(mContext);
        view.findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email.equalsIgnoreCase(edtUsername.getText().toString().trim())) {
                    dialog.dismiss();
                    return;
                }
                HashMap<String, String> mParams = new HashMap<>();
                mParams.put(NetworkUtility.TAGS.EMAIL_ADDRESS, edtUsername.getText().toString().trim());
                callUpdateProfileWS(mParams, null);
                dialog.dismiss();
            }
        });
        dialog.setTitle(getString(R.string.hint_email));
        dialog.setCustomView(view);
        dialog.showDialog();
    }

    BottomAlertDialog changePhoneNumberDialog;

    private void showChangePhoneNumberDialog() {
        final DialogChangePhoneNumberBinding dialogChangePhoneNumberBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_change_phone_number, null, false);
        changePhoneNumberDialog = new BottomAlertDialog(mContext);
//        dialogChangePhoneNumberBinding.editPhoneNumber.setEnabled(false);

        //Fetch User Details from Preference
        final UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        dialogChangePhoneNumberBinding.editPhoneNumber.setText(userDetails.phoneNumber);
        dialogChangePhoneNumberBinding.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNumber = dialogChangePhoneNumberBinding.editPhoneNumber.getText().toString();
                if (!phoneNumber.equals(userDetails.phoneNumber)) {
                    TEMP_PHONE_NUMBER = phoneNumber;
                    changePhoneNumber(phoneNumber);
                } else {
                    changePhoneNumberDialog.dismiss();
                }
            }
        });
        changePhoneNumberDialog.setTitle(getString(R.string.hint_mobile_number));
        changePhoneNumberDialog.setCustomView(dialogChangePhoneNumberBinding.getRoot());
        changePhoneNumberDialog.showDialog();
    }

    private void showChangeEmergencyContactDialog() {

        View view = View.inflate(mContext, R.layout.dialog_emergency_phone_number, null);

        final EditText edtContactName1 = (EditText) view.findViewById(R.id.edit_contact_name_1);
        final EditText edtContactNumber1 = (EditText) view.findViewById(R.id.edit_contact_number_1);
        final EditText edtContactName2 = (EditText) view.findViewById(R.id.edit_contact_name_2);
        final EditText edtContactNumber2 = (EditText) view.findViewById(R.id.edit_contact_number_2);
        final EditText edtContactName3 = (EditText) view.findViewById(R.id.edit_contact_name_3);
        final EditText edtContactNumber3 = (EditText) view.findViewById(R.id.edit_contact_number_3);

        if (jsonEmergencyContacts != null) {
            if (jsonEmergencyContacts.length() > 0) {
                edtContactName1.setText(jsonEmergencyContacts.optJSONObject(0).optString(NetworkUtility.TAGS.NAME));
                edtContactNumber1.setText(jsonEmergencyContacts.optJSONObject(0).optString(NetworkUtility.TAGS.NUMBER));
            }
            if (jsonEmergencyContacts.length() > 1) {
                edtContactName2.setText(jsonEmergencyContacts.optJSONObject(1).optString(NetworkUtility.TAGS.NAME));
                edtContactNumber2.setText(jsonEmergencyContacts.optJSONObject(1).optString(NetworkUtility.TAGS.NUMBER));
            }
            if (jsonEmergencyContacts.length() > 2) {
                edtContactName3.setText(jsonEmergencyContacts.optJSONObject(2).optString(NetworkUtility.TAGS.NAME));
                edtContactNumber3.setText(jsonEmergencyContacts.optJSONObject(2).optString(NetworkUtility.TAGS.NUMBER));
            }
        }

        final BottomAlertDialog dialog = new BottomAlertDialog(mContext);
        view.findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String number1 = edtContactNumber1.getText().toString().trim();
                String number2 = edtContactNumber2.getText().toString().trim();
                String number3 = edtContactNumber3.getText().toString().trim();
                if (TextUtils.isEmpty(edtContactName1.getText().toString().trim()) &&
                        TextUtils.isEmpty(edtContactNumber1.getText().toString().trim()) &&
                        TextUtils.isEmpty(edtContactName2.getText().toString().trim()) &&
                        TextUtils.isEmpty(edtContactNumber2.getText().toString().trim()) &&
                        TextUtils.isEmpty(edtContactName3.getText().toString().trim()) &&
                        TextUtils.isEmpty(edtContactNumber3.getText().toString().trim())
                        ) {
                    Utility.showToast(mContext, getString(R.string.validate_phone_number));
                } else if (TextUtils.isEmpty(edtContactName1.getText().toString().trim()) && !TextUtils.isEmpty(edtContactNumber1.getText().toString().trim())
                        ||
                        !TextUtils.isEmpty(edtContactName1.getText().toString().trim()) && TextUtils.isEmpty(edtContactNumber1.getText().toString().trim())
                        ) {
                    Utility.showToast(mContext, getString(R.string.validate_phone_number));
                } else if (TextUtils.isEmpty(edtContactName2.getText().toString().trim()) && !TextUtils.isEmpty(edtContactNumber2.getText().toString().trim())
                        ||
                        !TextUtils.isEmpty(edtContactName2.getText().toString().trim()) && TextUtils.isEmpty(edtContactNumber2.getText().toString().trim())
                        ) {
                    Utility.showToast(mContext, getString(R.string.validate_phone_number));
                } else if (TextUtils.isEmpty(edtContactName3.getText().toString().trim()) && !TextUtils.isEmpty(edtContactNumber3.getText().toString().trim())
                        ||
                        !TextUtils.isEmpty(edtContactName3.getText().toString().trim()) && TextUtils.isEmpty(edtContactNumber3.getText().toString().trim())
                        ) {
                    Utility.showToast(mContext, getString(R.string.validate_phone_number));
                } else if (number1.equals(number2) || number2.equals(number3) || number3.equals(number1)) {
                    Utility.showToast(mContext, getString(R.string.validate_phone_number_different));
                } else {

                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonObject;
                    try {
                        if (!TextUtils.isEmpty(edtContactName1.getText().toString().trim())) {
                            jsonObject = new JSONObject();
                            jsonObject.put(NetworkUtility.TAGS.NAME, edtContactName1.getText().toString().trim());
                            jsonObject.put(NetworkUtility.TAGS.NUMBER, edtContactNumber1.getText().toString().trim());
                            jsonArray.put(jsonObject);
                        }
                        if (!TextUtils.isEmpty(edtContactName2.getText().toString().trim())) {
                            jsonObject = new JSONObject();
                            jsonObject.put(NetworkUtility.TAGS.NAME, edtContactName2.getText().toString().trim());
                            jsonObject.put(NetworkUtility.TAGS.NUMBER, edtContactNumber2.getText().toString().trim());
                            jsonArray.put(jsonObject);
                        }
                        if (!TextUtils.isEmpty(edtContactName3.getText().toString().trim())) {
                            jsonObject = new JSONObject();
                            jsonObject.put(NetworkUtility.TAGS.NAME, edtContactName3.getText().toString().trim());
                            jsonObject.put(NetworkUtility.TAGS.NUMBER, edtContactNumber3.getText().toString().trim());
                            jsonArray.put(jsonObject);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    updateEmergencyContact(jsonArray);
                    dialog.dismiss();
                }
            }
        });
        dialog.setTitle(getString(R.string.label_emergency_contacts));
        dialog.setCustomView(view);
        dialog.showDialog();
    }


    BottomAlertDialog changePasswordDialog;

    private void showChangePasswordDialog() {

        View view = View.inflate(mContext, R.layout.dialog_change_password, null);
        changePasswordDialog = new BottomAlertDialog(mContext);
        final EditText editNewPassword = view.findViewById(R.id.edit_new_password);
        final EditText editOldPassword = view.findViewById(R.id.edit_old_password);
        final EditText editConfirmPassword = view.findViewById(R.id.edit_confirm_password);

        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                changePassword(editOldPassword.getText().toString(), editNewPassword.getText().toString(), editConfirmPassword.getText().toString());

            }
        });
        changePasswordDialog.setTitle(getString(R.string.label_change_password));
        changePasswordDialog.setCustomView(view);
        changePasswordDialog.showDialog();

    }


    private void showAddressDialog() {

        View view = View.inflate(mContext, R.layout.dialog_choose_address, null);
        boolean shouldOpenAddAddress = fillAddressRecyclerView((RecyclerView) view.findViewById(R.id.recycler_view));
        final BottomAlertDialog dialog = new BottomAlertDialog(mContext);
        view.findViewById(R.id.btn_add_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomAddressDialog(null);
//                dialog.dismiss();
            }
        });
        dialog.setTitle(getString(R.string.label_address));
        dialog.setCustomView(view);
        dialog.showDialog();

        if (shouldOpenAddAddress) {
            showBottomAddressDialog(null);
        }
    }
    private boolean fillAddressRecyclerView(RecyclerView recyclerView) {
        //Setting RecyclerView Adapter
        adapterAddressRecyclerView = new AddressRecyclerViewAdapterProfile(addressList, listener, 1);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapterAddressRecyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, R.drawable.divider_grey_normal, (int) getResources().getDimension(R.dimen.scale_16dp)));

        //Here we are checking if address is not there then open add address dialog immediatly
        return addressList == null || addressList.isEmpty();
    }


    public void showPlacePickerDialog(boolean isForceShow) {
        HomeActivity homeActivity = (HomeActivity) getActivity();
        if (!isForceShow) {
            if (homeActivity != null && homeActivity.mLocationTrackService != null) {
                isPlacePickerClicked = true;
                homeActivity.mLocationTrackService.requestLocationUpdate();
                return;
            }

        }

        try {
            Utility.hideKeyboard(mContext);
            showProgressDialog();
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(getActivity());
            startActivityForResult(intent, Utility.PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            Utility.showToast(mContext, getString(R.string.label_playservice_not_available));
        }
    }


    @Override
    public void onLocationFetched(Location mLocation) {
        super.onLocationFetched(mLocation);
    }

    @Override
    public void onLocationNotAvailable() {
        super.onLocationNotAvailable();
    }

    @Override
    public void onLocationSettingsDialogNeedToBeShow(Status locationRequest) {
        super.onLocationSettingsDialogNeedToBeShow(locationRequest);
        // Location settings are not satisfied, but this can be fixed
        // by showing the user a dialog.
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            locationRequest.startResolutionForResult(getActivity(), Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error.
        }
    }


    @Override
    public void gpsEnabled() {
        super.gpsEnabled();
        if (isPlacePickerClicked) {
            showPlacePickerDialog(true);
        }
    }

    private void showGPSEnableDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.MyAlertDialogStyle);
        /*builder.setCancelable(false);
        builder.setTitle(getString(R.string.label_force_logout));
        builder.setMessage(getString(R.string.desc_force_logout));
        builder.setPositiveButton(getString(R.string.label_Ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(android.selectedProvider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.show();
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);*/
        builder.setMessage(getString(R.string.label_turn_on_location))
                .setPositiveButton(getString(R.string.label_turn_on), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        dialog.cancel();
                    }
                });
               /* .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        showPlacePickerDialog(true);
                        dialog.cancel();
                    }
                });*/
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private String TEMP_ADDRESS_ID = "";
    private BottomAddAddressDialog dialog;
    AddressRecyclerViewAdapterProfile.AddressItemInteractionListener listener = new AddressRecyclerViewAdapterProfile.AddressItemInteractionListener() {
        @Override
        public void onEditClicked(AddressModel model, int position) {
            TEMP_ADDRESS_ID = model.address_id;
//            showAddAddressDialog(model);

            // showBottomAddressDialog(model); commented by majid khan 21 june 2018
            //showEditAddressDialog();
        }


        @Override
        public void onDeleteClicked(AddressModel model, int position) {
            //showAddressDeletionConfirmationDialog(model);
        }

        @Override
        public void onRowClicked(AddressModel model, int position) {

        }
    };

    private void showBottomAddressDialog(AddressModel model) {
        dialog = new BottomAddAddressDialog(ProfileDetailsFragmentnew.this, new BottomAddAddressDialog.AddAddressListener() {
            @Override
            public void onAddAddress(AddressModel addressModel) {
//                    mList.add(addressModel);

                if (addressList == null)
                    addressList = new ArrayList<>();

                addressList.add(addressModel);
                if (adapterAddressRecyclerView != null)
                    adapterAddressRecyclerView.notifyDataSetChanged();

                //Saving information in sharedpreference
                UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                if (userDetails != null) {
                    userDetails.addressList = addressList;
                    PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);
                } else {
                    GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
                    guestUserDetails.addressList = addressList;
                    PreferenceUtility.getInstance(mContext).saveGuestUserDetails(guestUserDetails);
                }

                if (dialog != null) {
                    dialog.dismiss();
                }
            }

            @Override
            public void onUpdateAddress(AddressModel addressModel) {
                if (!TextUtils.isEmpty(TEMP_ADDRESS_ID)) {
                    if (adapterAddressRecyclerView != null) {
                        adapterAddressRecyclerView.updateItem(addressModel);

                    }
                }

                //Saving information in sharedpreference
                UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                userDetails.addressList = addressList;
                PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        }, new ArrayList<String>(), model);

        dialog.showDialog();
    }

    // set  first address form list on view
    /* need to show only one address then i
     take zero position data from list*/
    private void setAddress() {
        if (addressList != null && addressList.size() >0) {
            for (int i = 0; i < addressList.size(); i++) {
                mBinding.imgAddress.setImageResource(Utility.getAddressCategoryBlueIcon(addressList.get(0).category));
                mBinding.tvAddressCategory.setText(addressList.get(0).category);
                mBinding.tvFullAddress.setText(addressList.get(0).getAddressWithInitials());
                hideAndShowView(addressList.get(0).is_subscribe);
                break;
            }
            mBinding.linearAddressLayout.setVisibility(View.VISIBLE);
            mBinding.linearManageCheepCareSubscription.setVisibility(View.VISIBLE);
        }else {
            mBinding.linearManageCheepCareSubscription.setVisibility(View.GONE);
            mBinding.linearAddressLayout.setVisibility(View.GONE);
        }
    }
    /*this method is used , when address is  subscribe
     then edit and delete text view not visible */
    /* In AddressModel have one variable is_subscribe if this variable is none this means
     * address is not subscribe */
    private void hideAndShowView(String isSubscribe){
        if(isSubscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.NONE)){
            mBinding.tvEdit.setVisibility(View.VISIBLE);
            mBinding.tvEdit.setOnClickListener(this);

            mBinding.tvDelete.setVisibility(View.VISIBLE);
            mBinding.tvDelete.setOnClickListener(this);
        }else {
            mBinding.tvEdit.setVisibility(View.INVISIBLE);
            mBinding.tvEdit.setOnClickListener(null);

            mBinding.tvDelete.setVisibility(View.INVISIBLE);
            mBinding.tvDelete.setOnClickListener(null);
        }

    }

    // open show address list Dialog
    private void showAddressListDialog() {
        if (addressListDialog != null) {
            addressListDialog.dismissAllowingStateLoss();
            addressListDialog = null;
        }
        addressListDialog = AddressListProfileDialog.newInstance(addressList);
        addressListDialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(), AddressListProfileDialog.TAG);
    }

    // show dialog for select home and office address
    private void showAddressCategorySelectionDialog() {
        addressCategorySelectionDialog = AddressCategorySelectionDialog.newInstance(Utility.EDIT_PROFILE_ACTIVITY,true ,addressList,Utility.EDIT_ADDRESD_POSITION);

        addressCategorySelectionDialog.show(((BaseAppCompatActivity) getContext()).getSupportFragmentManager(),
                AddressCategorySelectionDialog.TAG);
    }
    /* this method is used , when edit Address Api has success*/
    public void getDataFromEditAddressDialog(){
        callGetProfileWS();
    }
    ///////////////////////////// DELETE CONFIRMATION DIALOG//////////////////////////////////////

    private void showAddressDeletionConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.MyAlertDialogStyle);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.cheep_all_caps));
        builder.setMessage(getString(R.string.label_address_delete_message));
        builder.setPositiveButton(getString(R.string.label_Ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick() called with: dialogInterface = [" + dialogInterface + "], i = [" + i + "]");
                TEMP_ADDRESS_ID = addressList.get(0).address_id;
                callDeleteAddressWS(TEMP_ADDRESS_ID);
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

    //View.OnClickListener
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_edit:
                showAddressCategorySelectionDialog();
                break;
            case R.id.tv_delete:
                showAddressDeletionConfirmationDialog();
                break;
        }
    }



     /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// PageContent[START]/////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void callUpdateProfileWS(Map<String, String> mParams, HashMap<String, File> mFileParams) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.UPDATE_PROFILE
                , mCallUpdateProfileWSErrorListener
                , mCallUpdateProfileResponseListener
                , mHeaderParams
                , mParams
                , mFileParams);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }

    /**
     * Listeners for get profile calls
     */
    Response.Listener mCallUpdateProfileResponseListener = new Response.Listener() {
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
                        if (changeUsernameOrEmail != null) {
                            changeUsernameOrEmail.dismiss();
                        }
                        JSONObject jsonData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
                        UserDetails userDetails = (UserDetails) GsonUtility.getObjectFromJsonString(jsonData.toString(), UserDetails.class);
                        PreferenceUtility.getInstance(mContext).saveUserDetails(jsonData);
                        fillFields(userDetails);
                        mListener.profileUpdated();
                        if (userDetails != null) {
                            /*
                             * Update user detail in fierbase
                             * */
                            ChatUserModel chatUserModel = new ChatUserModel();
                            chatUserModel.setUserId(FirebaseUtils.getPrefixUserId(userDetails.userID));
                            chatUserModel.setUserName(userDetails.userName);
                            chatUserModel.setProfileImg(userDetails.profileImg);
                            FirebaseHelper.getUsersRef(chatUserModel.getUserId()).setValue(chatUserModel);
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
                        Utility.logout(mContext, true, statusCode);
                        if (getActivity() != null)
                            getActivity().finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallUpdateProfileWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallUpdateProfileWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(mContext.getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    /**
     * Calling delete address Web service
     *
     * @param addressId
     */
    private void callDeleteAddressWS(String addressId) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, String.valueOf(addressId));

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.DELETE_ADDRESS
                , mCallDeleteAddressWSErrorListener
                , mCallDeleteAddressResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
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

                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        addressList.remove(0);
                        setAddress();
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
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };


    /**
     * Calling get profile Web service
     */
    private void callGetProfileWS() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            return;
        }
        //        //Show Progress
        //showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.PROFILE
                , mCallGetProfileWSErrorListener
                , mCallGetProfileWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }

    /**
     * Listeners for get profile calls
     */
    Response.Listener mCallGetProfileWSResponseListener = new Response.Listener() {
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

                        JSONObject jsonData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
                        rating = jsonData.getString(NetworkUtility.TAGS.AVERAGE_RATING);
                        UserDetails userDetails = (UserDetails) GsonUtility.getObjectFromJsonString(jsonData.toString(), UserDetails.class);
                        PreferenceUtility.getInstance(mContext).saveUserDetails(jsonData);
                        jsonEmergencyContacts = jsonData.optJSONArray(NetworkUtility.TAGS.EMERGENCY_DATA);
                        addressList = GsonUtility.getObjectListFromJsonString(jsonData.optJSONArray(NetworkUtility.TAGS.ADDRESS).toString(), AddressModel[].class);
                        fillFields(userDetails);
                        // set address on view
                        setAddress();


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
            } catch (JSONException e) {
                e.printStackTrace();
                mCallGetProfileWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
//            hideProgressDialog();
        }
    };

    //RelationShip listner

    Response.ErrorListener mCallGetProfileWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    /**
     * Update Emergency Contact webservice call
     *
     * @param jsonArrayEmergencyContact
     */
    private void updateEmergencyContact(JSONArray jsonArrayEmergencyContact) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.EMERGENCY_DATA, jsonArrayEmergencyContact);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.UPDATE_EMERGENCY_CONTACTS
                , mCallUpdateEmergencyContactWSErrorListener
                , mCallUpdateEmergencyContactWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }

    /**
     * Listeners for tracking Webservice calls
     */
    Response.Listener mCallUpdateEmergencyContactWSResponseListener = new Response.Listener() {
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
                        jsonEmergencyContacts = jsonObject.getJSONArray(NetworkUtility.TAGS.DATA);

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
            } catch (JSONException e) {
                e.printStackTrace();
                mCallUpdateEmergencyContactWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallUpdateEmergencyContactWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    /**
     * Call Login WS Key Webservice
     */
    private void changePhoneNumber(String phoneNumber) {

        if (TextUtils.isEmpty(phoneNumber)) {
//            Utility.showSnackBar(getString(R.string.validate_phone_number), mBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.validate_phone_number));
            return;
        } //Length of phone number must bhi 10 in length
        else if (!Utility.isValidPhoneNumber(phoneNumber.trim())) {
//            Utility.showSnackBar(getString(R.string.validate_phone_number), mBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.validate_phone_number));
            return;
        }


        if (!Utility.isConnected(mContext)) {
            Utility.showToast(mContext, Utility.NO_INTERNET_CONNECTION);
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.PHONE_NUMBER, phoneNumber);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.EDIT_PHONE_NUMBER
                , mCallChangePhoneNumberWSErrorListener
                , mCallChangePhoneNumberWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);

    }

    /**
     * Listeners for tracking Webservice calls
     */
    Response.Listener mCallChangePhoneNumberWSResponseListener = new Response.Listener() {
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

//                        String message = jsonObject.getString(NetworkUtility.TAGS.OTP_CODE);
                        VerificationActivity.newInstance(mContext, PreferenceUtility.getInstance(mContext).getUserDetails(), TEMP_PHONE_NUMBER, "");
                        if (changePhoneNumberDialog != null) {
                            changePhoneNumberDialog.dismiss();
                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showToast(mContext, error_message);
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
                mCallChangePhoneNumberWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallChangePhoneNumberWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    /**
     * Call Login WS Key Webservice
     */
    private void changePassword(String oldPassword, String newPassword, String confirmPassword) {

        if (TextUtils.isEmpty(oldPassword)) {
            Utility.showSnackBar(getString(R.string.validate_empty_password), mBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.validate_empty_password));
            return;
        } else if (TextUtils.isEmpty(newPassword)) {
            Utility.showSnackBar(getString(R.string.validate_empty_password), mBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.validate_empty_password));
            return;
        } else if (TextUtils.isEmpty(confirmPassword)) {
            Utility.showSnackBar(getString(R.string.validate_empty_password), mBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.validate_empty_password));
            return;
        } else if (!newPassword.equalsIgnoreCase(confirmPassword)) {
            Utility.showSnackBar(getString(R.string.validate_confirm_password), mBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.validate_confirm_password));
            return;
        }
        //Length validation
        else if (oldPassword.length() < Utility.PASSWORD_MIN_LENGTH) {
//            Utility.showSnackBar(getString(R.string.validate_password_length), mBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.validate_password_length));
            return;
        } else if (newPassword.length() < Utility.PASSWORD_MIN_LENGTH) {
//            Utility.showSnackBar(getString(R.string.validate_password_length), mBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.validate_password_length));
            return;
        } else if (confirmPassword.length() < Utility.PASSWORD_MIN_LENGTH) {
//            Utility.showSnackBar(getString(R.string.validate_password_length), mBinding.getRoot());
            Utility.showToast(mContext, getString(R.string.validate_password_length));
            return;
        }

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            Utility.showToast(mContext, Utility.NO_INTERNET_CONNECTION);
            return;
        }


        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.OLD_PASSWORD, oldPassword);
        mParams.put(NetworkUtility.TAGS.NEW_PASSWORD, newPassword);


        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.CHANGE_PASSWORD
                , mCallChangePasswordWSErrorListener
                , mCallChangePasswordWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);

    }

    /**
     * Listeners for tracking Webservice calls
     */
    Response.Listener mCallChangePasswordWSResponseListener = new Response.Listener() {
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

                        if (changePasswordDialog != null)
                            changePasswordDialog.dismiss();

                        /*ArrayList<FAQModel> faqModelArrayList = getObjectListFromJsonString(jsonObject.getJSONArray(NetworkUtility.TAGS.DATA).toString(), FAQModel[].class);
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);*/

                        // Show message
                        Utility.showSnackBar(getString(R.string.msg_password_change_success), mBinding.getRoot());

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showToast(mContext, error_message);
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
                mCallChangePasswordWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallChangePasswordWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// PageContent[End]/////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /*************************************************************************************************************
     *************************************************************************************************************
     *****************************************Webservice Integration [End]**************************************
     *************************************************************************************************************
     */

    /**
     * On Click of Search
     */
    public void choosePictureFromGallery(int requestFileChooserCode, int requestPermissionCode) {
        Log.d(TAG, "choosePictureFromGallery() called with: requestFileChooserCode = [" + requestFileChooserCode + "], requestPermissionCode = [" + requestPermissionCode + "]");
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestPermissionCode);
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestPermissionCode);
            }
        } else {
            //Go ahead with file choosing
            startIntentFileChooser(requestFileChooserCode);
        }
    }

    public void startIntentFileChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_COVER:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                    startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_ADD_COVER);
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                    Utility.showSnackBar(getString(R.string.permission_denied_read), mBinding.getRoot());
                }
                break;
            case Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                    startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY);
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                    Utility.showSnackBar(getString(R.string.permission_denied_read), mBinding.getRoot());
                }
                break;
            case Utility.REQUEST_CODE_ADD_PROFILE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                    startCameraCaptureChooser(Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE);
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Denied Camera");
                    Utility.showSnackBar(getString(R.string.permission_denied_camera), mBinding.getRoot());
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                String selectedImagePath = MediaUtility.getPath(mContext, resultUri);
                Log.i(TAG, "onActivityResult: Path:" + selectedImagePath);
                //Load the image from Glide
                loadImage(selectedImagePath);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


//        Log.i(TAG, "onActivityResult: " + data.getData().toString());

       /* if (requestCode == Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS) {
            LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showPlacePickerDialog(true);
                return;
            }
        } else */
        if (requestCode == Utility.PLACE_PICKER_REQUEST && dialog != null) {
            dialog.onActivityResult(resultCode, data);
        } else if (requestCode == Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult: CurrentPath" + photoFile);

            /*File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);*/

//            File f = new File(mCurrentPhotoPath);
//            Uri contentUri = Uri.fromFile(f);
            Uri contentUri = FileProvider.getUriForFile(mContext, BuildConfig.FILE_PROVIDER_URL, photoFile);

            Intent intent = CropImage
                    .activity(contentUri)
//                    .setOutputUri(contentUri)

                    //Set Aspect ration
                    .setAspectRatio(1, 1)

                    //Set Activity Title
                    .setActivityTitle(getString(R.string.label_crop_image))
                    .setActivityMenuIconColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //AutoZoom enabled
                    .setAutoZoomEnabled(true)

                    //Allow rotation
                    .setAllowRotation(true)

                    //Check output compression quality
                    .setOutputCompressQuality(100)

                    //Whether guidelines will be shown or not
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setGuidelinesColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //Border Color
                    .setBorderLineColor(ContextCompat.getColor(mContext, R.color.white))
                    .setBorderLineThickness(getResources().getDimension(R.dimen.scale_3dp))

                    //Border Corner Color
                    .setBorderCornerColor(ContextCompat.getColor(mContext, R.color.white))

//                    Shape
                    .setCropShape(CropImageView.CropShape.OVAL)

                    .getIntent(mContext);

            startActivityForResult(intent, Utility.REQUEST_CODE_CROP_GET_FILE_ADD_PROFILE);
        } else if (requestCode == Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult: " + data.getData().toString());

            Intent intent = CropImage
                    .activity(data.getData())
//                    .setOutputUri(contentUri)

                    //Set Aspect ration
                    .setAspectRatio(1, 1)

                    //Set Activity Title
                    .setActivityTitle(getString(R.string.label_crop_image))
                    .setActivityMenuIconColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //AutoZoom enabled
                    .setAutoZoomEnabled(true)

                    //Allow rotation
                    .setAllowRotation(true)

                    //Check output compression quality
                    .setOutputCompressQuality(100)

                    //Whether guidelines will be shown or not
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setGuidelinesColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //Border Color
                    .setBorderLineColor(ContextCompat.getColor(mContext, R.color.white))
                    .setBorderLineThickness(getResources().getDimension(R.dimen.scale_3dp))

                    //Border Corner Color
                    .setBorderCornerColor(ContextCompat.getColor(mContext, R.color.white))

//                    Shape
                    .setCropShape(CropImageView.CropShape.OVAL)

                    .getIntent(mContext);

            startActivityForResult(intent, REQUEST_CODE_CROP_GET_FILE_ADD_PROFILE);
        } else if (requestCode == Utility.REQUEST_CODE_CROP_GET_FILE_ADD_PROFILE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                String selectedImagePath = MediaUtility.getPath(mContext, resultUri);
                Log.i(TAG, "onActivityResult: Path:" + selectedImagePath);
                //Load the image from Glide
                loadImage(selectedImagePath);

                HashMap<String, File> mFileParams = new HashMap<>();
                mFileParams.put(NetworkUtility.TAGS.PROFILE_IMAGE, new File(selectedImagePath));
                callUpdateProfileWS(null, mFileParams);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.i(TAG, "onActivityResult: Crop Error" + error.toString());
            }
        } else if (requestCode == Utility.REQUEST_CODE_GET_FILE_ADD_COVER_GALLERY && resultCode == RESULT_OK) {
            Intent intent = CropImage
                    .activity(data.getData())
//                    .setOutputUri(contentUri)

                    //Set Aspect ration
                    .setAspectRatio(Utility.X_RATIO, Utility.Y_RATIO)

                    //Set Activity Title
                    .setActivityTitle(getString(R.string.label_crop_image))
                    .setActivityMenuIconColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //AutoZoom enabled
                    .setAutoZoomEnabled(true)

                    //Allow rotation
                    .setAllowRotation(true)

                    //Check output compression quality
                    .setOutputCompressQuality(100)

                    //Whether guidelines will be shown or not
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setGuidelinesColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //Border Color
                    .setBorderLineColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                    .setBorderLineThickness(Utility.convertDpToPixel(3, mContext))

                    //Border Corner Color
                    .setBorderCornerColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    .getIntent(mContext);
            startActivityForResult(intent, Utility.REQUEST_CODE_CROP_GET_FILE_ADD_COVER);
        } else if (requestCode == Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_COVER && resultCode == RESULT_OK) {
//            File photoFile = new File(mCurrentPhotoPath);
            if (photoFile == null) {
                return;
            }
            Log.i(TAG, "onActivityResult: CurrentPath" + photoFile.getAbsolutePath());

            Uri contentUri = FileProvider.getUriForFile(mContext, BuildConfig.FILE_PROVIDER_URL, photoFile);

            Intent intent = CropImage
                    .activity(contentUri)

                    //Set Aspect ration
                    .setAspectRatio(Utility.X_RATIO, Utility.Y_RATIO)

                    //Set Activity Title
                    .setActivityTitle(getString(R.string.label_crop_image))
                    .setActivityMenuIconColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //AutoZoom enabled
                    .setAutoZoomEnabled(true)

                    //Allow rotation
                    .setAllowRotation(true)

                    //Check output compression quality
                    .setOutputCompressQuality(100)

                    //Whether guidelines will be shown or not
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setGuidelinesColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    //Border Color
                    .setBorderLineColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                    .setBorderLineThickness(Utility.convertDpToPixel(3, mContext))

                    //Border Corner Color
                    .setBorderCornerColor(ContextCompat.getColor(mContext, R.color.colorPrimary))

                    .getIntent(mContext);
            startActivityForResult(intent, Utility.REQUEST_CODE_CROP_GET_FILE_ADD_COVER);
        } else if (requestCode == Utility.REQUEST_CODE_CROP_GET_FILE_ADD_COVER) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                String selectedImagePath = MediaUtility.getPath(mContext, resultUri);
                Log.i(TAG, "onActivityResult: Path:" + selectedImagePath);
                loadCoverImage(selectedImagePath);

                HashMap<String, File> mFileParams = new HashMap<>();
                mFileParams.put(NetworkUtility.TAGS.PROFILE_BANNER, new File(resultUri.getPath()));
                callUpdateProfileWS(null, mFileParams);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.i(TAG, "onActivityResult: Crop Error" + error.toString());
            }
        }
    }


    private void loadImage(String selectedImagePath) {
        GlideUtility.showCircularImageView(mContext, TAG, mBinding.imgProfileNew, selectedImagePath, Utility.DEFAULT_PROFILE_SRC, true);
    }
    private void loadCoverImage(String selectedImagePath) {
        GlideUtility.loadImageView(mContext, mBinding.imgProfileNew, selectedImagePath, R.drawable.icon_profile_img_solid);
    }


    @Override
    public void volleyError(VolleyError error) {
        hideProgressDialog();
        Utility.showSnackBar(error.toString(), mBinding.getRoot());
    }

    @Override
    public void showSpecificMessage(String message) {
        hideProgressDialog();
        Utility.showSnackBar(message, mBinding.getRoot());
    }

    @Override
    public void forceLogout() {

    }

    @Override
    public void onClicked(JSONArray s) {
        if (s.length() >= 3) {
            mBinding.textAddContact.setVisibility(View.GONE);
        } else {
            mBinding.textAddContact.setVisibility(View.VISIBLE);
        }
        WebCallClass.updateEmergencyContactDetail(mContext, ProfileDetailsFragmentnew.this, ProfileDetailsFragmentnew.this, s);
    }


    @Override
    public void getUpdateEmergencyContactResponse(JSONArray emergency_contact) {
        jsonEmergencyContacts = emergency_contact;
    }


}
