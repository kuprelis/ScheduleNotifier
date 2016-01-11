package com.simaskuprelis.schedulenotifier.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.simaskuprelis.schedulenotifier.TimerService;

public class NotificationReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, TimerService.class);
        startWakefulService(context, i);
    }
}
