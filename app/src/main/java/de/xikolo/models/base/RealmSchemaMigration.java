package de.xikolo.models.base;

import java.io.File;

import de.xikolo.App;
import de.xikolo.utils.FileUtil;
import de.xikolo.utils.StorageUtil;
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

        /* This is not really a Realm database migration.
         * The schema version had to be increased in order to deal with the app's changed storage location.
         * So this is just the storage migration.
         */
        if (oldVersion == 2) {
            File oldStorageLocation = new File(FileUtil.getPublicAppStorageFolderPath());
            int fileCount = FileUtil.folderFileNumber(oldStorageLocation);
            if (oldStorageLocation.exists() && fileCount != 0) {
                File newStorageLocation = StorageUtil.getStorage(App.getInstance());
                /*ProgressDialog progressDialog = new android.app.ProgressDialog(App.getInstance());
                progressDialog.setTitle(R.string.app_name);
                progressDialog.setMessage(App.getInstance().getString(R.string.dialog_app_being_prepared));
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMax(fileCount);
                progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);

                progressDialog.show();*/

                StorageUtil.migrate(oldStorageLocation, newStorageLocation, (count) -> {
                    /*progressDialog.setProgress(count);

                    if (count == totalFiles)
                        progressDialog.hide();*/
                });


            }
        }
    }

}
