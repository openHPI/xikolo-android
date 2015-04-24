package de.xikolo.controller;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import de.xikolo.R;
import de.xikolo.controller.exceptions.WrongParameterException;
import de.xikolo.controller.helper.VideoController;
import de.xikolo.controller.module.VideoFragment;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.entities.VideoItemDetail;

public class VideoActivity extends BaseActivity {

    public static final String TAG = VideoActivity.class.getSimpleName();

    private VideoController mVideoController;

    private Course mCourse;
    private Module mModule;
    private Item<VideoItemDetail> mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        View videoContainer = findViewById(R.id.videoContainer);

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
        mVideoController.disableHeader();

        Bundle b = getIntent().getExtras();
        if (b == null || !b.containsKey(VideoFragment.KEY_COURSE) || !b.containsKey(VideoFragment.KEY_MODULE) || !b.containsKey(VideoFragment.KEY_ITEM)) {
            throw new WrongParameterException();
        } else {
            mCourse = getIntent().getExtras().getParcelable(VideoFragment.KEY_COURSE);
            mModule = getIntent().getExtras().getParcelable(VideoFragment.KEY_MODULE);
            mItem = getIntent().getExtras().getParcelable(VideoFragment.KEY_ITEM);
            mVideoController.returnFromSavedInstanceState(getIntent().getExtras());
        }

        setTitle(mItem.detail.title);

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
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }

        hideSystemBars();

        View layout = findViewById(R.id.container);
        if (Build.VERSION.SDK_INT >= 17) {
            layout.setFitsSystemWindows(true);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getRealSize(size); // API 17
            Rect usableRect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(usableRect);
            mVideoController.getControllerView().setPadding(0,
                    0,
                    size.x - usableRect.right,
                    size.y - usableRect.bottom);
        } else {
            layout.setFitsSystemWindows(false);
        }

        mVideoController.setVideo(mCourse, mModule, mItem);
    }

    private void hideSystemBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions;
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
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void showSystemBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions;
        if (Build.VERSION.SDK_INT >= 17) {
            uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE // API 16
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // API 16
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN; // API 16
        } else {
            uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        }
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.module, menu);
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
        b.putParcelable(VideoFragment.KEY_ITEM, mItem);
        mVideoController.onSaveInstanceState(b);
        intent.putExtras(b);
        setResult(RESULT_OK, intent);
    }

}
