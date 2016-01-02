package com.simaskuprelis.schedulenotifier;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class RestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (getResultCode() != Activity.RESULT_OK) return;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp.getBoolean(EventFragment.PREF_RESTART, false))
            TimerService.setServiceAlarm(context, true);
        sp.edit()
                .remove(EventFragment.PREF_RESTART)
                .commit();
    }
}
