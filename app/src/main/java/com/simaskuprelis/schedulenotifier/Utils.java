package com.simaskuprelis.schedulenotifier;

import java.util.Calendar;

public final class Utils {

    private Utils() {}

    public static int getDay(Calendar cal) {
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY: return 0;
            case Calendar.TUESDAY: return 1;
            case Calendar.WEDNESDAY: return 2;
            case Calendar.THURSDAY: return 3;
            case Calendar.FRIDAY: return 4;
            case Calendar.SATURDAY: return 5;
            case Calendar.SUNDAY: return 6;
            default: return 0;
        }
    }

    public static String formatTime(int time, boolean is24hour) {
        StringBuilder sb = new StringBuilder();
        time /= 60;
        int hour = time / 60;
        int minute = time % 60;
        String ampm = "";
        if (is24hour) {
            if (hour < 10) sb.append('0');
        } else {
            ampm = hour / 12 == 0 ? " AM" : " PM";
            if (hour > 12) hour = hour % 12;
            if (hour == 0) hour = 12;
        }
        sb.append(hour);
        sb.append(':');
        if (minute < 10) sb.append('0');
        sb.append(minute);
        sb.append(ampm);
        return sb.toString();
    }
}
