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
    private static final String PREF_EVENT_ID = "eventId";
    private static final String PREF_IS_STARTING = "isStarting";

    public TimerService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        EventManager em = EventManager.get(this);
        Calendar cal = Calendar.getInstance();
        int time = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        Event event = null;
        boolean isStarting = true;
        if (sp.getString(PREF_EVENT_ID, null) != null) {
            event = em.getEvent(UUID.fromString(sp.getString(PREF_EVENT_ID, null)));
            isStarting = sp.getBoolean(PREF_IS_STARTING, true);
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
                    if (event == null || e.getStartTime() <
                            (isStarting ? event.getStartTime() : event.getEndTime())) {
                        event = e;
                        isStarting = true;
                    }
                } else if (time < e.getEndTime()) {
                    if (event == null || e.getEndTime() <
                            (isStarting ? event.getStartTime() : event.getEndTime())) {
                        event = e;
                        isStarting = false;
                    }
                }
            }
            if (event != null) {
                setEvent(event.getId().toString(), isStarting);
            } else {
                postpone(24 * 60 - time);
                return;
            }
        }
        int timeLeft = isStarting ? event.getStartTime() - time : event.getEndTime() - time;
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (timeLeft <= 0) {
            nm.cancel(0);
            setEvent(null, true);
            postpone(0);
        } else if (timeLeft <= 60) {
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Time left: " + timeLeft)
                    .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                    .setOngoing(true)
                    .build();
            nm.notify(0, notification);
            postpone(1);
        } else {
            postpone(timeLeft - 60);
        }
    }

    private void postpone(int minutes) {
        Log.i(TAG, "Postponed for " + minutes);
        Intent i = new Intent(this, TimerService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        minutes *= 60 * 1000;
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + minutes, pi);
    }

    private void setEvent(String id, boolean isStarting) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(PREF_EVENT_ID, id)
                .putBoolean(PREF_IS_STARTING, isStarting)
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
            sp.remove(PREF_EVENT_ID).remove(PREF_IS_STARTING);
        }
        sp.putBoolean(PREF_NOTIFY, isOn).commit();
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = new Intent(context, TimerService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }
}
