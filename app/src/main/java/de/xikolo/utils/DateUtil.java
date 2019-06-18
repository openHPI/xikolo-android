package de.xikolo.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.xikolo.config.Config;

public class DateUtil {

    public static final String TAG = DateUtil.class.getSimpleName();

    public static final String XIKOLO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public static boolean nowIsBetween(String from, String to) {
        Date dateBegin = parse(from);
        Date dateEnd = parse(to);
        return nowIsBetween(dateBegin, dateEnd);
    }

    public static boolean nowIsBetween(Date from, Date to) {
        Date dateNow = new Date();

        if (from == null && to == null) {
            return false;
        }
        if (from == null) {
            if (dateNow.before(to)) {
                return true;
            }
        }
        if (to == null) {
            if (dateNow.after(from)) {
                return true;
            }
        }
        return from != null && to != null && dateNow.after(from) && dateNow.before(to);
    }

    public static boolean isPast(String date) {
        return isPast(parse(date));
    }

    public static boolean isPast(Date date) {
        Date dateNow = new Date();
        return date != null && dateNow.after(date);
    }

    public static boolean isFuture(String date) {
        return isFuture(parse(date));
    }

    public static boolean isFuture(Date date) {
        Date dateNow = new Date();
        return date != null && dateNow.before(date);
    }

    public static Date parse(String date) {
        SimpleDateFormat dateFm = new SimpleDateFormat(XIKOLO_DATE_FORMAT, Locale.getDefault());
        Date parsedDate = null;
        try {
            if (date != null) {
                parsedDate = dateFm.parse(date);
            }
        } catch (ParseException e) {
            if (Config.DEBUG)
                Log.w(TAG, "Failed parsing " + date, e);
            parsedDate = null;
        }
        return parsedDate;
    }

    public static String format(Date date) {
        SimpleDateFormat dateFm = new SimpleDateFormat(XIKOLO_DATE_FORMAT, Locale.getDefault());
        return dateFm.format(date);
    }

    public static String formatLocal(Date date) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault());
        dateFormat.setTimeZone(Calendar.getInstance().getTimeZone());
        return dateFormat.format(date);
    }

    public static String formatUTC(Date date) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    public static int compare(String lhs, String rhs) {
        Date dateLhs = parse(lhs);
        Date dateRhs = parse(rhs);

        return compare(dateLhs, dateRhs);
    }

    public static int compare(Date lhs, Date rhs) {
        if (lhs != null && rhs != null) {
            if (lhs.before(rhs)) {
                return 1;
            } else if (lhs.after(rhs)) {
                return -1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public static Date todaysMidnight() {
        Date now = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.set(Calendar.HOUR_OF_DAY, 24);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    public static Date nextSevenDays() {
        Calendar c = Calendar.getInstance();
        c.setTime(todaysMidnight());
        c.add(Calendar.DAY_OF_YEAR, 7);
        return c.getTime();
    }

}
