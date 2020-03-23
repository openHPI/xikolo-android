package de.xikolo.models;

import com.squareup.moshi.Json;

import de.xikolo.utils.LanguageUtil;
import io.realm.RealmObject;

public class VideoSubtitles extends RealmObject {

    public String language;

    @Json(name = "created_by_machine")
    public boolean createdByMachine;

    @Json(name = "vtt_url")
    public String vttUrl;

    public String getLanguageAsNativeName() {
        return LanguageUtil.INSTANCE.toNativeName(language);
    }

}
