package de.xikolo.controllers.secondscreen;


import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import de.xikolo.R;
import de.xikolo.controllers.base.BaseActivity;

public class SecondScreenActivity extends BaseActivity {

    public static final String TAG = SecondScreenActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_no_scroll);
        setupActionBar();

        setTitle(getString(R.string.title_section_second_screen) + " (" + getString(R.string.title_section_beta) + ")");

        String tag = "second_screen";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, new SecondScreenFragment(), tag);
            transaction.commit();
        }
    }

}
