package de.xikolo.controllers.course;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.R;
import de.xikolo.config.Config;
import de.xikolo.models.CourseProgress;
import de.xikolo.models.ExerciseStatistic;
import de.xikolo.models.SectionProgress;
import de.xikolo.models.VisitStatistic;
import de.xikolo.views.CustomFontTextView;

public class ProgressListAdapter extends RecyclerView.Adapter<ProgressListAdapter.ProgressViewHolder> {

    public static final String TAG = ProgressListAdapter.class.getSimpleName();

    private CourseProgress cp;
    private List<SectionProgress> spList;

    private Context context;

    public ProgressListAdapter(Context context) {
        this.context = context;
        this.spList = new ArrayList<>();
    }

    public void update(CourseProgress cp, List<SectionProgress> spList) {
        this.cp = cp;
        this.spList.clear();
        this.spList.addAll(spList);
        notifyDataSetChanged();
    }

    public void clear() {
        cp = null;
        spList.clear();
    }

    @Override
    public int getItemCount() {
        if (spList.size() == 0) {
            return 0;
        } else {
            return spList.size() + 1;
        }
    }

    @Override
    public ProgressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress, parent, false);
        return new ProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProgressViewHolder holder, int position) {
        String title;
        ExerciseStatistic mainExercises;
        ExerciseStatistic selftestExercises;
        ExerciseStatistic bonusExercises;
        VisitStatistic visits;
        if (position == spList.size()) {
            title = context.getString(R.string.total);
            mainExercises = cp.mainExercises;
            selftestExercises = cp.selftestExercises;
            bonusExercises = cp.bonusExercises;
            visits = cp.visits;

            holder.viewSeparator.setVisibility(View.VISIBLE);
        } else {
            SectionProgress sp = spList.get(position);
            title = sp.title;
            mainExercises = sp.mainExercises;
            selftestExercises = sp.selftestExercises;
            bonusExercises = sp.bonusExercises;
            visits = sp.visits;

            holder.viewSeparator.setVisibility(View.GONE);

        }

        holder.textTitle.setText(title);

        if (selftestExercises != null) {
            holder.progressSelftest.setVisibility(View.VISIBLE);
            setupExerciseProgressView(
                    holder.progressSelftest,
                    selftestExercises.pointsPossible,
                    selftestExercises.pointsScored,
                    R.string.self_test_points,
                    R.string.icon_selftest,
                    Config.FONT_XIKOLO
            );
        } else {
            holder.progressSelftest.setVisibility(View.GONE);
        }

        if (mainExercises != null) {
            holder.progressMain.setVisibility(View.VISIBLE);
            setupExerciseProgressView(
                    holder.progressMain,
                    mainExercises.pointsPossible,
                    mainExercises.pointsScored,
                    R.string.assignments_points,
                    R.string.icon_assignment,
                    Config.FONT_XIKOLO
            );
        } else {
            holder.progressMain.setVisibility(View.GONE);
        }

        if (bonusExercises != null) {
            holder.progressBonus.setVisibility(View.VISIBLE);
            setupExerciseProgressView(
                    holder.progressBonus,
                    bonusExercises.pointsPossible,
                    bonusExercises.pointsScored,
                    R.string.assignments_points,
                    R.string.icon_bonus,
                    Config.FONT_XIKOLO
            );
        } else {
            holder.progressBonus.setVisibility(View.GONE);
        }

        if (visits != null) {
            holder.progressVisits.setVisibility(View.VISIBLE);
            setupExerciseProgressView(
                    holder.progressVisits,
                    visits.itemsAvailable,
                    visits.itemsVisited,
                    R.string.items_visited,
                    R.string.icon_visited,
                    Config.FONT_MATERIAL
            );
        } else {
            holder.progressVisits.setVisibility(View.GONE);
        }
    }

    private void setupExerciseProgressView(View view, float base, float percent, @StringRes int label, @StringRes int icon, String iconFont) {
        TextView textCount = (TextView) view.findViewById(R.id.text_count);
        TextView textPercentage = (TextView) view.findViewById(R.id.text_percentage);
        CustomFontTextView iconView = (CustomFontTextView) view.findViewById(R.id.icon);
        ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress);

        iconView.setText(context.getString(icon));
        iconView.setCustomFont(context, iconFont);
        textCount.setText(String.format(context.getString(label), percent, base));

        int percentage = getPercentage(percent, base);
        textPercentage.setText(String.format(context.getString(R.string.percentage), percentage));
        progress.setProgress(percentage);
    }

    private int getPercentage(float state, float max) {
        int percentage;
        if (max > 0) {
            percentage = (int) (state / (max / 100.));
        } else {
            percentage = 100;
        }
        return percentage;
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_title) TextView textTitle;
        @BindView(R.id.progress_selftest) View progressSelftest;
        @BindView(R.id.progress_main) View progressMain;
        @BindView(R.id.progress_bonus) View progressBonus;
        @BindView(R.id.progress_visits) View progressVisits;
        @BindView(R.id.view_separator) View viewSeparator;

        public ProgressViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
