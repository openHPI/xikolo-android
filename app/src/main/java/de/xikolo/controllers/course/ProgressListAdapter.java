package de.xikolo.controllers.course;

import android.content.Context;
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
import de.xikolo.models.CourseProgress;
import de.xikolo.models.ExerciseStatistic;
import de.xikolo.models.SectionProgress;
import de.xikolo.models.VisitStatistic;

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
        if (cp == null) {
            throw new NullPointerException("Course progress can't be null");
        }
        if (spList == null) {
            throw new NullPointerException("Section progresses can't be null");
        }
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
        return spList.size() + 1;
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
            mainExercises = cp.mainExercises;
            selftestExercises = cp.selftestExercises;
            bonusExercises = cp.bonusExercises;
            visits = cp.visits;

            holder.viewSeparator.setVisibility(View.GONE);

        }

        holder.textTitle.setText(title);

        int percentage;

        holder.textCount1.setText(String.format(context.getString(R.string.self_test_points),
                selftestExercises.pointsScored,
                selftestExercises.pointsPossible));

        percentage = getPercentage(selftestExercises.pointsScored, selftestExercises.pointsPossible);
        holder.textPercentage1.setText(String.format(context.getString(R.string.percentage), percentage));
        holder.progressBar1.setProgress(percentage);

        holder.textCount2.setText(String.format(context.getString(R.string.assignments_points),
                mainExercises.pointsScored,
                mainExercises.pointsPossible));

        percentage = getPercentage(mainExercises.pointsScored, mainExercises.pointsPossible);
        holder.textPercentage2.setText(String.format(context.getString(R.string.percentage), percentage));
        holder.progressBar2.setProgress(percentage);

        holder.textCount3.setText(String.format(context.getString(R.string.items_visited),
                visits.itemsVisited,
                visits.itemsAvailable));

        percentage = getPercentage(visits.itemsVisited, visits.itemsAvailable);
        holder.textPercentage3.setText(String.format(context.getString(R.string.percentage), percentage));
        holder.progressBar3.setProgress(percentage);
    }

    private int getPercentage(int state, int max) {
        return getPercentage((float) state, (float) max);
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

        @BindView(R.id.textTitle) TextView textTitle;
        @BindView(R.id.textCount1) TextView textCount1;
        @BindView(R.id.textCount2) TextView textCount2;
        @BindView(R.id.textCount3) TextView textCount3;
        @BindView(R.id.progress1) ProgressBar progressBar1;
        @BindView(R.id.progress2) ProgressBar progressBar2;
        @BindView(R.id.progress3) ProgressBar progressBar3;
        @BindView(R.id.textPercentage1) TextView textPercentage1;
        @BindView(R.id.textPercentage2) TextView textPercentage2;
        @BindView(R.id.textPercentage3) TextView textPercentage3;
        @BindView(R.id.viewSeparator) View viewSeparator;

        public ProgressViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
