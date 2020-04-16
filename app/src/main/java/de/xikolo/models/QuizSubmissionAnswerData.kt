package de.xikolo.models

import io.realm.RealmList
import io.realm.RealmObject

open class QuizSubmissionAnswerData : RealmObject() {
    var type: String? = null
    var data: RealmList<String> = RealmList()
}
