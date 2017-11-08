package de.xikolo.controllers.second_screen;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.yatatsu.autobundle.AutoBundleField;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.controllers.base.BaseFragment;
import de.xikolo.controllers.dialogs.DownloadSlidesDialog;
import de.xikolo.controllers.dialogs.ProgressDialog;
import de.xikolo.events.DownloadCompletedEvent;
import de.xikolo.managers.DownloadManager;
import de.xikolo.managers.SecondScreenManager;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
import de.xikolo.utils.DownloadUtil;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SlideViewerFragment extends BaseFragment implements OnLoadCompleteListener, OnPageChangeListener {

    public static final String TAG = SlideViewerFragment.class.getSimpleName();

    private DownloadManager downloadManager;

    private ProgressDialog progressDialog;

    @BindView(R.id.pdf_view) PDFView pdfView;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.text_current_page) TextView textCurrentPage;

    @AutoBundleField String courseId;
    @AutoBundleField String sectionId;
    @AutoBundleField String itemId;

    @AutoBundleField(required = false) int currentPage = -1;

    private Course course;
    private Section section;
    private Item item;
    private Video video;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        downloadManager = new DownloadManager(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_slide_viewer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        course = Course.get(courseId);
        section = Section.get(sectionId);
        item = Item.get(itemId);
        video = Video.getForItemId(itemId);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.hide();
                textCurrentPage.setVisibility(View.GONE);
                if (currentPage >= 0) {
                    pdfView.jumpTo(currentPage);
                }
            }
        });

        if (downloadManager.downloadExists(item.id, DownloadUtil.VideoAssetType.SLIDES)) {
            initSlidesViewer();
        } else {
            DownloadSlidesDialog dialog = DownloadSlidesDialog.getInstance();
            dialog.setListener(new DownloadSlidesDialog.DownloadSlidesDialogListener() {
                @Override
                public void onDialogPositiveClick() {
                    downloadManager.startItemAssetDownload(item.id, DownloadUtil.VideoAssetType.SLIDES);
                    progressDialog = ProgressDialog.getInstance();
                    progressDialog.show(getFragmentManager(), ProgressDialog.TAG);
                }

                @Override
                public void onDialogNegativeClick() {
                    SlideViewerFragment.this.getActivity().finish();
                }
            });
            dialog.show(getFragmentManager(), DownloadSlidesDialog.TAG);
        }
    }

    private void initSlidesViewer() {
        if (downloadManager != null && downloadManager.downloadExists(item.id, DownloadUtil.VideoAssetType.SLIDES)) {
            File file = downloadManager.getDownloadFile(item.id, DownloadUtil.VideoAssetType.SLIDES);
            pdfView.fromFile(file)
                    .enableAnnotationRendering(true)
                    .onLoad(this)
                    .onPageChange(this)
                    .scrollHandle(new DefaultScrollHandle(getActivity()))
                    .load();
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        if (currentPage >= 0 && page != currentPage) {
            fab.show();
            textCurrentPage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void loadComplete(int nbPages) {
        if (currentPage >= 0) {
            pdfView.jumpTo(currentPage);
            if (fab != null) {
                fab.hide();
                textCurrentPage.setVisibility(View.GONE);
            }
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

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSecondScreenUpdateVideoEvent(SecondScreenManager.SecondScreenUpdateVideoEvent event) {
        if (event.itemId.equals(itemId)) {
            if (event.webSocketMessage.payload.containsKey("slide_number")) {
                try {
                    int page = Integer.parseInt(event.webSocketMessage.payload.get("slide_number"));
                    if (pdfView != null) {
                        currentPage = page;
                        if (currentPage != pdfView.getCurrentPage() && fab != null && !fab.isShown()) {
                            textCurrentPage.setText(String.format(getString(R.string.second_screen_pdf_pager), currentPage + 1));
                            pdfView.jumpTo(currentPage);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Couldn't parse Integer from " + event.webSocketMessage.payload.get("slide_number") + ": " + e.getMessage());
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadCompletedEvent(DownloadCompletedEvent event) {
        if (event.url.equals(video.slidesUrl)) {
            if (progressDialog != null && progressDialog.getDialog().isShowing()) {
                progressDialog.getDialog().cancel();
            }
            initSlidesViewer();
        }
    }

}
