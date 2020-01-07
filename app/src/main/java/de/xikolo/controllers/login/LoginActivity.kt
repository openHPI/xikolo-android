package de.xikolo.controllers.login

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.BaseActivity
import de.xikolo.controllers.dialogs.CreateTicketDialog
import de.xikolo.controllers.dialogs.CreateTicketDialogAutoBundle

class LoginActivity : BaseActivity() {

    companion object {
        val TAG = LoginActivity::class.java.simpleName
    }

    @AutoBundleField(required = false)
    var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        title = null

        val tag = "login"
        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentByTag(tag) == null) {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.content, LoginFragmentAutoBundle.builder().token(token).build(), tag)
            transaction.commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.helpdesk, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_helpdesk) {
            val dialog = CreateTicketDialogAutoBundle.builder().build()
            dialog.show(supportFragmentManager, CreateTicketDialog.TAG)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
