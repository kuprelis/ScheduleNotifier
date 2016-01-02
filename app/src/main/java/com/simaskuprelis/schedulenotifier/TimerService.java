package com.simaskuprelis.schedulenotifier;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.UUID;

public class TimerService extends IntentService {
    private static final String TAG = "TimerService";

    public static final String PREF_NOTIFY = "notify";
    private static final String PREF_EVENT_ID = "mEventId";
    private static final String PREF_IS_STARTING = "isStarting";

    private Event mEvent;
    private boolean mIsStarting;

    public TimerService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        EventManager em = EventManager.get(this);
        Calendar cal = Calendar.getInstance();
        int time = cal.get(Calendar.HOUR_OF_DAY) * 60 * 60
                + cal.get(Calendar.MINUTE) * 60
                + cal.get(Calendar.SECOND);
        mEvent = null;
        mIsStarting = true;
        if (sp.getString(PREF_EVENT_ID, null) != null) {
            mEvent = em.getEvent(UUID.fromString(sp.getString(PREF_EVENT_ID, null)));
            mIsStarting = sp.getBoolean(PREF_IS_STARTING, true);
        } else {
            int day;
            switch (cal.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:
                    day = 0;
                    break;
                case Calendar.TUESDAY:
                    day = 1;
                    break;
                case Calendar.WEDNESDAY:
                    day = 2;
                    break;
                case Calendar.THURSDAY:
                    day = 3;
                    break;
                case Calendar.FRIDAY:
                    day = 4;
                    break;
                case Calendar.SATURDAY:
                    day = 5;
                    break;
                case Calendar.SUNDAY:
                    day = 6;
                    break;
                default:
                    day = -1;
                    break;
            }
            for (Event e : em.getEvents(day)) {
                if (time < e.getStartTime()) {
                    if (mEvent == null || e.getStartTime() <
                            (mIsStarting ? mEvent.getStartTime() : mEvent.getEndTime())) {
                        mEvent = e;
                        mIsStarting = true;
                    }
                } else if (time < e.getEndTime()) {
                    if (mEvent == null || e.getEndTime() <
                            (mIsStarting ? mEvent.getStartTime() : mEvent.getEndTime())) {
                        mEvent = e;
                        mIsStarting = false;
                    }
                }
            }
            if (mEvent != null) {
                setEvent();
            } else {
                postpone(24 * 60 * 60 - time);
                return;
            }
        }
        int timeLeft = mIsStarting ? mEvent.getStartTime() - time : mEvent.getEndTime() - time;
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (timeLeft <= 0) {
            nm.cancel(0);
            mEvent = null;
            setEvent();
            postpone(0);
        } else if (timeLeft <= 60 * 60) {
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Time left: " + timeLeft / 60)
                    .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                    .setOngoing(true)
                    .build();
            nm.notify(0, notification);
            postpone(60 - cal.get(Calendar.SECOND));
        } else {
            postpone(timeLeft - 60 * 60);
        }
    }

    private void postpone(int seconds) {
        Log.i(TAG, "Postponed for " + seconds);
        Intent i = new Intent(this, TimerService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + seconds * 1000, pi);
    }

    private void setEvent() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(PREF_EVENT_ID, mEvent != null ? mEvent.getId().toString() : null)
                .putBoolean(PREF_IS_STARTING, mIsStarting)
                .commit();
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = new Intent(context, TimerService.class);
        SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (isOn) {
            context.startService(i);
        } else {
            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
            am.cancel(pi);
            pi.cancel();
            sp.putString(PREF_EVENT_ID, null);
            ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).cancel(0);
        }
        sp.putBoolean(PREF_NOTIFY, isOn).commit();
    }
}
