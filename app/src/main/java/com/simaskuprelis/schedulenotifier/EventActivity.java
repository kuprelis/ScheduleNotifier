package com.simaskuprelis.schedulenotifier;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.UUID;

public class EventActivity extends SingleFragmentActivity {
    private UUID mId;

    @Override
    protected Fragment createFragment() {
        return EventFragment.newInstance(mId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mId = (UUID)getIntent().getSerializableExtra(EventFragment.EXTRA_EVENT_ID);
        super.onCreate(savedInstanceState);
    }
}