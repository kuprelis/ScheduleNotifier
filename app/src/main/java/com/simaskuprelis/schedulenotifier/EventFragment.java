package com.simaskuprelis.schedulenotifier;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

public class EventFragment extends Fragment {
    public static final String EXTRA_EVENT_ID = "com.simaskuprelis.schedulenotifier.event_id";
    private Event mEvent;

    public static EventFragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_EVENT_ID, id);
        EventFragment fragment = new EventFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID id = (UUID)getArguments().getSerializable(EXTRA_EVENT_ID);
        mEvent = EventManager.get(getActivity()).getEvent(id);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event, container, false);
        return v;
    }
}