package com.simaskuprelis.schedulenotifier.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.simaskuprelis.schedulenotifier.Event;
import com.simaskuprelis.schedulenotifier.fragment.EventFragment;
import com.simaskuprelis.schedulenotifier.EventManager;
import com.simaskuprelis.schedulenotifier.R;

import java.util.ArrayList;
import java.util.UUID;

public class EventPagerActivity extends AppCompatActivity implements EventFragment.Callbacks {
    public static final String EXTRA_DAY = "com.simaskuprelis.schedulenotifier.day";

    private ViewPager mViewPager;
    private ArrayList<Event> mEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.viewPager);
        setContentView(mViewPager);
        mEvents = EventManager.get(this).getEvents(getIntent().getIntExtra(EXTRA_DAY, -1));
        mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Event e = mEvents.get(position);
                return EventFragment.newInstance(e.getId());
            }

            @Override
            public int getCount() {
                return mEvents.size();
            }
        });
        UUID id = (UUID) getIntent().getSerializableExtra(EventFragment.EXTRA_EVENT_ID);
        for (int i = 0; i < mEvents.size(); i++) {
            if (mEvents.get(i).getId().equals(id)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public void onEventUpdated(Event event) {
        setResult(RESULT_OK);
        if (event == null) finish();
    }
}