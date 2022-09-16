package de.xikolo.controllers.course

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewStub
import android.widget.Button
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import butterknife.BindView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.config.Feature
import de.xikolo.controllers.base.ViewModelActivity
import de.xikolo.controllers.dialogs.CourseDateListDialogAutoBundle
import de.xikolo.controllers.dialogs.CreateTicketDialog
import de.xikolo.controllers.dialogs.CreateTicketDialogAutoBundle
import de.xikolo.controllers.dialogs.ProgressDialogIndeterminate
import de.xikolo.controllers.dialogs.ProgressDialogIndeterminateAutoBundle
import de.xikolo.controllers.dialogs.UnenrollDialog
import de.xikolo.controllers.helper.CourseArea
import de.xikolo.controllers.helper.LoginHelper
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.controllers.section.CourseItemsActivityAutoBundle
import de.xikolo.controllers.webview.WebViewFragment
import de.xikolo.controllers.webview.WebViewFragmentAutoBundle
import de.xikolo.extensions.observe
import de.xikolo.extensions.observeOnce
import de.xikolo.extensions.observeUnsafeOnce
import de.xikolo.managers.UserManager
import de.xikolo.models.Course
import de.xikolo.models.dao.EnrollmentDao
import de.xikolo.models.dao.ItemDao
import de.xikolo.network.jobs.GetItemWithContentJob
import de.xikolo.network.jobs.base.NetworkCode
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.utils.DeepLinkingUtil
import de.xikolo.utils.IdUtil
import de.xikolo.utils.LanalyticsUtil
import de.xikolo.utils.ShortcutUtil
import de.xikolo.utils.extensions.createChooser
import de.xikolo.utils.extensions.openUrl
import de.xikolo.utils.extensions.shareCourseLink
import de.xikolo.utils.extensions.showToast
import de.xikolo.viewmodels.course.CourseViewModel

class CourseActivity : ViewModelActivity<CourseViewModel>(), UnenrollDialog.Listener {

    companion object {
        val TAG: String = CourseActivity::class.java.simpleName
    }

    @AutoBundleField(required = false)
    var courseId: String? = null

    @BindView(R.id.viewpager)
    lateinit var viewPager: ViewPager2

    @BindView(R.id.tabs)
    lateinit var tabLayout: TabLayout

    @BindView(R.id.stub_bottom)
    lateinit var stubBottom: ViewStub

    private var progressDialog: ProgressDialogIndeterminate? = null

    private var enrollBar: View? = null
    private var enrollButton: Button? = null

    private var areaState: CourseArea.State = CourseArea.All
    private var courseTab: CourseArea = CourseArea.LEARNINGS
    private var lastTrackedCourseTab: CourseArea? = null

    private var course: Course? = null

