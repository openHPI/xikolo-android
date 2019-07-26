package de.xikolo.controllers.main

import androidx.annotation.IdRes

interface MainActivityCallback {

    fun onFragmentAttached(@IdRes itemId: Int, title: String? = null)

    fun selectDrawerSection(@IdRes itemId: Int)

}
