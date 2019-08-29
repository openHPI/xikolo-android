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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import com.crashlytics.android.Crashlytics
import com.google.android.material.tabs.TabLayout
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.controllers.base.ViewModelActivity
import de.xikolo.controllers.dialogs.CourseDateListDialogAutoBundle
import de.xikolo.controllers.dialogs.ProgressDialogIndeterminate
import de.xikolo.controllers.dialogs.ProgressDialogIndeterminateAutoBundle
import de.xikolo.controllers.dialogs.UnenrollDialog
import de.xikolo.controllers.helper.CourseArea
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.controllers.section.CourseItemsActivityAutoBundle
import de.xikolo.controllers.webview.WebViewFragmentAutoBundle
import de.xikolo.events.NetworkStateEvent
import de.xikolo.extensions.observe
import de.xikolo.extensions.observeOnce
import de.xikolo.managers.UserManager
import de.xikolo.models.Course
import de.xikolo.models.dao.EnrollmentDao
import de.xikolo.models.dao.ItemDao
import de.xikolo.network.jobs.GetItemWithContentJob
import de.xikolo.network.jobs.base.NetworkCode
import de.xikolo.network.jobs.base.NetworkStateLiveData
import de.xikolo.utils.*
import de.xikolo.viewmodels.course.CourseViewModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CourseActivity : ViewModelActivity<CourseViewModel>(), UnenrollDialog.Listener {

    companion object {
        val TAG: String = CourseActivity::class.java.simpleName
    }

    @AutoBundleField(required = false)
    var courseId: String? = null

    @BindView(R.id.viewpager)
    lateinit var viewPager: ViewPager

    @BindView(R.id.tabs)
    lateinit var tabLayout: TabLayout

    @BindView(R.id.stub_bottom)
    lateinit var stubBottom: ViewStub

    private var progressDialog: ProgressDialogIndeterminate? = null

    private var adapter: CoursePagerAdapter? = null

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
            finishActivity()
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
            stubBottom.layoutResource = R.layout.content_enroll_button
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
            .observeOnce(this) {
                if (it.isValid) {
                    course = it
                    setupCourse(it)
                } else {
                    showDeepLinkErrorMessage()
                    createChooserFromCurrentIntent()
                    finishActivity()
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
    }

    private fun setupCourse(course: Course) {
        Crashlytics.setString("course_id", course.id)

        if(course.external){
            setAreaState(CourseArea.External)
            showCourseExternalBar()

            enrollButton?.setOnClickListener { enterExternalCourse(course) }
        } else if (!course.isEnrolled) {
            setAreaState(CourseArea.Locked)
            showEnrollBar()
        } else if (course.accessible) {
            setAreaState(CourseArea.All)
            hideEnrollBar()
        } else {
            setAreaState(CourseArea.Locked)
            showCourseUnavailableEnrollBar()
        }

        title = course.title

        adapter = CoursePagerAdapter(supportFragmentManager, course.id)
        adapter?.let {
            viewPager.adapter = it

            tabLayout.clearOnTabSelectedListeners()
            tabLayout.addOnTabSelectedListener(it)
            tabLayout.setupWithViewPager(viewPager)
        }
        setCourseTab(courseTab)

        handleCourseDeepLinkTab(intent)

        updateViewPagerTab()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        restartActivity()
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
                    showLoginRequiredMessage()
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
                    .observeOnce(this) {
                        if (it.isValid) {
                            startActivity(
                                CourseItemsActivityAutoBundle.builder(
                                    it.courseId,
                                    it.sectionId,
                                    it.position - 1 // positions in the API are not zero-indexed
                                ).build(this)
                            )
                            finishActivity()
                        } else {
                            showDeepLinkErrorMessage()
                            createChooserFromCurrentIntent()
                        }
                        true
                    }
                GetItemWithContentJob(itemId, viewModel.networkState, false).run()
            } else {
                showLoginRequiredMessage()
                createChooserFromCurrentIntent()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater

        if (course?.isEnrolled == true) {
            if (viewModel.dateCount > 0) {
                inflater.inflate(R.menu.course_dates, menu)
            }
            inflater.inflate(R.menu.unenroll, menu)
        }
        inflater.inflate(R.menu.share, menu)

        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home    -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
            R.id.action_share    -> {
                ShareUtil.shareCourseLink(this, courseId!!)
                return true
            }
            R.id.action_unenroll -> {
                val dialog = UnenrollDialog()
                dialog.listener = this
                dialog.show(supportFragmentManager, UnenrollDialog.TAG)
                return true
            }
            R.id.course_dates    -> {
                val dialog = CourseDateListDialogAutoBundle.builder(courseId!!).build()
                dialog.show(supportFragmentManager, UnenrollDialog.TAG)
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
        adapter?.getItem(viewPager.currentItem)?.onActivityResult(requestCode, resultCode, data)
    }

    private fun setAreaState(state: CourseArea.State) {
        areaState = state
        adapter?.notifyDataSetChanged()
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

    private fun showCourseExternalBar() {
        enrollBar?.visibility = View.VISIBLE
        enrollButton?.isEnabled = true
        enrollButton?.isClickable = true
        enrollButton?.setText(R.string.btn_external_course)
    }

    private fun showCourseUnavailableEnrollBar() {
        enrollBar?.visibility = View.VISIBLE
        enrollButton?.isEnabled = false
        enrollButton?.isClickable = false
        enrollButton?.setText(R.string.btn_starts_soon)
    }

    private fun finishActivity() {
        finish()
    }

    private fun restartActivity() {
        finish()
        startActivity(intent)
    }

    private fun createChooserFromCurrentIntent() {
        intent?.let {
            Handler().postDelayed({
                startActivity(
                    Intent.createChooser(
                        Intent(it.action, it.data),
                        null)
                )
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
        ToastUtil.show(R.string.error)
    }

    private fun showNoNetworkToast() {
        ToastUtil.show(R.string.toast_no_network)
    }

    private fun showLoginRequiredMessage() {
        ToastUtil.show(R.string.toast_please_log_in)
    }

    private fun showDeepLinkErrorMessage() {
        ToastUtil.show(R.string.notification_deep_link_error)
    }

    private fun openLogin() {
        val intent = LoginActivityAutoBundle.builder().build(this)
        startActivity(intent)
    }

    private fun enroll() {
        showProgressDialog()

        val enrollmentCreationNetworkState = NetworkStateLiveData()
        enrollmentCreationNetworkState
            .observeOnce(this) {
                when (it.code) {
                    NetworkCode.SUCCESS    -> {
                        restartActivity()
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
                        showLoginRequiredMessage()
                        openLogin()
                        true
                    }
                    else                   -> false
                }
            }

        viewModel.enroll(enrollmentCreationNetworkState)
    }

    private fun unenroll() {
        EnrollmentDao.Unmanaged.findForCourse(courseId)?.id?.let { enrollmentId ->
            showProgressDialog()

            val enrollmentDeletionNetworkState = NetworkStateLiveData()
            enrollmentDeletionNetworkState
                .observeOnce(this) {
                    if (it.code == NetworkCode.STARTED) {
                        return@observeOnce false
                    }

                    when (it.code) {
                        NetworkCode.SUCCESS    -> {
                            finishActivity()
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

    private fun enterExternalCourse(course: Course){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(course.externalUrl))
        startActivity(intent)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    override fun onNetworkEvent(event: NetworkStateEvent) {
        super.onNetworkEvent(event)

        if (event.isOnline) {
            toolbar.subtitle = ""
            tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.apptheme_toolbar))
            setColorScheme(R.color.apptheme_toolbar, R.color.apptheme_statusbar)
        } else {
            toolbar.subtitle = getString(R.string.offline_mode)
            tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.offline_mode_toolbar))
            setColorScheme(R.color.offline_mode_toolbar, R.color.offline_mode_statusbar)
        }
    }

    inner class CoursePagerAdapter internal constructor(private val fragmentManager: FragmentManager, private val courseId: String) : FragmentPagerAdapter(fragmentManager), TabLayout.OnTabSelectedListener {

        override fun getPageTitle(position: Int): CharSequence? {
            return getString(areaState.get(position).titleRes)
        }

        override fun getCount(): Int {
            return areaState.size
        }

        override fun getItem(position: Int): Fragment? {
            // Check if this Fragment already exists.
            // Fragment Name is saved by FragmentPagerAdapter implementation.
            val name = makeFragmentName(R.id.viewpager, position)
            var fragment: Fragment? = fragmentManager.findFragmentByTag(name)
            if (fragment == null) {
                when (areaState.get(position)) {
                    CourseArea.LEARNINGS      -> fragment = LearningsFragmentAutoBundle.builder(courseId).build()
                    CourseArea.DISCUSSIONS    -> fragment = WebViewFragmentAutoBundle.builder(Config.HOST_URL + Config.COURSES + courseId + "/" + Config.DISCUSSIONS)
                        .inAppLinksEnabled(true)
                        .externalLinksEnabled(false)
                        .build()
                    CourseArea.PROGRESS       -> fragment = ProgressFragmentAutoBundle.builder(courseId).build()
                    CourseArea.COURSE_DETAILS -> fragment = DescriptionFragmentAutoBundle.builder(courseId).build()
                    CourseArea.CERTIFICATES   -> fragment = CertificatesFragmentAutoBundle.builder(courseId).build()
                    CourseArea.DOCUMENTS      -> fragment = DocumentListFragmentAutoBundle.builder(courseId).build()
                    CourseArea.ANNOUNCEMENTS  -> fragment = AnnouncementListFragmentAutoBundle.builder(courseId).build()
                    CourseArea.RECAP          -> fragment = WebViewFragmentAutoBundle.builder(Config.HOST_URL + Config.RECAP + courseId)
                        .inAppLinksEnabled(true)
                        .externalLinksEnabled(false)
                        .build()
                }
            }
            return fragment
        }

        private fun makeFragmentName(viewId: Int, index: Int): String {
            return "android:switcher:$viewId:$index"
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
