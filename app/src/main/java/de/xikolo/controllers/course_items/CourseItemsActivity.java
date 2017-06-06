package de.xikolo.controllers.course_items;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yatatsu.autobundle.AutoBundleField;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controllers.base.BasePresenterActivity;
import de.xikolo.controllers.helper.SectionDownloadHelper;
import de.xikolo.managers.ItemManager;
import de.xikolo.managers.Result;
import de.xikolo.models.Item;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.course_items.CourseItemsPresenter;
import de.xikolo.presenters.course_items.CourseItemsPresenterFactory;
import de.xikolo.presenters.course_items.CourseItemsView;
import de.xikolo.utils.DateUtil;
import de.xikolo.utils.LanalyticsUtil;

public class CourseItemsActivity extends BasePresenterActivity<CourseItemsPresenter, CourseItemsView> implements CourseItemsView {

    public static final String TAG = CourseItemsActivity.class.getSimpleName();

    @AutoBundleField
    private String courseId;

    @AutoBundleField
    private String sectionId;

    @AutoBundleField(required = false)
    private String itemId;

    private ViewPager viewpager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_tabs);

        int index = 0;
        if (itemId != null) {
            index = module.items.indexOf(item);
        }
        module.items.get(index).progress.visited = true;
        itemManager.updateProgression(progressionResult, module.items.get(index));

        if (index == 0) {
            LanalyticsUtil.trackVisitedItem(module.items.get(0).id, course.id, module.id);
        }

        // Initialize the ViewPager and set an adapter
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        ModulePagerAdapter adapter = new ModulePagerAdapter(getSupportFragmentManager(), module.items);
        viewpager.setAdapter(adapter);
        viewpager.setOffscreenPageLimit(2);

        // Bind the tabs to the ViewPager
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewpager);

            tabLayout.setOnTabSelectedListener(adapter);


            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) {
                    tab.setCustomView(adapter.getCustomTabView(i, tabLayout.getSelectedTabPosition(), tabLayout));
                }
            }
        }

        viewpager.setCurrentItem(index, false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (presenter.hasDownloadableContent(sectionId)) {
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
            SectionDownloadHelper sectionDownloadHelper = new SectionDownloadHelper(this);
            sectionDownloadHelper.initSectionDownloads(course, section);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    protected PresenterFactory<CourseItemsPresenter> getPresenterFactory() {
        return new CourseItemsPresenterFactory(sectionId);
    }

    public class ModulePagerAdapter extends FragmentPagerAdapter implements TabLayout.OnTabSelectedListener {

        private final float transparent = 0.7f;
        private final float opaque = 1f;

        private List<Item> items;
        private FragmentManager fragmentManager;

        public ModulePagerAdapter(FragmentManager fm, List<Item> items) {
            super(fm);
            this.fragmentManager = fm;
            this.items = items;

            List<Item> toRemove = new ArrayList<>();
            for (Item item : items) {
                if (!DateUtil.nowIsAfter(item.deadline)) {
                    toRemove.add(item);
                }
            }
            this.items.removeAll(toRemove);
        }

        public View getCustomTabView(int position, int currentPosition, ViewGroup parent) {
            final View layout = getLayoutInflater().inflate(R.layout.container_custom_tab, parent, false);

            TextView label = (TextView) layout.findViewById(R.id.tabLabel);
            View unseenIndicator = layout.findViewById(R.id.unseenIndicator);

            if (position != currentPosition) {
                ViewCompat.setAlpha(label, transparent);
                ViewCompat.setAlpha(unseenIndicator, transparent);
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
            if (fragment == null) {
                switch (item.type) {
                    case Item.TYPE_TEXT:
                    case Item.TYPE_QUIZ:
                    case Item.TYPE_LTI:
                    case Item.TYPE_PEER:
                        fragment = ItemWebViewFragment.newInstance(course, module, items.get(position));
                        break;
                    case Item.TYPE_VIDEO:
                        fragment = VideoPreviewFragment.newInstance(course, module, items.get(position));
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

                ViewCompat.setAlpha(label, opaque);
                ViewCompat.setAlpha(unseenIndicator, opaque);

                unseenIndicator.setVisibility(View.GONE);
                Item item = items.get(tabLayout.getSelectedTabPosition());
                item.visited = true;
                itemManager.updateProgression(progressionResult, item);

                LanalyticsUtil.trackVisitedItem(item.id, course.id, module.id);
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            View view = tab.getCustomView();
            if (view != null) {
                TextView label = (TextView) view.findViewById(R.id.tabLabel);
                View unseenIndicator = view.findViewById(R.id.unseenIndicator);

                ViewCompat.setAlpha(label, transparent);
                ViewCompat.setAlpha(unseenIndicator, transparent);
            }
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }


    }

}
