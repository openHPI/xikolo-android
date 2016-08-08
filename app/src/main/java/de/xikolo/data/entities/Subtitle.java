package de.xikolo.data.entities;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@AutoValue
public abstract class Subtitle implements Parcelable {

    @SerializedName("src_lang")
    public abstract String language();

    @SerializedName("texts")
    public abstract List<Text> textList();

    public static Subtitle create(String language, List<Text> textList) {
        return new AutoValue_Subtitle(language, textList);
    }

    public static TypeAdapter<Subtitle> typeAdapter(Gson gson) {
        return new AutoValue_Subtitle.GsonTypeAdapter(gson);
    }

    @AutoValue
    public static abstract class Text implements Parcelable {

        @SerializedName("identifier")
        public abstract String identifier();

        @SerializedName("text")
        public abstract String text();

        @SerializedName("start")
        public abstract String start();

        @SerializedName("end")
        public abstract String end();

        @SerializedName("settings")
        public abstract String settings();

        public static Text create(String identifier, String text, String start, String end, String settings) {
            return new AutoValue_Subtitle_Text(identifier, text, start, end, settings);
        }

        public static TypeAdapter<Text> typeAdapter(Gson gson) {
            return new AutoValue_Subtitle_Text.GsonTypeAdapter(gson);
        }

    }

}
