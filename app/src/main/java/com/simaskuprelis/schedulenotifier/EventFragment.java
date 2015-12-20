package com.simaskuprelis.schedulenotifier;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.util.Date;
import java.util.UUID;

public class EventFragment extends Fragment {
    public static final String EXTRA_EVENT_ID = "com.simaskuprelis.schedulenotifier.event_id";
    private static final String DIALOG_TIME = "time";
    private static final int REQUEST_START_TIME = 0;
    private static final int REQUEST_END_TIME = 1;

    private Event mEvent;
    private EditText mTitleField;
    private LinearLayout mSelector;
    private Button mStartButton;
    private Button mEndButton;
    private Callbacks mCallbacks;

    public static EventFragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_EVENT_ID, id);
        EventFragment fragment = new EventFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity)
            mCallbacks = (Callbacks)context;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID id = (UUID)getArguments().getSerializable(EXTRA_EVENT_ID);
        mEvent = EventManager.get(getActivity()).getEvent(id);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event, container, false);

        mTitleField = (EditText)v.findViewById(R.id.titleField);
        String title = mEvent.getTitle();
        if (title != null && title.length() > 0)
            mTitleField.setText(mEvent.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals(mEvent.getTitle())) return;
                mEvent.setTitle(s.toString());
                mCallbacks.onEventUpdated(mEvent);
            }
        });

        mSelector = (LinearLayout)v.findViewById(R.id.weekday_selector);
        for (int i = 0; i < mSelector.getChildCount(); i++) {
            ToggleButton tb = (ToggleButton)mSelector.getChildAt(i);
            if (mEvent.isRepeated(i))
                tb.setChecked(true);
            tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int index = mSelector.indexOfChild(buttonView);
                    mEvent.setRepeated(index, isChecked);
                    mCallbacks.onEventUpdated(mEvent);
                }
            });
        }

        mStartButton = (Button)v.findViewById(R.id.startTimeButton);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mEvent.getStartDate());
                dialog.setTargetFragment(EventFragment.this, REQUEST_START_TIME);
                dialog.show(fm, DIALOG_TIME);
            }
        });
        mEndButton = (Button)v.findViewById(R.id.endTimeButton);
        mEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mEvent.getEndDate());
                dialog.setTargetFragment(EventFragment.this, REQUEST_END_TIME);
                dialog.show(fm, DIALOG_TIME);
            }
        });
        updateDate();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_event, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete_event:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.confirmation)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EventManager.get(getActivity()).deleteEvent(mEvent);
                                mCallbacks.onEventUpdated(mEvent);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case REQUEST_START_TIME:
                mEvent.setStartDate((Date)data.getSerializableExtra(TimePickerFragment.EXTRA_DATE));
                mCallbacks.onEventUpdated(mEvent);
                updateDate();
                break;

            case REQUEST_END_TIME:
                mEvent.setEndDate((Date)data.getSerializableExtra(TimePickerFragment.EXTRA_DATE));
                mCallbacks.onEventUpdated(mEvent);
                updateDate();
                break;
        }
    }

    private void updateDate() {
        if (DateFormat.is24HourFormat(getActivity())) {
            mStartButton.setText(DateFormat.format("HH:mm", mEvent.getStartDate()));
            mEndButton.setText(DateFormat.format("HH:mm", mEvent.getEndDate()));
        } else {
            mStartButton.setText(DateFormat.format("K:mm a", mEvent.getStartDate()));
            mEndButton.setText(DateFormat.format("K:mm a", mEvent.getEndDate()));
        }
    }

    public interface Callbacks {
        void onEventUpdated(Event event);
    }
}