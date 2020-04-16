package de.xikolo.models

import io.realm.RealmList
import io.realm.RealmObject

open class QuizSubmissionAnswer() : RealmObject() {

    constructor(questionId: String, type: String, data: String) : this(
        questionId,
        type,
        listOf(data)
    )

    constructor(questionId: String, type: String, data: List<String>) : this() {
        this.questionId = questionId
        this.value = QuizSubmissionAnswerData().apply {
            this.type = type
            this.data = RealmList<String>().apply {
                data.forEach {
                    add(it)
                }
            }
        }
    }

    var questionId: String? = null
    var value: QuizSubmissionAnswerData? = null
}
