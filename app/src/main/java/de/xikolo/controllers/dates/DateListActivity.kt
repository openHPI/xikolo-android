package de.xikolo.controllers.dates


import android.os.Bundle
import de.xikolo.R
import de.xikolo.controllers.base.BaseActivity

class DateListActivity : BaseActivity() {

    companion object {
        val TAG: String = DateListActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank)
        setupActionBar()

        title = getString(R.string.title_section_dates)

        val tag = "dates"

        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentByTag(tag) == null) {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.content, DateListFragment(), tag)
            transaction.commit()
        }
    }

}
