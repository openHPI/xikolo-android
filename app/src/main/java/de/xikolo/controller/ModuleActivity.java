package de.xikolo.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.controller.exceptions.WrongParameterException;
import de.xikolo.controller.helper.CacheController;
import de.xikolo.controller.helper.ModuleDownloadController;
import de.xikolo.controller.module.PagerFragment;
import de.xikolo.controller.module.VideoFragment;
import de.xikolo.controller.module.WebItemFragment;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.model.ItemModel;
import de.xikolo.model.Result;
import de.xikolo.model.events.NetworkStateEvent;
import de.xikolo.util.DateUtil;
import de.xikolo.util.ToastUtil;

public class ModuleActivity extends BaseActivity {

    public static final String TAG = ModuleActivity.class.getSimpleName();

    public static final String ARG_COURSE = "arg_course";
    public static final String ARG_MODULE = "arg_module";
    public static final String ARG_ITEM = "arg_item";

    private Course mCourse;
    private Module mModule;
    private Item mItem;

    private ItemModel mItemModel;
    private Result<Void> mProgressionResult;

    private PagerSlidingTabStrip mPagerSlidingTabStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);
        setupActionBar();

        Bundle b = getIntent().getExtras();
        if (b == null || !b.containsKey(ARG_COURSE) || !b.containsKey(ARG_MODULE)) {
            if (videoCastManager.isConnected()) {
                CacheController cacheController = new CacheController();
                cacheController.readCachedExtras();
                if (cacheController.getCourse() != null) {
                    mCourse = cacheController.getCourse();
                }
                if (cacheController.getModule() != null) {
                    mModule = cacheController.getModule();
                }
                if (cacheController.getItem() != null) {
                    mItem = cacheController.getItem();
                }
            } else {
                throw new WrongParameterException();
            }
        } else {
            this.mCourse = b.getParcelable(ARG_COURSE);
            this.mModule = b.getParcelable(ARG_MODULE);
            this.mItem = b.getParcelable(ARG_ITEM);
        }

        mItemModel = new ItemModel(jobManager);
        mProgressionResult = new Result<Void>() {
        };

        setTitle(mModule.name);

        // Initialize the ViewPager and set an adapter
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        ModulePagerAdapter adapter = new ModulePagerAdapter(getSupportFragmentManager(), this, pager, mModule.items);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(3);

        // Bind the tabs to the ViewPager
        mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mPagerSlidingTabStrip.setViewPager(pager);

        mPagerSlidingTabStrip.setOnPageChangeListener(adapter);

        if (mItem != null) {
            int index = mModule.items.indexOf(mItem);

            pager.setCurrentItem(index, false);
            mModule.items.get(index).progress.visited = true;

            if (index == 0) {
                mItemModel.updateProgression(mProgressionResult, mModule, mModule.items.get(index));
            }
        } else {
            mItemModel.updateProgression(mProgressionResult, mModule, mModule.items.get(0));
            mModule.items.get(0).progress.visited = true;
        }
        pager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onEventMainThread(NetworkStateEvent event) {
        super.onEventMainThread(event);

        if (mPagerSlidingTabStrip != null) {
            if (event.isOnline()) {
                mPagerSlidingTabStrip.setBackgroundColor(ContextCompat.getColor(this, R.color.apptheme_main));
            } else {
                mPagerSlidingTabStrip.setBackgroundColor(ContextCompat.getColor(this, R.color.offline_mode_actionbar));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.download, menu);
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
            Log.d(TAG, "Menu Download reached");
            ModuleDownloadController moduleDownloadController = new ModuleDownloadController(this);
            moduleDownloadController.initModuleDownloads(mCourse, mModule);
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
        b.putParcelable(ARG_MODULE, mModule);
        intent.putExtras(b);
        setResult(RESULT_OK, intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public class ModulePagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener, PagerSlidingTabStrip.CustomTabProvider {

        private static final float OPAQUE = 1.0f;
        private static final float HALF_TRANSP = 0.5f;
        private List<Item> mItems;
        private Context mContext;
        private FragmentManager mFragmentManager;
        private ViewPager mPager;
        private int lastPosition = 0;
        private float tabTextAlpha = HALF_TRANSP;
        private float tabTextSelectedAlpha = OPAQUE;

        public ModulePagerAdapter(FragmentManager fm, Context context, ViewPager pager, List<Item> items) {
            super(fm);
            mItems = items;
            mPager = pager;

            List<Item> toRemove = new ArrayList<>();
            for (Item item : items) {
                if (!DateUtil.nowIsBetween(item.available_from, item.available_to) || item.locked) {
                    toRemove.add(item);
                }
            }
            mItems.removeAll(toRemove);

            mContext = context;
            mFragmentManager = fm;
        }

        @Override
        public View getCustomTabView(final ViewGroup viewGroup, int position) {
            final View layout = getLayoutInflater().inflate(R.layout.tab_item, null);

            TextView label = (TextView) layout.findViewById(R.id.tabLabel);
            View unseenIndicator = layout.findViewById(R.id.unseenIndicator);

            float alpha = mPager.getCurrentItem() == position ? tabTextSelectedAlpha : tabTextAlpha;
            ViewCompat.setAlpha(label, alpha);
            ViewCompat.setAlpha(unseenIndicator, alpha);

            final Item item = mItems.get(position);
            if (!item.progress.visited) {
                unseenIndicator.setVisibility(View.VISIBLE);
            } else {
                unseenIndicator.setVisibility(View.GONE);
            }

            label.setText(getPageTitle(position));

            final GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public void onLongPress(MotionEvent e) {
                    ToastUtil.show(item.title, Gravity.CENTER_HORIZONTAL | Gravity.TOP,
                            0, (int) viewGroup.getY() + layout.getHeight() + mPagerSlidingTabStrip.getIndicatorHeight());
                }

            });

            layout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });

            return layout;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Item item = mItems.get(position);
            String title = "";
            if (item.type.equals(Item.TYPE_TEXT)) {
                title = mContext.getString(R.string.icon_text);
            } else if (item.type.equals(Item.TYPE_VIDEO)) {
                title = mContext.getString(R.string.icon_video);
            } else if (item.type.equals(Item.TYPE_SELFTEST)) {
                title = mContext.getString(R.string.icon_selftest);
            } else if (item.type.equals(Item.TYPE_ASSIGNMENT)
                    || item.type.equals(Item.TYPE_EXAM)
                    || item.type.equals(Item.TYPE_PEER)) {
                title = mContext.getString(R.string.icon_assignment);
            } else if (item.type.equals(Item.TYPE_LTI)) {
                title = mContext.getString(R.string.icon_lti);
            }
            return title;
        }

        @Override
        public int getCount() {
            return this.mItems.size();
        }

        @Override
        public Fragment getItem(int position) {
            Item item = mItems.get(position);

            // Check if this Fragment already exists.
            // Fragment Name is saved by FragmentPagerAdapter implementation.
            String name = makeFragmentName(R.id.pager, position);
            Fragment fragment = mFragmentManager.findFragmentByTag(name);
            if (fragment == null) {
                if (item.type.equals(Item.TYPE_TEXT)) {
                    fragment = WebItemFragment.newInstance(mCourse, mModule, mItems.get(position));
                } else if (item.type.equals(Item.TYPE_VIDEO)) {
                    fragment = VideoFragment.newInstance(mCourse, mModule, mItems.get(position));
                } else if (item.type.equals(Item.TYPE_SELFTEST)
                        || item.type.equals(Item.TYPE_ASSIGNMENT)
                        || item.type.equals(Item.TYPE_EXAM)) {
                    fragment = WebItemFragment.newInstance(mCourse, mModule, mItems.get(position));
                } else if (item.type.equals(Item.TYPE_LTI)) {
                    fragment = WebItemFragment.newInstance(mCourse, mModule, mItems.get(position));
                } else if (item.type.equals(Item.TYPE_PEER)) {
                    fragment = WebItemFragment.newInstance(mCourse, mModule, mItems.get(position));
                }
            }
            return fragment;
        }

        private String makeFragmentName(int viewId, int index) {
            return "android:switcher:" + viewId + ":" + index;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mItems.get(position).progress.visited = true;

            notifyDataSetChanged();

            mItemModel.updateProgression(mProgressionResult, mModule, mItems.get(position));

            if (lastPosition != position) {
                PagerFragment fragment = (PagerFragment) getItem(lastPosition);
                fragment.pageChanged();
                lastPosition = position;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            PagerFragment fragment = (PagerFragment) getItem(lastPosition);
            fragment.pageScrolling(state);
        }

    }

}
