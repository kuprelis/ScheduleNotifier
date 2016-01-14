package com.simaskuprelis.schedulenotifier;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;

import com.simaskuprelis.schedulenotifier.activity.EventListActivity;
import com.simaskuprelis.schedulenotifier.receiver.NotificationReceiver;

import java.util.Calendar;

public class TimerService extends IntentService {
    private static final String TAG = "TimerService";

    private static final String ACTION_NOTIFY = "com.simaskuprelis.schedulenotifier.NOTIFY";
    private static final String PERM_PRIVATE = "com.simaskuprelis.schedulenotifier.PRIVATE";

    public static final String PREF_NOTIFY = "notify";
    private static final String PREF_EVENT_TIME = "time";
    private static final String PREF_EVENT_TITLE = "title";
    private static final String PREF_IS_STARTING = "isStarting";

    private String mTitle;
    private int mNextTime;
    private boolean mIsStarting;

    public TimerService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Calendar cal = Calendar.getInstance();
        int time = cal.get(Calendar.HOUR_OF_DAY) * 60 * 60
                + cal.get(Calendar.MINUTE) * 60
                + cal.get(Calendar.SECOND);
        mNextTime = sp.getInt(PREF_EVENT_TIME, -1);
        if (mNextTime != -1) {
            mTitle = sp.getString(PREF_EVENT_TITLE, null);
            mIsStarting = sp.getBoolean(PREF_IS_STARTING, true);
        } else {
            EventManager em = EventManager.get(this);
            Event event = em.getDisplayEvent(getDay(cal), time);
            if (event == null) {
                postpone(intent, 24 * 60 * 60 - time);
                return;
            }
            mTitle = event.getTitle();
            mIsStarting = time < event.getStartTime();
            mNextTime = mIsStarting ? event.getStartTime() : event.getEndTime();
            setEvent();
        }
        int timeLeft = mNextTime - time;
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (timeLeft <= 0) {
            nm.cancel(0);
            mNextTime = -1;
            setEvent();
            postpone(intent, 0);
        } else if (timeLeft <= 60 * 60) {
            StringBuilder sb = new StringBuilder();
            if (mTitle != null && mTitle.length() > 0) sb.append(mTitle);
            else sb.append(getString(R.string.no_title));
            sb.append(' ');
            sb.append(mIsStarting ? getString(R.string.starts) : getString(R.string.ends));
            sb.append(' ');
            sb.append(Event.formatTime(mNextTime, DateFormat.is24HourFormat(this)));
            int iconId = getIconId(timeLeft / 60 + (timeLeft % 60 > 0 ? 1 : 0));
            Intent i = new Intent(this, EventListActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(iconId)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(sb.toString())
                    .setContentIntent(pi)
                    .setOngoing(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build();
            nm.notify(0, notification);
            postpone(intent, 60 - cal.get(Calendar.SECOND));
        } else {
            postpone(intent, timeLeft - 60 * 60);
        }
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = new Intent(ACTION_NOTIFY);
        SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (isOn) {
            context.sendBroadcast(i, PERM_PRIVATE);
        } else {
            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
            am.cancel(pi);
            pi.cancel();
            sp.putInt(PREF_EVENT_TIME, -1);
            ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).cancel(0);
        }
        sp.putBoolean(PREF_NOTIFY, isOn).commit();
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = new Intent(ACTION_NOTIFY);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public static void restartService(Context context) {
        if (!isServiceAlarmOn(context)) return;
        setServiceAlarm(context, false);
        setServiceAlarm(context, true);
    }

    private void postpone(Intent usedIntent, int seconds) {
        Intent i = new Intent(ACTION_NOTIFY);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long trigger = System.currentTimeMillis() + seconds * 1000;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlarmManager.AlarmClockInfo aci = new AlarmManager.AlarmClockInfo(trigger, pi);
            am.setAlarmClock(aci, pi);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC, trigger, pi);
        } else {
            am.set(AlarmManager.RTC, trigger, pi);
        }
        NotificationReceiver.completeWakefulIntent(usedIntent);
    }

    private void setEvent() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(PREF_EVENT_TIME, mNextTime)
                .putString(PREF_EVENT_TITLE, mTitle)
                .putBoolean(PREF_IS_STARTING, mIsStarting)
                .commit();
    }

    private int getDay(Calendar cal) {
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return 0;
            case Calendar.TUESDAY:
                return 1;
            case Calendar.WEDNESDAY:
                return 2;
            case Calendar.THURSDAY:
                return 3;
            case Calendar.FRIDAY:
                return 4;
            case Calendar.SATURDAY:
                return 5;
            case Calendar.SUNDAY:
                return 6;
            default:
                return -1;
        }
    }

    private int getIconId(int level) {
        switch (level) {
            case 1:
                return R.drawable.ic_timer_1;
            case 2:
                return R.drawable.ic_timer_2;
            case 3:
                return R.drawable.ic_timer_3;
            case 4:
                return R.drawable.ic_timer_4;
            case 5:
                return R.drawable.ic_timer_5;
            case 6:
                return R.drawable.ic_timer_6;
            case 7:
                return R.drawable.ic_timer_7;
            case 8:
                return R.drawable.ic_timer_8;
            case 9:
                return R.drawable.ic_timer_9;
            case 10:
                return R.drawable.ic_timer_10;
            case 11:
                return R.drawable.ic_timer_11;
            case 12:
                return R.drawable.ic_timer_12;
            case 13:
                return R.drawable.ic_timer_13;
            case 14:
                return R.drawable.ic_timer_14;
            case 15:
                return R.drawable.ic_timer_15;
            case 16:
                return R.drawable.ic_timer_16;
            case 17:
                return R.drawable.ic_timer_17;
            case 18:
                return R.drawable.ic_timer_18;
            case 19:
                return R.drawable.ic_timer_19;
            case 20:
                return R.drawable.ic_timer_20;
            case 21:
                return R.drawable.ic_timer_21;
            case 22:
                return R.drawable.ic_timer_22;
            case 23:
                return R.drawable.ic_timer_23;
            case 24:
                return R.drawable.ic_timer_24;
            case 25:
                return R.drawable.ic_timer_25;
            case 26:
                return R.drawable.ic_timer_26;
            case 27:
                return R.drawable.ic_timer_27;
            case 28:
                return R.drawable.ic_timer_28;
            case 29:
                return R.drawable.ic_timer_29;
            case 30:
                return R.drawable.ic_timer_30;
            case 31:
                return R.drawable.ic_timer_31;
            case 32:
                return R.drawable.ic_timer_32;
            case 33:
                return R.drawable.ic_timer_33;
            case 34:
                return R.drawable.ic_timer_34;
            case 35:
                return R.drawable.ic_timer_35;
            case 36:
                return R.drawable.ic_timer_36;
            case 37:
                return R.drawable.ic_timer_37;
            case 38:
                return R.drawable.ic_timer_38;
            case 39:
                return R.drawable.ic_timer_39;
            case 40:
                return R.drawable.ic_timer_40;
            case 41:
                return R.drawable.ic_timer_41;
            case 42:
                return R.drawable.ic_timer_42;
            case 43:
                return R.drawable.ic_timer_43;
            case 44:
                return R.drawable.ic_timer_44;
            case 45:
                return R.drawable.ic_timer_45;
            case 46:
                return R.drawable.ic_timer_46;
            case 47:
                return R.drawable.ic_timer_47;
            case 48:
                return R.drawable.ic_timer_48;
            case 49:
                return R.drawable.ic_timer_49;
            case 50:
                return R.drawable.ic_timer_50;
            case 51:
                return R.drawable.ic_timer_51;
            case 52:
                return R.drawable.ic_timer_52;
            case 53:
                return R.drawable.ic_timer_53;
            case 54:
                return R.drawable.ic_timer_54;
            case 55:
                return R.drawable.ic_timer_55;
            case 56:
                return R.drawable.ic_timer_56;
            case 57:
                return R.drawable.ic_timer_57;
            case 58:
                return R.drawable.ic_timer_58;
            case 59:
                return R.drawable.ic_timer_59;
            case 60:
                return R.drawable.ic_timer_60;
            default:
                return R.drawable.ic_timer_60;
        }
    }
}
