package de.xikolo.controller.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.manager.ModuleManager;
import de.xikolo.model.Course;
import de.xikolo.model.Module;

public class CourseLearningsFragment extends ContentFragment {

    public final static String TAG = CourseLearningsFragment.class.getSimpleName();

    private static final String ARG_COURSE = "arg_course";

    private static final String KEY_MODULES = "key_modules";

    private TextView text;

    private ModuleManager mModuleManager;

    private Course mCourse;
    private List<Module> mModules;

    public CourseLearningsFragment() {
        // Required empty public constructor
    }

    public static CourseLearningsFragment newInstance(Course course) {
        CourseLearningsFragment fragment = new CourseLearningsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_COURSE, course);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mModules != null) {
            outState.putParcelableArrayList(KEY_MODULES, (ArrayList<Module>) mModules);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCourse = getArguments().getParcelable(ARG_COURSE);
        }
        if (savedInstanceState != null) {
            mModules = savedInstanceState.getParcelableArrayList(KEY_MODULES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_learnings, container, false);

        text = (TextView) layout.findViewById(R.id.textView1);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        mModuleManager = new ModuleManager(getActivity()) {
            @Override
            public void onModulesRequestReceived(List<Module> modules) {
                if (modules != null) {
                    mModules = modules;
                    for (Module m : modules) {
                        text.append(m.name + "\n");
                    }
                }
            }

            @Override
            public void onModulesRequestCancelled() {
            }
        };

        if (mModules == null) {
            mModuleManager.requestModules(mCourse, true);
        } else {
            for (Module m : mModules) {
                text.append(m.name + "\n");
            }
        }
    }
}
