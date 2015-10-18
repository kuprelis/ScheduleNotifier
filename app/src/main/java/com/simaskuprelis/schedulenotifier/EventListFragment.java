package com.simaskuprelis.schedulenotifier;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class EventListFragment extends ListFragment {
    private static final String TAG = "EventListFragment";

    private Callbacks mCallbacks;
    private ArrayList<Event> mEvents;
    private Button mNewEventButton;
    private LinearLayout mSelector;
    private int mSelection;

    private CompoundButton.OnCheckedChangeListener mButtonListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isChecked) return;

            mSelection = mSelector.indexOfChild(buttonView);
            for (int i = 0; i < mSelector.getChildCount(); i++) {
                if (i != mSelection) {
                    ToggleButton tb = (ToggleButton)mSelector.getChildAt(i);
                    tb.setChecked(false);
                }
            }
            mEvents.clear();
            mEvents.addAll(EventManager.get(getActivity()).getEvents(mSelection));
            ((EventAdapter)getListAdapter()).notifyDataSetChanged();
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity)
            mCallbacks = (Callbacks)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEvents = EventManager.get(getActivity()).getEvents(Event.MONDAY);
        setListAdapter(new EventAdapter(mEvents));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_list, container, false);

        mSelector = (LinearLayout)v.findViewById(R.id.weekday_selector);
        for (int i = 0; i < mSelector.getChildCount(); i++) {
            ToggleButton button = (ToggleButton)mSelector.getChildAt(i);
            button.setOnCheckedChangeListener(mButtonListener);
        }


        mNewEventButton = (Button)v.findViewById(R.id.new_event);
        mNewEventButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createEvent();
            }
        });
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private void createEvent() {
        Event event = new Event();
        EventManager.get(getActivity()).addEvent(event);
        mCallbacks.onEventSelected(event);
    }

    public interface Callbacks {
        void onEventSelected(Event event);
    }

    private class EventAdapter extends ArrayAdapter<Event> {
        public EventAdapter(ArrayList<Event> events) {
            super(getActivity(), 0, events);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_event, null);
            }

            Event e = getItem(position);

            TextView title = (TextView)convertView.findViewById(R.id.titleTextView);
            title.setText(e.getTitle());

            int color = getResources().getColor(R.color.blue500);

            LinearLayout display = (LinearLayout)convertView.findViewById(R.id.weekday_display);
            for (int i = 0; i < display.getChildCount(); i++) {
                TextView tv = (TextView)display.getChildAt(i);
                if (e.isRepeated(i))
                    tv.setTextColor(color);
            }

            TextView time = (TextView)convertView.findViewById(R.id.timeTextView);
            time.setText("fix me");

            return convertView;
        }
    }
}