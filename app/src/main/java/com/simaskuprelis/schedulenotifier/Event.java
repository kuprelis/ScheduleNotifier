package com.simaskuprelis.schedulenotifier;

import java.util.Date;
import java.util.UUID;

public class Event {
    public static final int MONDAY = 0;
    public static final int TUESDAY = 1;
    public static final int WEDNESDAY = 2;
    public static final int THURSDAY = 3;
    public static final int FRIDAY = 4;
    public static final int SATURDAY = 5;
    public static final int SUNDAY = 6;

    private Date mEndDate;
    private Date mStartDate;
    private boolean[] mRepeat;
    private UUID mId;
    private String mTitle;

    public Event() {
        mId = UUID.randomUUID();
        mRepeat = new boolean[7];
    }

    public Date getEndDate() {
        return mEndDate;
    }

    public void setEndDate(Date endDate) {
        mEndDate = endDate;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        mStartDate = startDate;
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
}