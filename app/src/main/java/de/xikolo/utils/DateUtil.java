package de.xikolo.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
            return true;
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

    public static boolean nowIsAfter(String date) {
        return nowIsAfter(parse(date));
    }

    public static boolean nowIsAfter(Date date) {
        Date dateNow = new Date();

        if (date == null) {
            return true;
        } else if (dateNow.after(date)) {
            return true;
        }

        return false;
    }

    public static boolean nowIsBefore(String date) {
        return nowIsBefore(parse(date));
    }

    public static boolean nowIsBefore(Date date) {
        Date dateNow = new Date();

        if (date == null) {
            return true;
        } else if (dateNow.before(date)) {
            return true;
        }

        return false;
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

}
