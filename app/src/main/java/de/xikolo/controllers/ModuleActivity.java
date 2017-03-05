package de.xikolo.controllers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controllers.activities.BaseActivity;
import de.xikolo.controllers.exceptions.WrongParameterException;
import de.xikolo.controllers.helper.CacheController;
import de.xikolo.controllers.helper.ModuleDownloadController;
import de.xikolo.controllers.module.ItemWebViewFragment;
import de.xikolo.controllers.module.VideoFragment;
import de.xikolo.managers.ItemManager;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Module;
import de.xikolo.managers.Result;
import de.xikolo.events.NetworkStateEvent;
import de.xikolo.utils.DateUtil;
import de.xikolo.utils.LanalyticsUtil;

public class ModuleActivity extends BaseActivity {

    public static final String TAG = ModuleActivity.class.getSimpleName();

    public static final String ARG_COURSE = "arg_course";
    public static final String ARG_MODULE = "arg_module";
    public static final String ARG_ITEM = "arg_item";

    private Course course;
    private Module module;
    private Item item;

    private ItemManager itemManager;
    private Result<Void> progressionResult;

    private ViewPager viewpager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_tabs);
        setupActionBar();

        Bundle b = getIntent().getExtras();
        if (b == null || !b.containsKey(ARG_COURSE) || !b.containsKey(ARG_MODULE)) {
            CacheController cacheController = new CacheController();
            cacheController.readCachedExtras();
            if (cacheController.getCourse() != null) {
                course = cacheController.getCourse();
            }
            if (cacheController.getModule() != null) {
                module = cacheController.getModule();
            }
            if (cacheController.getItem() != null) {
                item = cacheController.getItem();
            }
            if (course != null && module != null && item != null) {
                Bundle restartBundle = new Bundle();
//                restartBundle.putParcelable(ARG_COURSE, course);
                restartBundle.putParcelable(ARG_MODULE, module);
                restartBundle.putParcelable(ARG_ITEM, item);
                Intent restartIntent = new Intent(ModuleActivity.this, ModuleActivity.class);
                restartIntent.putExtras(restartBundle);
                finish();
                startActivity(restartIntent);
            }
        } else {
            this.course = b.getParcelable(ARG_COURSE);
            this.module = b.getParcelable(ARG_MODULE);
            this.item = b.getParcelable(ARG_ITEM);
        }

        if (course == null) {
            throw new WrongParameterException("Course is null");
        }
        if (module == null) {
            throw new WrongParameterException("Module is null for Course " + course.title + " (" + course.id + ")");
        }
        if (module == null || module.items.size() == 0) {
            throw new WrongParameterException("Module Items are empty for Course " + course.title + " (" + course.id + ")" +
                    " and Module " + module.name + " (" + module.id + ")");
        }

        itemManager = new ItemManager(jobManager);
        progressionResult = new Result<Void>() {
        };

        setTitle(module.name);

        int index = 0;
        if (item != null) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean downloadableContent = false;
        if (module != null && module.items != null) {
            for (Item item : module.items) {
                if (item.type.equals(Item.TYPE_VIDEO)) {
                    downloadableContent = true;
                    break;
                }
            }
        }

        if (downloadableContent) {
            // Inflate the menu; this adds items to the action bar if it is present.
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
            setResult();
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        if (id == R.id.action_download) {
            ModuleDownloadController moduleDownloadController = new ModuleDownloadController(this);
            moduleDownloadController.initModuleDownloads(course, module);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult();
        finish();
    }

    private void setResult() {
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putParcelable(ARG_MODULE, module);
        intent.putExtras(b);
        setResult(RESULT_OK, intent);
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
                if (!DateUtil.nowIsBetween(item.available_from, item.available_to) || item.locked) {
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
            if (!item.progress.visited) {
                unseenIndicator.setVisibility(View.VISIBLE);
            } else {
                unseenIndicator.setVisibility(View.GONE);
            }

            label.setText(getPageTitle(position));

            return layout;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Context context = GlobalApplication.getInstance();
            Item item = items.get(position);

            return Item.getIcon(context, item.type, item.exercise_type);
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
                    case Item.TYPE_SELFTEST:
                    case Item.TYPE_LTI:
                    case Item.TYPE_PEER:
                        fragment = ItemWebViewFragment.newInstance(course, module, items.get(position));
                        break;
                    case Item.TYPE_VIDEO:
                        fragment = VideoFragment.newInstance(course, module, items.get(position));
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
                item.progress.visited = true;
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
