package de.xikolo.models;

import android.content.Context;

import com.squareup.moshi.Json;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import de.xikolo.App;
import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.config.BuildFlavor;
import de.xikolo.models.base.JsonAdapter;
import de.xikolo.models.base.RealmAdapter;
import de.xikolo.utils.DateUtil;
import de.xikolo.utils.DisplayUtil;
import de.xikolo.utils.LanguageUtil;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.HasMany;
import moe.banana.jsonapi2.HasOne;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class Course extends RealmObject implements JsonAdapter<Course.JsonModel> {

    public enum Filter {
        ALL, MY
    }

    public static final int TAB_LEARNINGS = 0;
    public static final int TAB_DISCUSSIONS = 1;
    public static final int TAB_PROGRESS = 2;
    public static final int TAB_COURSE_DETAILS = 3;
    public static final int TAB_DOCUMENTS = 4;
    public static final int TAB_ANNOUNCEMENTS = 5;
    public static final int TAB_RECAP = 6;

    @PrimaryKey
    public String id;

    public String title;

    public String slug;

    public Date startDate;

    public Date endDate;

    public String shortAbstract;

    public String description;

    public String imageUrl;

    public String language;

    public String status;

    public String classifiers;

    public String teachers;

    public boolean accessible;

    public boolean enrollable;

    public boolean hidden;

    public boolean external;

    public String externalUrl;

    public String policyUrl;

    public boolean onDemand;

    public String enrollmentId;

    public String channelId;

    public boolean isEnrolled() {
        return enrollmentId != null;
    }

    public static Course get(String id) {
        Realm realm = Realm.getDefaultInstance();
        Course model = realm.where(Course.class).equalTo("id", id).findFirst();
        if (model != null) model = realm.copyFromRealm(model);
        realm.close();
        return model;
    }

    public static Course find(String identifier) {
        Realm realm = Realm.getDefaultInstance();
        Course model = realm.where(Course.class)
                .beginGroup()
                    .equalTo("id", identifier)
                    .or()
                    .equalTo("slug", identifier)
                .endGroup()
                .findFirst();
        if (model != null) model = realm.copyFromRealm(model);
        realm.close();
        return model;
    }

    @Override
    public JsonModel convertToJsonResource() {
        Course.JsonModel model = new Course.JsonModel();
        model.setId(id);
        model.title = title;
        model.slug = slug;
        model.startDate = DateUtil.format(startDate);
        model.endDate = DateUtil.format(endDate);
        model.shortAbstract = shortAbstract;
        model.description = description;
        model.imageUrl = imageUrl;
        model.language = language;
        model.status = status;
        model.classifiers = classifiers;
        model.teachers = teachers;
        model.accessible = accessible;
        model.enrollable = enrollable;
        model.hidden = hidden;
        model.external = external;
        model.externalUrl = externalUrl;
        model.policyUrl = policyUrl;
        model.onDemand = onDemand;

        if (enrollmentId != null) {
            model.enrollment = new HasOne<>(new Enrollment.JsonModel().getType(), enrollmentId);
        }

        if (channelId != null) {
            model.channel = new HasOne<>(new Channel.JsonModel().getType(), channelId);
        }

        return model;
    }

    public String getFormattedDate() {
        Context context = App.getInstance();

        DateFormat dateOut;
        if (DisplayUtil.is7inchTablet(context)) {
            dateOut = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        } else {
            dateOut = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        }

        if (DateUtil.isPast(endDate)) {
            return context.getString(R.string.course_date_self_paced);
        }

        if (DateUtil.isPast(startDate) && endDate == null) {
            if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
                return context.getString(R.string.course_date_self_paced);
            } else {
                return String.format(context.getString(R.string.course_date_since), dateOut.format(startDate));
            }
        }

        if (DateUtil.isFuture(startDate) && endDate == null) {
            if (BuildConfig.X_FLAVOR == BuildFlavor.OPEN_WHO) {
                return context.getString(R.string.course_date_coming_soon);
            } else {
                return String.format(context.getString(R.string.course_date_beginning), dateOut.format(startDate));
            }
        }

        if (startDate != null && endDate != null) {
            return dateOut.format(startDate) + " - " + dateOut.format(endDate);
        }

        return context.getString(R.string.course_date_coming_soon);
    }

    public String getFormattedLanguage() {
        return LanguageUtil.languageForCode(App.getInstance(), language);
    }

    @JsonApi(type = "courses")
    public static class JsonModel extends Resource implements RealmAdapter<Course> {

        public String title;

        public String slug;

        @Json(name = "start_at")
        public String startDate;

        @Json(name = "end_at")
        public String endDate;

        @Json(name = "abstract")
        public String shortAbstract;

        public String description;

        @Json(name = "image_url")
        public String imageUrl;

        public String language;

        public String status;

        public transient String classifiers;

        public String teachers;

        public boolean accessible;

        public boolean enrollable;

        public boolean hidden;

        public boolean external;

        @Json(name = "external_url")
        public String externalUrl;

        @Json(name = "policy_url")
        public String policyUrl;

        @Json(name = "qualified_certificate_available")
        public boolean qualifiedCertificateAvailable;

        @Json(name = "on_demand")
        public boolean onDemand;

        @Json(name = "user_enrollment")
        public HasOne<Enrollment.JsonModel> enrollment;

        @Json(name = "sections")
        public HasMany<Section.JsonModel> sections;

        @Json(name = "channel")
        public HasOne<Section.JsonModel> channel;

        @Override
        public Course convertToRealmObject() {
            Course course = new Course();
            course.id = getId();
            course.title = title;
            course.slug = slug;
            course.startDate = DateUtil.parse(startDate);
            course.endDate = DateUtil.parse(endDate);
            course.shortAbstract = shortAbstract;
            course.description = description;
            course.imageUrl = imageUrl;
            course.language = language;
            course.status = status;
            course.classifiers = classifiers;
            course.teachers = teachers;
            course.accessible = accessible;
            course.enrollable = enrollable;
            course.hidden = hidden;
            course.external = external;
            course.externalUrl = externalUrl;
            course.policyUrl = policyUrl;
            course.onDemand = onDemand;

            if (enrollment != null) {
                course.enrollmentId = enrollment.get().getId();
            }

            if (channel != null) {
                course.channelId = channel.get().getId();
            }

            return course;
        }

    }

}
