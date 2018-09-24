package de.xikolo.controllers.secondscreen;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yatatsu.autobundle.AutoBundleField;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.R;
import de.xikolo.controllers.base.BaseFragment;
import de.xikolo.controllers.dialogs.ChooseLanguageDialog;
import de.xikolo.controllers.dialogs.ChooseLanguageDialogAutoBundle;
import de.xikolo.managers.SecondScreenManager;
import de.xikolo.models.Item;
import de.xikolo.models.SubtitleTrack;
import de.xikolo.models.Video;
import de.xikolo.utils.AndroidDimenUtil;
import de.xikolo.utils.TimeUtil;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class TranscriptViewerFragment extends BaseFragment implements ChooseLanguageDialog.Listener {

    public static final String TAG = TranscriptViewerFragment.class.getSimpleName();

    @AutoBundleField String courseId;
    @AutoBundleField String sectionId;
    @AutoBundleField String itemId;

    @AutoBundleField(required = false) long currentTime;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fab) FloatingActionButton actionButton;

    private LinearLayoutManager layoutManager;
    private TranscriptViewerAdapter adapter;

    private boolean syncScroll;

    private Video video;
    private List<SubtitleTrack> subtitles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Item item = Item.get(itemId);
        video = Video.getForContentId(item.contentId);
        subtitles = SubtitleTrack.listForVideoId(video.id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transcript_viewer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(getActivity(), view);

        adapter = new TranscriptViewerAdapter();
        adapter.updateSubtitles(subtitles.get(0).listCues());

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        syncScroll = true;

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    syncScroll = false;
                }
            }
        });

        actionButton.setVisibility(View.GONE);
        actionButton.setOnClickListener((v) -> {
            actionButton.hide();
            syncScroll = true;
            performScroll();
        });

        if (currentTime > 0) {
            updateCurrentTime(currentTime);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    private void updateCurrentTime(long millis) {
        this.currentTime = millis;

        if (adapter != null) {
            adapter.updateTime(currentTime);
        }

        performScroll();
    }

    private void performScroll() {
        if (syncScroll && layoutManager != null) {
            int position = subtitles.get(0).getTextPosition(currentTime);
            if (position >= 0) {
                AppBarLayout appBarLayout = getActivity().findViewById(R.id.appbar);
                appBarLayout.setExpanded(true, false);
                layoutManager.scrollToPositionWithOffset(position, AndroidDimenUtil.getActionBarHeight() * 2);
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSecondScreenUpdateVideoEvent(SecondScreenManager.SecondScreenUpdateVideoEvent event) {
        if (event.itemId.equals(itemId)) {
            if (event.webSocketMessage.payload.containsKey("current_time")) {
                String time = event.webSocketMessage.payload.get("current_time");
                updateCurrentTime(TimeUtil.secondsToMillis(time));
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (subtitles.size() > 1) {
            inflater.inflate(R.menu.language, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int videoId = item.getItemId();
        switch (videoId) {
            case R.id.action_language:
                showLanguageDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLanguageDialog() {
        ChooseLanguageDialog dialog = ChooseLanguageDialogAutoBundle.builder(video.id).build();
        dialog.setListener(this);
        dialog.show(getActivity().getSupportFragmentManager(), ChooseLanguageDialog.TAG);
    }

    @Override
    public void onItemClick(int position) {
        if (adapter != null) {
            adapter.updateSubtitles(subtitles.get(position).listCues());
            performScroll();
        }
    }

}
