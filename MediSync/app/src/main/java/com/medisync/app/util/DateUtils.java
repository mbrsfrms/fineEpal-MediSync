package com.medisync.app.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Date and time helpers. Dates are stored as text so the database stays readable. Pure Java, no Android dependency. */
public final class DateUtils {
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";
    public static final String TIME_12H_PATTERN = "h:mm a";

    private DateUtils() { }

    public static String today() { return format(new Date(), DATE_PATTERN); }

    public static String now() { return format(new Date(), DATE_TIME_PATTERN); }

    public static String format(Date d, String pattern) {
        return new SimpleDateFormat(pattern, Locale.US).format(d);
    }

    /** Parses "yyyy-MM-dd HH:mm" or "yyyy-MM-dd" into millis; returns null when invalid. */
    public static Long parseToMillis(String text) {
        if (text == null) return null;
        String t = text.trim();
        String[] patterns = {DATE_TIME_PATTERN, DATE_PATTERN};
        for (String p : patterns) {
            SimpleDateFormat f = new SimpleDateFormat(p, Locale.US);
            f.setLenient(false);
            try {
                return f.parse(t).getTime();
            } catch (ParseException ignored) {
                // try the next pattern
            }
        }
        return null;
    }

    /** Human readable date, e.g. "Mar 12, 2017". Returns the input when it cannot be parsed. */
    public static String formatDisplayDate(String date) {
        Long millis = parseToMillis(date);
        if (millis == null) return date;
        return format(new Date(millis), "MMM d, yyyy");
    }

    /** Whole years between a date of birth and today; -1 when unknown. */
    public static int ageInYears(String dob) {
        Long millis = parseToMillis(dob);
        if (millis == null) return -1;
        Calendar birth = Calendar.getInstance();
        birth.setTimeInMillis(millis);
        Calendar now = Calendar.getInstance();
        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--;
        return age;
    }

    /** Converts "8:00 AM" to minutes after midnight; returns -1 when invalid. */
    public static int minutesOf(String time12h) {
        if (time12h == null) return -1;
        SimpleDateFormat f = new SimpleDateFormat(TIME_12H_PATTERN, Locale.US);
        f.setLenient(false);
        try {
            Date d = f.parse(time12h.trim());
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        } catch (ParseException e) {
            return -1;
        }
    }

    /** Converts "14:30" (24h, as used by time pickers) to minutes after midnight; -1 when invalid. */
    public static int minutesOf24h(String hhmm) {
        if (hhmm == null) return -1;
        String[] parts = hhmm.trim().split(":");
        if (parts.length != 2) return -1;
        try {
            int h = Integer.parseInt(parts[0]), m = Integer.parseInt(parts[1]);
            if (h < 0 || h > 23 || m < 0 || m > 59) return -1;
            return h * 60 + m;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Minutes after midnight to "8:00 AM". */
    public static String formatMinutes(int minutes) {
        int h = (minutes / 60) % 24, m = minutes % 60;
        int h12 = h % 12 == 0 ? 12 : h % 12;
        return h12 + ":" + (m < 10 ? "0" : "") + m + " " + (h < 12 ? "AM" : "PM");
    }

    /** Minutes after midnight to "HH:mm" (24h). */
    public static String formatMinutes24h(int minutes) {
        return String.format(Locale.US, "%02d:%02d", (minutes / 60) % 24, minutes % 60);
    }

    /** Parses "8:00 AM, 2:00 PM" into a sorted list of minutes. Invalid parts are skipped. */
    public static List<Integer> parseTimes(String text) {
        List<Integer> out = new ArrayList<>();
        if (text == null) return out;
        for (String part : text.split(",")) {
            int m = minutesOf(part);
            if (m >= 0) out.add(m);
        }
        Collections.sort(out);
        return out;
    }

    public static int nowMinutes() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    }

    /** Date string N days from today (negative = past). */
    public static String daysFromToday(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, days);
        return format(c.getTime(), DATE_PATTERN);
    }

    /** Returns the millis for a date string at a given minute of the day. */
    public static long millisAt(String date, int minuteOfDay) {
        Long base = parseToMillis(date);
        Calendar c = Calendar.getInstance();
        if (base != null) c.setTimeInMillis(base);
        c.set(Calendar.HOUR_OF_DAY, minuteOfDay / 60);
        c.set(Calendar.MINUTE, minuteOfDay % 60);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /** Shifts a yyyy-MM-dd date by a number of days. Returns the input when it cannot be parsed. */
    public static String shiftDate(String date, int days) {
        Long millis = parseToMillis(date);
        if (millis == null) return date;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.add(Calendar.DAY_OF_YEAR, days);
        return format(c.getTime(), DATE_PATTERN);
    }

    /** Whole calendar days from a to b (b - a). Returns 0 when either date is invalid. */
    public static int daysBetween(String a, String b) {
        Long ma = parseToMillis(a), mb = parseToMillis(b);
        if (ma == null || mb == null) return 0;
        Calendar ca = Calendar.getInstance(), cb = Calendar.getInstance();
        ca.setTimeInMillis(ma);
        cb.setTimeInMillis(mb);
        ca.set(Calendar.HOUR_OF_DAY, 12); ca.set(Calendar.MINUTE, 0); ca.set(Calendar.SECOND, 0); ca.set(Calendar.MILLISECOND, 0);
        cb.set(Calendar.HOUR_OF_DAY, 12); cb.set(Calendar.MINUTE, 0); cb.set(Calendar.SECOND, 0); cb.set(Calendar.MILLISECOND, 0);
        return (int) Math.round((cb.getTimeInMillis() - ca.getTimeInMillis()) / 86_400_000d);
    }
}
