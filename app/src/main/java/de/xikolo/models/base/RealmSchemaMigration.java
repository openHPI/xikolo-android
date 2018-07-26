package de.xikolo.models.base;

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
        }

        if (oldVersion == 2) {
            schema.create("Document")
                .addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                .addField("title", String.class)
                .addField("description", String.class)
                .addRealmListField("tags", String.class)
                .addField("isPublic", boolean.class)
                .addRealmListField("courseIds", String.class);

            schema.create("DocumentLocalization")
                .addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                .addField("title", String.class)
                .addField("description", String.class)
                .addField("language", String.class)
                .addField("revision", int.class)
                .addField("fileUrl", String.class)
                .addField("documentId", String.class);
        }
    }

}
