package de.xikolo.controllers.course;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.yatatsu.autobundle.AutoBundleField;

import de.xikolo.R;
import de.xikolo.controllers.base.BasePresenterActivity;
import de.xikolo.controllers.dialogs.ProgressDialog;
import de.xikolo.controllers.dialogs.UnenrollDialog;
import de.xikolo.controllers.shared.WebViewFragmentAutoBundle;
import de.xikolo.models.Course;
import de.xikolo.presenters.base.PresenterFactory;
import de.xikolo.presenters.course.CourseDetailsPresenter;
import de.xikolo.presenters.course.CourseDetailsPresenterFactory;
import de.xikolo.presenters.course.CourseDetailsView;
import de.xikolo.config.Config;
import de.xikolo.utils.ToastUtil;

public class CourseDetailsActivity extends BasePresenterActivity<CourseDetailsPresenter, CourseDetailsView> implements CourseDetailsView, UnenrollDialog.UnenrollDialogListener {

    public static final String TAG = CourseDetailsActivity.class.getSimpleName();

    @AutoBundleField String courseId;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        setupActionBar();
    }

    @Override
    public void setupView(Course course) {
        setTitle(course.title);

        String tag = "content";

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content, WebViewFragmentAutoBundle.builder(Config.URI + Config.COURSES + courseId).build(), tag);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.unenroll, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_unenroll:
                UnenrollDialog dialog = new UnenrollDialog();
                dialog.setUnenrollDialogListener(this);
                dialog.show(getSupportFragmentManager(), UnenrollDialog.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        presenter.unenroll(courseId);
    }

    @Override
    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.getInstance();
        }
        if (!progressDialog.getDialog().isShowing()) {
            progressDialog.show(getSupportFragmentManager(), ProgressDialog.TAG);
        }
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.getDialog().isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void showErrorToast() {
        ToastUtil.show(R.string.error);
    }

    @Override
    public void showNoNetworkToast() {
        ToastUtil.show(R.string.toast_no_network);
    }

    @NonNull
    @Override
    protected PresenterFactory<CourseDetailsPresenter> getPresenterFactory() {
        return new CourseDetailsPresenterFactory(courseId);
    }

}
