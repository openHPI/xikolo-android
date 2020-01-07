package de.xikolo.controllers.cast

import android.view.KeyEvent
import android.view.Menu
import android.view.View

import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity

import de.xikolo.R

class CastActivity : ExpandedControllerActivity() {

    override fun onStart() {
        super.onStart()
        showSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        showSystemBars()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.cast, menu)
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item)
        return true
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return CastContext.getSharedInstance(this)
            .onDispatchVolumeKeyEventBeforeJellyBean(event) || super.dispatchKeyEvent(event)
    }

    private fun showSystemBars() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

}
