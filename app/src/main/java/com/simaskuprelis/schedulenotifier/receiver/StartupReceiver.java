package com.simaskuprelis.schedulenotifier.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.simaskuprelis.schedulenotifier.R;
import com.simaskuprelis.schedulenotifier.TimerService;

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isOn = sp.getBoolean(TimerService.PREF_NOTIFY, false);
        TimerService.setServiceAlarm(context, isOn);
    }
}
