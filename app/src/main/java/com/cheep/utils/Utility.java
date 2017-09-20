package com.cheep.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.activity.HomeActivity;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mixpanel.android.java_websocket.util.Base64;

import org.cryptonode.jncryptor.AES256JNCryptor;
import org.cryptonode.jncryptor.CryptorException;
import org.cryptonode.jncryptor.JNCryptor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.cheep.utils.SuperCalendar.SuperFormatter;


/**
 * Created by pankaj on 9/26/16.
 * common utilities methods
 */

public class Utility {
    public static final class GUEST_STATIC_INFO {
        public static final String USERNAME = "Guest";
    }

    public static final String STATIC_LAT = "19.1363246";
    public static final String STATIC_LNG = "72.82766";

    private static final String TAG = "Utility";
    public static final String EMPTY_STRING = "";
    public static final String ZERO_STRING = "0";
    public static final String ONE_CHARACTER_SPACE = " ";
    public static final String DEFAULT_LOCATION = "0.0";
    public static final String ACTION_NEW_JOB_CREATE = "action_new_job_create";
    public static final String ACTION_HIRE_PROVIDER = "action_hire_provider";
    public static final String ACTION_HIRE_PROVIDER_WITH_REFRESHING_TASK_DETAILS = "action_hire_provider_with_refreshing_task_details";

    public static final String ACTION_REGISTER = "action_register";
    public static final String ACTION_LOGIN = "action_login";
    public static final String ACTION_CHANGE_PHONE_NUMBER = "action_change_phone_number";

    //Date Formats
    public static final String DATE_FORMAT_DD_MM_YY = SuperFormatter.DATE + "/" + SuperFormatter.MONTH_NUMBER + "/" + SuperFormatter.YEAR_4_DIGIT;
    public static final String DATE_FORMAT_DD_MMM = SuperFormatter.DATE + " " + SuperFormatter.MONTH_JAN;
    public static final String DATE_FORMAT_DD_MMM_YYYY = SuperFormatter.DATE + " " + SuperFormatter.MONTH_JAN + " " + SuperFormatter.YEAR_4_DIGIT;
    //TODO :: commented on 13 sept 2017 as per 24 hours formation
//    public static final String DATE_FORMAT_HH_MM_AM = SuperFormatter.HOUR_12_HOUR_2_DIGIT + ":" + SuperFormatter.MINUTE + " " + SuperFormatter.AM_PM;
//    public static final String DATE_FORMAT_DD_MMM_HH_MM_AM = SuperFormatter.DATE + " " + SuperFormatter.MONTH_JAN + " " + SuperFormatter.HOUR_12_HOUR_2_DIGIT + ":" + SuperFormatter.MINUTE + "" + SuperFormatter.AM_PM;
//    public static final String DATE_FORMAT_TASK_HAS_BEEN_PAID_TIME = SuperFormatter.HOUR_12_HOUR_2_DIGIT + ":" + SuperFormatter.MINUTE + "" + SuperFormatter.AM_PM;

    public static final String DATE_FORMAT_HH_MM_AM = SuperFormatter.HOUR_24_HOUR + ":" + SuperFormatter.MINUTE + " ";
    public static final String DATE_FORMAT_DD_MMM_HH_MM_AM = SuperFormatter.DATE + " " + SuperFormatter.MONTH_JAN + " " + SuperFormatter.HOUR_24_HOUR + ":" + SuperFormatter.MINUTE + "";
    public static final String DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE = SuperFormatter.DATE + " " + SuperFormatter.MONTH_JAN;
    public static final String DATE_FORMAT_TASK_HAS_BEEN_PAID_TIME = SuperFormatter.HOUR_24_HOUR + ":" + SuperFormatter.MINUTE + "";
    public static final String DATE_TIME_FORMAT_SERVICE_YEAR = SuperFormatter.YEAR_4_DIGIT + "-" + SuperFormatter.MONTH_NUMBER + "-" + SuperFormatter.DATE + " " + SuperFormatter.HOUR_24_HOUR + ":" + SuperFormatter.MINUTE + ":" + SuperFormatter.SECONDS;
    //    dd MMMM, HH:mm a
    public static final String DATE_TIME_DD_MMMM_HH_MM = SuperFormatter.DATE + " " + SuperFormatter.MONTH_JANUARY + " " + SuperFormatter.HOUR_24_HOUR + ":" + SuperFormatter.MINUTE + "";
    public static final String DATE_FORMAT_FULL_DATE = SuperFormatter.FULL_DATE;

    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PHONE_MIN_LENGTH = 10;

