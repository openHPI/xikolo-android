package de.xikolo.controllers.second_screen;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.models.Subtitle;
import de.xikolo.utils.TimeUtil;

public class TranscriptViewerAdapter extends RecyclerView.Adapter<TranscriptViewerAdapter.SubtitleViewHolder> {

    public static final String TAG = TranscriptViewerAdapter.class.getSimpleName();

    private Subtitle subtitle;

    private long currentTime;

    public TranscriptViewerAdapter() {
        this.subtitle = null;
        this.currentTime = 0;
    }

    public void updateSubtitles(Subtitle subtitle) {
        this.subtitle = subtitle;
        this.notifyDataSetChanged();
    }

    public void updateTime(long time) {
        this.currentTime = time;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return subtitle.textList().size();
    }

    @Override
    public SubtitleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtitle, parent, false);
        return new SubtitleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SubtitleViewHolder holder, int position) {
        final Subtitle.Text subtitleText = subtitle.textList().get(position);

        holder.textSubtitle.setText(subtitleText.text());

        long timeStart = subtitleText.startAsMillis();
        long timeEnd = subtitleText.endAsMillis();

        holder.textTime.setText(TimeUtil.getTimeString(timeStart) + "-" + TimeUtil.getTimeString(timeEnd));

        if (timeStart <= currentTime && currentTime < timeEnd) {
            holder.layout.setBackgroundColor(ContextCompat.getColor(GlobalApplication.getInstance(), R.color.background_light_gray));
        } else {
            holder.layout.setBackgroundColor(ContextCompat.getColor(GlobalApplication.getInstance(), R.color.background_main));
        }
    }

    static class SubtitleViewHolder extends RecyclerView.ViewHolder {

        View layout;
        TextView textTime;
        TextView textSubtitle;

        public SubtitleViewHolder(View view) {
            super(view);

            layout = view;
            textTime = (TextView) view.findViewById(R.id.text_time);
            textSubtitle = (TextView) view.findViewById(R.id.text_subtitle);
        }
    }

}
