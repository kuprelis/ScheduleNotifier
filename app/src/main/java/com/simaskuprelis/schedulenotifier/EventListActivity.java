package com.simaskuprelis.schedulenotifier;

import android.content.Intent;
import android.support.v4.app.Fragment;

public class EventListActivity extends SingleFragmentActivity
        implements EventListFragment.Callbacks {

    @Override
    protected Fragment createFragment() {
        return new EventListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onEventSelected(Event event) {
        Intent i = new Intent(this, EventActivity.class);
        i.putExtra(EventFragment.EXTRA_EVENT_ID, event.getId());
        startActivity(i);
    }
}