package de.xikolo.models

import com.squareup.moshi.Json
import de.xikolo.models.base.RealmAdapter
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import moe.banana.jsonapi2.HasOne
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.Resource

open class QuizQuestion : RealmObject() {

    companion object {
        const val TYPE_SELECT_MULTIPLE = "select_multiple"
        const val TYPE_SELECT_ONE = "select_one"
        const val TYPE_FREE_TEXT = "free_text"
    }

    @PrimaryKey
    var id: String? = null

    var text: String? = null

    var explanation: String? = null

    var type: String? = null

    var position: Int = 0

    var maxPoints: Float = 0f

    var shuffleOptions: Boolean = false

    var options = RealmList<QuizQuestionOption>()

    var quizId: String? = null

    @JsonApi(type = "quiz-questions")
    class JsonModel : Resource(), RealmAdapter<QuizQuestion> {

        var text: String? = null

        var explanation: String? = null

        @field:Json(name = "type")
        var quizType: String = ""

        var position: Int = 0

        @field:Json(name = "max_points")
        var maxPoints: Float = 0.toFloat()

        @field:Json(name = "shuffle_options")
        var shuffleOptions: Boolean = false

        var options: List<QuizQuestionOption>? = null

        var quiz: HasOne<Quiz.JsonModel>? = null

        override fun convertToRealmObject(): QuizQuestion {
            val quizQuestion = QuizQuestion()
            quizQuestion.id = id
            quizQuestion.text = text
            quizQuestion.explanation = explanation
            quizQuestion.type = quizType
            quizQuestion.position = position
            quizQuestion.maxPoints = maxPoints
            quizQuestion.shuffleOptions = shuffleOptions
            quizQuestion.options.addAll(options!!)

            if (quiz != null) {
                quizQuestion.quizId = quiz!!.get().id
            }

            return quizQuestion
        }
    }
}
