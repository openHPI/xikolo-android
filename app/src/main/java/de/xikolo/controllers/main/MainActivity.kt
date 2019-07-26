package de.xikolo.controllers.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import butterknife.BindView
import com.google.android.material.navigation.NavigationView
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.config.FeatureConfig
import de.xikolo.config.GlideApp
import de.xikolo.controllers.base.BaseFragment
import de.xikolo.controllers.base.ViewModelActivity
import de.xikolo.controllers.downloads.DownloadsActivity
import de.xikolo.controllers.helper.CourseListFilter
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.controllers.secondscreen.SecondScreenActivity
import de.xikolo.controllers.settings.SettingsActivity
import de.xikolo.events.LoginEvent
import de.xikolo.events.LogoutEvent
import de.xikolo.extensions.observe
import de.xikolo.managers.UserManager
import de.xikolo.utils.DeepLinkingUtil
import de.xikolo.utils.LanalyticsUtil
import de.xikolo.utils.PlayServicesUtil
import de.xikolo.viewmodels.main.NavigationViewModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : ViewModelActivity<NavigationViewModel>(), NavigationView.OnNavigationItemSelectedListener, MainActivityCallback {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    @BindView(R.id.drawer_layout)
    lateinit var drawerLayout: DrawerLayout

    @BindView(R.id.navigation)
    lateinit var navigationView: NavigationView

    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun createViewModel(): NavigationViewModel {
        return NavigationViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupActionBar()

        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        navigationView.setNavigationItemSelectedListener(this)

        if (Config.DEBUG) {
            Log.d(TAG, "Build Type: " + BuildConfig.X_TYPE)
            Log.d(TAG, "Build Flavor: " + BuildConfig.X_FLAVOR)
        }

        // check Play Services, display dialog is update needed
        PlayServicesUtil.checkPlayServicesWithDialog(this)

        handleIntent(intent)

        updateDrawer()
        selectDrawerSection(viewModel.drawerSection)

        viewModel.announcements
            .observe(this) {
                updateDrawer()
            }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        drawerToggle.syncState()
    }

    override fun onStart() {
        super.onStart()

        updateDrawer()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            DeepLinkingUtil.getType(uri)?.let {
                when (it) {
                    DeepLinkingUtil.Type.ALL_COURSES -> selectDrawerSection(R.id.navigation_all_courses)
                    DeepLinkingUtil.Type.NEWS        -> selectDrawerSection(R.id.navigation_news)
                    DeepLinkingUtil.Type.MY_COURSES  -> selectDrawerSection(R.id.navigation_my_courses)
                }
            }
        }
    }

    override fun selectDrawerSection(@IdRes itemId: Int) {
        navigationView.setCheckedItem(itemId)
        onNavigationItemSelected(navigationView.menu.findItem(itemId))
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onFragmentAttached(@IdRes itemId: Int, title: String?) {
        navigationView.setCheckedItem(itemId)
        drawerToggle.isDrawerIndicatorEnabled = true
        supportActionBar?.title = title ?: navigationView.menu.findItem(itemId).title
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        viewModel.drawerSection = item.itemId

        val fragmentManager = supportFragmentManager
        var tag: String? = null
        var intent: Intent? = null
        var newFragment: BaseFragment? = null

        when (item.itemId) {
            R.id.navigation_login        -> {
                if (UserManager.isAuthorized) {
                    newFragment = ProfileFragment()
                    tag = "profile"

                    LanalyticsUtil.trackVisitedProfile()
                } else {
                    intent = LoginActivityAutoBundle.builder().build(this)
                }
            }
            R.id.navigation_channels     -> {
                newFragment = ChannelListFragment()
                tag = "channels"
            }
            R.id.navigation_all_courses  -> {
                newFragment = CourseListFragmentAutoBundle.builder(CourseListFilter.ALL).build()
                tag = "all_courses"
            }
            R.id.navigation_my_courses   -> {
                newFragment = CourseListFragmentAutoBundle.builder(CourseListFilter.MY).build()
                tag = "my_courses"
            }
            R.id.navigation_dates        -> {
                newFragment = DateListFragment()
                tag = "dates"
            }
            R.id.navigation_certificates -> {
                newFragment = CertificateListFragment()
                tag = "certificates"
            }
            R.id.navigation_news         -> {
                newFragment = NewsListFragment()
                tag = "news"

                LanalyticsUtil.trackVisitedAnnouncements(null)
            }
        }

        when (item.itemId) {
            R.id.navigation_second_screen -> {
                intent = Intent(this, SecondScreenActivity::class.java)
            }
            R.id.navigation_downloads     -> {
                intent = Intent(this, DownloadsActivity::class.java)

                LanalyticsUtil.trackVisitedDownloads()
            }
            R.id.navigation_settings      -> {
                intent = Intent(this, SettingsActivity::class.java)

                LanalyticsUtil.trackVisitedPreferences()
            }
        }

        if (tag != null && newFragment != null) {
            val oldFragment = fragmentManager.findFragmentByTag(tag)
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.container, oldFragment ?: newFragment, tag)
            transaction.addToBackStack(tag)
            transaction.commit()
            setAppBarExpanded(true)
        } else if (intent != null) {
            startActivity(intent)
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    override fun onBackPressed() {
        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
            if (UserManager.isAuthorized
                && navigationView.checkedItem?.itemId == R.id.navigation_all_courses
                || navigationView.checkedItem?.itemId == R.id.navigation_my_courses) {
                finish()
            } else if (supportFragmentManager.backStackEntryCount > 1) {
                supportFragmentManager.popBackStack()
                setAppBarExpanded(true)
            } else {
                super.onBackPressed()
            }
        } else {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun updateDrawer() {
        val checkedItem = navigationView.checkedItem

        navigationView.menu.clear()
        for (x in 0 until navigationView.headerCount) {
            navigationView.removeHeaderView(navigationView.getHeaderView(x))
        }

        navigationView.inflateMenu(R.menu.navigation)

        if (!FeatureConfig.CHANNELS) {
            navigationView.menu.findItem(R.id.navigation_channels).isVisible = false
        }
        if (!FeatureConfig.SECOND_SCREEN) {
            navigationView.menu.findItem(R.id.navigation_second_screen).isVisible = false
        }
        if (UserManager.isAuthorized) {
            val headerView = navigationView.inflateHeaderView(R.layout.item_navigation_profile)
            headerView.setOnClickListener {
                onNavigationItemSelected(navigationView.menu.findItem(R.id.navigation_login))
            }

            viewModel.user?.let { user ->
                val profile = user.profile
                headerView.findViewById<TextView>(R.id.textName).text = String.format(
                    getString(R.string.user_name),
                    profile.firstName,
                    profile.lastName
                )

                headerView.findViewById<TextView>(R.id.textEmail).text = profile.email

                GlideApp.with(this)
                    .load(user.avatarUrl)
                    .circleCrop()
                    .allPlaceholders(R.drawable.avatar)
                    .into(headerView.findViewById(R.id.imgProfile))
            }

            navigationView.menu.findItem(R.id.navigation_login).isVisible = false

            if (viewModel.unreadAnnouncementsCount > 0) {
                navigationView.menu.findItem(R.id.navigation_news).setActionView(R.layout.item_navigation_counter).apply {
                    actionView.findViewById<TextView>(R.id.textCounter).text = viewModel.unreadAnnouncementsCount.toString()
                }
            }
        }
        navigationView.invalidate()

        checkedItem?.let {
            navigationView.setCheckedItem(it)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginEvent(event: LoginEvent) {
        updateDrawer()
        viewModel.onRefresh()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutEvent(event: LogoutEvent) {
        updateDrawer()
        viewModel.onRefresh()
    }

}
