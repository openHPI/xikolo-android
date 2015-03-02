package de.xikolo.controller;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import de.xikolo.R;
import de.xikolo.controller.exceptions.WrongParameterException;

public class QuizDemoActivity extends BaseActivity {

    public static final String TAG = QuizDemoActivity.class.getSimpleName();

    public static final String ARG_URL = "arg_url";
    
    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizdemo);
        setupActionBar();

        Bundle b = getIntent().getExtras();
        if (b == null || !b.containsKey(ARG_URL)) {
            throw new WrongParameterException();
        } else {
            this.mUrl = b.getString(ARG_URL);
        }

        setTitle(getString(R.string.title_section_quiz));

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, WebViewFragment.newInstance(mUrl, true, false), tag);
            transaction.commit();
        }
    }

}
