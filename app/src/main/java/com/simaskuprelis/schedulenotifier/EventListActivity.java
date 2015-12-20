package com.simaskuprelis.schedulenotifier;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class EventListActivity extends SingleFragmentActivity
        implements EventListFragment.Callbacks {

    private static final int REQUEST_EDIT = 0;

    @Override
    protected Fragment createFragment() {
        return new EventListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_EDIT) {
            EventManager.get(this).save();
            FragmentManager fm = getSupportFragmentManager();
            EventListFragment fragment =
                    (EventListFragment)fm.findFragmentById(R.id.fragmentContainer);
            fragment.updateUI();
        }
    }

    @Override
    public void onEventSelected(Event event, int day) {
        Intent i = new Intent(this, EventPagerActivity.class);
        i.putExtra(EventFragment.EXTRA_EVENT_ID, event.getId());
        i.putExtra(EventPagerActivity.EXTRA_DAY, day);
        startActivityForResult(i, REQUEST_EDIT);
    }
}