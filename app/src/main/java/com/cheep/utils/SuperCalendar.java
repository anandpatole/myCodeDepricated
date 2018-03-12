package com.cheep.utils;

import android.text.TextUtils;
import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by pankaj on 11/27/16.
 */

public class SuperCalendar {
    Calendar mCalendar;
    private static SuperCalendar superCalendar;
    TimeZone mTimeZone;
    private static final String TAG = "SuperCalendar";

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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatOfThisDate/*, Locale.US*/);
        mCalendar.setTime(simpleDateFormat.parse(dateTime));
        mCalendar.setTimeZone(mTimeZone);
    }

    public String format(String formatDate) {
        return DateFormat.format(formatDate, mCalendar).toString();
    }


    public long getTimestampFromDate(String date, String dateFormat) {
        if (!TextUtils.isEmpty(date)) {
            // Create a DateFormatter object for displaying date in specified format.
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat/*,Locale.US*/);
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

    /**
     * as per new logic in cheep care
     * <p>
     * if selected day is weekday then we need to calculate 3 hours difference
     * for weekends it would be 6 hours.
     *
     * @param isWeekend
     * @return
     */
    public SuperCalendar getNext3HoursTime(boolean isWeekend) {
        try {

            int minHourDiffWeekend = Integer.valueOf(Utility.MIN_HOUR_DIFFERENCE_WEEKEND);
            int minHourDiffWeekDay = Integer.valueOf(Utility.MIN_HOUR_DIFFERENCE_WEEKDAY);

            SuperCalendar superCalendar = clone();
            superCalendar.mCalendar.add(Calendar.HOUR, isWeekend ? minHourDiffWeekend : minHourDiffWeekDay);

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

    public boolean isWorkingHour(String startTime, String endTime) {
        SuperCalendar startDate = SuperCalendar.getInstance();
        startDate.setTime(CalendarUtility.getDate(startTime, Utility.DATE_FORMAT_HH_MM_SS));
        startDate.mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DATE));

        SuperCalendar endDate = SuperCalendar.getInstance();
        endDate.setTime(CalendarUtility.getDate(endTime, Utility.DATE_FORMAT_HH_MM_SS));
        endDate.mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DATE));

        LogUtils.LOGE(TAG, "initiateUI: date" + toString());
        LogUtils.LOGE(TAG, "initiateUI: start date" + startDate.toString());
        LogUtils.LOGE(TAG, "initiateUI: end date" + endDate.toString());


        boolean isWorkingHour = mCalendar.after(startDate.mCalendar) && mCalendar.before(endDate.mCalendar);
        LogUtils.LOGE(TAG, "initiateUI: isWorkingHour " + isWorkingHour);

        return isWorkingHour;
    }

    public interface SuperTimeZone {
        interface GMT {
            public final String GMT = "GMT";
        }

        interface IST {
            public final String ASIA_CALCUTTA = "Asia/Calcutta";
        }
    }


    public interface SuperFormatter {
        //COMMON FORMATS, DONT CHANGE THE COMMON FORMATS
        String HOUR_24_HOUR = "HH";
        //        public static final String HOUR_12_HOUR_2_DIGIT = "hh";
//        public static final String HOUR_12_HOUR_1_DIGIT = "h";
        String MINUTE = "mm";
        String SECONDS = "ss";
        String MILLISECONDS = "SSS";

//        public static final String AM_PM = "a";

        String DATE = "dd";

        String MONTH_JAN = "MMM";
        String MONTH_JANUARY = "MMMM";
        String MONTH_NUMBER = "MM";

        String YEAR_4_DIGIT = "yyyy";
        String YEAR_2_DIGIT = "yy";

        String WEEK_SUNDAY = "EEEE";
        String WEEK_SUN = "EEE";

        String PAYMENT_DATE = "yyyy-MM-dd hh:mm:ss";
        String FULL_DATE = "yyyy-MM-dd hh:mm:ss aa";
    }
}
