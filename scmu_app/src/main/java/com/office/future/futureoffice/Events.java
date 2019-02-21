package com.office.future.futureoffice;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.office.future.futureoffice.dummy.DummyContent;
import com.office.future.futureoffice.dummy.DummyContent.DummyItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class Events extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private List<MyEvent> events;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public Events() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static Events newInstance(int columnCount) {
        Events fragment = new Events();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        events = new ArrayList<>();
        getCalendar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyEventRecyclerViewAdapter(events, mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(MyEvent item);
    }

    private void getCalendar() {
        String DEBUG_TAG = "MyActivity";
        String[] INSTANCE_PROJECTION = new String[]{
                CalendarContract.Instances.EVENT_ID,      // 0
                CalendarContract.Instances.BEGIN,         // 1
                CalendarContract.Instances.END,           // 2
                CalendarContract.Instances.TITLE,         // 3
                CalendarContract.Instances.EVENT_LOCATION // 4
        };

        // The indices for the projection array above.
        final int PROJECTION_ID_INDEX = 0;
        final int PROJECTION_BEGIN_INDEX = 1;
        final int PROJECTION_END_INDEX = 2;
        final int PROJECTION_TITLE_INDEX = 3;
        final int PROJECTION_LOCATION_INDEX = 4;

        // Specify the date range you want to search for recurring
        // event instances
        final Calendar beginTime = Calendar.getInstance();
        final Calendar endTime = Calendar.getInstance();

        // get events from the start of the day until the same evening.
        beginTime.set(beginTime.get(Calendar.YEAR), beginTime.get(Calendar.MONTH), beginTime.get(Calendar.DAY_OF_MONTH), beginTime.get(Calendar.HOUR_OF_DAY), beginTime.get(Calendar.MINUTE), beginTime.get(Calendar.SECOND));
        endTime.set(beginTime.get(Calendar.YEAR), beginTime.get(Calendar.MONTH), beginTime.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        endTime.add(Calendar.DAY_OF_MONTH,7);
        long startMillis = beginTime.getTimeInMillis();
        long endMillis = endTime.getTimeInMillis();

        Cursor cur;
        ContentResolver cr = getContext().getContentResolver();

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // Submit the query
        cur = cr.query(builder.build(),
                INSTANCE_PROJECTION,
                null,
                null,
                CalendarContract.Events.DTSTART + " ASC");

        while (cur.moveToNext()) {
            String title = null;
            String location = null;
            long eventID = 0;
            long beginVal = 0;
            long endVal = 0;

            // Get the field values
            eventID = cur.getLong(PROJECTION_ID_INDEX);
            beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
            title = cur.getString(PROJECTION_TITLE_INDEX);
            endVal = cur.getLong(PROJECTION_END_INDEX);
            location = cur.getString(PROJECTION_LOCATION_INDEX);

            if(beginVal!=endVal-86400000) {
                MyEvent event = new MyEvent(eventID, beginVal, endVal, title, location);
                events.add(event);
            }
        }
        Collections.sort(events, new CustomComparator());
    }

    public class CustomComparator implements Comparator<MyEvent> {
        @Override
        public int compare(MyEvent o1, MyEvent o2) {
            if (o1.getBegin()<o2.getBegin())
                return -1;
            else if (o1.getBegin()==o2.getBegin())
                return 0;
            else return 1;
        }
    }

}
