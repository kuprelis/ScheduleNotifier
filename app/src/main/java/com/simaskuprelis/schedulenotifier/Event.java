package com.simaskuprelis.schedulenotifier;

import java.sql.Time;
import java.util.UUID;

public class Event {
    public static final int MONDAY = 0;
    public static final int TUESDAY = 1;
    public static final int WEDNESDAY = 2;
    public static final int THURSDAY = 3;
    public static final int FRIDAY = 4;
    public static final int SATURDAY = 5;
    public static final int SUNDAY = 6;

    private Time mEndTime;
    private Time mStartTime;
    private boolean[] mWeekdays;
    private UUID mId;
    private String mTitle;

    public Event() {
        mId = UUID.randomUUID();
        mWeekdays = new boolean[7];
    }

    public Time getEndTime() {
        return mEndTime;
    }

    public void setEndTime(Time endTime) {
        mEndTime = endTime;
    }

    public Time getStartTime() {
        return mStartTime;
    }

    public void setStartTime(Time startTime) {
        mStartTime = startTime;
    }

    public boolean isRepeatedOn(int weekday) {
        return mWeekdays[weekday];
    }

    public void setRepeatedOn(int weekday, boolean repeat) {
        mWeekdays[weekday] = repeat;
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
}