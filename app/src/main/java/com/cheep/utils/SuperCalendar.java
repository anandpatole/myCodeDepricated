package com.cheep.utils;

import android.text.TextUtils;
import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by pankaj on 11/27/16.
 */

public class SuperCalendar {
    Calendar mCalendar;
    private static SuperCalendar superCalendar;
    TimeZone mTimeZone;

    private SuperCalendar() {
        mCalendar = Calendar.getInstance();
        mTimeZone = mCalendar.getTimeZone();
    }

    public static SuperCalendar getInstance() {
        superCalendar = new SuperCalendar();
        return superCalendar;
    }

    //========
    public Calendar getCalendar() {
        return mCalendar;
    }

    public void setCalendar(Calendar calendar) {
        this.mCalendar = calendar;
        this.mTimeZone = calendar.getTimeZone();
    }

    public void setTime(Date date) {

        mCalendar.setTime(date);
    }

    public void setTimeInMillis(long millis) {
        mCalendar.setTimeInMillis(millis);
    }

    public void setLocaleTimeZone() {
        mTimeZone = TimeZone.getDefault();
        mCalendar.setTimeZone(mTimeZone);
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = TimeZone.getTimeZone(timeZone);
        mCalendar.setTimeZone(mTimeZone);
    }

    public void setTimeInString(String dateTime, String formatOfThisDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatOfThisDate, Locale.US);
        mCalendar.setTime(simpleDateFormat.parse(dateTime));
        mCalendar.setTimeZone(mTimeZone);
    }

    public String format(String formatDate) {
        return DateFormat.format(formatDate, mCalendar).toString();
    }

    public long getTimestampFromDate(String date, String dateFormat) {
        if (!TextUtils.isEmpty(date)) {
            // Create a DateFormatter object for displaying date in specified format.
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat,Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                Date dt = formatter.parse(date);
                if (dt != null) {
                    return (dt.getTime() / 1000);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public SuperCalendar getWeekStartDate() {
        try {
            SuperCalendar superCalendar = clone();
            superCalendar.mCalendar.set(Calendar.DAY_OF_WEEK, 1);
            return superCalendar;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return this;
    }

    public SuperCalendar getNext3HoursTime() {
        try {
            SuperCalendar superCalendar = clone();
            superCalendar.mCalendar.add(Calendar.HOUR, 3);
            return superCalendar;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return this;
    }

    public SuperCalendar getWeekEndDate() {
        try {
            SuperCalendar superCalendar = clone();
            superCalendar.mCalendar.set(Calendar.DAY_OF_WEEK, 7);
            return superCalendar;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return this;
    }


    public long getTimeInMillis() {
        return mCalendar.getTimeInMillis();
    }

    //Override to string
    @Override
    public String toString() {
        return mCalendar.get(Calendar.DATE) + "/" + (mCalendar.get(Calendar.MONTH) + 1) + "/" + mCalendar.get(Calendar.YEAR) + " " + mCalendar.get(Calendar.HOUR_OF_DAY) + ":" + mCalendar.get(Calendar.MINUTE) + ":" + mCalendar.get(Calendar.SECOND) + ":" + mCalendar.get(Calendar.MILLISECOND);
    }

    @Override
    protected SuperCalendar clone() throws CloneNotSupportedException {
        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.mCalendar = (Calendar) mCalendar.clone();
        superCalendar.mCalendar.setTimeZone(mCalendar.getTimeZone());
        return superCalendar;
    }

    public void set(int field, int value) {
        mCalendar.set(field, value);
    }

    public interface SuperTimeZone {
        interface GMT {
            public final String GMT = "GMT";
        }

        interface IST {
            public final String ASIA_CALCUTTA = "Asia/Calcutta";
        }
    }


    public static interface SuperFormatter {
        //COMMON FORMATS, DONT CHANGE THE COMMON FORMATS
        public static final String HOUR_24_HOUR = "HH";
        public static final String HOUR_12_HOUR_2_DIGIT = "hh";
        public static final String HOUR_12_HOUR_1_DIGIT = "h";
        public static final String MINUTE = "mm";
        public static final String SECONDS = "ss";
        public static final String MILLISECONDS = "SSS";

        public static final String AM_PM = "a";

        public static final String DATE = "dd";

        public static final String MONTH_JAN = "MMM";
        public static final String MONTH_JANUARY = "MMMM";
        public static final String MONTH_NUMBER = "MM";

        public static final String YEAR_4_DIGIT = "yyyy";
        public static final String YEAR_2_DIGIT = "yy";

        public static final String WEEK_SUNDAY = "EEEE";
        public static final String WEEK_SUN = "EEE";

        public static final String PAYMENT_DATE = "yyyy-MM-dd hh:mm:ss";
        public static final String FULL_DATE = "yyyy-MM-dd hh:mm:ss aa";
    }
}
