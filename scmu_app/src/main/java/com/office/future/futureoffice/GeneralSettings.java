package com.office.future.futureoffice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GeneralSettings.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GeneralSettings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeneralSettings extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MyConfig config;

    private OnFragmentInteractionListener mListener;

    public GeneralSettings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GeneralSettings.
     */
    // TODO: Rename and change types and number of parameters
    public static GeneralSettings newInstance(String param1, String param2) {
        GeneralSettings fragment = new GeneralSettings();
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
        View view = inflater.inflate(R.layout.fragment_general_settings, container, false);
        Button selectWorkplace = (Button)view.findViewById(R.id.button);
        selectWorkplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WorkAddressActivity.class);
                if (config != null) {
                    Bundle b = new Bundle();
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (0) : {
                if (resultCode == Activity.RESULT_OK) {
                    Bundle b = data.getExtras();
                    config.setLatitude(b.getDouble("latitude"));
                    config.setLongitude(b.getDouble("longitude"));
                    config.setAddress(b.getString("address", ""));
                }
                break;
            }
        }
    }

    @Override
    public void onPause(){
        Singleton.getInstance().setConfigs(config, getContext());
        super.onPause();
    }

    @Override
    public void onResume(){
        config = Singleton.getInstance().getConfigs(getContext());
        super.onResume();
    }

}
