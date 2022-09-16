package de.xikolo.controllers.login

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.lifecycle.Observer
import butterknife.BindView
import com.google.android.material.textfield.TextInputEditText
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.config.Feature
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.controllers.helper.LoginHelper
import de.xikolo.managers.UserManager
import de.xikolo.network.jobs.base.NetworkCode
import de.xikolo.network.jobs.base.NetworkState
import de.xikolo.storages.UserStorage
import de.xikolo.utils.extensions.getString
import de.xikolo.utils.extensions.openUrl
import de.xikolo.utils.extensions.showToast
import de.xikolo.viewmodels.login.LoginViewModel

class LoginFragment : ViewModelFragment<LoginViewModel>() {

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
            if (activity?.openUrl(Config.HOST_URL + Config.ACCOUNT + Config.NEW) != true) {
                showToast(R.string.error_plain)
            }
        }


        if (Feature.enabled("sso_provider")) {
            containerSSO.visibility = View.VISIBLE
            buttonSSO.setOnClickListener {
                hideKeyboard(view)
                startSSOLogin()
            }

            if (Feature.enabled("disabled_registration")) {
                buttonNew.visibility = View.GONE
            }
        }

        textForgotPassword.setOnClickListener {
            hideKeyboard(view)
            if (activity?.openUrl(Config.HOST_URL + Config.ACCOUNT + Config.RESET) != true) {
                showToast(R.string.error_plain)
            }
        }

        viewModel.loginNetworkState
            .observeForever(
                object : Observer<NetworkState> {
                    override fun onChanged(it: NetworkState) {
                        when (it.code) {
                            NetworkCode.SUCCESS -> {
                                viewModel.loginNetworkState.removeObserver(this)

                                viewModel.requestUserWithProfile()
                            }
                            else                -> handleCode(it.code)
                        }
                    }
                }
            )

        viewModel.profileNetworkState
            .observeForever(
                object : Observer<NetworkState> {
                    override fun onChanged(it: NetworkState) {
                        when (it.code) {
                            NetworkCode.SUCCESS -> {
                                viewModel.profileNetworkState.removeObserver(this)

                                App.instance.state.login.loggedIn()

                                hideAnyProgress()
                                activity?.finish()
                            }
                            else                -> handleCode(it.code)
                        }
                    }
                }
            )

        showContent()
    }

    override fun onResume() {
        super.onResume()

        if (UserManager.isAuthorized) {
            activity?.finish()
            return
        }

        token?.let {
            showBlockingProgress()

            val userStorage = UserStorage()
            userStorage.accessToken = it

            viewModel.requestUserWithProfile()
        }
    }

    private fun handleCode(code: NetworkCode) {
        if (code != NetworkCode.STARTED && code != NetworkCode.SUCCESS) {
            UserManager.logout()
            hideAnyProgress()
            when (code) {
                NetworkCode.NO_NETWORK -> showNetworkRequired()
                else                   -> showLoginFailedToast()
            }
        }
    }

    private fun login(view: View) {
        hideKeyboard(view)
        val email = editTextEmail.text.toString().trim { it <= ' ' }
        val password = editTextPassword.text.toString()

        if (!isEmailValid(email)) {
            editTextEmail.error = getString(R.string.error_email)
            return
        }

        if (password.isEmpty()) {
            editTextPassword.error = getString(R.string.error_password)
            return
        }

        showBlockingProgress()
        viewModel.login(email, password)
    }

    private fun startSSOLogin() {
        val intent = LoginHelper.ssoLoginIntent(requireActivity())
        startActivity(intent)
    }

    private fun showLoginFailedToast() {
        showToast(R.string.toast_log_in_failed)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
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

    override fun onRefresh() {
        hideAnyProgress()
    }

}
