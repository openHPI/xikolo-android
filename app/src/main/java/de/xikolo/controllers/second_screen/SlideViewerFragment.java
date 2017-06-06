//package de.xikolo.controllers.secondscreen;
//
//import android.annotation.TargetApi;
//import android.os.Build;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.design.widget.FloatingActionButton;
//import android.support.v4.app.Fragment;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import com.github.barteksc.pdfviewer.PDFView;
//import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
//import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
//import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
//
//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.ThreadMode;
//
//import java.io.File;
//
//import de.xikolo.GlobalApplication;
//import de.xikolo.R;
//import de.xikolo.controllers.dialogs.DownloadSlidesDialog;
//import de.xikolo.controllers.dialogs.ProgressDialog;
//import de.xikolo.events.DownloadCompletedEvent;
//import de.xikolo.managers.DownloadManager;
//import de.xikolo.managers.SecondScreenManager;
//import de.xikolo.models.Course;
//import de.xikolo.models.Item;
//import de.xikolo.models.Section;
//import de.xikolo.models.Video;
//
//@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//public class SlideViewerFragment extends Fragment implements OnLoadCompleteListener, OnPageChangeListener {
//
//    public static final String TAG = SlideViewerFragment.class.getSimpleName();
//
//    private PDFView pdfView;
//
//    private DownloadManager downloadManager;
//
//    private ProgressDialog progressDialog;
//
//    private FloatingActionButton fab;
//    private TextView textCurrentPage;
//
//    private Course course;
//    private Section module;
//    private Item<Video> item;
//
//    public static final String ARG_COURSE = "arg_course";
//    public static final String ARG_MODULE = "arg_module";
//    public static final String ARG_ITEM = "arg_item";
//
//    private final static String KEY_CURRENT_PAGE = "current_page";
//
//    private int currentPage;
//
//    public SlideViewerFragment() {
//        // Required empty public constructor
//    }
//
//    public static SlideViewerFragment newInstance(Course course, Section module, Item item) {
//        SlideViewerFragment fragment = new SlideViewerFragment();
//        Bundle args = new Bundle();
////        args.putParcelable(ARG_COURSE, course);
//        args.putParcelable(ARG_MODULE, module);
//        args.putParcelable(ARG_ITEM, item);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        if (getArguments() != null) {
//            course = getArguments().getParcelable(ARG_COURSE);
//            module = getArguments().getParcelable(ARG_MODULE);
//            item = getArguments().getParcelable(ARG_ITEM);
//        }
//
//        if (savedInstanceState != null) {
//            currentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE);
//        } else {
//            currentPage = -1;
//        }
//
//        downloadManager = new DownloadManager(GlobalApplication.getInstance().getJobManager(), getActivity());
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_slide_viewer, container, false);
//    }
//
//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        fab = (FloatingActionButton) view.findViewById(R.id.fab);
//        textCurrentPage = (TextView) view.findViewById(R.id.text_current_page);
//
//        pdfView = (PDFView) view.findViewById(R.id.pdf_view);
//
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fab.hide();
//                textCurrentPage.setVisibility(View.GONE);
//                if (currentPage >= 0) {
//                    pdfView.jumpTo(currentPage);
//                }
//            }
//        });
//
//        if (downloadManager.downloadExists(DownloadManager.DownloadFileType.SLIDES, course, module, item)) {
//            initSlidesViewer();
//        } else {
//            DownloadSlidesDialog dialog = DownloadSlidesDialog.getInstance();
//            dialog.setListener(new DownloadSlidesDialog.DownloadSlidesDialogListener() {
//                @Override
//                public void onDialogPositiveClick() {
//                    downloadManager.startDownload(item.detail.slides_url, DownloadManager.DownloadFileType.SLIDES, course, module, item);
//                    progressDialog = ProgressDialog.getInstance();
//                    progressDialog.show(getFragmentManager(), ProgressDialog.TAG);
//                }
//
//                @Override
//                public void onDialogNegativeClick() {
//                    SlideViewerFragment.this.getActivity().finish();
//                }
//            });
//            dialog.show(getFragmentManager(), DownloadSlidesDialog.TAG);
//        }
//    }
//
//    private void initSlidesViewer() {
//        if (downloadManager != null && downloadManager.downloadExists(DownloadManager.DownloadFileType.SLIDES, course, module, item)) {
//            File file = downloadManager.getDownloadFile(DownloadManager.DownloadFileType.SLIDES, course, module, item);
//            pdfView.fromFile(file)
//                    .enableAnnotationRendering(true)
//                    .onLoad(this)
//                    .onPageChange(this)
//                    .scrollHandle(new DefaultScrollHandle(getActivity()))
//                    .load();
//        }
//    }
//
//    @Override
//    public void onPageChanged(int page, int pageCount) {
//        if (currentPage >= 0 && page != currentPage) {
//            fab.show();
//            textCurrentPage.setVisibility(View.VISIBLE);
//        }
//    }
//
//    @Override
//    public void loadComplete(int nbPages) {
//        if (currentPage >= 0) {
//            pdfView.jumpTo(currentPage);
//            if (fab != null) {
//                fab.hide();
//                textCurrentPage.setVisibility(View.GONE);
//            }
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
//        outState.putInt(KEY_CURRENT_PAGE, currentPage);
//        super.onSaveInstanceState(outState);
//    }
//
//    @SuppressWarnings("unused")
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onSecondScreenUpdateVideoEvent(SecondScreenManager.SecondScreenUpdateVideoEvent event) {
//        if (event.getItem().equals(item)) {
//            if (event.getWebSocketMessage().payload().containsKey("slide_number")) {
//                try {
//                    int page = Integer.parseInt(event.getWebSocketMessage().payload().get("slide_number"));
//                    if (pdfView != null) {
//                        currentPage = page;
//                        if (currentPage != pdfView.getCurrentPage() && fab != null && !fab.isShown()) {
//                            textCurrentPage.setText(String.format(getString(R.string.second_screen_pdf_pager), currentPage + 1));
//                            pdfView.jumpTo(currentPage);
//                        }
//                    }
//                } catch (Exception e) {
//                    Log.e(TAG, "Couldn't parse Integer from " + event.getWebSocketMessage().payload().get("slide_number") + ": " + e.getMessage());
//                }
//            }
//        }
//    }
//
//    @SuppressWarnings("unused")
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onDownloadCompletedEvent(DownloadCompletedEvent event) {
//        if (event.getDownload().localUri.contains(item.id)
//                && DownloadManager.DownloadFileType.getDownloadFileTypeFromUri(event.getDownload().localUri) == DownloadManager.DownloadFileType.SLIDES) {
//            if (progressDialog != null && progressDialog.getDialog().isShowing()) {
//                progressDialog.getDialog().cancel();
//            }
//            initSlidesViewer();
//        }
//    }
//
//}
