package de.xikolo.controller;

import android.app.ActionBar;
import android.content.Intent;
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

import de.xikolo.R;
import de.xikolo.controller.exceptions.WrongParameterException;
import de.xikolo.controller.module.VideoFragment;
import de.xikolo.entities.Item;
import de.xikolo.entities.ItemVideo;
import de.xikolo.controller.helper.VideoController;

public class VideoActivity extends BaseActivity {

    public static final String TAG = VideoActivity.class.getSimpleName();

    private VideoController mVideoController;

    private Item<ItemVideo> mItem;

    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
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
        if (b == null || !b.containsKey(VideoFragment.KEY_ITEM)) {
            throw new WrongParameterException();
        } else {
            mItem = getIntent().getExtras().getParcelable(VideoFragment.KEY_ITEM);
            mVideoController.returnFromSavedInstanceState(getIntent().getExtras());
        }

        setTitle(mItem.object.title);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (Build.VERSION.SDK_INT >= 16) {
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

        mActionBar = getActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_alpha)));

        hideSystemBars();

        if (Build.VERSION.SDK_INT >= 16) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getRealSize(size);
            Rect usableRect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(usableRect);
            mVideoController.getControllerView().setPadding(0,
                    0,
                    size.x - usableRect.right,
                    size.y - usableRect.bottom);
        }

        Uri uri = Uri.parse(mItem.object.url);
        mVideoController.setVideoURI(uri);
    }

    private void hideSystemBars() {
        View decorView = getWindow().getDecorView();
        int uiOptions;
        if (Build.VERSION.SDK_INT >= 16) {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // API 14
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE // API 14
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // API 16
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // API 16
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // API 16
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE; // API 16
        } else {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // API 14
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE; // API 14
        }
        decorView.setSystemUiVisibility(uiOptions);
        mActionBar.hide();
    }

    private void showSystemBars() {
        mActionBar.show();
        View decorView = getWindow().getDecorView();
        int uiOptions;
        if (Build.VERSION.SDK_INT >= 16) {
            uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // API 16
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // API 16
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE; // API 16
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
            finish();
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
