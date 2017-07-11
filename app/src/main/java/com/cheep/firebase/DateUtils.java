package com.cheep.firebase;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by sanjay on 26/7/16.
 */
public class DateUtils
{
    private static final String TAG = DateUtils.class.getSimpleName() ;

    public static String DATE_FORMATE_CHAT_HEADER="dd MMMM,yyyy";
    public static String DATE_FORMATE_CHAT_TIME="hh:mm aa";
    public static String DATE_FORMATE_CHAT_HH_MM_SS="hh:mm:ss";
    public static String DATE_FORMATE_CHAT_HH_MM_AA="hh:mm aa";
    public static String DATE_FORMAT_dd_MM_yyyy="dd/MM/yyyy";
    public static String DATE_FORMAT_BIRTH_DATE="yyyy-MM-dd";
    public static String DATE_FORMAT_YYYY_MM_DD="yyyy-MM-dd";
    public static String DATE_FORMAT_TOURNAMENT_DATE="MMM dd,yyyy";

    /**
     * Return date in specified format.
     *
     * @param milliSeconds Date in milliseconds
     * @param dateFormat   Date format
     * @return String representing date in specified format
     */
    public static String getFormatedDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat/*, Locale.getDefault()*/);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static Date getFormatedDate(String date, String dateFormat)
    {
        if(!TextUtils.isEmpty(date))
        {
            // Create a DateFormatter object for displaying date in specified format.
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat/*,Locale.US*/);
            try
            {
                return formatter.parse(date);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getFormatedDate(String date, String sourceFormat,String destFormat)
    {
        if(!TextUtils.isEmpty(date))
        {
            // Create a DateFormatter object for displaying date in specified format.
            SimpleDateFormat sdfSource = new SimpleDateFormat(sourceFormat/*,Locale.US*/);
            SimpleDateFormat sdfDest = new SimpleDateFormat(destFormat/*,Locale.US*/);
            try
            {
                Date dt= sdfSource.parse(date);
                if(dt!=null)
                {
                    return sdfDest.format(dt);
                }
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * conver date object into string
     * @param date specify date object
     * @param dateFormat specify required date format
     * @return return converted date
     */
    public static String getFormatedDate(Date date, String dateFormat)
    {
        if(date!=null)
        {
            // Create a DateFormatter object for displaying date in specified format.
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat/*,Locale.US*/);

            // Create a calendar object that will convert the date and time value in milliseconds to date.
            return formatter.format(date);
        }
        return "";
    }

    public static long getCurrentDateTime()
    {
        return Calendar.getInstance().getTimeInMillis();
    }

    /**
     * This method is used to get Date object from specified milliseconds and format.
     * @param milliSeconds specify value of date in milliseconds.
     * @param format specify destination date format.
     * @return return the Date object of specified date format.
     */
    public static Date getDate(long milliSeconds, String format)
    {
        SimpleDateFormat formatter = new SimpleDateFormat(format/*,Locale.US*/);
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        String dateString=formatter.format(calendar.getTime());
        try
        {
           return formatter.parse(dateString);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method is used to compare two date
     * @param date1 Pass value of first date
     * @param date2 Pass value of second date
     * @return return true if both date are equal. Otherwise return false.
     */
    public static Boolean isEqual(long date1, long date2)
    {
        Date dt1=getDate(date1,DATE_FORMAT_dd_MM_yyyy);
        Date dt2=getDate(date2,DATE_FORMAT_dd_MM_yyyy);
        if(dt1!=null && dt2!=null)
        {
            if(dt1.compareTo(dt2)==0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Used to check if timestamp is yesterday date
     * @param timestamp
     */
    public static Boolean isYesterdayDate(long timestamp)
    {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        if(isEqual(cal.getTimeInMillis(),timestamp))
        {
            return true;
        }
        return false;
    }

    // Used to convert 24hr format to 12hr format with AM/PM values
    public static String getFormatedTime(int hours, int mins) {

        String timeSet = "";
        if (hours > 12) {
            hours -= 12;
            timeSet = "PM";
        } else if (hours == 0) {
            hours += 12;
            timeSet = "AM";
        } else if (hours == 12)
            timeSet = "PM";
        else
            timeSet = "AM";

        String minutes = "";
        if (mins < 10)
            minutes = "0" + mins;
        else
            minutes = String.valueOf(mins);

        // Append in a StringBuilder
        String aTime = String.valueOf(hours) + ':' +
                minutes + " " + timeSet;
        return aTime;
    }
}
