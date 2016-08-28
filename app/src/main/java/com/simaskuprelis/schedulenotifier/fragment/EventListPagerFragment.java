package com.simaskuprelis.schedulenotifier.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.simaskuprelis.schedulenotifier.Event;
import com.simaskuprelis.schedulenotifier.EventManager;
import com.simaskuprelis.schedulenotifier.R;
import com.simaskuprelis.schedulenotifier.TimerService;
import com.simaskuprelis.schedulenotifier.Utils;

import java.util.Calendar;

public class EventListPagerFragment extends Fragment {

    private ViewPager mPager;
    private TabLayout mTabs;
    private FloatingActionButton mAddEventFAB;

    private Callbacks mCallbacks;

    private ListPagerAdapter mAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity)
            mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_list_pager, container, false);

        mPager = (ViewPager) v.findViewById(R.id.list_pager);
        mAdapter = new ListPagerAdapter(getChildFragmentManager());
        updateUI();
        mPager.setCurrentItem(Utils.getDay(Calendar.getInstance()));

        mTabs = (TabLayout) v.findViewById(R.id.pager_tabs);
        mTabs.setupWithViewPager(mPager);

        mAddEventFAB = (FloatingActionButton) v.findViewById(R.id.add_event_FAB);
        mAddEventFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Event event = new Event();
                int day = mPager.getCurrentItem();
                event.setRepeated(day, true);
                EventManager.get(getContext()).addEvent(event);
                mCallbacks.onEventCreated(event, day);
            }
        });

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        EventManager.get(getContext()).save();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_event_list_pager, menu);
        boolean notify = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(TimerService.PREF_NOTIFY, false);
        menu.findItem(R.id.menu_notify)
                .setTitle(notify ? R.string.notifications_disable : R.string.notifications_enable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_notify) {
            boolean notify = !PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getBoolean(TimerService.PREF_NOTIFY, false);
            TimerService.setServiceAlarm(getContext(), notify);
            item.setTitle(notify ? R.string.notifications_disable : R.string.notifications_enable);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateUI() {
        mPager.setAdapter(mAdapter);
    }

    public interface Callbacks {
        void onEventCreated(Event event, int day);
    }

    private class ListPagerAdapter extends FragmentStatePagerAdapter {

        public ListPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return EventListFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.monday_short);
                case 1: return getString(R.string.tuesday_short);
                case 2: return getString(R.string.wednesday_short);
                case 3: return getString(R.string.thursday_short);
                case 4: return getString(R.string.friday_short);
                case 5: return getString(R.string.saturday_short);
                case 6: return getString(R.string.sunday_short);
                default: return Integer.toString(position);
            }
        }
    }
}
