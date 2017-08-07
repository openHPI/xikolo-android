package de.xikolo.controllers.announcement;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.yatatsu.autobundle.AutoBundleField;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.controllers.base.LoadingStatePresenterFragment;
import de.xikolo.models.Announcement;
import de.xikolo.presenters.announcement.AnnouncementPresenter;
import de.xikolo.presenters.announcement.AnnouncementPresenterFactory;
import de.xikolo.presenters.announcement.AnnouncementView;
import de.xikolo.presenters.base.PresenterFactory;
import in.uncod.android.bypass.Bypass;

public class AnnouncementFragment extends LoadingStatePresenterFragment<AnnouncementPresenter, AnnouncementView> implements AnnouncementView {

    public static final String TAG = AnnouncementFragment.class.getSimpleName();

    @AutoBundleField String announcementId;

    @BindView(R.id.text) TextView text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.content_announcement;
    }

    @Override
    public void showAnnouncement(Announcement announcement) {
        Bypass bypass = new Bypass(getActivity());
        CharSequence spannable = bypass.markdownToSpannable(announcement.text);
        text.setText(spannable);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @NonNull
    @Override
    protected PresenterFactory<AnnouncementPresenter> getPresenterFactory() {
        return new AnnouncementPresenterFactory(announcementId);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_refresh:
                onRefresh();
                return true;
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
