package de.xikolo.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static final String TAG = DateUtil.class.getSimpleName();

    public static final String XIKOLO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static boolean nowIsBetween(String from, String to) {
        Date dateBegin = parse(from);
        Date dateEnd = parse(to);

        Date dateNow = new Date();

        if (dateBegin == null && dateEnd == null) {
            return true;
        }
        if (dateBegin == null) {
            if (dateNow.before(dateEnd)) {
                return true;
            }
        }
        if (dateEnd == null) {
            if (dateNow.after(dateBegin)) {
                return true;
            }
        }
        return dateNow.after(dateBegin) && dateNow.before(dateEnd);
    }

    public static boolean nowIsAfter(String date) {
        Date d = parse(date);

        Date dateNow = new Date();

        if (d == null) {
            return true;
        } else if (dateNow.after(d)) {
            return true;
        }

        return false;
    }

    public static boolean nowIsBefore(String date) {
        Date d = parse(date);

        Date dateNow = new Date();

        if (d == null) {
            return true;
        } else if (dateNow.before(d)) {
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

        if (dateLhs != null && dateRhs != null) {
            if (dateLhs.before(dateRhs)) {
                return 1;
            } else if (dateLhs.after(dateRhs)) {
                return -1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

}
