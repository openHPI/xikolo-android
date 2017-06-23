//package de.xikolo.controllers.secondscreen;
//
//import android.annotation.TargetApi;
//import android.os.Build;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.design.widget.AppBarLayout;
//import android.support.design.widget.FloatingActionButton;
//import android.support.v4.app.Fragment;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//
//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.ThreadMode;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import de.xikolo.R;
//import de.xikolo.controllers.dialogs.ChooseLanguageDialog;
//import de.xikolo.controllers.secondscreen.TranscriptViewerAdapter;
//import de.xikolo.models.Item;
//import de.xikolo.models.Subtitle;
//import de.xikolo.managers.SecondScreenManager;
//import de.xikolo.utils.AndroidDimenUtil;
//import de.xikolo.utils.TimeUtil;
//
//@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//public class TranscriptViewerFragment extends Fragment implements ChooseLanguageDialog.ChooseLanguageDialogListener {
//
//    public static final String TAG = TranscriptViewerFragment.class.getSimpleName();
//
//    private Item item;
//    private List<Subtitle> subtitleList;
//
//    public static final String ARG_ITEM = "arg_item";
//    public static final String ARG_SUBTITLES = "arg_subtitles";
//
//    public static final String KEY_CURRENT_TIME = "arg_current_time";
//
//    private RecyclerView recyclerView;
//    private LinearLayoutManager layoutManager;
//    private TranscriptViewerAdapter adapter;
//
//    private FloatingActionButton actionButton;
//
//    private long currentTime;
//
//    private boolean syncScroll;
//
//    public TranscriptViewerFragment() {
//
//        // Required empty public constructor
//    }
//
//    public static TranscriptViewerFragment newInstance(Item item, List<Subtitle> subtitleList) {
//        TranscriptViewerFragment fragment = new TranscriptViewerFragment();
//        Bundle args = new Bundle();
//        args.putParcelable(ARG_ITEM, item);
//        args.putParcelableArrayList(ARG_SUBTITLES, (ArrayList<Subtitle>) subtitleList);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setHasOptionsMenu(true);
//
//        if (getArguments() != null) {
//            item = getArguments().getParcelable(ARG_ITEM);
//            subtitleList = getArguments().getParcelableArrayList(ARG_SUBTITLES);
//        }
//        if (savedInstanceState != null) {
//            currentTime = savedInstanceState.getLong(KEY_CURRENT_TIME);
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_transcript_viewer, container, false);
//    }
//
//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
//
//        adapter = new TranscriptViewerAdapter();
//        adapter.updateSubtitles(subtitleList.get(0));
//
//        layoutManager = new LinearLayoutManager(getContext());
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setAdapter(adapter);
//
//        syncScroll = true;
//
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                    syncScroll = false;
//                }
//            }
//        });
//
//        actionButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
//        actionButton.setVisibility(View.GONE);
//        actionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                actionButton.hide();
//                syncScroll = true;
//                performScroll();
//            }
//        });
//
//        if (currentTime > 0) {
//            updateCurrentTime(currentTime);
//        }
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        EventBus.getDefault().register(this);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//
//        EventBus.getDefault().unregister(this);
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        outState.putLong(KEY_CURRENT_TIME, currentTime);
//        super.onSaveInstanceState(outState);
//    }
//
//    private void updateCurrentTime(long millis) {
//        this.currentTime = millis;
//
//        if (adapter != null) {
//            adapter.updateTime(currentTime);
//        }
//
//        performScroll();
//    }
//
//    private void performScroll() {
//        if (syncScroll && layoutManager != null) {
//            int position = subtitleList.get(0).getTextPosition(currentTime);
//            if (position >= 0) {
//                AppBarLayout appBarLayout = (AppBarLayout) getActivity().findViewById(R.id.appbar);
//                appBarLayout.setExpanded(true, false);
//                layoutManager.scrollToPositionWithOffset(position, AndroidDimenUtil.getActionBarHeight() * 2);
//            }
//        }
//    }
//
//    @SuppressWarnings("unused")
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onSecondScreenUpdateVideoEvent(SecondScreenManager.SecondScreenUpdateVideoEvent event) {
//        if (event.getItem().equals(item)) {
//            if (event.getWebSocketMessage().payload().containsKey("current_time")) {
//                String time = event.getWebSocketMessage().payload().get("current_time");
//                updateCurrentTime(TimeUtil.secondsToMillis(time));
//            }
//        }
//    }
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        Log.d(TAG, "onCreateOptionsMenu");
//        if (subtitleList != null && subtitleList.size() > 1) {
//            inflater.inflate(R.menu.language, menu);
//        }
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int videoId = item.getItemId();
//        switch (videoId) {
//            case R.id.action_language:
//                showLanguageDialog();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void showLanguageDialog() {
//        ChooseLanguageDialog dialog = ChooseLanguageDialog.getInstance(subtitleList);
//        dialog.setMobileDownloadDialogListener(this);
//        dialog.show(getActivity().getSupportFragmentManager(), ChooseLanguageDialog.TAG);
//    }
//
//    @Override
//    public void onDialogItemClick(int position) {
//        if (adapter != null && subtitleList != null) {
//            adapter.updateSubtitles(subtitleList.get(position));
//            performScroll();
//        }
//    }
//
//}
