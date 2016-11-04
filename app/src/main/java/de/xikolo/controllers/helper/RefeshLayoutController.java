package de.xikolo.controllers.helper;

import android.support.v4.widget.SwipeRefreshLayout;

import de.xikolo.R;

public class RefeshLayoutController {

    public static void setup(SwipeRefreshLayout layout, SwipeRefreshLayout.OnRefreshListener listener) {
        layout.setColorSchemeResources(
                R.color.apptheme_second,
                R.color.apptheme_main);
        layout.setOnRefreshListener(listener);

    }

}
