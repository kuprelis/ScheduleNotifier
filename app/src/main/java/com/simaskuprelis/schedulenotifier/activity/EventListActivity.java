package com.simaskuprelis.schedulenotifier.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.simaskuprelis.schedulenotifier.Event;
import com.simaskuprelis.schedulenotifier.fragment.EventFragment;
import com.simaskuprelis.schedulenotifier.fragment.EventListFragment;
import com.simaskuprelis.schedulenotifier.EventManager;
import com.simaskuprelis.schedulenotifier.R;
import com.simaskuprelis.schedulenotifier.TimerService;

import java.util.UUID;

public class EventListActivity extends SingleFragmentActivity
        implements EventListFragment.Callbacks, EventFragment.Callbacks {
    private static final String TAG = "EventListActivity";

    private static final int REQUEST_EDIT = 0;

    private int mDay = -1;
    private boolean mShouldRestart = false;

    @Override
    protected Fragment createFragment() {
        return new EventListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sp.contains(TimerService.PREF_NOTIFY) ||
                (sp.getBoolean(TimerService.PREF_NOTIFY, false) && !TimerService.isServiceAlarmOn(this)))
            TimerService.setServiceAlarm(this, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        restartService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_EDIT) {
            FragmentManager fm = getSupportFragmentManager();
            EventListFragment fragment =
                    (EventListFragment) fm.findFragmentById(R.id.fragmentContainer);
            fragment.updateUI();
            mShouldRestart = true;
            restartService();
        }
    }

    @Override
    public void onEventSelected(Event event) {
        if (findViewById(R.id.detailFragmentContainer) == null) {
            Intent i = new Intent(this, EventPagerActivity.class);
            i.putExtra(EventFragment.EXTRA_EVENT_ID, event.getId());
            i.putExtra(EventPagerActivity.EXTRA_DAY, mDay);
            startActivityForResult(i, REQUEST_EDIT);
        } else {
            FragmentManager fm = getSupportFragmentManager();
            clearDetail(fm);
            Fragment fragment = EventFragment.newInstance(event.getId());
            fm.beginTransaction()
                    .add(R.id.detailFragmentContainer, fragment)
                    .commit();
        }
    }

    @Override
    public void onEventUpdated(Event event) {
        mShouldRestart = true;
        FragmentManager fm = getSupportFragmentManager();
        if (event == null) clearDetail(fm);
        EventListFragment fragment = (EventListFragment) fm.findFragmentById(R.id.fragmentContainer);
        fragment.updateUI();
    }

    @Override
    public void onDayChanged(int day) {
        mDay = day;
        if (day == -1) return;
        FragmentManager fm = getSupportFragmentManager();
        EventFragment fragment = (EventFragment) fm.findFragmentById(R.id.detailFragmentContainer);
        if (fragment == null) return;
        UUID id = (UUID) fragment.getArguments().getSerializable(EventFragment.EXTRA_EVENT_ID);
        if (!EventManager.get(this).getEvent(id).isRepeated(day)) clearDetail(fm);
    }

    @Override
    public void onEventDeleted(Event event) {
        FragmentManager fm = getSupportFragmentManager();
        EventFragment ef = (EventFragment) fm.findFragmentById(R.id.detailFragmentContainer);
        mShouldRestart = true;
        if (ef != null && ef.getEvent() == event) clearDetail(fm);
    }

    @Override
    public void onActionModeFinished() {
        restartService();
    }

    private void clearDetail(FragmentManager fm) {
        Fragment detail = fm.findFragmentById(R.id.detailFragmentContainer);
        if (detail != null) {
            fm.beginTransaction().remove(detail).commit();
            restartService();
        }
    }

    private void restartService() {
        if (mShouldRestart) {
            TimerService.restartService(this);
            mShouldRestart = false;
        }
    }
}