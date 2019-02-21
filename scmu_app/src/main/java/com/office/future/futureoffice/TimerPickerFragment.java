package com.office.future.futureoffice;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TimerPickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        int id=getArguments().getInt("editTextId");
        EditText activityButton = (EditText)getActivity().findViewById(id);
        String zero = minute<10 ? "0":"";
        String date =hourOfDay+":"+zero+minute;
        activityButton.setText(date);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour,minute;
        String time = getArguments().getString("editTextTime");
        SimpleDateFormat formatterTime=new SimpleDateFormat("H:m");
        try {
            Date date= formatterTime.parse(time);
            hour = date.getHours();
            minute = date.getMinutes();
        } catch (Exception e) {
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, true);
    }



}
