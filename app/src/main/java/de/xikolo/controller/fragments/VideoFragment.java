package de.xikolo.controller.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import de.xikolo.R;
import de.xikolo.manager.ItemObjectManager;
import de.xikolo.model.Course;
import de.xikolo.model.Item;
import de.xikolo.model.Module;
import de.xikolo.model.Video;
import de.xikolo.util.Network;

public class VideoFragment extends Fragment {

    public static final String TAG = VideoFragment.class.getSimpleName();

    public static final String ARG_COURSE = "arg_course";
    public static final String ARG_MODULE = "arg_module";
    public static final String ARG_ITEM = "arg_item";

    private Course mCourse;
    private Module mModule;
    private Item<Video> mItem;

    private VideoView mVideo;
    private MediaController mVideoController;

    private ItemObjectManager mItemManager;

    public VideoFragment() {
        // Required empty public constructor
    }

    public static VideoFragment newInstance(Course course, Module module, Item item) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_COURSE, course);
        args.putParcelable(ARG_MODULE, module);
        args.putParcelable(ARG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCourse = getArguments().getParcelable(ARG_COURSE);
            mModule = getArguments().getParcelable(ARG_MODULE);
            mItem = getArguments().getParcelable(ARG_ITEM);
        }
//        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_video, container, false);

        mVideo = (VideoView) layout.findViewById(R.id.video);
        mVideoController = new MediaController(getActivity());
        mVideoController.setAnchorView(mVideo);
        mVideoController.setKeepScreenOn(true);

        int topContainerId1 = getResources().getIdentifier("mediacontroller_progress", "id", "android");
        SeekBar seekbar = (SeekBar) mVideoController.findViewById(topContainerId1);

        mVideo.setMediaController(mVideoController);

        mItemManager = new ItemObjectManager(getActivity()) {
            @Override
            public void onItemRequestReceived(Item item) {
                mItem = item;
                Uri uri = Uri.parse(mItem.object.url);
                mVideo.setVideoURI(uri);
                mVideo.start();
            }

            @Override
            public void onItemRequestCancelled() {
            }
        };

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Network.isOnline(getActivity())) {
            Type type = new TypeToken<Item<Video>>() {
            }.getType();
            mItemManager.requestItemObject(mCourse, mModule, mItem, type, true);
        } else {
            Network.showNoConnectionToast(getActivity());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
//            case R.id.action_refresh:
//                onRefresh();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
