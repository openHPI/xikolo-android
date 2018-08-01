package de.xikolo.controllers.downloads


import android.os.Bundle
import de.xikolo.R
import de.xikolo.controllers.base.BaseActivity

class DownloadsActivity : BaseActivity() {

    companion object {
        val TAG: String = DownloadsActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank)
        setupActionBar()

        title = getString(R.string.title_section_downloads)

        val tag = "downloads"

        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentByTag(tag) == null) {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.content, DownloadsFragment(), tag)
            transaction.commit()
        }
    }

}
