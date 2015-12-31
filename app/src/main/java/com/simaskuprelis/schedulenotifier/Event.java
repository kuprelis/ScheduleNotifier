package com.simaskuprelis.schedulenotifier;

import java.util.Calendar;
import java.util.UUID;

public class Event {
    private static final String TAG = "Event";

    private int mStartTime;
    private int mEndTime;
    private boolean[] mRepeat;
    private UUID mId;
    private String mTitle;

    public Event() {
        mId = UUID.randomUUID();
        mRepeat = new boolean[7];
        Calendar cal = Calendar.getInstance();
        int time = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        mStartTime = time;
        mEndTime = time;
    }

    public int getStartTime() {
        return mStartTime;
    }

    public void setStartTime(int startTime) {
        mStartTime = startTime;
    }

    public int getEndTime() {
        return mEndTime;
    }

    public void setEndTime(int endTime) {
        mEndTime = endTime;
    }

    public boolean isRepeated(int day) {
        return mRepeat[day];
    }

    public void setRepeated(int day, boolean repeat) {
        mRepeat[day] = repeat;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public UUID getId() {
        return mId;
    }

    public static String formatTime(int time, boolean is24hour) {
        StringBuilder sb = new StringBuilder();
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