package de.xikolo.controllers.section

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
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
import de.xikolo.controllers.base.ViewModelActivity
import de.xikolo.controllers.dialogs.CreateTicketDialog
import de.xikolo.controllers.dialogs.CreateTicketDialogAutoBundle
import de.xikolo.controllers.helper.SectionDownloadHelper
import de.xikolo.controllers.webview.WebViewFragmentAutoBundle
import de.xikolo.extensions.observe
import de.xikolo.models.Course
import de.xikolo.models.Item
import de.xikolo.models.Section
import de.xikolo.utils.LanalyticsUtil
import de.xikolo.viewmodels.section.CourseItemsViewModel

class CourseItemsActivity : ViewModelActivity<CourseItemsViewModel>() {

    companion object {
        val TAG: String = CourseItemsActivity::class.java.simpleName

        private const val PAGER_ITEM_TRANSPARENT = 0.7f
        private const val PAGER_ITEM_OPAQUE = 1f
    }

    @AutoBundleField
    lateinit var courseId: String

    @AutoBundleField
    lateinit var sectionId: String

    @AutoBundleField
    var index: Int = 0

    private var lastTrackedIndex: Int = -1

    @BindView(R.id.viewpager)
    lateinit var viewPager: ViewPager2

    @BindView(R.id.tabs)
    lateinit var tabLayout: TabLayout

    private var course: Course? = null
    private var section: Section? = null

    override fun createViewModel(): CourseItemsViewModel {
        return CourseItemsViewModel(courseId, sectionId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_blank_tabs)
        setupActionBar()

        FirebaseCrashlytics.getInstance().setCustomKey("course_id", courseId)
        FirebaseCrashlytics.getInstance().setCustomKey("section_id", sectionId)

        val adapter = ItemsPagerAdapter()
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 2
        adapter.setupTabs()
        tabLayout.addOnTabSelectedListener(adapter)

        viewModel.course
            .observe(this) {
                course = it
            }

        viewModel.section
            .observe(this) {
                section = it
                title = it.title

                adapter.items = it.accessibleItems
                viewPager.currentItem = index
                onItemSelected(index)
            }
    }

    private fun onItemSelected(position: Int) {
        index = position

        Bundle().also { bundle ->
            CourseItemsActivityAutoBundle.pack(this, bundle)
            intent.replaceExtras(bundle)
        }

        section?.accessibleItems?.get(index)?.let {
            FirebaseCrashlytics.getInstance().setCustomKey("item_id", it.id)

            viewModel.markItemVisited(it)

            if (lastTrackedIndex != index) {
                LanalyticsUtil.trackVisitedItem(it.id, courseId, sectionId, it.contentType)
            }
            lastTrackedIndex = index
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (section?.hasDownloadableContent() == true) {
            menuInflater.inflate(R.menu.download, menu)
        }
        menuInflater.inflate(R.menu.helpdesk, menu)
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home    -> {
                NavUtils.navigateUpFromSameTask(this)
                true
            }
            R.id.action_download -> {
                SectionDownloadHelper(this).initSectionDownloads(course!!, section!!)
                true
            }
            R.id.action_helpdesk -> {
                val dialog = CreateTicketDialogAutoBundle.builder().courseId(courseId).build()
                dialog.show(supportFragmentManager, CreateTicketDialog.TAG)
                true
            }
            else                 -> super.onOptionsItemSelected(item)
        }
    }

    override fun onConnectivityChange(isOnline: Boolean) {
        super.onConnectivityChange(isOnline)

        if (isOnline) {
            tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbar))
        } else {
            tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbar_offline))
        }
    }

    inner class ItemsPagerAdapter :
        FragmentStateAdapter(this), TabLayout.OnTabSelectedListener {

        fun setupTabs() {
            TabLayoutMediator(tabLayout, viewPager, true, true) { tab, position ->
                tab.text = getPageTitle(position)
            }.attach()
        }

        var items: List<Item> = listOf()
            set(value) {
                field = value
                val oldIndex = index
                notifyDataSetChanged()
                onItemSelected(oldIndex)

                for (position in 0 until tabLayout.tabCount) {
                    tabLayout.getTabAt(position)?.apply {
                        customView =
                            layoutInflater.inflate(R.layout.view_tab_section, parent, false).apply {
                                val label = findViewById<TextView>(R.id.tabLabel)
                                val unseenIndicator = findViewById<View>(R.id.unseenIndicator)

                                if (position != tabLayout.selectedTabPosition) {
                                    label.alpha = PAGER_ITEM_TRANSPARENT
                                    unseenIndicator.alpha = PAGER_ITEM_TRANSPARENT
                                }

                                unseenIndicator.visibility =
                                    if (!items[position].visited) {
                                        View.VISIBLE
                                    } else {
                                        View.GONE
                                    }

                                label.text = getPageTitle(position)
                            }
                    }
                }
            }

        private fun getPageTitle(position: Int): CharSequence? {
            return getString(items[position].iconRes)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun createFragment(position: Int): Fragment {
            val item = items[position]

            val url = Config.HOST_URL + Config.COURSES + courseId + "/" + Config.ITEMS + item.id
            return if (course?.enrollment?.proctored == true && item.proctored) {
                ProctoredItemFragment()
            } else when (item.contentType) {
                Item.TYPE_LTI ->
                    LtiExerciseFragmentAutoBundle.builder(courseId, sectionId, item.id).build()
                Item.TYPE_PEER ->
                    PeerAssessmentFragmentAutoBundle.builder(
                        courseId,
                        sectionId,
                        item.id
                    ).build()
                Item.TYPE_QUIZ ->
                    WebViewFragmentAutoBundle.builder(url)
                        .inAppLinksEnabled(true)
                        .externalLinksEnabled(false)
                        .build()
                Item.TYPE_TEXT ->
                    RichTextFragmentAutoBundle.builder(courseId, sectionId, item.id).build()
                Item.TYPE_VIDEO ->
                    VideoPreviewFragmentAutoBundle.builder(
                        courseId,
                        sectionId,
                        item.id
                    ).build()
                else ->
                    WebViewFragmentAutoBundle.builder(url)
                        .inAppLinksEnabled(false)
                        .externalLinksEnabled(false)
                        .build()
            }
        }

        override fun onTabSelected(tab: TabLayout.Tab) {
            onItemSelected(tab.position)

            tab.customView?.let {
                val label = it.findViewById<TextView>(R.id.tabLabel)
                val unseenIndicator = it.findViewById<View>(R.id.unseenIndicator)

                label.alpha = PAGER_ITEM_OPAQUE
                unseenIndicator.alpha = PAGER_ITEM_OPAQUE

                unseenIndicator.visibility = View.GONE
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            tab.customView?.let {
                val label = it.findViewById<TextView>(R.id.tabLabel)
                val unseenIndicator = it.findViewById<View>(R.id.unseenIndicator)

                label.alpha = PAGER_ITEM_TRANSPARENT
                unseenIndicator.alpha = PAGER_ITEM_TRANSPARENT
            }
        }

        override fun onTabReselected(tab: TabLayout.Tab) {}
    }
}
