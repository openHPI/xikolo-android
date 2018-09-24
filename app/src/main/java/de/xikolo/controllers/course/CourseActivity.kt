package de.xikolo.controllers.course

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.*
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewStub
import android.widget.Button
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.controllers.base.BasePresenterActivity
import de.xikolo.controllers.dialogs.ProgressDialog
import de.xikolo.controllers.dialogs.ProgressDialogAutoBundle
import de.xikolo.controllers.dialogs.UnenrollDialog
import de.xikolo.controllers.helper.CacheHelper
import de.xikolo.controllers.helper.CourseArea
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.controllers.webview.WebViewFragmentAutoBundle
import de.xikolo.events.NetworkStateEvent
import de.xikolo.models.Course
import de.xikolo.presenters.base.PresenterFactory
import de.xikolo.presenters.course.CoursePresenter
import de.xikolo.presenters.course.CoursePresenterFactory
import de.xikolo.presenters.course.CourseView
import de.xikolo.utils.ShareUtil
import de.xikolo.utils.ToastUtil
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CourseActivity : BasePresenterActivity<CoursePresenter, CourseView>(), CourseView, UnenrollDialog.Listener {

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

    private var progressDialog: ProgressDialog? = null

    private var adapter: CoursePagerAdapter? = null

    private var enrollBar: View? = null
    private var enrollButton: Button? = null

    private var areaState: CourseArea.State = CourseArea.All

    private var enrolled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank_tabs)
        setupActionBar(false)
        enableOfflineModeToolbar(true)
    }

    override fun setupView(course: Course, courseTab: CourseArea) {
        title = course.title

        if (stubBottom.parent != null) {
            stubBottom.layoutResource = R.layout.content_enroll_button
            enrollBar = stubBottom.inflate()
            enrollButton = enrollBar?.findViewById(R.id.button_enroll)
            enrollButton?.setOnClickListener { _ -> presenter.enroll() }
        }

        courseId = course.id

        adapter = CoursePagerAdapter(supportFragmentManager)
        adapter?.let { a ->
            viewPager.adapter = a

            tabLayout.clearOnTabSelectedListeners()
            tabLayout.addOnTabSelectedListener(a)
            tabLayout.setupWithViewPager(viewPager)
        }

        viewPager.currentItem = areaState.indexOf(courseTab)

        hideEnrollBar()
    }

    override fun onPresenterCreatedOrRestored(presenter: CoursePresenter) {
        val action = intent.action

        if (action != null && action == Intent.ACTION_VIEW) {
            presenter.handleDeepLink(intent.data)
        } else if (courseId == null) {
            val cacheController = CacheHelper()
            cacheController.readCachedExtras()
            if (cacheController.course != null) {
                courseId = cacheController.course.id
            }
            if (courseId != null) {
                val restartIntent = CourseActivityAutoBundle.builder().courseId(courseId).build(this)
                finish()
                startActivity(restartIntent)
            }
        } else {
            presenter.initCourse(courseId)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) {
            val action = intent.action

            if (action != null && action == Intent.ACTION_VIEW) {
                presenter.handleDeepLink(intent.data)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater

        if (enrolled) {
            inflater.inflate(R.menu.unenroll, menu)
        }

        inflater.inflate(R.menu.share, menu)
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
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
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        presenter.unenroll()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        adapter?.getItem(viewPager.currentItem)?.onActivityResult(requestCode, resultCode, data)
    }

    override fun setAreaState(state: CourseArea.State) {
        areaState = state
        adapter?.notifyDataSetChanged()
    }

    override fun hideEnrollBar() {
        enrolled = true
        enrollBar?.visibility = View.GONE
    }

    override fun showEnrollBar() {
        enrolled = false
        enrollBar?.visibility = View.VISIBLE
        enrollButton?.isEnabled = true
        enrollButton?.isClickable = true
        enrollButton?.setText(R.string.btn_enroll)
    }

    override fun showCourseUnavailableEnrollBar() {
        enrolled = true
        enrollBar?.visibility = View.VISIBLE
        enrollButton?.isEnabled = false
        enrollButton?.isClickable = false
        enrollButton?.setText(R.string.btn_starts_soon)
    }

    override fun finishActivity() {
        finish()
    }

    override fun restartActivity() {
        finish()
        startActivity(intent)
    }

    override fun showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialogAutoBundle.builder().build()
        }
        progressDialog?.show(supportFragmentManager, ProgressDialog.TAG)
    }

    override fun hideProgressDialog() {
        if (progressDialog?.dialog?.isShowing == true) {
            progressDialog?.dismiss()
        }
    }

    override fun showErrorToast() {
        ToastUtil.show(R.string.error)
    }

    override fun showNoNetworkToast() {
        ToastUtil.show(R.string.toast_no_network)
    }

    override fun showLoginRequiredMessage() {
        ToastUtil.show(R.string.toast_please_log_in)
    }

    override fun openLogin() {
        val intent = LoginActivityAutoBundle.builder().build(this)
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

    override fun getPresenterFactory(): PresenterFactory<CoursePresenter> {
        return CoursePresenterFactory()
    }

    inner class CoursePagerAdapter internal constructor(private val fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager), TabLayout.OnTabSelectedListener {

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
                courseId?.let { courseId ->
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
            }
            return fragment
        }

        private fun makeFragmentName(viewId: Int, index: Int): String {
            return "android:switcher:$viewId:$index"
        }

        override fun onTabSelected(tab: TabLayout.Tab) {
            val tabPosition = tabLayout.selectedTabPosition
            viewPager.setCurrentItem(tabPosition, true)
            presenter.setCourseTab(areaState.get(tabPosition))
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {

        }

        override fun onTabReselected(tab: TabLayout.Tab) {

        }
    }
}
