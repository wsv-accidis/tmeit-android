package se.tmeit.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Utilities for working with date/time values.
 */
public final class DateTimeUtils {
    private static final String ISO_8601_DATETIME = "yyyy-MM-dd'T'HH:mm:ssZ";

    private DateTimeUtils() {
    }

    public static String formatIso8601(Calendar calendar) {
        if (null == calendar) {
            return "";
        }
        return new SimpleDateFormat(ISO_8601_DATETIME).format(calendar.getTime());
    }

    public static Calendar parseIso8601(String str) {
        if (!str.isEmpty()) {
            try {
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTime(new SimpleDateFormat(ISO_8601_DATETIME).parse(str));
                return calendar;
            } catch (ParseException ignored) {
                return null;
            }
        } else {
            return null;
        }
    }
}
