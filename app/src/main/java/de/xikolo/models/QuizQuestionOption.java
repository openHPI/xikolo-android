package de.xikolo.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class QuizQuestionOption extends RealmObject {

    @PrimaryKey
    public String id;

    public int position;

    public String text;

    public boolean correct;

    public String explanation;
}