    public static final int DEFAULT_PROFILE_SRC = R.drawable.icon_profile_img_solid;
    public static final int DEFAULT_CHEEP_LOGO = R.drawable.ic_cheep_circular_icon;
//    public static final String DEFAULT_PROFILE_URL = "http://msz.uniklinikum-dresden.de/zkn/images/mitarbeiter/dummy120_dagobert83_female_user_icon.png";//"http://lorempixel.com/200/200/people/";

    public static final String LOCALE_FOR_HINDI = "hi";
    public static final String LOCALE_FOR_ENGLISH = "en";

    public static final String ACTION_CALL = "call";
    public static final String ACTION_CHAT = "chat";
    /*
    * chat types
    * */
    public static final String CHAT_TYPE_DATE = "date";
    public static final String CHAT_TYPE_MESSAGE = "message";
    public static final String CHAT_TYPE_MEDIA = "media";
    public static final String CHAT_TYPE_MONEY = "money";

    public static final int REQUEST_CODE_PERMISSION_LOCATION = 101;
    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 102;
    public static final int REQUEST_CODE_GET_FILE = 103;
    public static final int REQUEST_CODE_GET_FILE_ADD_COVER = 104;
    public static final int REQUEST_CODE_GET_FILE_ADD_PROFILE = 105;
    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_COVER = 106;


    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE = 107;
    public static final int REQUEST_CODE_CROP_GET_FILE_ADD_PROFILE = 110;
    public static final int REQUEST_CODE_CHANGE_LOCATION = 108;
    public static final int REQUEST_CODE_GET_FILE_ADD_PHOTO = 111;
    public static final int REQUEST_CODE_CHECK_LOCATION_SETTINGS = 112;

    public static final int REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE = 113;
    public static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_ADD_PROFILE_CAMERA = 114;
    public static final int REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY = 115;
    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY = 116;
    public static final int PLACE_PICKER_REQUEST = 117;
    public static final int REQUEST_CODE_ADD_PROFILE_CAMERA = 118;
    public static final int REQUEST_CODE_VIDEO_CAPTURE = 119;
    public static final int REQUEST_CODE_GET_VIDEO_GALLERY = 120;

    /*
    * @Sanjay
    * Chat image chooser constants
    * */
    public static final int REQUEST_CODE_IMAGE_CAPTURE_CHAT_MEDIA = 121;
    public static final int REQUEST_CODE_GET_FILE_CHAT_MEDIA_GALLERY = 122;
    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_CHAT_MEDIA = 123;
    public static final int REQUEST_CODE_GET_FILE_CHAT_MEDIA = 124;
    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_CHAT_MEDIA_GALLERY = 125;
    public static final int REQUEST_CODE_CHAT_MEDIA_CAMERA = 126;

    public static final int REQUEST_CODE_IMAGE_CAPTURE_ADD_COVER = 127;
    public static final int REQUEST_CODE_GET_FILE_ADD_COVER_GALLERY = 128;
    public static final int REQUEST_CODE_CROP_GET_FILE_ADD_COVER = 129;


    public static final int REQUEST_START_PAYMENT = 200;
    public static final int ADDITIONAL_REQUEST_START_PAYMENT = 300;

    public static final String REMOVE = "remove";
    public static final String ADD = "add";

    //Different Types of Braoadcast Actions
    public static final String BR_ON_TASK_CREATED = "com.cheep.ontaskcreated";
    public static final String BR_ON_TASK_CREATED_FOR_INSTA_BOOKING = "com.cheep.ontaskcreated.instabooking";
    public static final String BR_ON_TASK_CREATED_FOR_STRATEGIC_PARTNER = "com.cheep.ontaskcreated.strategicpartner";
    public static final String BR_NEW_TASK_ADDED = "com.cheep.newtask.added";
    public static final String BR_ON_LOGIN_SUCCESS = "com.cheep.login.success";
    public static final int CHAT_PAGINATION_RECORD_LIMIT = 20;

    public static final int X_RATIO = 16;
    public static final int Y_RATIO = 9;
    public static final String DEBUG = "debug";

    /*
    Home Screen Category Image Ratio
     */
    public static final float CATEGORY_IMAGE_RATIO = (float) 2.3972;


    /**
     * Regular expression for mobile number
     */
    public static final String MOBILE_REGREX = "(((\\+*)((0[ -]+)*|(91[- ]+)*)(\\d{12}+|\\d{10}+))|\\d{5}([- ]*)\\d{6})";
//    public static final String MOBILE_REGREX_IOS = "\"(?\\+\\d\\d\\s+)?((?:\\(\\d\\d\\)|\\d\\d)\\s+)?)(\\d{4,10}\\-?\\d{4})\"";

