package de.xikolo.models.migrate;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class RealmSchemaMigration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        // DynamicRealm exposes an editable schema
        RealmSchema schema = realm.getSchema();

        // Data schema migrations
        // See https://realm.io/docs/java/latest/#migrations
        if (oldVersion == 1) {
            schema.create("Channel")
                .addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                .addField("title", String.class)
                .addField("slug", String.class)
                .addField("color", String.class)
                .addField("position", int.class)
                .addField("description", String.class)
                .addField("imageUrl", String.class);

            schema.get("Course")
                .addField("channelId", String.class);

            oldVersion++;
        }

        if (oldVersion == 2) {
            // Document and DocumentLocalization were not added for some user,
            // therefore a fix was provided with the next version.

            oldVersion++;
        }

        if (oldVersion == 3) {
            // This DB version only fixes the last one.
            if (!schema.contains("Document")) {
                schema.create("Document")
                    .addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("title", String.class)
                    .addField("description", String.class)
                    .addRealmListField("tags", String.class)
                    .addField("isPublic", boolean.class)
                    .addRealmListField("courseIds", String.class);
            }

            if (!schema.contains("DocumentLocalization")) {
                schema.create("DocumentLocalization")
                    .addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("title", String.class)
                    .addField("description", String.class)
                    .addField("language", String.class)
                    .addField("revision", int.class)
                    .addField("fileUrl", String.class)
                    .addField("documentId", String.class);
            }

            oldVersion++;
        }

        if (oldVersion == 4) {
            schema.create("CourseCertificateDetails")
                .addField("available", boolean.class)
                .addField("threshold", double.class);

            schema.create("CourseCertificates")
                .addRealmObjectField("confirmationOfParticipationUrl", schema.get("CourseCertificateDetails"))
                .addRealmObjectField("recordOfAchievementUrl", schema.get("CourseCertificateDetails"))
                .addRealmObjectField("qualifiedCertificateUrl", schema.get("CourseCertificateDetails"));

            schema.get("Course")
                .addRealmObjectField("certificates", schema.get("CourseCertificates"));

            schema.create("EnrollmentCertificates")
                .addField("confirmationOfParticipationUrl", String.class)
                .addField("recordOfAchievementUrl", String.class)
                .addField("qualifiedCertificateUrl", String.class);

            schema.get("Enrollment")
                .addRealmObjectField("certificates", schema.get("EnrollmentCertificates"));

            oldVersion++;
        }

        if (oldVersion == 5) {
            schema.create("VideoSubtitles")
                .addField("language", String.class)
                .addField("createdByMachine", boolean.class)
                .addField("vttUrl", String.class);

            schema.get("Video")
                .addRealmListField("subtitles", schema.get("VideoSubtitles"));

            oldVersion++;
        }
    }

}
