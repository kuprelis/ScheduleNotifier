package com.simaskuprelis.schedulenotifier;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

public class EventManager {
    private static final String TAG = "EventManager";
    private static final String FILENAME = "events.json";

    private static EventManager sEventManager;
    private Context mAppContext;
    private ArrayList<Event> mEvents;

    private EventManager(Context appContext) {
        mAppContext = appContext;
        try {
            mEvents = loadEvents();
            Log.i(TAG, "Events loaded");
        } catch (Exception e) {
            mEvents = new ArrayList<>();
            Log.e(TAG, "Error loading events: " + e);
        }
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

    public ArrayList<Event> getEvents(int day) {
        ArrayList<Event> events = new ArrayList<>();
        if (day == -1) {
            events.addAll(mEvents);
        } else {
            for (Event e : mEvents) {
                if (e.isRepeated(day))
                    events.add(e);
            }
        }
        return events;
    }

    private void saveEvents() throws IOException {
        Gson gson = new Gson();
        JSONArray array = new JSONArray();
        for (Event e : mEvents) array.put(gson.toJson(e));
        Writer writer = null;
        try {
            OutputStream out = mAppContext.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(array.toString());
        } finally {
            if (writer != null) writer.close();
        }
    }

    private ArrayList<Event> loadEvents() throws IOException, JSONException {
        ArrayList<Event> events = new ArrayList<>();
        BufferedReader reader = null;
        try {
            InputStream in = mAppContext.openFileInput(FILENAME);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) jsonString.append(line);
            JSONArray array = (JSONArray)new JSONTokener(jsonString.toString()).nextValue();
            Gson gson = new Gson();
            for (int i = 0; i < array.length(); i++)
                events.add(gson.fromJson(array.getString(i), Event.class));
        } catch (FileNotFoundException e) {
            // Only on fresh start
        } finally {
            if (reader != null)
                reader.close();
        }
        return events;
    }



    public boolean save() {
        Collections.sort(mEvents, new Comparator<Event>() {
            @Override
            public int compare(Event lhs, Event rhs) {
                long a = lhs.getStartTime();
                long b = rhs.getStartTime();
                if (a > b) return 1;
                if (a < b) return -1;
                return 0;
            }
        });
        try {
            saveEvents();
            Log.i(TAG, "Events saved");
            return true;
        } catch (IOException ioe) {
            Log.e(TAG, "Error saving events: " + ioe);
            return false;
        }
    }
}