package de.xikolo.controller;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.widget.MediaController;
import android.widget.VideoView;

import de.xikolo.R;
import de.xikolo.controller.exceptions.WrongParameterException;
import de.xikolo.model.Item;
import de.xikolo.model.Video;

public class VideoActivity extends Activity {

    public static final String TAG = VideoActivity.class.getSimpleName();

    public static final String ARG_ITEM = "arg_item";
    public static final String ARG_TIME = "arg_time";
    int mTime;
    private VideoView mVideo;
    private MediaController mVideoController;

    private Item<Video> mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_video);

        Bundle b = getIntent().getExtras();
        if (b == null || !b.containsKey(ARG_ITEM)) {
            throw new WrongParameterException();
        } else {
            this.mItem = b.getParcelable(ARG_ITEM);
            if (b.containsKey(ARG_TIME)) {
                mTime = b.getInt(ARG_TIME);
            }
        }

        setTitle(mItem.object.title);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (Build.VERSION.SDK_INT > 15) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        mVideoController.show();
                    }
                }
            }
        });

        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_alpha)));

        hideControls();

        mVideo = (VideoView) findViewById(R.id.video);

        mVideoController = new MediaController(this) {
            @Override
            public void show() {
                showControls();
                super.show();
            }

            @Override
            public void hide() {
                hideControls();
                super.hide();
            }
        };
        mVideoController.setAnchorView(mVideo);
        mVideoController.setKeepScreenOn(true);

        if (Build.VERSION.SDK_INT > 16) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getRealSize(size);
            Rect usableRect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(usableRect);
            mVideoController.setPadding(0,
                    0,
                    size.x - usableRect.right,
                    size.y - usableRect.bottom);
        }

        mVideo.setMediaController(mVideoController);
        Uri uri = Uri.parse(mItem.object.url);
        mVideo.setVideoURI(uri);

        if (mTime > 0) {
            mVideo.seekTo(mTime);
        }
        mVideo.start();
    }

    private void hideControls() {
        View decorView = getWindow().getDecorView();
        int uiOptions;
        if (Build.VERSION.SDK_INT > 16) {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        } else if (Build.VERSION.SDK_INT > 15) {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE;
        } else {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        decorView.setSystemUiVisibility(uiOptions);
        getActionBar().hide();
    }

    private void showControls() {
        getActionBar().show();
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
//        if (id == android.R.id.home) {
//            finish();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieSyncManager.getInstance().sync();
    }

}
