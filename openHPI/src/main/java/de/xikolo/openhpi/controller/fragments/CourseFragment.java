package de.xikolo.openhpi.controller.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import de.xikolo.openhpi.R;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CourseFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CourseFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CourseFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentTabHost mTabHost;

//    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WebViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CourseFragment newInstance() {
        CourseFragment fragment = new CourseFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }
    public CourseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize the ViewPager and set an adapter
        View layout = inflater.inflate(R.layout.fragment_course, container, false);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) layout.findViewById(R.id.tabs);
        ViewPager pager = (ViewPager) layout.findViewById(R.id.pager);
        pager.setAdapter(new CoursePagerAdapter(getChildFragmentManager()));

        // Bind the tabs to the ViewPager
        tabs.setViewPager(pager);

        return layout;
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }

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

    public class CoursePagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {
                getString(R.string.learnings),
                getString(R.string.discussions),
                getString(R.string.progress),
                getString(R.string.details),
                getString(R.string.announcements),
                getString(R.string.rooms)
        };

        public CoursePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = CoursesFragment.newInstance() ;
            switch (position) {
                case 0:
                    fragment = WebViewFragment.newInstance(getString(R.string.url_news));
                    break;
                case 1:
                    fragment = CoursesFragment.newInstance();
                    break;
                case 2:
                    fragment = DownloadsFragment.newInstance();
                    break;
                case 3:
                    fragment = WebViewFragment.newInstance(getString(R.string.url_news));
                    break;
                case 4:
                    fragment = CoursesFragment.newInstance();
                    break;
                case 5:
                    fragment = DownloadsFragment.newInstance();
                    break;
            }
            return fragment;
        }

    }

}
