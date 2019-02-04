package de.xikolo.controllers.helper;

import android.view.View;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import de.xikolo.utils.AndroidDimenUtil;

public class CollapsingToolbarHelper {

    public static final String TAG = CollapsingToolbarHelper.class.getSimpleName();

    public static void lockCollapsingToolbar(String title, AppBarLayout appBarLayout, CollapsingToolbarLayout collapsingToolbarLayout, Toolbar toolbar, View scrimTop, View scrimBottom) {
        if (appBarLayout != null) {
            appBarLayout.setExpanded(false, false);
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
            lp.height = AndroidDimenUtil.getActionBarHeight() + AndroidDimenUtil.getStatusBarHeight();
        }

        if (collapsingToolbarLayout != null)
            collapsingToolbarLayout.setTitleEnabled(false);

        if (toolbar != null)
            toolbar.setTitle(title);

        if (scrimTop != null)
            scrimTop.setVisibility(View.INVISIBLE);

        if (scrimBottom != null)
            scrimBottom.setVisibility(View.INVISIBLE);
    }

}
