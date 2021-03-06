package de.xikolo.controllers.downloads


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import de.xikolo.R
import de.xikolo.controllers.base.BaseActivity
import de.xikolo.controllers.dialogs.CreateTicketDialog
import de.xikolo.controllers.dialogs.CreateTicketDialogAutoBundle

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.helpdesk, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_helpdesk -> {
                val dialog = CreateTicketDialogAutoBundle.builder().build()
                dialog.show(supportFragmentManager, CreateTicketDialog.TAG)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
