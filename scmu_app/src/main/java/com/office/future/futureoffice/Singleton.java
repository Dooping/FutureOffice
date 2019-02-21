package com.office.future.futureoffice;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by Dooping on 04/04/2016.
 */
public class Singleton {
    private static Singleton ourInstance = new Singleton();
    private static final String CONFIG_FILE = "configFile.conf";
    private static final String CALENDAR_FILE = "calendarFile.conf";
    private static final String PREFERENCES_FILE = "preferencesFile.conf";

    public static Singleton getInstance() {
        return ourInstance;
    }

    private Singleton() {
    }

    public MyConfig getConfigs(Context context){//ESP8266HUZZAH
        ObjectInputStream input;
        MyConfig config = null;

        try {
            input = new ObjectInputStream(new FileInputStream(new File(new File(context.getFilesDir(),"")+File.separator+CONFIG_FILE)));
            config = (MyConfig) input.readObject();
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }

    public void setConfigs(MyConfig config, Context context){
        ObjectOutput out;
        try {
            out = new ObjectOutputStream(new FileOutputStream(new File(new File(context.getFilesDir(),"")+File.separator+CONFIG_FILE)));
            out.writeObject(config);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MyPreferences getPreferences(Context context){//ESP8266HUZZAH
        ObjectInputStream input;
        MyPreferences preferences = null;

        try {
            input = new ObjectInputStream(new FileInputStream(new File(new File(context.getFilesDir(),"")+File.separator+PREFERENCES_FILE)));
            preferences = (MyPreferences) input.readObject();
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return preferences;
    }

    public void setPreferences(MyPreferences preferences, Context context){
        ObjectOutput out;
        try {
            out = new ObjectOutputStream(new FileOutputStream(new File(new File(context.getFilesDir(),"")+File.separator+PREFERENCES_FILE)));
            out.writeObject(preferences);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNextAppointment(MyEvent event, Context context){
        ObjectOutput out;
        try {
            File file = new File(new File(context.getFilesDir(),"")+File.separator+CALENDAR_FILE);
            if(event == null && file.exists())
                file.delete();
            out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(event);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MyEvent getNextAppointment(Context context){
        ObjectInputStream input;
        MyEvent event = null;

        try {
            input = new ObjectInputStream(new FileInputStream(new File(new File(context.getFilesDir(),"")+File.separator+CALENDAR_FILE)));
            event = (MyEvent) input.readObject();
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    public List<String> getEmails(Context context, long id) {
        List<String> emails = new ArrayList<>();
        String[] INSTANCE_PROJECTION = new String[]{
                CalendarContract.Attendees.ATTENDEE_EMAIL,  // 0
        };

        // The indices for the projection array above.
        final int PROJECTION_EMAIL_INDEX = 0;



        Cursor cur;
        ContentResolver cr = context.getContentResolver();

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Attendees.CONTENT_URI.buildUpon();


        String selection = CalendarContract.Attendees.EVENT_ID + " = ?";
        String[] selectionArgs = new String[] {Long.toString(id)};
        // Submit the query
        cur = cr.query(builder.build(),
                INSTANCE_PROJECTION,
                selection,
                selectionArgs,
                null);

        while (cur.moveToNext()) {
            String email = null;

            // Get the field values
            email = cur.getString(PROJECTION_EMAIL_INDEX);
            emails.add(email);
        }
        return emails;
    }
}
