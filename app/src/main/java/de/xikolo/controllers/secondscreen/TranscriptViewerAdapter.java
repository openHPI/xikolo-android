package de.xikolo.controllers.secondscreen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.models.SubtitleCue;
import de.xikolo.utils.TimeUtil;

public class TranscriptViewerAdapter extends RecyclerView.Adapter<TranscriptViewerAdapter.SubtitleViewHolder> {

    public static final String TAG = TranscriptViewerAdapter.class.getSimpleName();

    private List<SubtitleCue> cues;

    private long currentTime;

    public TranscriptViewerAdapter() {
        this.cues = new ArrayList<>();
        this.currentTime = 0;
    }

    public void updateSubtitles(List<SubtitleCue> cues) {
        this.cues = cues;
        this.notifyDataSetChanged();
    }

    public void updateTime(long time) {
        this.currentTime = time;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return cues.size();
    }

    @Override
    public SubtitleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtitle, parent, false);
        return new SubtitleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SubtitleViewHolder holder, int position) {
        final SubtitleCue cue = cues.get(position);

        holder.textSubtitle.setText(cue.text);

        long timeStart = cue.startAsMillis();
        long timeEnd = cue.endAsMillis();

        holder.textTime.setText(TimeUtil.getTimeString(timeStart) + "-" + TimeUtil.getTimeString(timeEnd));

        if (timeStart <= currentTime && currentTime < timeEnd) {
            holder.layout.setBackgroundColor(ContextCompat.getColor(App.getInstance(), R.color.background_light_gray));
        } else {
            holder.layout.setBackgroundColor(ContextCompat.getColor(App.getInstance(), R.color.background_main));
        }
    }

    static class SubtitleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layout_subtitle) View layout;
        @BindView(R.id.text_time) TextView textTime;
        @BindView(R.id.text_subtitle) TextView textSubtitle;

        public SubtitleViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
