package de.xikolo.controllers.settings

import android.os.Bundle
import android.view.Menu
import de.xikolo.R
import de.xikolo.controllers.base.BaseActivity

class SettingsActivity : BaseActivity() {

    companion object {
        val TAG: String = SettingsActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank)
        setupActionBar()

        title = getString(R.string.title_section_settings)

        val tag = "settings"

        val fragmentManager = fragmentManager
        if (fragmentManager.findFragmentByTag(tag) == null) {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.content, SettingsFragment.newInstance(), tag)
            transaction.commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // for not showing cast icon in Settings Screen
        return true
    }

}
