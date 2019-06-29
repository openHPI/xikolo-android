package de.xikolo.controllers.login

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.lifecycle.Observer
import butterknife.BindView
import com.google.android.material.textfield.TextInputEditText
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.BuildFlavor
import de.xikolo.config.Config
import de.xikolo.config.FeatureConfig
import de.xikolo.config.GlideApp
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.controllers.dialogs.ProgressDialogIndeterminate
import de.xikolo.controllers.dialogs.ProgressDialogIndeterminateAutoBundle
import de.xikolo.events.LoginEvent
import de.xikolo.managers.UserManager
import de.xikolo.network.jobs.base.NetworkCode
import de.xikolo.storages.UserStorage
import de.xikolo.utils.ToastUtil
import de.xikolo.viewmodels.login.LoginViewModel
import org.greenrobot.eventbus.EventBus

class LoginFragment : NetworkStateFragment<LoginViewModel>() {

    companion object {
        val TAG: String = LoginFragment::class.java.simpleName
    }

    @AutoBundleField(required = false)
    var token: String? = null

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.editEmail)
    lateinit var editTextEmail: TextInputEditText

    @BindView(R.id.editPassword)
    lateinit var editTextPassword: TextInputEditText

    @BindView(R.id.top_image)
    lateinit var topImage: ImageView

    @BindView(R.id.text_credentials)
    lateinit var textCredentials: TextView

    @BindView(R.id.textForgotPw)
    lateinit var textForgotPassword: TextView

    @BindView(R.id.btnSSO)
    lateinit var buttonSSO: Button

    @BindView(R.id.btnLogin)
    lateinit var buttonLogin: Button

    @BindView(R.id.btnNew)
    lateinit var buttonNew: Button

    @BindView(R.id.ssoContainer)
    lateinit var containerSSO: View

    private var progressDialog: ProgressDialogIndeterminate = ProgressDialogIndeterminateAutoBundle.builder().build()

    override val layoutResource = R.layout.fragment_login

    override fun createViewModel(): LoginViewModel {
        return LoginViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setHomeButtonEnabled(true)
            }
        }

        textCredentials.text = String.format(getString(R.string.login_with_credentials), Config.HOST)

        GlideApp.with(this)
            .load(R.drawable.login_header_v2)
            .dontAnimate()
            .noPlaceholders()
            .fitCenter()
            .into(topImage)

        editTextPassword.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login(v)
                true
            } else {
                false
            }
        }

        buttonLogin.setOnClickListener {
            login(it)
        }

        buttonNew.setOnClickListener {
            hideKeyboard(view)
            startUrlIntent(Config.HOST_URL + Config.ACCOUNT + Config.NEW)
        }


        if (FeatureConfig.SSO_LOGIN) {
            containerSSO.visibility = View.VISIBLE
            buttonSSO.setOnClickListener {
                hideKeyboard(view)
                startSSOLogin()
            }
        }

        textForgotPassword.setOnClickListener {
            hideKeyboard(view)
            startUrlIntent(Config.HOST_URL + Config.ACCOUNT + Config.RESET)
        }

        viewModel.loginNetworkState
            .observe(this, Observer {
                when (it.code) {
                    NetworkCode.SUCCESS -> viewModel.requestUserWithProfile()
                    else                -> handleCode(it.code)
                }
            })

        viewModel.profileNetworkState
            .observe(this, Observer {
                when (it.code) {
                    NetworkCode.SUCCESS -> {
                        hideProgressDialog()
                        activity?.finish()

                        EventBus.getDefault().postSticky(LoginEvent())
                    }
                    else                -> handleCode(it.code)
                }
            })

        showContent()
    }

    override fun onResume() {
        super.onResume()

        if (UserManager.isAuthorized) {
            activity?.finish()
            return
        }

        token?.let {
            showProgressDialog()

            val userStorage = UserStorage()
            userStorage.accessToken = it

            viewModel.requestUserWithProfile()
        }
    }

    private fun handleCode(code: NetworkCode) {
        if (code != NetworkCode.STARTED && code != NetworkCode.SUCCESS) {
            UserManager.logout()
            hideProgressDialog()
            when (code) {
                NetworkCode.NO_NETWORK -> showNoNetworkToast()
                else                   -> showLoginFailedToast()
            }
        }
    }

    private fun login(view: View) {
        hideKeyboard(view)
        val email = editTextEmail.text.toString().trim { it <= ' ' }
        val password = editTextPassword.text.toString()
        if (isEmailValid(email)) {
            if (password != "") {
                showProgressDialog()
                viewModel.login(email, password)
            } else {
                editTextPassword.error = getString(R.string.error_password)
            }
        } else {
            editTextEmail.error = getString(R.string.error_email)
        }
    }

    private fun startSSOLogin() {
        val strategy =
            when (BuildConfig.X_FLAVOR) {
                BuildFlavor.OPEN_WHO -> "who"
                BuildFlavor.OPEN_SAP -> "sap"
                BuildFlavor.OPEN_HPI -> "hpi"
                else                 -> null
            }

        val intent = SsoLoginActivityAutoBundle.builder(
            Config.HOST_URL + "auth/" + strategy + "?in_app=true&redirect_to=/auth/" + strategy,
            getString(R.string.login_sso)
        ).build(activity!!)
        startActivity(intent)
    }

    private fun showProgressDialog() {
        progressDialog.show(childFragmentManager, ProgressDialogIndeterminate.TAG)
    }

    private fun hideProgressDialog() {
        progressDialog.dismiss()
    }

    private fun showNoNetworkToast() {
        ToastUtil.show(R.string.toast_no_network)
    }

    private fun showLoginFailedToast() {
        ToastUtil.show(R.string.toast_log_in_failed)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(activity!!)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun hideKeyboard(view: View) {
        activity?.apply {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun isEmailValid(email: CharSequence): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun startUrlIntent(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    override fun onRefresh() {
        hideAnyProgress()
    }

}
