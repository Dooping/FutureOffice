package com.office.future.futureoffice;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AdvancedSettings.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AdvancedSettings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdvancedSettings extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MyConfig config;

    EditText fromHour;
    EditText toHour;
    CheckBox checkBoxIntrusion;
    EditText serverEditText;

    private OnFragmentInteractionListener mListener;

    public AdvancedSettings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdvancedSettings.
     */
    // TODO: Rename and change types and number of parameters
    public static AdvancedSettings newInstance(String param1, String param2) {
        AdvancedSettings fragment = new AdvancedSettings();
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

        config = Singleton.getInstance().getConfigs(getContext());
        if (config == null) {
            config = new MyConfig();
            Intent intent = new Intent(getContext(), WorkAddressActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_advanced_settings, container, false);
        serverEditText = (EditText)view.findViewById(R.id.serverEditText);
        String serverAddress = config.getServerAddress();
        serverEditText.setHint(MyConfig.DEFAULT_SERVER_ADDRESS);
        if (serverAddress != null)
            serverEditText.setText(serverAddress);
        serverEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                config.setServerAddress(serverEditText.getText().toString());
                Singleton.getInstance().setConfigs(config,getContext());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });


        fromHour= (EditText)view.findViewById(R.id.fromHour);
        toHour= (EditText)view.findViewById(R.id.toHour);

        fromHour.setFocusable(false);
        toHour.setFocusable(false);

        fromHour.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickedEditHour(fromHour.getId(), fromHour.getText().toString());
                    }
                }
        );
        toHour.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickedEditHour(toHour.getId(), toHour.getText().toString());
                    }
                }
        );

        final TextView fromLabel= (TextView)view.findViewById(R.id.fromLabel);
        final TextView toLabel= (TextView)view.findViewById(R.id.toLabel);
        checkBoxIntrusion= (CheckBox)view.findViewById(R.id.checkBoxIntrusion);
        checkBoxIntrusion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBoxIntrusion.isChecked()){
                    String serverAddress = Singleton.getInstance().getConfigs(getContext()).getServerAddress();
                    serverAddress = (serverAddress == null || serverAddress.equals("")) ? MyConfig.DEFAULT_SERVER_ADDRESS : serverAddress;
                    PostTask pt = new PostTask(serverAddress, "intrusion/on");
                    pt.execute();
                    /*fromHour.setEnabled(true);
                    toHour.setEnabled(true);
                    fromLabel.setEnabled(true);
                    toLabel.setEnabled(true);*/
                }else{
                    String serverAddress = Singleton.getInstance().getConfigs(getContext()).getServerAddress();
                    serverAddress = (serverAddress == null || serverAddress.equals("")) ? MyConfig.DEFAULT_SERVER_ADDRESS : serverAddress;
                    PostTask pt = new PostTask(serverAddress, "intrusion/off");
                    pt.execute();
                    /*fromHour.setEnabled(false);
                    toHour.setEnabled(false);
                    fromLabel.setEnabled(false);
                    toLabel.setEnabled(false);*/
                }
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

    @Override
    public void onPause(){
        //config.setServerAddress(serverEditText.getText().toString());
        Singleton.getInstance().setConfigs(config,getContext());
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        super.onPause();
    }

    public void clickedEditHour(int editTextId, String time){
        DialogFragment newFragment = new TimerPickerFragment();
        Bundle args= new Bundle();
        args.putInt("editTextId",editTextId);
        args.putString("editTextTime", time);
        newFragment.setArguments(args);
        newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
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
}
