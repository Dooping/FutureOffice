package com.office.future.futureoffice;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,Office.OnFragmentInteractionListener,
        Home.OnFragmentInteractionListener, Events.OnListFragmentInteractionListener,
        Settings.OnFragmentInteractionListener, Help.OnFragmentInteractionListener,
        GeneralSettings.OnFragmentInteractionListener,AdvancedSettings.OnFragmentInteractionListener{

    private int mInterval = 30000;
    private Handler mHandler;
    int notificationID;
    HttpURLConnection urlConnection;
    private boolean logout = false;
    private static final String TAG = "MainActivity";

    String urlETPart1 = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=";
    String urlETPart2 = "&destinations=";
    private String urlETPart3 = "&mode=driving&language=en&key=%20AIzaSyC8VXekt7EaFhcCwsouuNdArooPfDwMgbM";

    String estimatedTime;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

      /*  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        if (Singleton.getInstance().getConfigs(getApplicationContext()) == null) {


            /*try {
                addresses = geocoder.getFromLocationName("work", 1);
                if(addresses.size() > 0) {
                    double latitude= addresses.get(0).getLatitude();
                    double longitude= addresses.get(0).getLongitude();
                    config.setLatitude(latitude);
                    config.setLongitude(longitude);

                    Toast.makeText(this,addresses.get(0).getAddressLine(1), Toast.LENGTH_LONG).show();
                    Singleton.getInstance().setConfigs(config, getApplicationContext());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }*/



            //tentar ir buscar a do google maps
            Settings settingsFragment = new Settings();
            replaceFragmentFromMenu(settingsFragment);
        }else{
            Home fragment = new Home();
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit).add(R.id.fragment_container, fragment, "HOME").commit();
        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(RegistrationIntentService.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Intent locationServiceIntent = new Intent(getApplicationContext(), LocationService.class);
                    startService(locationServiceIntent);
                } else {
                    //mInformationTextView.setText(getString(R.string.token_error_message));
                }
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        mHandler = new Handler();
        startRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                LocationManager lm = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                if(null != location && null != Singleton.getInstance().getConfigs(getBaseContext())) {
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

                    StrictMode.setThreadPolicy(policy);

                    JSONObject jsonRootObject = new JSONObject(getJSON(urlETPart1 + latitude + "," + longitude +
                            urlETPart2 + Singleton.getInstance().getConfigs(getBaseContext()).getLatitude() + ","
                            + Singleton.getInstance().getConfigs(getBaseContext()).getLongitude() + urlETPart3));

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
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date systemDate = Calendar.getInstance().getTime();
                String myDate = sdf.format(systemDate);

                MyEvent event = Singleton.getInstance().getNextAppointment(getBaseContext());
                Calendar calendar = Calendar.getInstance();
                String eveDate;
                if(event !=null && !estimatedTime.isEmpty()){
                    calendar.setTimeInMillis(event.getBegin());
                    Date eventDate = calendar.getTime();
                    eveDate = sdf.format(eventDate);
                    try {
                        Date date1 = sdf.parse(myDate);
                        Date date2 = sdf.parse(eveDate);
                        int diffHours = dateDifHours(date1, date2);
                        int diffMins = dateDifMins(date1, date2);
                        int isLate = youLate(diffHours, diffMins);
                        if(isLate>0) {
                            try {
                                updateStatus(); //this function can change value of mInterval.
                            } finally {
                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getBaseContext());

                                Intent notificationIntent = new Intent(getBaseContext(), MainActivity.class);
                                PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, 0);

                                mBuilder.setSmallIcon(R.drawable.ic_stat_name);
                                mBuilder.setContentTitle("You are late for your next appointment");
                                mBuilder.setContentText("By: " + isLate + " minutes");
                                mBuilder.setContentIntent(pendingIntent);
                                mBuilder.setAutoCancel(true);

                                Notification notification = mBuilder.build();

                                mNotificationManager.notify(notificationID, notification);
                                mHandler.postDelayed(mStatusChecker, mInterval);
                            }
                        } else mHandler.postDelayed(mStatusChecker, mInterval);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }else mHandler.postDelayed(mStatusChecker, mInterval);

        }

    };

    void startRepeatingTask() {
        //mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    void updateStatus(){
        mInterval = 30000;
    }

    @Override
    public void onBackPressed(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(getSupportFragmentManager().getBackStackEntryCount() == 0 && !logout){
                String reservation = "Press again to Leave";
                Toast.makeText(this, reservation, Toast.LENGTH_LONG).show();
                logout = true;
            }
            else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                finish();
            }
            else{
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Home fragment = new Home();
            replaceFragmentFromMenu(fragment);
        } else if (id == R.id.nav_office) {
            Office fragment = new Office();
            replaceFragmentFromMenu(fragment);
        } else if (id == R.id.nav_events) {
            Events fragment = new Events();
            replaceFragmentFromMenu(fragment);
        } else if (id == R.id.nav_settings) {
            Settings fragment = new Settings();
            replaceFragmentFromMenu(fragment);
        } else if (id == R.id.nav_help) {
            Help fragment = new Help();
            replaceFragmentFromMenu(fragment);
        } else if (id == R.id.nav_logout) {
            Intent locationServiceIntent = new Intent(this, LocationService.class);
            stopService(locationServiceIntent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void replaceFragmentFromMenu (android.support.v4.app.Fragment fragment){
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        if(backStateName == Home.class.getName())
            manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        else {
            manager.popBackStack(backStateName, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            FragmentTransaction ft = manager.beginTransaction();
            ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
            ft.replace(R.id.fragment_container, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
        logout=false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(MyEvent item) {

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
    private int dateDifHours(Date date1, Date date2){
        long millse = date1.getTime() - date2.getTime();
        long mills = Math.abs(millse);

        int Hours = (int) (mills/(1000 * 60 * 60));

        return Hours;
    }
    private int dateDifMins(Date date1, Date date2){
        long millse = date1.getTime() - date2.getTime();
        long mills = Math.abs(millse);

        int Mins = (int) (mills/(1000*60)) % 60;

        return Mins;
    }
    private int youLate(int hour, int minute){
        int totalMinutes = hour*60 + minute;
        int minutesToWork = 0;
        String[] separated = estimatedTime.split(" ");
        //Log.d("estimatedTime",estimatedTime);
        if(separated[1].equals("hours")) {
            minutesToWork += (Integer.parseInt(separated[0])*60);
            minutesToWork += Integer.parseInt(separated[2]);
        }
        else{
            minutesToWork += Integer.parseInt(separated[0]);
        }
        return minutesToWork-totalMinutes;
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(RegistrationIntentService.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
