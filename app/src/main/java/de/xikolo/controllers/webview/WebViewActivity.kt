package de.xikolo.controllers.webview

import android.os.Bundle
import android.view.MenuItem
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.BaseActivity
import de.xikolo.controllers.dialogs.CreateTicketDialog
import de.xikolo.controllers.dialogs.CreateTicketDialogAutoBundle

class WebViewActivity : BaseActivity() {

    companion object {
        val TAG = WebViewActivity::class.java.simpleName
    }

    @AutoBundleField
    lateinit var title: String

    @AutoBundleField
    lateinit var url: String

    @AutoBundleField(required = false)
    var inAppLinksEnabled: Boolean = false

    @AutoBundleField(required = false)
    var externalLinksEnabled: Boolean = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank)
        setupActionBar()

        setTitle(title)

        val tag = "content"
        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentByTag(tag) == null) {
            val transaction = fragmentManager.beginTransaction()
            val fragment = WebViewFragmentAutoBundle.builder(url)
                .inAppLinksEnabled(inAppLinksEnabled)
                .externalLinksEnabled(externalLinksEnabled)
                .build()
            transaction.replace(R.id.content, fragment, tag)
            transaction.commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home    -> {
                finish()
                return true
            }
            R.id.action_helpdesk -> {
                val dialog = CreateTicketDialogAutoBundle.builder().build()
                dialog.show(supportFragmentManager, CreateTicketDialog.TAG)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
