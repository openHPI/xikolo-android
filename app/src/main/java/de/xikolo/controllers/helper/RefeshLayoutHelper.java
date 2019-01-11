package de.xikolo.controllers.helper;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.xikolo.R;

public class RefeshLayoutHelper {

    public static void setup(SwipeRefreshLayout layout, SwipeRefreshLayout.OnRefreshListener listener) {
        layout.setColorSchemeResources(
                R.color.apptheme_second,
                R.color.apptheme_main);
        layout.setOnRefreshListener(listener);
    }

}
