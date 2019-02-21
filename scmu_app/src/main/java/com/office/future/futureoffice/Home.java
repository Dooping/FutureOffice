package com.office.future.futureoffice;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.LocationListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Home.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Home extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    HttpURLConnection urlConnection;

    String urlETPart1 = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=";
    String urlETPart2 = "&destinations=";
    String urlETPart3 = "&mode=driving&language=en&key=%20AIzaSyC8VXekt7EaFhcCwsouuNdArooPfDwMgbM";

    String urlWeatherPart1 = "http://api.openweathermap.org/data/2.5/weather?lat=";
    String urlWeatherPart2 = "&lon=";
    String urlWeatherPart3 = "&appid=8f77266320fd293d1b4f253a0cb063eb";

    String temperature;

    String estimatedTime;

    List<MyEvent> events;

    private OnFragmentInteractionListener mListener;

    public Home() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Home.
     */
    // TODO: Rename and change types and number of parameters
    public static Home newInstance(String param1, String param2) {
        Home fragment = new Home();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        events = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        getCalendar();
        LinearLayout appointments = (LinearLayout)view.findViewById(R.id.appointment);
        TextView noAppointments = (TextView)view.findViewById(R.id.noAppointment);
        if(events.size()==0)
            appointments.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        else{
            noAppointments.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            MyEvent event = events.get(0);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView date = (TextView) view.findViewById(R.id.date);
            TextView begin = (TextView) view.findViewById(R.id.begin);
            TextView end = (TextView) view.findViewById(R.id.end);
            TextView location = (TextView) view.findViewById(R.id.localization);
            title.setText(event.getTitle());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(event.getBegin());
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            date.setText(formatter.format(calendar.getTime()));
            formatter = new SimpleDateFormat("HH:mm");
            begin.setText(formatter.format(calendar.getTime()));
            calendar.setTimeInMillis(event.getEnd());
            end.setText(formatter.format(calendar.getTime()));
            location.setText(event.getLocation());
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        try {
            JSONObject jsonRootObject = new JSONObject(getJSON(
                    urlWeatherPart1 + Singleton.getInstance().getConfigs(getContext()).getLatitude() +
                            urlWeatherPart2 + Singleton.getInstance().getConfigs(getContext()).getLongitude() +
                            urlWeatherPart3));
            JSONObject coords = jsonRootObject.getJSONObject("main");
            Double temperatureBeforeConvertion = Double.parseDouble(coords.optString("temp").toString());
            temperatureBeforeConvertion = temperatureBeforeConvertion - 273.15;
            int temperatureAfterConvertion = temperatureBeforeConvertion.intValue();
            temperature = Integer.toString(temperatureAfterConvertion);

            LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(null != location) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();


                jsonRootObject = new JSONObject(getJSON(urlETPart1 + latitude + "," + longitude +
                        urlETPart2 + Singleton.getInstance().getConfigs(getContext()).getLatitude() + ","
                        + Singleton.getInstance().getConfigs(getContext()).getLongitude() + urlETPart3));

                JSONArray rows = jsonRootObject.getJSONArray("rows");
                JSONObject row0 = rows.getJSONObject(0);
                JSONArray elements = row0.getJSONArray("elements");
                JSONObject element0 = elements.getJSONObject(0);
                JSONObject duration = element0.getJSONObject("duration");
                estimatedTime = duration.getString("text");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView temperatureTextView = (TextView) view.findViewById(R.id.temperature);
        temperatureTextView.setText(temperature + "º");
        TextView estimatedTimeTextView = (TextView) view.findViewById(R.id.estimatedTime);
        estimatedTimeTextView.setText(estimatedTime);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public String getJSON(String jsonUrl) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(jsonUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return result.toString();
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
        if (events.size()>0)
            Singleton.getInstance().setNextAppointment(events.get(0), getContext());
        else
            Singleton.getInstance().setNextAppointment(null, getContext());
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
