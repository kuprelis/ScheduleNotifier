package com.simaskuprelis.schedulenotifier;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.text.format.DateFormat;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity)
            mCallbacks = (Callbacks)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelection = -1;
        mEvents = EventManager.get(getActivity()).getEvents(mSelection);
        setListAdapter(new EventAdapter(mEvents));
        setHasOptionsMenu(true);
    }

    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_list, container, false);

        mSelector = (LinearLayout)v.findViewById(R.id.weekday_selector);
        for (int i = 0; i < mSelector.getChildCount(); i++) {
            ((ToggleButton) mSelector.getChildAt(i))
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (!isChecked) {
                                if (mSelector.indexOfChild(buttonView) == mSelection) {
                                    mSelection = -1;
                                    updateUI();
                                }
                            } else {
                                mSelection = mSelector.indexOfChild(buttonView);
                                for (int i = 0; i < mSelector.getChildCount(); i++) {
                                    if (i != mSelection)
                                        ((ToggleButton) mSelector.getChildAt(i)).setChecked(false);
                                }
                                updateUI();
                            }
                            mCallbacks.onDayChanged(mSelection);
                        }
                    });
        }

        mNewEventButton = (Button)v.findViewById(R.id.new_event);
        mNewEventButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createEvent();
            }
        });

        ListView lv = (ListView)v.findViewById(android.R.id.list);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            registerForContextMenu(lv);
        } else {
            lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            lv.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.event_list_item_context, menu);
                    toggleSelector();
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_delete_event:
                            EventAdapter ea = (EventAdapter)getListAdapter();
                            EventManager em = EventManager.get(getActivity());
                            for (int i = 0; i < ea.getCount(); i++) {
                                if (getListView().isItemChecked(i))
                                    em.deleteEvent(ea.getItem(i));
                            }
                            mode.finish();
                            updateUI();
                            return true;
                        default:
                            return false;
                    }
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    toggleSelector();
                }
            });
        }
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_event_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean notify = TimerService.isServiceAlarmOn(getActivity());
        menu.findItem(R.id.menu_notify)
                .setTitle(notify ? R.string.notifications_disable : R.string.notifications_enable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_event:
                createEvent();
                return true;

            case R.id.menu_notify:
                boolean notify = TimerService.isServiceAlarmOn(getActivity());
                TimerService.setServiceAlarm(getActivity(), !notify);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    getActivity().invalidateOptionsMenu();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.event_list_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int pos = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
        Event e = ((EventAdapter)getListAdapter()).getItem(pos);

        if (item.getItemId() == R.id.menu_delete_event) {
            EventManager.get(getActivity()).deleteEvent(e);
            updateUI();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Event e = ((EventAdapter)getListAdapter()).getItem(position);
        mCallbacks.onEventSelected(e);
    }

    public void updateUI() {
        mEvents.clear();
        mEvents.addAll(EventManager.get(getActivity()).getEvents(mSelection));
        ((EventAdapter)getListAdapter()).notifyDataSetChanged();
    }

    private void createEvent() {
        Event event = new Event();
        if (mSelection != -1) event.setRepeated(mSelection, true);
        EventManager.get(getActivity()).addEvent(event);
        mCallbacks.onEventSelected(event);
    }

    private void toggleSelector() {
        for (int i = 0; i < mSelector.getChildCount(); i++) {
            View v = mSelector.getChildAt(i);
            v.setEnabled(!v.isEnabled());
        }
    }

    public interface Callbacks {
        void onEventSelected(Event event);
        void onDayChanged(int day);
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

            TextView titleView = (TextView)convertView.findViewById(R.id.titleTextView);
            String title = e.getTitle();
            if (title != null && title.length() > 0)
                titleView.setText(e.getTitle());
            else
                titleView.setText(R.string.no_title);

            int highlight = getResources().getColor(R.color.blue500);
            int lowlight = getResources().getColor(R.color.gray_txt);

            LinearLayout display = (LinearLayout)convertView.findViewById(R.id.weekday_display);
            for (int i = 0; i < display.getChildCount(); i++) {
                TextView tv = (TextView)display.getChildAt(i);
                if (e.isRepeated(i))
                    tv.setTextColor(highlight);
                else
                    tv.setTextColor(lowlight);
            }

            TextView timeView = (TextView)convertView.findViewById(R.id.timeTextView);
            StringBuilder sb = new StringBuilder();
            boolean is24hour = DateFormat.is24HourFormat(getActivity());
            sb.append(Event.formatTime(e.getStartTime(), is24hour));
            sb.append(" - ");
            sb.append(Event.formatTime(e.getEndTime(), is24hour));
            timeView.setText(sb.toString());

            return convertView;
        }
    }
}