    override fun createViewModel(): CourseViewModel {
        return courseId?.let {
            CourseViewModel(it)
        } ?: run {
            showDeepLinkErrorMessage()
            createChooserFromCurrentIntent()
            finish()
            CourseViewModel("")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (courseId == null) {
            courseId = handleCourseDeepLink(intent)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank_tabs)
        setupActionBar(false)
        enableOfflineModeToolbar(true)

        if (stubBottom.parent != null) {
            stubBottom.layoutResource = R.layout.view_enroll_button
            enrollBar = stubBottom.inflate()
            enrollButton = enrollBar?.findViewById(R.id.button_enroll)
            enrollButton?.setOnClickListener { enroll() }
        }
        hideEnrollBar()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        handleItemDeepLink(intent)
        viewModel.course
            .observeUnsafeOnce(this) {
                if (it.isValid) {
                    course = it

                    val adapter = CoursePagerAdapter()
                    viewPager.adapter = adapter
                    adapter.setupTabs()
                    tabLayout.addOnTabSelectedListener(adapter)

                    handleCourseDeepLinkTab(intent)
                    setupCourse(it)

                    viewModel.course
                        .observe(this) {
                            course = it
                            setupCourse(it)
                        }
                } else {
                    showDeepLinkErrorMessage()
                    createChooserFromCurrentIntent()
                    finish()
                }
                true
            }

        viewModel.dates
            .observe(this) {
                invalidateOptionsMenu()
            }
    }

    private fun updateViewPagerTab() {
        viewPager.currentItem = areaState.indexOf(courseTab)
        tabLayout.getTabAt(tabLayout.selectedTabPosition)?.select()
    }

    private fun setupCourse(course: Course) {
        FirebaseCrashlytics.getInstance().setCustomKey("course_id", course.id)

        if (course.external) {
            setAreaState(CourseArea.External)
            showCourseExternalBar(course)

            enrollButton?.setOnClickListener { enterExternalCourse(course) }
        } else if (!course.enrollable && !course.isEnrolled) {
            setAreaState(CourseArea.Locked)
            showCourseNotEnrollableBar()
        } else if (!course.isEnrolled) {
            setAreaState(CourseArea.Locked)
            showEnrollBar()
        } else if (!course.accessible) {
            setAreaState(CourseArea.Locked)
            showCourseUnavailableEnrollBar()
        } else {
            setAreaState(CourseArea.All)
            hideEnrollBar()
        }

        title = course.title

        setCourseTab(courseTab)
        updateViewPagerTab()

        if (Feature.SHORTCUTS && course.isEnrolled) {
            ShortcutUtil.addCourse(applicationContext, course.id, course.title)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        handleCourseDeepLink(intent)?.let {
            restartActivity()
        }
    }

    private fun handleCourseDeepLink(intent: Intent?): String? {
        if (intent?.action === Intent.ACTION_VIEW) {
            DeepLinkingUtil.getCourseIdentifier(intent.data)?.let {
                return it
            }
        }
        return null
    }

    private fun handleCourseDeepLinkTab(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val tab = DeepLinkingUtil.getTab(intent.data?.path)
            if (!areaState.areas.contains(tab)) {
                if (!UserManager.isAuthorized) {
                    showLoginRequiredToast()
                } else {
                    showDeepLinkErrorMessage()
                }
                createChooserFromCurrentIntent()
            } else {
                setCourseTab(tab)
            }
        }
    }

    private fun handleItemDeepLink(intent: Intent?) {
        // if the deeplink leads to an item, fetch it here to start the CourseItemsActivity
        DeepLinkingUtil.getItemIdentifier(intent?.data?.path)?.let { itemId ->
            if (UserManager.isAuthorized) {
                ItemDao(viewModel.realm).find(IdUtil.base62ToUUID(itemId))
                    .observeUnsafeOnce(this) {
                        if (it.isValid) {
                            startActivity(
                                CourseItemsActivityAutoBundle.builder(
                                    it.courseId,
                                    it.sectionId,
                                    it.position - 1 // positions in the API are not zero-indexed
                                ).build(this)
                            )
                            finish()
                        } else {
                            showDeepLinkErrorMessage()
                            createChooserFromCurrentIntent()
                        }
                        true
                    }
                GetItemWithContentJob(itemId, viewModel.networkState, false).run()
            } else {
                showLoginRequiredToast()
                createChooserFromCurrentIntent()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater

        if (course?.isEnrolled == true) {
            if (viewModel.dateCount > 0) {
                inflater.inflate(R.menu.course_dates, menu)
            }
            inflater.inflate(R.menu.unenroll, menu)
        }
        inflater.inflate(R.menu.share, menu)
        inflater.inflate(R.menu.helpdesk, menu)

        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    NavUtils.navigateUpFromSameTask(this)
                } else {
                    startActivity(
                        NavUtils.getParentActivityIntent(this)
                    )
                    finish()
                }
                return true
            }
            R.id.action_refresh -> {
                viewModel.onRefresh()
                return true
            }
            R.id.action_share -> {
                shareCourseLink(courseId!!)
                return true
            }
            R.id.action_unenroll -> {
                val dialog = UnenrollDialog()
                dialog.listener = this
                dialog.show(supportFragmentManager, UnenrollDialog.TAG)
                return true
            }
            R.id.course_dates -> {
                val dialog = CourseDateListDialogAutoBundle.builder(courseId!!).build()
                dialog.show(supportFragmentManager, UnenrollDialog.TAG)
                return true
            }
            R.id.action_helpdesk -> {
                val dialog = CreateTicketDialogAutoBundle.builder().courseId(courseId).build()
                dialog.show(supportFragmentManager, CreateTicketDialog.TAG)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setCourseTab(tab: CourseArea) {
        courseTab = tab
        courseId?.let {
            if (lastTrackedCourseTab !== courseTab) {
                when (tab) {
                    CourseArea.DISCUSSIONS   -> LanalyticsUtil.trackVisitedPinboard(it)
                    CourseArea.PROGRESS      -> LanalyticsUtil.trackVisitedProgress(it)
                    CourseArea.ANNOUNCEMENTS -> LanalyticsUtil.trackVisitedAnnouncements(it)
                    CourseArea.RECAP         -> LanalyticsUtil.trackVisitedRecap(it)
                    else                     -> return
                }
            }
        }
        lastTrackedCourseTab = courseTab
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        unenroll()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        (viewPager.adapter as CoursePagerAdapter).getFragmentAt(viewPager.currentItem)
            ?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        val webViewFragment =
            (viewPager.adapter as CoursePagerAdapter).getFragmentAt(viewPager.currentItem)
                as? WebViewFragment
        if (webViewFragment?.onBack() != true) {
            super.onBackPressed()
        }
    }

    private fun setAreaState(state: CourseArea.State) {
        areaState = state
        viewPager.adapter?.notifyDataSetChanged()
    }

    private fun hideEnrollBar() {
        enrollBar?.visibility = View.GONE
    }

    private fun showEnrollBar() {
        enrollBar?.visibility = View.VISIBLE
        enrollButton?.isEnabled = true
        enrollButton?.isClickable = true
        enrollButton?.setText(R.string.btn_enroll)
    }

    private fun showCourseExternalBar(course: Course) {
        enrollBar?.visibility = View.VISIBLE
        enrollButton?.isEnabled = true
        enrollButton?.isClickable = true

        enrollButton?.setText(R.string.btn_external_course)
        Uri.parse(course.externalUrl)?.host?.let {
            enrollButton?.setText(
                getString(R.string.btn_external_course_target, it)
            )
        }
    }

    private fun showCourseUnavailableEnrollBar() {
        enrollBar?.visibility = View.VISIBLE
        enrollButton?.isEnabled = false
        enrollButton?.isClickable = false
        enrollButton?.setText(R.string.btn_starts_soon)
    }

    private fun showCourseNotEnrollableBar() {
        enrollBar?.visibility = View.VISIBLE
        enrollButton?.isEnabled = false
        enrollButton?.isClickable = false
        enrollButton?.setText(R.string.btn_not_enrollable)
    }

    private fun restartActivity() {
        finish()
        startActivity(intent)
    }

    private fun createChooserFromCurrentIntent() {
        intent?.let {
            Handler().postDelayed({
                Intent(it.action, it.data).createChooser(this, null, true)?.let {
                    startActivity(it)
                } ?: run {
                    showToast(R.string.error_plain)
                }
            }, 300)
        }
    }

    private fun showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialogIndeterminateAutoBundle.builder().build()
        }
        progressDialog?.show(supportFragmentManager, ProgressDialogIndeterminate.TAG)
    }

    private fun hideProgressDialog() {
        if (progressDialog?.dialog?.isShowing == true) {
            progressDialog?.dismiss()
        }
    }

    private fun showErrorToast() {
        showToast(R.string.error)
    }

    private fun showNoNetworkToast() {
        showToast(R.string.toast_no_network)
    }

    private fun showLoginRequiredToast() {
        showToast(R.string.toast_please_log_in)
    }

    private fun showDeepLinkErrorMessage() {
        showToast(R.string.notification_deep_link_error)
    }

    private fun openLogin() {
        val intent = LoginHelper.loginIntent(this)
        startActivity(intent)
    }

    private fun enroll() {
        showProgressDialog()

        val enrollmentCreationNetworkState = NetworkStateLiveData()
        enrollmentCreationNetworkState
            .observeOnce(this) {
                when (it.code) {
                    NetworkCode.SUCCESS -> {
                        viewModel.onRefresh()
                        hideProgressDialog()
                        true
                    }
                    NetworkCode.NO_NETWORK -> {
                        hideProgressDialog()
                        showNoNetworkToast()
                        true
                    }
                    NetworkCode.NO_AUTH    -> {
                        hideProgressDialog()
                        showLoginRequiredToast()
                        openLogin()
                        finish()
                        true
                    }
                    else                   -> false
                }
            }

        viewModel.enroll(enrollmentCreationNetworkState)
    }

    private fun unenroll() {
        val courseIdentifier = course?.id ?: courseId // courseId may also be slug when deeplinking
        EnrollmentDao.Unmanaged.findForCourse(courseIdentifier)?.id?.let { enrollmentId ->
            showProgressDialog()

            val enrollmentDeletionNetworkState = NetworkStateLiveData()
            enrollmentDeletionNetworkState
                .observeOnce(this) {
                    if (it.code == NetworkCode.STARTED) {
                        return@observeOnce false
                    }

                    when (it.code) {
                        NetworkCode.SUCCESS -> {
                            finish()
                            hideProgressDialog()
                        }
                        NetworkCode.NO_NETWORK -> {
                            hideProgressDialog()
                            showNoNetworkToast()
                        }
                        else                   -> {
                            hideProgressDialog()
                            showErrorToast()
                        }
                    }
                    true
                }

            viewModel.unenroll(enrollmentId, enrollmentDeletionNetworkState)
        }
    }

    private fun enterExternalCourse(course: Course) {
        openUrl(course.externalUrl)
    }

    override fun onConnectivityChange(isOnline: Boolean) {
        super.onConnectivityChange(isOnline)

        if (isOnline) {
            toolbar?.subtitle = ""
            tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbar))
            setColorScheme(R.color.toolbar, R.color.statusbar)
        } else {
            toolbar?.subtitle = getString(R.string.offline_mode)
            tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbar_offline))
            setColorScheme(R.color.toolbar_offline, R.color.statusbar_offline)
        }
    }

    inner class CoursePagerAdapter :
        FragmentStateAdapter(this), TabLayout.OnTabSelectedListener {

        private fun getPageTitle(position: Int): CharSequence? {
            return getString(areaState.get(position).titleRes)
        }

        // Warning: implementation-dependent
        fun getFragmentAt(position: Int): Fragment? {
            return supportFragmentManager.findFragmentByTag("f$position")
        }

        fun setupTabs() {
            TabLayoutMediator(tabLayout, viewPager, true, true) { tab, position ->
                tab.text = getPageTitle(position)
            }.attach()
        }

        override fun getItemCount(): Int {
            return areaState.size
        }

        override fun createFragment(position: Int): Fragment {
            val courseIdentifier = course?.id ?: courseId!!
            return when (areaState.get(position)) {
                CourseArea.LEARNINGS ->
                    LearningsFragmentAutoBundle.builder(courseIdentifier).build()
                CourseArea.DISCUSSIONS ->
                    WebViewFragmentAutoBundle.builder(
                        Config.HOST_URL + Config.COURSES +
                            courseIdentifier + "/" + Config.DISCUSSIONS
                    )
                        .allowBack(true)
                        .inAppLinksEnabled(true)
                        .externalLinksEnabled(false)
                        .build()
                CourseArea.PROGRESS ->
                    ProgressFragmentAutoBundle.builder(courseIdentifier).build()
                CourseArea.COURSE_DETAILS ->
                    DescriptionFragmentAutoBundle.builder(courseIdentifier).build()
                CourseArea.CERTIFICATES ->
                    CertificatesFragmentAutoBundle.builder(courseIdentifier).build()
                CourseArea.DOCUMENTS ->
                    DocumentListFragmentAutoBundle.builder(courseIdentifier).build()
                CourseArea.ANNOUNCEMENTS ->
                    AnnouncementListFragmentAutoBundle.builder(courseIdentifier).build()
                CourseArea.RECAP ->
                    WebViewFragmentAutoBundle.builder(
                        Config.HOST_URL + Config.RECAP + courseIdentifier
                    )
                        .inAppLinksEnabled(true)
                        .externalLinksEnabled(false)
                        .build()
            }
        }

        override fun onTabSelected(tab: TabLayout.Tab) {
            val tabPosition = tabLayout.selectedTabPosition
            viewPager.setCurrentItem(tabPosition, true)
            setCourseTab(areaState.get(tabPosition))
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {}

        override fun onTabReselected(tab: TabLayout.Tab) {}
    }
}
