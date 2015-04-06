package com.example.damihl.robotmove.uifragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.damihl.robotmove.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OdometryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OdometryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OdometryFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static OdometryFragment newInstance(int sectionNumber) {
        OdometryFragment fragment = new OdometryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public OdometryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_odometry, container, false);
        return rootView;
    }
}
