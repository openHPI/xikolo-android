package de.xikolo.controllers.main

interface MainActivityCallback {

    val isDrawerOpen: Boolean

    fun onFragmentAttached(id: Int, title: String)

    fun updateDrawer()

    fun selectDrawerSection(pos: Int)

}
