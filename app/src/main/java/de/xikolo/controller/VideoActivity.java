package de.xikolo.controller;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import de.xikolo.R;
import de.xikolo.controller.exceptions.WrongParameterException;
import de.xikolo.controller.helper.VideoController;
import de.xikolo.controller.module.VideoFragment;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.model.ItemModel;
import de.xikolo.model.Result;

public class VideoActivity extends BaseActivity {

    public static final String TAG = VideoActivity.class.getSimpleName();

    private VideoController mVideoController;
    private Course mCourse;
    private Module mModule;
    private Item<VideoItemDetail> mItem;
    private ItemModel itemModel;

    private View mVideoMetadataView;
    private TextView mVideoTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        setupActionBar();
        enableOfflineModeToolbar(false);
        setColorScheme(R.color.black, R.color.black);
        actionBar.setTitle("");
        actionBar.setSubtitle("");

        View videoContainer = findViewById(R.id.videoContainer);

        itemModel = new ItemModel(jobManager);

        mVideoMetadataView = findViewById(R.id.videoMetadata);
        mVideoTitleText = (TextView) findViewById(R.id.textTitle);

        mVideoController = new VideoController(this, videoContainer);
        mVideoController.setControllerListener(new VideoController.ControllerListener() {
            @Override
            public void onControllerShow() {
                showSystemBars();
            }

            @Override
            public void onControllerHide() {
                hideSystemBars();
            }
        });

        Bundle b = getIntent().getExtras();
        if (b == null || !b.containsKey(VideoFragment.KEY_COURSE) || !b.containsKey(VideoFragment.KEY_MODULE) || !b.containsKey(VideoFragment.KEY_ITEM)) {
            throw new WrongParameterException();
        } else {
            mCourse = getIntent().getExtras().getParcelable(VideoFragment.KEY_COURSE);
            mModule = getIntent().getExtras().getParcelable(VideoFragment.KEY_MODULE);
            mItem = getIntent().getExtras().getParcelable(VideoFragment.KEY_ITEM);

            itemModel.getLocalVideoProgress(new Result<VideoItemDetail>() {
                @Override
                protected void onSuccess(VideoItemDetail result, DataSource dataSource) {
                    mItem.detail = result;
                }
            }, mItem.detail);

            mVideoTitleText.setText(mItem.detail.title);
        }

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (Build.VERSION.SDK_INT >= 17) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        mVideoController.show();
                    }
                } else {
                    if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                        mVideoController.show();
                    }
                }
            }
        });

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        hideSystemBars();

        updateVideoView(getResources().getConfiguration().orientation);

        mVideoController.setupVideo(mCourse, mModule, mItem);
    }

    private void updateVideoView(int orientation) {
        View layout = findViewById(R.id.container);
        if (Build.VERSION.SDK_INT >= 17) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                layout.setFitsSystemWindows(true);

                actionBar.hide();

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getRealSize(size); // API 17

                View videoContainer = mVideoController.getVideoContainer();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoContainer.getLayoutParams();
                params.height = size.y;
                params.setMargins(0, 0, 0, 0);
                videoContainer.setLayoutParams(params);
                videoContainer.requestLayout();

                int statusBarHeight = 0;
                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    statusBarHeight = getResources().getDimensionPixelSize(resourceId);
                }

                int videoOffset = (size.y - size.x / 16 * 9) / 2;

                int systemBarHeight = size.y - getResources().getDisplayMetrics().heightPixels;

                mVideoController.getControllerView().setPadding(0,
                        videoOffset > statusBarHeight ? videoOffset : statusBarHeight,
                        size.x - getResources().getDisplayMetrics().widthPixels,
                        videoOffset > systemBarHeight ? videoOffset : systemBarHeight);

                mVideoMetadataView.setVisibility(View.GONE);
            } else {
                layout.setFitsSystemWindows(false);

                actionBar.show();

                int actionBarHeight = 0;
                TypedValue tv = new TypedValue();
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                }

                View videoContainer = mVideoController.getVideoContainer();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) videoContainer.getLayoutParams();
                params.height = (int) Math.ceil(getResources().getDisplayMetrics().widthPixels / 16. * 9.);
                params.setMargins(0, actionBarHeight, 0, 0);
                videoContainer.setLayoutParams(params);
                videoContainer.requestLayout();

                Log.d(TAG, "Toolbar height " + actionBarHeight);

                mVideoController.getControllerView().setPadding(0, 0, 0, 0);

                mVideoMetadataView.setVisibility(View.VISIBLE);
            }
        } else {
            layout.setFitsSystemWindows(false);
        }
    }

    private void hideSystemBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (Build.VERSION.SDK_INT >= 17) {
                uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE// API 16
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // API 16
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // API 16
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // API 14
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE // API 14
                        | View.SYSTEM_UI_FLAG_FULLSCREEN; // API 16
            } else {
                uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // API 14
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE; // API 14
            }
        } else {
            uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        }
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void showSystemBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (Build.VERSION.SDK_INT >= 17) {
                uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE // API 16
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // API 16
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN; // API 16
            } else {
                uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            }
        } else {
            uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        }
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        enableCastMediaRouterButton(false);
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mVideoController != null) {
            mVideoController.pause();
            VideoItemDetail itemDetail = mVideoController.getVideoItemDetail();
            if (itemDetail != null) {
                itemModel.updateLocalVideoProgress(new Result<Void>() {
                }, itemDetail);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mVideoController.show();

        updateVideoView(newConfig.orientation);
    }

}