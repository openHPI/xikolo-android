package de.xikolo.controllers.main

import android.support.v4.app.Fragment

interface MainFragment {

    val fragment: Fragment

    val activityCallback: MainActivityCallback?
}
