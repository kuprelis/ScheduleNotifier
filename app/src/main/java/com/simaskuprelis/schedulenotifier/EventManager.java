package com.simaskuprelis.schedulenotifier;

import android.content.Context;

import java.util.ArrayList;
import java.util.UUID;

public class EventManager {
    private static final String TAG = "EventManager";

    private static EventManager sEventManager;
    private Context mAppContext;
    private ArrayList<Event> mEvents;

    private EventManager(Context appContext) {
        mAppContext = appContext;
        mEvents = new ArrayList<>();
    }

    public static EventManager get(Context c) {
        if (sEventManager == null) {
            sEventManager = new EventManager(c.getApplicationContext());
        }
        return sEventManager;
    }

    public void addEvent(Event event) {
        mEvents.add(event);
    }

    public void deleteEvent(Event event) {
        mEvents.remove(event);
    }

    public Event getEvent(UUID id) {
        for (Event e : mEvents) {
            if (e.getId().equals(id)) {
                return e;
            }
        }
        return null;
    }

    public ArrayList<Event> getEvents(int weekday) {
        ArrayList<Event> events = new ArrayList<>();
        for (Event e : mEvents) {
            if (e.isRepeatedOn(weekday))
                events.add(e);
        }
        return events;
    }
}