package com.office.future.futureoffice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Office.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Office#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Office extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String urlETPart1 = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=";
    String urlETPart2 = "&destinations=";
    String urlETPart3 = "&mode=driving&language=en&key=%20AIzaSyC8VXekt7EaFhcCwsouuNdArooPfDwMgbM";

    HttpURLConnection urlConnection;

    String distance;

    String estimatedTime;

    //View view;

    Switch switchButton;
    TextView textView;
    String switchOn = "Switch is ON";
    String switchOff = "Switch is OFF";

    TextView luminosityText;
    TextView temperatureText;

    private OnFragmentInteractionListener mListener;

    public Office() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Office.
     */
    // TODO: Rename and change types and number of parameters
    public static Office newInstance(String param1, String param2) {
        Office fragment = new Office();
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


    }

    @Override
    public void onResume() {
        super.onResume();

        String serverAddress = Singleton.getInstance().getConfigs(getContext()).getServerAddress();
        serverAddress = (serverAddress == null || serverAddress.equals("")) ? MyConfig.DEFAULT_SERVER_ADDRESS : serverAddress;

        new RequestTask().execute("http://"+serverAddress+":8080/arduino/data");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        try {

            LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            JSONObject jsonRootObject = new JSONObject(getJSON(urlETPart1 + latitude+","+longitude +
                    urlETPart2 + Singleton.getInstance().getConfigs(getContext()).getLatitude() + ","
                    + Singleton.getInstance().getConfigs(getContext()).getLongitude() + urlETPart3));

            JSONArray rows = jsonRootObject.getJSONArray("rows");
            JSONObject row0 = rows.getJSONObject(0);
            JSONArray elements = row0.getJSONArray("elements");
            JSONObject element0 = elements.getJSONObject(0);
            JSONObject distanceTo = element0.getJSONObject("distance");
            distance = distanceTo.getString("text");
            JSONObject duration = element0.getJSONObject("duration");
            estimatedTime = duration.getString("text");

            TextView distanceTextView = (TextView) this.getView().findViewById(R.id.distance);
            distanceTextView.setText(distance);
            TextView estimatedTimeTextView = (TextView) this.getView().findViewById(R.id.estimatedTime);
            estimatedTimeTextView.setText(estimatedTime);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_office, container, false);
        TextView distanceTextView = (TextView) view.findViewById(R.id.distance);
        distanceTextView.setText(distance);
        TextView estimatedTimeTextView = (TextView) view.findViewById(R.id.estimatedTime);
        estimatedTimeTextView.setText(estimatedTime);
        temperatureText = (TextView) view.findViewById(R.id.temperatureText);
        luminosityText = (TextView) view.findViewById(R.id.luminosityText);

        Switch blinds = (Switch) view.findViewById(R.id.blindsSwitch);
        blinds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PostTask pt;
                String serverAddress = Singleton.getInstance().getConfigs(getContext()).getServerAddress();
                serverAddress = (serverAddress == null || serverAddress.equals("")) ? MyConfig.DEFAULT_SERVER_ADDRESS : serverAddress;
                if(isChecked)
                    pt = new PostTask(serverAddress, "blinds/open");
                else
                    pt = new PostTask(serverAddress, "blinds/closed");
                pt.execute();
            }
        });

        Switch ac = (Switch) view.findViewById(R.id.acSwitch);
        ac.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PostTask pt;
                String serverAddress = Singleton.getInstance().getConfigs(getContext()).getServerAddress();
                serverAddress = (serverAddress == null || serverAddress.equals("")) ? MyConfig.DEFAULT_SERVER_ADDRESS : serverAddress;
                if(isChecked)
                    pt = new PostTask(serverAddress, "ac/on");
                else
                    pt = new PostTask(serverAddress, "ac/off");
                pt.execute();
            }
        });

        Button sendEmail = (Button)view.findViewById(R.id.sendEmail);
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyEvent event = Singleton.getInstance().getNextAppointment(getContext());
                List<String> emails = Singleton.getInstance().getEmails(getContext(), event.getId());
                Intent i = new Intent(Intent.ACTION_SEND);
                String[] emailsArray = new String[emails.size()];
                emails.toArray(emailsArray);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , emailsArray);
                i.putExtra(Intent.EXTRA_SUBJECT, event.getTitle());
                i.putExtra(Intent.EXTRA_TEXT, "Estimated time of arrival: "+estimatedTime);
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button goMap = (Button)view.findViewById(R.id.goMap);
        goMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyConfig config = Singleton.getInstance().getConfigs(getContext());
                Intent intent = new Intent(getContext(), WorkAddressActivity.class);
                if (config != null) {
                    Bundle b = new Bundle();
                    b.putBoolean("isEdit", false);
                    b.putString("address", config.getAddress());
                    b.putDouble("latitude", config.getLatitude());
                    b.putDouble("longitude", config.getLongitude());
                    intent.putExtras(b);
                }
                startActivityForResult(intent, 0);
            }
        });

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

    private class PostTask extends AsyncTask<String, String, String> {
        private String address;
        private String path;
        public PostTask(String address, String path) {
            this.address = address;
            this.path = path;
        }

        @Override
        protected String doInBackground(String... data) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://"+address+":8080/arduino/"+path);

            try {
                //execute http post
                HttpResponse response = httpclient.execute(httppost);

            } catch (ClientProtocolException e) {

            } catch (IOException e) {

            }
            return "executed";
        }
    }

    class RequestTask extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject object = new JSONObject(result);
                String s = object.getString("temperature")+"ºC";
                temperatureText.setText(s);
                s = object.getString("luminosity");
                if(s!=null)
                    luminosityText.setText(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
