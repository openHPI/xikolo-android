package de.xikolo.controller.module.helper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.Set;

import de.greenrobot.event.EventBus;
import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Download;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.entities.VideoItemDetail;
import de.xikolo.data.net.DownloadHelper;
import de.xikolo.model.DownloadModel;
import de.xikolo.model.events.DownloadCompletedEvent;
import de.xikolo.view.IconButton;

public class DownloadViewController {

    public static final String TAG = DownloadViewController.class.getSimpleName();
    
    private DownloadModel.DownloadFileType type;
    
    private Course course;
    
    private Module module;
    
    private Item<VideoItemDetail> item;

    private DownloadModel downloadModel;
    
    private View view;
    
    private TextView text;
    
    private IconButton button;
    
    private String uri;
    
    public DownloadViewController(DownloadModel.DownloadFileType type, Course course, Module module, Item<VideoItemDetail> item) {
        this.type = type;
        this.course = course;
        this.module = module;
        this.item = item;
        
        this.downloadModel = new DownloadModel();

        LayoutInflater inflater = LayoutInflater.from(GlobalApplication.getInstance());
        view = inflater.inflate(R.layout.container_download, null);

        text = (TextView) view.findViewById(R.id.textDownload);
        
        button = (IconButton) view.findViewById(R.id.buttonDownload);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadModel.startDownload(uri, 
                        DownloadViewController.this.type, 
                        DownloadViewController.this.course, 
                        DownloadViewController.this.module, 
                        DownloadViewController.this.item);
            }
        });
        
        if (button == null) {
            Log.w(TAG, "button null");
        } else {
            Log.i(TAG, "button not null");
        }

        if (text == null) {
            Log.w(TAG, "text null");
        } else {
            Log.i(TAG, "text not null");
        }
        
        switch (type) {
            case SLIDES:
                uri = item.detail.slides_url;
                text.setText(GlobalApplication.getInstance().getText(R.string.slides_as_pdf));
                button.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_pdf));
                break;
            case TRANSCRIPT:
                uri = item.detail.transcript_url;
                text.setText(GlobalApplication.getInstance().getText(R.string.transcript_as_pdf));
                button.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_pdf));
                break;
            case VIDEO_HD:
                uri = item.detail.stream.hd_url;
                text.setText(GlobalApplication.getInstance().getText(R.string.video_hd_as_mp4));
                button.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_video));
                break;
            case VIDEO_SD:
                uri = item.detail.stream.sd_url;
                text.setText(GlobalApplication.getInstance().getText(R.string.video_sd_as_mp4));
                button.setIconText(GlobalApplication.getInstance().getText(R.string.icon_download_video));
                break;
        }
        
        if (uri == null) {
            view.setVisibility(View.GONE);
        }

        EventBus.getDefault().register(this);
    }
    
    public View getView() {
        return view;
    }

    public void onEventMainThread(DownloadCompletedEvent event) {
        Log.i(TAG, event.getMessage());
        if (event.getDownload().localUri.contains(item.id)) {
            Log.i(TAG, "this is me " + item.id);
            String suffix = DownloadModel.DownloadFileType.getDownloadFileTypeFromUri(event.getDownload().localUri).getFileSuffix();
            Log.i(TAG, suffix);
        }

        Set<Download> downloadSet = DownloadHelper.getAllDownloads();
        for (Download dl : downloadSet) {
            Log.d(TAG, dl.status + ": " + dl.localFilename);
        }

    }
    
}
