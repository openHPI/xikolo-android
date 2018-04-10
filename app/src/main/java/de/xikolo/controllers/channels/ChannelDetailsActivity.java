package de.xikolo.controllers.channels;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewStub;
import android.widget.ImageView;

import com.yatatsu.autobundle.AutoBundleField;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.config.GlideApp;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.models.Channel;
import de.xikolo.utils.AndroidDimenUtil;
import de.xikolo.utils.ShareUtil;

public class ChannelDetailsActivity extends BaseActivity {

    public static final String TAG = ChannelDetailsActivity.class.getSimpleName();

    @AutoBundleField String channelId;

    @AutoBundleField(required = false) boolean scrollToCourses = false;

    @BindView(R.id.toolbar_image) ImageView imageView;
    @BindView(R.id.appbar) AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.stub_bottom) ViewStub stubBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_collapsing);
        setupActionBar(true);
        enableOfflineModeToolbar(false);

        Channel channel = Channel.get(channelId);

        setTitle(channel.name);

        int color = Color.parseColor(channel.color);
        collapsingToolbar.setContentScrimColor(color);
        collapsingToolbar.setBackgroundColor(color);
        collapsingToolbar.setStatusBarScrimColor(color);

        String tag = "content";

        if(channel.imageUrl != null){
            GlideApp.with(this).load(channel.imageUrl).into(imageView);
        } else {
            lockCollapsingToolbar(channel.name);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            final ChannelDetailsFragment fragment = ChannelDetailsFragmentAutoBundle.builder(channelId).scrollToCourses(scrollToCourses).build();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, fragment, tag);
            transaction.commit();
        }
    }

    private void lockCollapsingToolbar(String title) {
        appBarLayout.setExpanded(false, false);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        lp.height = AndroidDimenUtil.getActionBarHeight() + AndroidDimenUtil.getStatusBarHeight();
        collapsingToolbar.setTitleEnabled(false);
        toolbar.setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_share:
                ShareUtil.shareCourseLink(this, channelId);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
