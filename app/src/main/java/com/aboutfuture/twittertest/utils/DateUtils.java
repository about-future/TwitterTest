package com.aboutfuture.twittertest.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    public static String longDateFormat(String stringDate) {
        // Original date format
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
        // Modified date format
        Date date = simpleDateFormat.parse(stringDate, pos);
        SimpleDateFormat simpleDateReformat = new SimpleDateFormat("HH:mm, d MMMM yyyy", Locale.US);

        // Set the timezone reference for formatting
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));

        return simpleDateReformat.format(date);
    }
}
