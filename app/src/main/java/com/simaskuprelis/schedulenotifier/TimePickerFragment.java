package com.simaskuprelis.schedulenotifier;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    public static final String EXTRA_DATE = "com.simaskuprelis.schedulenotifier.date";

    private Date mDate;
    private Calendar mCalendar;

    public static TimePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        TimePickerFragment fragment = new TimePickerFragment();
        args.putSerializable(EXTRA_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDate = (Date)getArguments().getSerializable(EXTRA_DATE);
        mCalendar = Calendar.getInstance();
        mCalendar.setTime(mDate);
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);
        TimePickerDialog tpd = new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        tpd.setTitle(null);
        return tpd;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        mDate = mCalendar.getTime();
        sendResult(Activity.RESULT_OK);
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) return;
        Intent i = new Intent();
        i.putExtra(EXTRA_DATE, mDate);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }
}
