package de.xikolo.models

import com.squareup.moshi.Json
import de.xikolo.models.base.JsonAdapter
import de.xikolo.models.base.RealmAdapter
import de.xikolo.utils.extensions.asDate
import de.xikolo.utils.extensions.formattedString
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import moe.banana.jsonapi2.HasOne
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource
import java.util.Date

open class QuizSubmission : RealmObject(), JsonAdapter<QuizSubmission.JsonModel> {

    @PrimaryKey
    var id: String? = null

    var createdAt: Date? = null

    var submittedAt: Date? = null

    var submitted: Boolean = false

    var points: Float = 0f

    var answers: RealmList<QuizSubmissionAnswer> = RealmList()

    var quizId: String? = null

    override fun convertToJsonResource(): JsonModel {
        val model = JsonModel()
        model.id = id
        model.createdAt = createdAt?.formattedString
        model.submittedAt = submittedAt?.formattedString
        model.submitted = submitted
        model.points = points

        val modelAnswers = mutableMapOf<String, Map<String, Any>>()
        answers.forEach { answer ->
            modelAnswers[answer.questionId!!] =
                mapOf(
                    "type" to answer.value?.type!!, "data" to
                        if (answer.value?.type!! == QuizQuestion.TYPE_SELECT_MULTIPLE) {
                            // map to list of strings
                            answer.value?.data!!
                        } else {
                            // map to single string
                            answer.value?.data?.first()!!
                        }
                )
        }

        model.answers = modelAnswers

        model.quiz = HasOne(Quiz.JsonModel().type, quizId)

        return model
    }

    @JsonApi(type = "quiz-submissions")
    class JsonModel : Resource(), RealmAdapter<QuizSubmission> {

        @field:Json(name = "created_at")
        var createdAt: String? = null

        @field:Json(name = "submitted_at")
        var submittedAt: String? = null

        var submitted: Boolean = false

        var points: Float = 0f

        // data schema is Map<String, Map<String, String>> for free_text and select_one
        // and Map<String, Map<String, Map<String, List<String>> for select_multiple
        var answers: Map<String, Map<String, Any>>? = null

        var quiz: HasOne<Quiz.JsonModel>? = null

        override fun convertToRealmObject(): QuizSubmission {
            val quizSubmission = QuizSubmission()
            quizSubmission.id = id
            quizSubmission.createdAt = createdAt.asDate
            quizSubmission.submittedAt = submittedAt.asDate
            quizSubmission.submitted = submitted
            quizSubmission.points = points

            answers?.forEach { answer ->
                quizSubmission.answers.add(
                    QuizSubmissionAnswer(
                        answer.key, answer.value["type"] as String,
                        if (answer.value["type"] == QuizQuestion.TYPE_SELECT_MULTIPLE) {
                            // data is a list of strings
                            answer.value["data"] as List<String>
                        } else {
                            // data is a string and will be converted to list here
                            listOf(answer.value["data"] as String)
                        }
                    )
                )
            }

            if (quiz != null) {
                quizSubmission.quizId = quiz!!.get().id
            }

            return quizSubmission
        }
    }
}
