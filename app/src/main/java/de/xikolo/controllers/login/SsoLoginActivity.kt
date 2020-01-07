package de.xikolo.controllers.login

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.base.BaseActivity
import de.xikolo.controllers.dialogs.CreateTicketDialog
import de.xikolo.controllers.dialogs.CreateTicketDialogAutoBundle
import de.xikolo.controllers.webview.WebViewFragmentAutoBundle
import de.xikolo.extensions.observe

class SsoLoginActivity : BaseActivity() {

    companion object {
        val TAG = SsoLoginActivity::class.java.simpleName
    }

    @AutoBundleField
    lateinit var url: String

    @AutoBundleField
    lateinit var title: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank)
        setupActionBar()

        setTitle(title)

        val tag = "content"
        val fragment = WebViewFragmentAutoBundle.builder(url)
            .inAppLinksEnabled(true)
            .externalLinksEnabled(true)
            .build()

        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentByTag(tag) == null) {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.content, fragment, tag)
            transaction.commit()
        }

        App.instance.state.login
            .observe(this) { isLoggedIn ->
                if (isLoggedIn) {
                    finish()
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.helpdesk, menu)
        return true
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
