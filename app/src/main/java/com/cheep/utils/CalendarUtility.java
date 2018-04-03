package com.cheep.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.cheep.R;
import com.cheep.model.ProviderModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by kruti on 6/3/18.
 */

public class CalendarUtility {

    private static final String TAG = CalendarUtility.class.getSimpleName();

    public static String getDate(long milliSeconds, String dateFormat) {
        String finalDate = "";
        try {
            // Create a DateFormatter object for displaying date in specified format.
            SuperCalendar superCalendar = SuperCalendar.getInstance();
            superCalendar.setTimeInMillis(milliSeconds);
            superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            finalDate = superCalendar.format(dateFormat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalDate;
    }

    public static Date getDate(String dateTime, String fromFormat) {
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fromFormat, Locale.ENGLISH);
        try {
            date = simpleDateFormat.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /*
     * get formated date from Server Date
     * */
    @SuppressLint("SimpleDateFormat")
    public static String getFormatedDate(String dateS, String inputDate, String OutputDate) {
        String shoppingdate = dateS;

        try {
            //7/18/2016 12:00:00 AM

            SimpleDateFormat inputFormat = new SimpleDateFormat(inputDate/*, Locale.US*/);
            SimpleDateFormat outputFormat = new SimpleDateFormat(OutputDate/*, Locale.US*/);
            Date date;

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

    public static String getDateDifference(Context mContext, String date, String taskType) {
        if (TextUtils.isEmpty(date))
            return (taskType.equalsIgnoreCase(Utility.TASK_TYPE.SUBSCRIBED) ? mContext.getString(R.string.cheep_care) : "") + mContext.getString(R.string.format_task_start_time, "");

        String sCurrentDt = com.cheep.firebase.DateUtils.getFormatedDate(Calendar.getInstance().getTime(), Utility.DATE_FORMAT_FULL_DATE);
        Date mFutureDate = com.cheep.firebase.DateUtils.getFormatedDate(date, Utility.DATE_FORMAT_FULL_DATE);
        Date mCurrentDate = com.cheep.firebase.DateUtils.getFormatedDate(sCurrentDt, Utility.DATE_FORMAT_FULL_DATE);
        long diff = (mFutureDate != null ? mFutureDate.getTime() : 0) - (mCurrentDate != null ? mCurrentDate.getTime() : 0);

        String timespan = DateUtils.getRelativeTimeSpanString(mFutureDate != null ? mFutureDate.getTime() : 0, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        LogUtils.LOGD(TAG, "getDateDifference() returned: " + timespan);
        if (diff > 0) {
            return (taskType.equalsIgnoreCase(Utility.TASK_TYPE.SUBSCRIBED) ? mContext.getString(R.string.cheep_care) : "") + mContext.getString(R.string.format_task_start_time, timespan);
        } else {
            return (taskType.equalsIgnoreCase(Utility.TASK_TYPE.SUBSCRIBED) ? mContext.getString(R.string.cheep_care) : "") +
                    mContext.getString(R.string.format_task_start_soon);
        }
    }

    public static String get2HourTimeSlots(String timeStamp) {
        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
        try {
            superCalendar.setTimeInMillis(Long.parseLong(timeStamp));

            superCalendar.setLocaleTimeZone();
//        String task_original_date_time = superCalendar.format(Utility.DATE_FORMAT_DD_MMM + " " + Utility.DATE_FORMAT_HH_MM_AM);

            Date d = superCalendar.getCalendar().getTime();

            SimpleDateFormat timeFormatter = new SimpleDateFormat(Utility.TIME_FORMAT_24HH_MM);
            String fromHour = timeFormatter.format(d);
            SuperCalendar superCalendarToDate = SuperCalendar.getInstance();
            superCalendarToDate.setTimeInMillis(superCalendar.getCalendar().getTimeInMillis());
            superCalendarToDate.getCalendar().add(Calendar.HOUR_OF_DAY, 2);
            Date toDate = superCalendarToDate.getCalendar().getTime();
            String toHour = timeFormatter.format(toDate);

            return fromHour + " hrs - " + toHour + " hrs";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String get2HourTimeSlotsForPastTaskScreen(String timeStamp) {
        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
        try {
            superCalendar.setTimeInMillis(Long.parseLong(timeStamp));

            superCalendar.setLocaleTimeZone();
//        String task_original_date_time = superCalendar.format(Utility.DATE_FORMAT_DD_MMM + " " + Utility.DATE_FORMAT_HH_MM_AM);

            Date d = superCalendar.getCalendar().getTime();

            SimpleDateFormat timeFormatter = new SimpleDateFormat(Utility.TIME_FORMAT_24HH_MM);
            String fromHour = timeFormatter.format(d);
            SuperCalendar superCalendarToDate = SuperCalendar.getInstance();
            superCalendarToDate.setTimeInMillis(superCalendar.getCalendar().getTimeInMillis());
            superCalendarToDate.getCalendar().add(Calendar.HOUR_OF_DAY, 2);
            Date toDate = superCalendarToDate.getCalendar().getTime();
            String toHour = timeFormatter.format(toDate);

            return "between " + fromHour + " hrs to " + toHour + " hrs";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String fetchMessageFromDateOfMonth(Context context, int day, SuperCalendar
            superStartDateTimeCalendar, ProviderModel providerModel) {
        String date;
        String DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_TH = SuperCalendar.SuperFormatter.DATE + context.getString(R.string.label_th_date) + SuperCalendar.SuperFormatter.MONTH_JAN;
        String DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_ST = SuperCalendar.SuperFormatter.DATE + context.getString(R.string.label_st_date) + SuperCalendar.SuperFormatter.MONTH_JAN;
        String DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_RD = SuperCalendar.SuperFormatter.DATE + context.getString(R.string.label_rd_date) + SuperCalendar.SuperFormatter.MONTH_JAN;
        String DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_ND = SuperCalendar.SuperFormatter.DATE + context.getString(R.string.label_nd_date) + SuperCalendar.SuperFormatter.MONTH_JAN;

        if (day >= 11 && day <= 13) {
            date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_TH);
        } else {
            switch (day % 10) {
                case 1:
                    date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_ST);
                    break;
                case 2:
                    date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_ND);
                    break;
                case 3:
                    date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_RD);
                    break;
                default:
                    date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_TH);
                    break;
            }
        }
        // as per  24 hour format 13 spt 2017
//        String DATE_FORMAT_TASK_HAS_BEEN_PAID_TIME = SuperCalendar.SuperFormatter.HOUR_12_HOUR_2_DIGIT + ":" + SuperCalendar.SuperFormatter.MINUTE + "' '" + SuperCalendar.SuperFormatter.AM_PM;
//        String time = superStartDateTimeCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);

        // set time format 24 hours
        Date d = superStartDateTimeCalendar.getCalendar().getTime();

        SimpleDateFormat timeFormatter = new SimpleDateFormat(Utility.TIME_FORMAT_24HH_MM);
        String fromHour = timeFormatter.format(d);
        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeInMillis(superStartDateTimeCalendar.getCalendar().getTimeInMillis());
        superCalendar.getCalendar().add(Calendar.HOUR_OF_DAY, 2);

        Date toDate = superCalendar.getCalendar().getTime();
        String toHour = timeFormatter.format(toDate);

        String message = context.getString(R.string.desc_task_payment_done_acknowledgement
                , providerModel.userName, date + context.getString(R.string.label_between) + fromHour + " hrs - " + toHour + " hrs");
        message = message.replace(".", "");
//        message = message.replace(getString(R.string.label_am_caps), getString(R.string.label_am_small)).replace(getString(R.string.label_pm_caps), getString(R.string.label_pm_small));
        return message + ".";
    }

    public static String getDateStringWithSuffic(int i) {
        int j = i % 10, k = i % 100;
        if (j == 1 && k != 11) {
            return i + "st";
        }
        if (j == 2 && k != 12) {
            return i + "nd";
        }
        if (j == 3 && k != 13) {
            return i + "rd";
        }
        return i + "th";
    }
}
