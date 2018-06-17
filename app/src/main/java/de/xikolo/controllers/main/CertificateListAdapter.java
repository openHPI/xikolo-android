package de.xikolo.controllers.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.config.GlideApp;
import de.xikolo.models.Course;

public class CertificateListAdapter extends RecyclerView.Adapter<CertificateListAdapter.CertificateViewHolder> {

    public static final String TAG = CertificateListAdapter.class.getSimpleName();

    private List<Course> courseList = new ArrayList<>();
    private OnCertificateCardClickListener callback;

    public CertificateListAdapter(OnCertificateCardClickListener callback) {
        this.callback = callback;
    }

    public void update(List<Course> courseList) {
        this.courseList = courseList;
        notifyDataSetChanged();
    }

    public void clear() {
        courseList.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    @Override
    public CertificateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certificate_list, parent, false);
        return new CertificateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CertificateViewHolder holder, int position) {
        final Course course = courseList.get(position);

        holder.textTitle.setText(course.title);

        holder.header.setOnClickListener(v -> callback.onCourseClicked(course.id));

        if (course.imageUrl != null)
            GlideApp.with(App.getInstance()).load(course.imageUrl).into(holder.courseImage);
        else
            holder.courseImage.setVisibility(View.GONE);

        if (!course.certificates.qualifiedCertificateAvailable)
            holder.qualifiedCertificate.setVisibility(View.GONE);
        else
            holder.qualifiedCertificate.setVisibility(View.VISIBLE);

        if (course.certificates.qualifiedCertificateUrl != null) {
            holder.qualifiedCertificateButton.setEnabled(true);
            holder.qualifiedCertificateButton.setOnClickListener(v -> callback.onViewCertificateClicked(course.certificates.qualifiedCertificateUrl));
        } else
            holder.qualifiedCertificateButton.setEnabled(false);


        if (!course.certificates.recordOfAchievementAvailable)
            holder.recordOfAchievement.setVisibility(View.GONE);
        else
            holder.recordOfAchievement.setVisibility(View.VISIBLE);

        if (course.certificates.recordOfAchievementUrl != null) {
            holder.recordOfAchievementButton.setEnabled(true);
            holder.recordOfAchievementButton.setOnClickListener(v -> callback.onViewCertificateClicked(course.certificates.recordOfAchievementUrl));
        } else
            holder.recordOfAchievementButton.setEnabled(false);


        if (!course.certificates.confirmationOfParticipationAvailable)
            holder.confirmationOfParticipation.setVisibility(View.GONE);
        else
            holder.confirmationOfParticipation.setVisibility(View.VISIBLE);

        if (course.certificates.confirmationOfParticipationUrl != null) {
            holder.confirmationOfParticipationButton.setEnabled(true);
            holder.confirmationOfParticipationButton.setOnClickListener(v -> callback.onViewCertificateClicked(course.certificates.confirmationOfParticipationUrl));
        } else
            holder.confirmationOfParticipationButton.setEnabled(false);
    }

    public interface OnCertificateCardClickListener {

        void onViewCertificateClicked(String url);

        void onCourseClicked(String courseId);
    }

    static class CertificateViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.certificateHeader) ViewGroup header;
        @BindView(R.id.courseImage) ImageView courseImage;
        @BindView(R.id.textTitle) TextView textTitle;
        @BindView(R.id.qualifiedCertificateContainer) RelativeLayout qualifiedCertificate;
        @BindView(R.id.qualifiedCertificateButton) Button qualifiedCertificateButton;
        @BindView(R.id.recordOfAchievementContainer) RelativeLayout recordOfAchievement;
        @BindView(R.id.recordOfAchievementButton) Button recordOfAchievementButton;
        @BindView(R.id.confirmationOfParticipationContainer) RelativeLayout confirmationOfParticipation;
        @BindView(R.id.confirmationOfParticipationButton) Button confirmationOfParticipationButton;

        public CertificateViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
