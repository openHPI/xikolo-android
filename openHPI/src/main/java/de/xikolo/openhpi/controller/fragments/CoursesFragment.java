package de.xikolo.openhpi.controller.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.xikolo.openhpi.R;
import de.xikolo.openhpi.manager.CoursesManager;
import de.xikolo.openhpi.model.Course;
import de.xikolo.openhpi.model.Courses;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CoursesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CoursesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CoursesFragment extends Fragment implements CoursesManager.OnCoursesReceivedListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = CoursesFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView mText;

    private CoursesManager mCoursesManager;

    private SwipeRefreshLayout mRefreshLayout;

//    private OnFragmentInteractionListener mListener;

    public CoursesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WebViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CoursesFragment newInstance() {
        CoursesFragment fragment = new CoursesFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_courses, container, false);

        mText = (TextView) layout.findViewById(R.id.text);
        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshlayout);

        mRefreshLayout.setColorScheme(R.drawable.orange,
                R.drawable.red,
                R.drawable.yellow,
                R.drawable.red);
        mRefreshLayout.setOnRefreshListener(this);

        mCoursesManager = new CoursesManager(this, getActivity());
        onRefresh();

        return layout;
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);
        mCoursesManager.requestCourses();
    }

    @Override
    public void onCoursesReceived(Courses courses) {
        mRefreshLayout.setRefreshing(false);
        mText.setText("");
        for (Course course : courses.getCourses()) {
            mText.append(course.title + "\n");
        }
    }

    @Override
    public void onCoursesRequestCancelled() {
        mRefreshLayout.setRefreshing(false);
    }

    //    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.webview, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_refresh:
                onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        public void onFragmentInteraction(Uri uri);
//    }

}