    /**
     * ongoing user chatId
     */
    public static String CURRENT_CHAT_ID = "";
    public static Boolean IS_FROM_NOTIFICATION = false;

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.length() == Utility.PHONE_MIN_LENGTH;
    }

    public static int parseInt(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getJobs(Context context, String integer) {
        if (parseInt(integer) == 0)
            return context.getString(R.string.label_x_jobs, "No");
        else if (Utility.parseInt(integer) == 1)
            return context.getString(R.string.label_x_job, integer);
        else
            return context.getString(R.string.label_x_jobs, integer);
    }

    public static String getTasks(Context context, String integer) {
        if (parseInt(integer) == 0)
            return context.getString(R.string.label_x_tasks, "No");
        else if (Utility.parseInt(integer) == 1)
            return context.getString(R.string.label_x_task, integer);
        else
            return context.getString(R.string.label_x_tasks, integer);
    }

    public static String getReviews(Context context, String integer) {
        if (parseInt(integer) == 0)
            return context.getString(R.string.label_x_reviews, "No");
        else if (Utility.parseInt(integer) == 1)
            return context.getString(R.string.label_x_review, integer);
        else
            return context.getString(R.string.label_x_reviews, integer);
    }

    public static String getComments(Context context, String integer) {
        if (parseInt(integer) == 0)
            return context.getString(R.string.label_no_comments_yet);
        else if (Utility.parseInt(integer) == 1)
            return context.getString(R.string.label_x_comment, integer);
        else
            return context.getString(R.string.label_x_comments, integer);
    }

    /**
     * return price1 if it is not blank else return price2;
     *
     * @param price1
     * @param price2
     * @return
     */
    public static String getActualPrice(String price1, String price2) {
        if (TextUtils.isEmpty(price1))
            return price2;
        return price1;
    }

    public static void showRating(String rating, RatingBar ratingBar) {
        try {
            ratingBar.setRating(Float.parseFloat(rating));
        } catch (Exception e) {
            ratingBar.setRating(0);
            e.printStackTrace();
        }
    }

    public static String getUniqueTagForNetwork(Context context, String url) {
        return url + "_" + System.identityHashCode(context);
    }

    public static void setSwipeRefreshLayoutColors(SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.splash_gradient_start, R.color.splash_gradient_end);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method would do AES Encryption while doing Payment
     *
     * @param orgString : Original String
     * @return : Encrypted String
     */
    public static String applyAESEncryption(String orgString) {
        JNCryptor cryptor = new AES256JNCryptor();
        byte[] plaintext = orgString.getBytes();
        String password = BuildConfig.X_API_KEY;

        // Encrype it
        byte[] encryptedtextInBytes = null;
        try {
            encryptedtextInBytes = cryptor.encryptData(plaintext, password.toCharArray());
        } catch (CryptorException e) {
            // Something went wrong
            e.printStackTrace();
        }
        if (encryptedtextInBytes == null) {
            return EMPTY_STRING;
        }
        String encryptedText = Base64.encodeBytes(encryptedtextInBytes);
        Log.i("TAG", "applyAESEncryption: Encrypted Text" + encryptedText);

        // Decrypt it
       /* byte[] decryptedTextInBytes = null;
        try {
            decryptedTextInBytes = cryptor.decryptData(encryptedText.getBytes(), password.toCharArray());
        } catch (CryptorException e) {
            // Something went wrong
            e.printStackTrace();
        }

        if (decryptedTextInBytes != null) {
            String decryptedText = new String(decryptedTextInBytes);
            Log.i("TAG", "applyAESEncryption: decrypted Text" + decryptedText);
        }*/

        return encryptedText;
    }

    /*public static int getHeightFromWidthForSixteenNineRatio(int width) {
        Log.d(TAG, "getHeightFromWidthForSixteenNineRatio() called with: width = [" + width + "]");
        Log.d(TAG, "getHeightFromWidthForSixteenNineRatio() returned: " + ((width * 9) / 16));
        return ((width * 9) / 16);
    }*/

    public static int getHeightFromWidthForTwoOneRatio(int width) {
//        Log.d(TAG, "getHeightFromWidthForTwoOneRatio() called with: width = [" + width + "]");
//        Log.d(TAG, "getHeightFromWidthForTwoOneRatio() returned: " + (width / 2));
        return (width / 2);
    }

    /**
     * Below would provide dynamic height of image based on #Utility.CATEGORY_IMAGE_RATIO
     *
     * @param width
     * @return
     */
    public static int getHeightCategoryImageBasedOnRatio(int width) {
//        Log.d(TAG, "getHeightCategoryImageBasedOnRatio() called with: width = [" + width + "]");
//        Log.d(TAG, "getHeightCategoryImageBasedOnRatio() called with: width = [" + Math.round((width / CATEGORY_IMAGE_RATIO)) + "]");
        return Math.round((width / CATEGORY_IMAGE_RATIO));
    }

    public static String getUniqueTransactionId() {
        return String.valueOf(System.currentTimeMillis());
    }


    public static final String NO_INTERNET_CONNECTION = "Hey, we see a problem with your internet" + new String(Character.toChars(0x1f914)) + ". We\'ll wait while you check your connection and try again";

    //Bundle Extra parameters
    public static class Extra {
        public static final String WHICH_FRAG = "which_frag";
        public static final String USER_DETAILS = "userdetails";
        public static final String INFO_TYPE = "infoType";
        public static final String DATA = "DATA";
        public static final String IS_FIRST_TIME = "isFirstTime";
        public static final String DATA_2 = "DATA_2";
        public static final String PASSWORD = "password";
        public static final String SELECTED_IMAGE_PATH = "selectedImagePath";
        public static final String CORRECT_OTP = "correctOTP";
        public static final String CATEGORY = "category";
        public static final String PHONE_NUMBER = "phone_number";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String CITY_NAME = "city_name";
        public static final String SESSION_EXPIRE = "session_expire";
        public static final String IMAGE_URL = "image_url";
        public static final String ACTION = "action";
        public static final String USER_NAME = "user_name";
        public static final String ADDRESS_ID = "address_id";
        public static final String ADDRESS_TEXT = "address_text";
        public static final String CATEGORY_ID = "category_id";
        public static final String IS_DATE_SELECTED = "is_date_selected";
        public static final String DATE_TIME = "date_time";
        public static final String TASK_DETAIL = "task_detail";
        public static final String TASK_STATUS = "task_status";
        public static final String TASK_DETAIL_MODEL = "task_detail_model";
        public static final String TASK_ID = "task_id";
        public static final String PAYMENT_VIEW = "payment_view";
        public static final String PAYMENT_VIEW_IS_ADDITIONAL_CHARGE = "isAdditional";

        public static final String CHAT_NOTIFICATION_DATA = "chat_notification_data";
        public static final String PROFILE_FROM_FAVOURITE = "from_favorite";
        public static final String FROM_WHERE = "from_where";
        public static final String TASK_TYPE_IS_INSTA = "isInsta";
        public static final String SELECTED_ADDRESS_MODEL = "selectedAddressModel";
        public static final String LOCATION_INFO = "location_info";
        public static final String IS_INSTA_BOOKING_TASK = "isInstaBookingTask";
    }


    /*1 for platinum
    2 for Gold
    3 for Silver
    4 for bronze*/
    public static class PRO_LEVEL {
        public static final String PLATINUM = "1";
        public static final String GOLD = "2";
        public static final String SILVER = "3";
        public static final String BRONZE = "4";
    }

    /**
     * Keyboard related methods for hiding and showing
     */
    public static void hideKeyboard(Context context) {
        Activity activity = (Activity) context;
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            // check if no view has focus:
            View view = activity.getCurrentFocus();
            if (view != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static void hideKeyboard(Context context, AppCompatEditText edt) {
        Activity activity = (Activity) context;
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            // check if no view has focus:
            if (edt != null) {
                edt.clearFocus();
                inputManager.hideSoftInputFromWindow(edt.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static void showKeyboard(Context context, AppCompatEditText edt) {
        Activity activity = (Activity) context;
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            // check if no view has focus:
            if (edt != null) {
                edt.requestFocus();
                inputManager.showSoftInput(edt,
                        InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    //Method to do necessary thing at logout
    public static void logout(Context mContext, boolean isSessionExpire, int action) {
        // Clear the Users Preference Information
        PreferenceUtility.getInstance(mContext).onUserLogout();

        //Redirect user to Home Screen
//        LoginActivity.newInstance(mContext, isSessionExpire, action);
        HomeActivity.newInstance(mContext);
    }

    //Method to get Device width and height in array, [0] = width, [1] = height
    public static int[] getDeviceWidthHeight(Activity activity) {

        int size[] = new int[2];

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            DisplayMetrics displayMetrics = activity.getResources()
                    .getDisplayMetrics();
            size[0] = displayMetrics.widthPixels;
            size[1] = displayMetrics.heightPixels;
        } else {
            Display mDisplay = activity.getWindowManager().getDefaultDisplay();
            size[0] = mDisplay.getWidth();
            size[1] = mDisplay.getHeight();
        }
        return size;
    }

    /**
     * Get Application version name
     *
     * @param context
     * @return
     */
    public static String getApplicationVersion(Context context) {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "0.0.0";
    }


    public static String getDate(long milliSeconds, String dateFormat) {
        String finalDate = "";
        try {
            // Create a DateFormatter object for displaying date in specified format.
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat/*, Locale.US*/);

            // Create a calendar object that will convert the date and time value in milliseconds to date.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(milliSeconds);

            finalDate = formatter.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalDate;
    }

    /*
     * get formated date from Server Date
     * */
    public static String getFormatedDate(String dateS, String inputDate, String OutputDate) {
        String shoppingdate = dateS;

        try {
            //7/18/2016 12:00:00 AM
            String inputPattern = inputDate;
            String outputPattern = OutputDate;

            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern/*, Locale.US*/);
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern/*, Locale.US*/);
            Date date = null;

            try {
                date = inputFormat.parse(dateS);
                shoppingdate = outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return shoppingdate;
    }

    public static String getDateDifference(String date) {
        if (TextUtils.isEmpty(date))
            return "";

        Date mFutureDate = com.cheep.firebase.DateUtils.getFormatedDate(date, Utility.DATE_FORMAT_FULL_DATE);
        String timespan = DateUtils.getRelativeTimeSpanString(mFutureDate.getTime()).toString();
        Log.d(TAG, "getDateDifference() returned: " + timespan);
        return timespan;
        /*String sCurrentDt = DateUtils.getFormatedDate(Calendar.getInstance().getTime(), Utility.DATE_FORMAT_FULL_DATE);
        Date mCurrentDate=DateUtils.getFormatedDate(sCurrentDt,Utility.DATE_FORMAT_FULL_DATE);
        Date mFutureDate=DateUtils.getFormatedDate(date,Utility.DATE_FORMAT_FULL_DATE);
        if(mCurrentDate==null || mFutureDate==null)
            return "";

        long diff = mFutureDate.getTime() - mCurrentDate.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
        int diffInDays = (int) ((mFutureDate.getTime() - mCurrentDate.getTime()) / (1000 * 60 * 60 * 24));
        NumberFormat numberFormat = new DecimalFormat("00");
        if (diffInDays >0)
        {
            return String.format("%d days",diffInDays);
        }
        else
        {
            return String.format("%s:%s hrs",numberFormat.format(diffHours),numberFormat.format(diffMinutes));
        }*/
    }

    public static String getDateDifference(Context mContext, String date) {
        if (TextUtils.isEmpty(date))
            return "";

        String sCurrentDt = com.cheep.firebase.DateUtils.getFormatedDate(Calendar.getInstance().getTime(), Utility.DATE_FORMAT_FULL_DATE);
        Date mFutureDate = com.cheep.firebase.DateUtils.getFormatedDate(date, Utility.DATE_FORMAT_FULL_DATE);
        Date mCurrentDate = com.cheep.firebase.DateUtils.getFormatedDate(sCurrentDt, Utility.DATE_FORMAT_FULL_DATE);
        long diff = mFutureDate.getTime() - mCurrentDate.getTime();

        String timespan = DateUtils.getRelativeTimeSpanString(mFutureDate.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        Log.d(TAG, "getDateDifference() returned: " + timespan);
        if (diff > 0) {
            return mContext.getString(R.string.format_task_start_time, timespan);
        } else {
            return mContext.getString(R.string.format_task_start_soon);
        }
        /*String sCurrentDt = DateUtils.getFormatedDate(Calendar.getInstance().getTime(), Utility.DATE_FORMAT_FULL_DATE);
        Date mCurrentDate=DateUtils.getFormatedDate(sCurrentDt,Utility.DATE_FORMAT_FULL_DATE);
        Date mFutureDate=DateUtils.getFormatedDate(date,Utility.DATE_FORMAT_FULL_DATE);
        if(mCurrentDate==null || mFutureDate==null)
            return "";

        long diff = mFutureDate.getTime() - mCurrentDate.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
        int diffInDays = (int) ((mFutureDate.getTime() - mCurrentDate.getTime()) / (1000 * 60 * 60 * 24));
        NumberFormat numberFormat = new DecimalFormat("00");
        if (diffInDays >0)
        {
            return String.format("%d days",diffInDays);
        }
        else
        {
            return String.format("%s:%s hrs",numberFormat.format(diffHours),numberFormat.format(diffMinutes));
        }*/
    }

    //Loading Circular image from url to imageview
    public static void showCircularImageView(Context context, String tag, ImageView img, String url, int placeholderRes) {
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(url)
                .transform(new CircleTransform(context, url, tag))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    /*public static void showCircularImageView(Context context, ImageView img, int imageToLoad, int placeholderRes) {

        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }*/

    public static void showCircularImageView(Context context, String tag, ImageView img, String imageToLoad, int placeholderRes, boolean isRounded, float strockwidthIndp) {
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context, isRounded, Color.WHITE, (int) Utility.convertDpToPixel(strockwidthIndp, context), imageToLoad, tag))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    public static void showCircularImageView(Context context, String tag, ImageView img, String imageToLoad, int placeholderRes, boolean isRounded) {
//        Log.d(TAG, "showCircularImageView() called with: context = [" + context + "], tag = [" + tag + "], img = [" + img + "], imageToLoad = [" + imageToLoad + "], placeholderRes = [" + placeholderRes + "], isRounded = [" + isRounded + "]");
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context, isRounded, Color.WHITE, 5, imageToLoad, tag))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, Context context) {
//int color, int cornerDips, int borderDips,
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
//
//        final int borderSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) R.dimen.scale_2dp,
//                context.getResources().getDisplayMetrics());
//        final int cornerSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) R.dimen.scale_5dp,
//                context.getResources().getDisplayMetrics());
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        // prepare canvas for transfer
        paint.setAntiAlias(true);
        paint.setColor(0xFFFFFFFF);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, 15, 15, paint);

        // draw bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        // draw border
        paint.setColor(ContextCompat.getColor(context, R.color.grey_varient_1));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) 15);
        canvas.drawRoundRect(rectF, 15, 15, paint);

        return output;
    }


    public static void showCircularImageViewWithColorBorder(Context context, String tag, ImageView img, String imageToLoad, int placeholderRes, int color, boolean isRounded) {
//        Log.d(TAG, "showCircularImageView() called with: context = [" + context + "], tag = [" + tag + "], img = [" + img + "], imageToLoad = [" + imageToLoad + "], placeholderRes = [" + placeholderRes + "], isRounded = [" + isRounded + "]");
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context, isRounded, ContextCompat.getColor(context, color), 5, imageToLoad, tag))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    /*public static void showCircularImageView(Context context, String tag, ImageView img, String imageToLoad, int placeholderRes, boolean isRounded, int cbo) {
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .transform(new CircleTransform(context, isRounded, Color.WHITE, 5, imageToLoad, tag))
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }*/

    public static void loadImageView(Context context, ImageView img, String imageToLoad, int placeholderRes) {

        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    public static void loadImageView(Context context, ImageView img, String imageToLoad) {
        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .crossFade()
                .into(img);
    }

    public static void loadImageView(Context context, ImageView img, int imageToLoad, int placeholderRes) {

        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .crossFade()
                .into(img);
    }

    public static void loadImageView(Context context, ImageView img, String imageToLoad, int placeholderRes, RequestListener requestListener) {

        if (!isActivityCorrectForGlide(context)) {
            return;
        }
        Glide
                .with(context)
                .load(imageToLoad)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .listener(requestListener)
                .crossFade()
                .into(img);
    }

    private static boolean isActivityCorrectForGlide(Context context) {
        if (context instanceof Activity) {
            if (context == null || ((Activity) context).isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && ((Activity) context).isDestroyed())) {
                return false;
            }
        }
        return true;
    }

    /**
     * GSON UTILITY Methods
     */
    private static Gson gson = new GsonBuilder().create();

    public static <T> Object getObjectFromJsonString(String jsonData, Class modelClass) {
        return gson.fromJson(jsonData, modelClass);
    }

    public static String getJsonStringFromObject(Object modelClass) {
        return gson.toJson(modelClass);
    }

    public static <T> String getJsonStringFromObject(List<T> objectArrayList) {
        return gson.toJson(objectArrayList, new TypeToken<List<T>>() {
        }.getType());
    }

    public static <T> ArrayList<T> getObjectListFromJsonString(String jsonData, Class myclass) {
        return new ArrayList<>(Arrays.asList((T[]) gson.fromJson(jsonData, myclass)));
    }

    public static void openCustomerCareCallDialer(Context context) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + context.getString(R.string.custom_care_phone_number)));
        context.startActivity(intent);
    }

    public static void openCustomerCareCallDialer(Context context, String mobileNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mobileNumber));
        context.startActivity(intent);
    }

    /**
     * Check whether device is having google playservice up to date or note
     *
     * @param context Context of activity
     * @return true if device is having google playservice up to date, false if not.
     */
    public static int checkGooglePlayService(Context context) {
        //Check if Google PLayservice is available
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        return googleApiAvailability.isGooglePlayServicesAvailable(context);
    }

    /**
     * Checking whether there is internet connection or not
     *
     * @param mContext The Context
     * @return Whether there is internet connection or not
     */
    public static boolean isConnected(Context mContext) {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static void showSnackBar(String message, View view) {
        Snackbar.make(view, message, 3000).show();
    }

    public static void showToast(Context mContext, String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }


    // copy text to clipboard
    public static void setClipboard(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(context.getString(R.string.label_copied_text), text);
            clipboard.setPrimaryClip(clip);
        }
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                try {
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                } catch (NumberFormatException e) {
                    return null;
                }

            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static final class FILTER_TYPES {
        public static final String FILTER_TYPE_FEATURED = "Featured";
        public static final String FILTER_TYPE_POPULAR = "Popular";
        public static final String FILTER_TYPE_FAVOURITES = "Favourites";
    }

    public static final class BOOLEAN {
        public static final String YES = "yes";
        public static final String NO = "no";
    }

    public static final class BROADCAST_TYPE {
        public static final int UPDATE_FAVOURITE = 1;
        public static final int UPDATE_COMMENT_COUNT = 2;
        public static final int TASK_PAID = 3;
        public static final int TASK_RATED = 4;
        public static final int TASK_CANCELED = 5;
        public static final int NEW_NOTIFICATION = 6;
        public static final int TASK_RESCHEDULED = 7;
        public static final int TASK_PROCESSING = 8;

        // When quote requested by any of the PRO
        public static final int QUOTE_REQUESTED_BY_PRO = 9;

        // When detail request sent by PRO
        public static final int REQUEST_FOR_DETAIL = 10;

        // When Task status changes
        public static final int TASK_STATUS_CHANGE = 11;

        // When additional payment requested
        public static final int ADDITIONAL_PAYMENT_REQUESTED = 12;

        // When detail request getting rejected by User
        public static final int DETAIL_REQUEST_REJECTED = 13;

        // When AnyTask is Created & We need to check for Alert Disable/Enable in @HomeActivity
        public static final int TASK_START_ALERT = 14;

        // When detail request getting accepted by User
        public static final int DETAIL_REQUEST_ACCEPTED = 15;

        //When Payment has been paid by the user, Need to redirect the user to MyTask Screen
        public static final int PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN = 16;
    }

    public static final class REQUEST_TYPE {
        public static final String QUOTE_REQUESTED = "quote";//sp added quote
        public static final String DETAIL_REQUIRED = "detail";//sp requested detailed information
    }

    public final class SEND_TASK_DETAIL_REQUESTED_STATUS {
        public static final String INITIAL = "no";
        public static final String ALREADY_REQUESTED = "pending";
        public static final String ACCEPTED = "accepted";
        public static final String REJECTED = "rejected";
    }

    public static final class PAYMENT_STATUS {
        public static final String PAYMENT_INITIATED = "payment_initiated";//not using it
        public static final String COMPLETED = "completed";
        public static final String FAILED = "failed";
    }

    public static final class TASK_STATUS {
        /**
         * 1->If task created and only quotes is there.
         * 2->Task created and user paid to sp, but sp not started the task yet.
         */
        public static final String PENDING = "pending";

        /**
         * If user Payed and task is in progress
         */
        public static final String PAID = "paid";

        /**
         * If user starts task on my home
         */
        public static final String PROCESSING = "processing";

        /**
         * If user tries to reschdule the task.
         */
        public static final String RESCHEDULE_REQUESTED = "reschedule_requested";

        /**
         * If Task's Reschedule Request has been cancelled by User
         */
        public static final String RESCHEDULE_REQUEST_REJECTED = "reschedule_request_rejected";

        /**
         * If tasks completed by SP
         */
        public static final String COMPLETION_REQUEST = "completion_request";

        /**
         * If tasks completed confirmed by User
         */
        public static final String COMPLETION_CONFIRM = "completion_confirm";

        /**
         * If user starts task on my home
         */
        public static final String CANCELLED_CUSTOMER = "cancel_by_customer";

        /**
         * If user starts task on my home
         */
        public static final String CANCELLED_SP = "cancel_by_sp";

        /**
         * If Task is Disputed
         */
        public static final String DISPUTED = "dispute";

        /**
         * If Task is Elapsed
         */
        public static final String ELAPSED = "elapsed";

        /**
         * If Additional Payment is Requested by SP
         */
        public static final String ADDITIONAL_PAYMENT_REQUESTED = "additional_payment_requested";

    }

    public static final class TASK_TYPE {
        public static final String STRATEGIC = "strategic"; //1->if task created and only quotes is there, 2-> task created and user paid to sp, but sp not started the task yet.
        public static final String NORMAL = "normal";//if user payed and task is in progress
    }

    public static final class STRATEGIC_PARTNER_BRAND {
        public static final String VLCC = "VLCC"; //1->if task created and only quotes is there, 2-> task created and user paid to sp, but sp not started the task yet.
    }

    public static String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static String urlEncodeUTF8(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                    urlEncodeUTF8(entry.getKey().toString()),
                    urlEncodeUTF8(entry.getValue().toString())
            ));
        }
        return sb.toString();
    }

    public class NOTIFICATION_TYPE {
        public static final String QUOTE_REQUEST = "QUOTE_REQUEST";
        public static final String TASK_STATUS_CHANGE = "TASK_STATUS_CHANGE";
        public static final String ADDITIONAL_PAYMENT_REQUESTED = "additional_payment_requested";
        public static final String REQUEST_FOR_DETAIL = "REQUEST_FOR_DETAIL";
        public static final String CHAT_MESSAGE = "FIREBASE";
        public static final String TASK_CREATE = "TASK_CREATE";
        public static final String TASK_START_ALERT = "TASK_START_ALERT";
        public static final String WEB_CUSTOM_NOTIFICATION = "WEB_CUSTOM_NOTIFICATION";
    }

    public static final String SESSION_EXPIRE = "session_expire";

    /**
     * Call CheepHelpline number
     *
     * @param mContext
     */
    public static void initiateCallToCheepHelpLine(Context mContext) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(BuildConfig.CHEEP_HELPLINE_NUMBER));
        mContext.startActivity(intent);
    }

    /**
     * Redirect the user to Playstore
     */
    public static void redirectUserToPlaystore(Context context) {
        // User cancelled the dialog
        Uri uri = Uri.parse(BuildConfig.PLAYSTORE_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (context.getPackageManager().resolveActivity(intent, 0) != null) {
            context.startActivity(intent);
        }
    }

    /**
     * @param p_videoPath path of video file
     * @return returns bitmap thumbnail for video
     * @throws Throwable exception
     */
    public static Bitmap getVideoThumbnail(String p_videoPath)
            throws Throwable {
        Bitmap m_bitmap = null;
        MediaMetadataRetriever m_mediaMetadataRetriever = null;
        try {
            m_mediaMetadataRetriever = new MediaMetadataRetriever();
            m_mediaMetadataRetriever.setDataSource(p_videoPath);
            m_bitmap = m_mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception m_e) {
            throw new Throwable(
                    "Exception in getVideoThumbnail(String p_videoPath)"
                            + m_e.getMessage());
        } finally {
            if (m_mediaMetadataRetriever != null) {
                m_mediaMetadataRetriever.release();
            }
        }
        return m_bitmap;
    }

    /**
     * Tags for Strategic Partner Questionnary screen
     **/
    public static String TEMPLATE_DATE_PICKER = "calendar";
    public static String TEMPLATE_TIME_PICKER = "timepicker";
    public static String TEMPLATE_UPLOAD = "upload";
    public static String TEMPLATE_LOCATION = "location";
    public static String TEMPLATE_TEXT_FIELD = "textbox";
    public static String TEMPLATE_DROPDOWN = "dropdown";


    /**
     * get decimal value to show price
     *
     * @param quotePrice string value
     * @return decimal value
     */
    public static String getQuotePriceFormatter(String quotePrice) {

        if (quotePrice == null || quotePrice.equalsIgnoreCase("null"))
            return "0.00";
        if (quotePrice.equalsIgnoreCase("") || quotePrice.equalsIgnoreCase("0") || quotePrice.equalsIgnoreCase("0.0"))
            return "0.00";
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        double price = Double.parseDouble(quotePrice);
        return formatter.format(price);
    }

  /*  public static String getExperienceString(String exp) {
        try {
            float expFloat = Float.parseFloat(exp);
            if (expFloat > 1) {
                return exp + " Years \nExperience";
            } else {
                return exp + " Year \nExperience";
            }
        } catch (NumberFormatException e) {
            return exp + " Year \nExperience";
        }
    }*/
}
