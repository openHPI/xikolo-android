package de.xikolo.controllers.channels;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;

import com.yatatsu.autobundle.AutoBundleField;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.config.GlideApp;
import de.xikolo.controllers.base.BaseActivity;
import de.xikolo.controllers.helper.CollapsingToolbarHelper;
import de.xikolo.models.Channel;
import de.xikolo.utils.ShareUtil;

public class ChannelDetailsActivity extends BaseActivity {

    public static final String TAG = ChannelDetailsActivity.class.getSimpleName();

    @AutoBundleField String channelId;

    @AutoBundleField(required = false) boolean scrollToCourses = false;

    @BindView(R.id.toolbar_image) ImageView imageView;
    @BindView(R.id.appbar) AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.stub_bottom) ViewStub stubBottom;
    @BindView(R.id.scrim_top) View scrimTop;
    @BindView(R.id.scrim_bottom) View scrimBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_collapsing);
        setupActionBar(true);
        enableOfflineModeToolbar(false);

        Channel channel = Channel.get(channelId);

        setTitle(channel.title);

        int color = channel.getColorOrDefault();
        collapsingToolbar.setContentScrimColor(color);
        collapsingToolbar.setBackgroundColor(color);
        collapsingToolbar.setStatusBarScrimColor(color);

        String tag = "content";

        if (channel.imageUrl != null) {
            GlideApp.with(this).load(channel.imageUrl).into(imageView);
        } else {
            CollapsingToolbarHelper.lockCollapsingToolbar(channel.title, appBarLayout, collapsingToolbar, toolbar, scrimTop, scrimBottom);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            final ChannelDetailsFragment fragment = ChannelDetailsFragmentAutoBundle.builder(channelId).scrollToCourses(scrollToCourses).build();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, fragment, tag);
            transaction.commit();
        }
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
