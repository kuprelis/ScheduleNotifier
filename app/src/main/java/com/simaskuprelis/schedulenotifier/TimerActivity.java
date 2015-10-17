package com.simaskuprelis.schedulenotifier;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class TimerActivity extends AppCompatActivity {

    private static final String PREF_NOTIFY = "notify";

    private Button mEditButton;
    private TextView mEventTextView;
    private TextView mInfoTextView;
    private CheckBox mNotificationCheckBox;
    private TextView mTimeTextView;

    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timer);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!mPreferences.contains(PREF_NOTIFY)) {
            mPreferences.edit()
                    .putBoolean(PREF_NOTIFY, true)
                    .commit();
        }

        mTimeTextView = (TextView)findViewById(R.id.timeTextView);
        mTimeTextView.setText("15");

        mInfoTextView = (TextView)findViewById(R.id.infoTextView);
        mInfoTextView.setText(getString(R.string.minutes_until));

        mEventTextView = (TextView)findViewById(R.id.eventTextView);
        mEventTextView.setText("event");

        mEditButton = (Button)findViewById(R.id.editButton);
        mEditButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TimerActivity.this, EventListActivity.class);
                startActivity(i);
            }
        });

        mNotificationCheckBox = (CheckBox)findViewById(R.id.notificationCheckBox);
        mNotificationCheckBox.setChecked(mPreferences.getBoolean(PREF_NOTIFY, false));
        mNotificationCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mPreferences.edit()
                        .putBoolean(PREF_NOTIFY, b)
                        .commit();
            }
        });
    }
}