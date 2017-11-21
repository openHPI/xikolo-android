package de.xikolo.controllers.course_items;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yatatsu.autobundle.AutoBundleField;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import de.xikolo.R;
import de.xikolo.config.Config;
import de.xikolo.controllers.base.BasePresenterActivity;
import de.xikolo.controllers.helper.SectionDownloadHelper;
import de.xikolo.controllers.webview.WebViewFragmentAutoBundle;
import de.xikolo.events.NetworkStateEvent;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.course_items.CourseItemsPresenter;
import de.xikolo.presenters.course_items.CourseItemsPresenterFactory;
import de.xikolo.presenters.course_items.CourseItemsView;

public class CourseItemsActivity extends BasePresenterActivity<CourseItemsPresenter, CourseItemsView> implements CourseItemsView {

    public static final String TAG = CourseItemsActivity.class.getSimpleName();

    @AutoBundleField String courseId;

    @AutoBundleField String sectionId;

    @AutoBundleField int index;

    private ViewPager viewpager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_tabs);
        setupActionBar();

        // Initialize the ViewPager and set an adapter
        viewpager = (ViewPager) findViewById(R.id.viewpager);
    }

    @Override
    public void setupView(List<Item> itemList) {
        ItemsPagerAdapter adapter = new ItemsPagerAdapter(getSupportFragmentManager(), itemList);
        viewpager.setAdapter(adapter);
        viewpager.setOffscreenPageLimit(2);

        // Bind the tabs to the ViewPager
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewpager);

            tabLayout.clearOnTabSelectedListeners();
            tabLayout.addOnTabSelectedListener(adapter);

            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) {
                    tab.setCustomView(adapter.getCustomTabView(i, tabLayout.getSelectedTabPosition(), tabLayout));
                }
            }
        }
    }

    @Override
    public void setCurrentItem(int index) {
        viewpager.setCurrentItem(index, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (presenter.hasDownloadableContent()) {
            getMenuInflater().inflate(R.menu.download, menu);
        }
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar module clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        if (id == R.id.action_download) {
            presenter.onSectionDownloadClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void startSectionDownload(Course course, Section section) {
        SectionDownloadHelper sectionDownloadHelper = new SectionDownloadHelper(this);
        sectionDownloadHelper.initSectionDownloads(course, section);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }

    @Override
    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onNetworkEvent(NetworkStateEvent event) {
        super.onNetworkEvent(event);

        if (tabLayout != null) {
            if (event.isOnline()) {
                tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.apptheme_toolbar));
            } else {
                tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.offline_mode_toolbar));
            }
        }
    }

    @NonNull
    @Override
    protected PresenterFactory<CourseItemsPresenter> getPresenterFactory() {
        return new CourseItemsPresenterFactory(courseId, sectionId, index);
    }

    public class ItemsPagerAdapter extends FragmentPagerAdapter implements TabLayout.OnTabSelectedListener {

        private final float transparent = 0.7f;
        private final float opaque = 1f;

        private List<Item> items;
        private FragmentManager fragmentManager;

        public ItemsPagerAdapter(FragmentManager fm, List<Item> items) {
            super(fm);
            this.fragmentManager = fm;
            this.items = items;
        }

        public View getCustomTabView(int position, int currentPosition, ViewGroup parent) {
            final View layout = getLayoutInflater().inflate(R.layout.container_custom_tab, parent, false);

            TextView label = (TextView) layout.findViewById(R.id.tabLabel);
            View unseenIndicator = layout.findViewById(R.id.unseenIndicator);

            if (position != currentPosition) {
                label.setAlpha(transparent);
                unseenIndicator.setAlpha(transparent);
            }

            final Item item = items.get(position);
            if (!item.visited) {
                unseenIndicator.setVisibility(View.VISIBLE);
            } else {
                unseenIndicator.setVisibility(View.GONE);
            }

            label.setText(getPageTitle(position));

            return layout;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Item item = items.get(position);
            return getString(item.getIconRes());
        }

        @Override
        public int getCount() {
            return this.items.size();
        }

        @Override
        public Fragment getItem(int position) {
            Item item = items.get(position);

            // Check if this Fragment already exists.
            // Fragment Name is saved by FragmentPagerAdapter implementation.
            String name = makeFragmentName(R.id.viewpager, position);
            Fragment fragment = fragmentManager.findFragmentByTag(name);
            String url = Config.HOST_URL + Config.COURSES + courseId + "/" + Config.ITEMS + item.id;
            if (fragment == null) {
                switch (item.type) {
                    case Item.TYPE_LTI:
                        fragment = WebViewFragmentAutoBundle.builder(url)
                                .inAppLinksEnabled(false)
                                .externalLinksEnabled(false)
                                .build();
                        break;
                    case Item.TYPE_QUIZ:
                    case Item.TYPE_PEER:
                        fragment = WebViewFragmentAutoBundle.builder(url)
                                .inAppLinksEnabled(true)
                                .externalLinksEnabled(false)
                                .build();
                        break;
                    case Item.TYPE_TEXT:
                        fragment = RichTextFragmentAutoBundle.builder(courseId, sectionId, item.id).build();
                        break;
                    case Item.TYPE_VIDEO:
                        fragment = VideoPreviewFragmentAutoBundle.builder(courseId, sectionId, item.id).build();
                        break;
                }
            }
            return fragment;
        }

        private String makeFragmentName(int viewId, int index) {
            return "android:switcher:" + viewId + ":" + index;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            viewpager.setCurrentItem(tabLayout.getSelectedTabPosition(), true);
            View view = tab.getCustomView();
            if (view != null) {
                TextView label = (TextView) view.findViewById(R.id.tabLabel);
                View unseenIndicator = view.findViewById(R.id.unseenIndicator);

                label.setAlpha(opaque);
                unseenIndicator.setAlpha(opaque);

                unseenIndicator.setVisibility(View.GONE);

                int tabPosition = tabLayout.getSelectedTabPosition();
                presenter.onItemSelected(tabPosition);
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            View view = tab.getCustomView();
            if (view != null) {
                TextView label = (TextView) view.findViewById(R.id.tabLabel);
                View unseenIndicator = view.findViewById(R.id.unseenIndicator);

                label.setAlpha(transparent);
                unseenIndicator.setAlpha(transparent);
            }
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }

    }

}
