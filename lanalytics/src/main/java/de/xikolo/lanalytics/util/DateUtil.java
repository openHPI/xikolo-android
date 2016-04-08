package de.xikolo.lanalytics.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("unused")
public class DateUtil {

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    public static Date parse(String date) {
        SimpleDateFormat dateFm = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Date parsedDate = null;
        try {
            if (date != null) {
                parsedDate = dateFm.parse(date);
            }
        } catch (ParseException e) {
            parsedDate = null;
        }
        return parsedDate;
    }

    public static String format(Date date) {
        SimpleDateFormat dateFm = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return dateFm.format(date);
    }

}
