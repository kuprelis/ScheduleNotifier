package com.simaskuprelis.schedulenotifier;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    public static final String EXTRA_TIME = "com.simaskuprelis.schedulenotifier.time";

    private int mTime;

    public static TimePickerFragment newInstance(int time) {
        Bundle args = new Bundle();
        TimePickerFragment fragment = new TimePickerFragment();
        args.putInt(EXTRA_TIME, time);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mTime = getArguments().getInt(EXTRA_TIME);
        int hour = mTime / 100;
        int minute = mTime % 100;
        TimePickerDialog tpd = new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        tpd.setTitle(null);
        return tpd;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mTime = hourOfDay * 100 + minute;
        sendResult(Activity.RESULT_OK);
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) return;
        Intent i = new Intent();
        i.putExtra(EXTRA_TIME, mTime);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }
}
