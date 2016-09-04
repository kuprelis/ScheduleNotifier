package com.simaskuprelis.schedulenotifier.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.simaskuprelis.schedulenotifier.Event;
import com.simaskuprelis.schedulenotifier.EventManager;
import com.simaskuprelis.schedulenotifier.R;
import com.simaskuprelis.schedulenotifier.Utils;

import java.util.ArrayList;
import java.util.List;

public class EventListFragment extends Fragment {
    private static final String TAG = "EventListFragment";
    private static final String KEY_DAY = "day";

    private Callbacks mCallbacks;

    private ArrayList<Event> mEvents;
    private int mDay;

    private RecyclerView mRecyclerView;
    private TextView mEmptyListText;

    public static EventListFragment newInstance(int day) {
        Bundle args = new Bundle();
        args.putInt(KEY_DAY, day);
        EventListFragment fragment = new EventListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity)
            mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDay = getArguments().getInt(KEY_DAY);
        mEvents = EventManager.get(getContext()).getEvents(mDay);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_list, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new EventListAdapter(mEvents));

        mEmptyListText = (TextView) v.findViewById(R.id.empty_list_text);
        mEmptyListText.setVisibility(mEvents.size() > 0 ? View.GONE : View.VISIBLE);

        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public interface Callbacks {
        void onEventSelected(Event event, int day);
    }

    private class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {
        private List<Event> mEvents;

        public EventListAdapter(List<Event> events) {
            mEvents = events;
        }

        @Override
        public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_event, parent, false);
            return new EventViewHolder(v);
        }

        @Override
        public void onBindViewHolder(EventViewHolder holder, final int position) {
            final Event e = mEvents.get(position);

            String title = e.getTitle();
            if (title != null && title.length() > 0)
                holder.mTitle.setText(e.getTitle());
            else
                holder.mTitle.setText(R.string.no_title);

            Resources res = getContext().getResources();
            for (int i = 0; i < holder.mDisplay.getChildCount(); i++) {
                TextView tv = (TextView) holder.mDisplay.getChildAt(i);
                if (e.isRepeated(i))
                    tv.setTextColor(res.getColor(R.color.accent));
                else
                    tv.setTextColor(res.getColor(R.color.secondary_text));
            }

            StringBuilder sb = new StringBuilder();
            boolean is24hour = DateFormat.is24HourFormat(getContext());
            sb.append(Utils.formatTime(e.getStartTime(), is24hour));
            sb.append(getString(R.string.time_range_separator));
            sb.append(Utils.formatTime(e.getEndTime(), is24hour));
            holder.mTime.setText(sb.toString());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallbacks.onEventSelected(e, mDay);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mEvents.size();
        }

        public class EventViewHolder extends RecyclerView.ViewHolder {
            protected TextView mTitle;
            protected TextView mTime;
            protected LinearLayout mDisplay;

            public EventViewHolder(View v) {
                super(v);
                mTitle =  (TextView) v.findViewById(R.id.list_item_title);
                mTime = (TextView) v.findViewById(R.id.list_item_time);
                mDisplay = (LinearLayout) v.findViewById(R.id.weekday_display);
            }
        }
    }
}