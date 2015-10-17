package com.simaskuprelis.schedulenotifier;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class EventListFragment extends ListFragment {
    private static final String TAG = "EventListFragment";

    private Callbacks mCallbacks;
    private ArrayList<Event> mEvents;
    private Button mNewEventButton;
    private LinearLayout mSelector;

    private OnClickListener mButtonListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            resetButtons();
            view.setEnabled(false);
            mEvents = EventManager.get(getActivity()).getEvents(mSelector.indexOfChild(view));
            ((EventAdapter)getListAdapter()).notifyDataSetChanged();
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
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
        for (int i = 0; i < mSelector.getChildCount(); i++)
            mSelector.getChildAt(i).setOnClickListener(mButtonListener);

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
        event.setRepeatedOn(Event.MONDAY, true);
        EventManager.get(getActivity()).addEvent(event);
        mCallbacks.onEventSelected(event);
    }

    private void resetButtons() {
        for (int i = 0; i < mSelector.getChildCount(); i++)
            mSelector.getChildAt(i).setEnabled(true);
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
                        .inflate(R.layout.list_item_event, parent);
            }

            Event e = getItem(position);

            TextView title = (TextView)convertView.findViewById(R.id.titleTextView);
            title.setText(e.getTitle());

            int color = getResources().getColor(R.color.blue500);

            LinearLayout display = (LinearLayout)convertView.findViewById(R.id.weekday_display);
            for (int i = 0; i < display.getChildCount(); i++) {
                if (e.isRepeatedOn(i))
                    ((TextView)display.getChildAt(i)).setTextColor(color);
            }

            TextView time = (TextView)convertView.findViewById(R.id.timeTextView);
            time.setText(e.getStartTime().toString() + " - " + e.getEndTime().toString());

            return convertView;
        }
    }
}