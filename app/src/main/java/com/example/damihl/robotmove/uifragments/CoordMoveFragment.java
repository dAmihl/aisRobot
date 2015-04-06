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
 * {@link CoordMoveFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CoordMoveFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CoordMoveFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CoordMoveFragment newInstance(int sectionNumber) {
        CoordMoveFragment fragment = new CoordMoveFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public CoordMoveFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_coord_move, container, false);
        return rootView;
    }

}
