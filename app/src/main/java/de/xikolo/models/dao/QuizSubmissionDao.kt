package de.xikolo.models.dao

import de.xikolo.extensions.asCopy
import de.xikolo.models.QuizSubmission
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.kotlin.where

class QuizSubmissionDao(realm: Realm) : BaseDao<QuizSubmission>(QuizSubmission::class, realm) {

    fun allForQuiz(quizId: String) = all("quizId" to quizId)

    class Unmanaged {
        companion object {

            @JvmStatic
            fun find(id: String?): QuizSubmission? =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<QuizSubmission>()
                        .equalTo("id", id)
                        .findFirst()
                        ?.asCopy()
                }

            @JvmStatic
            fun allForQuiz(id: String?): List<QuizSubmission> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<QuizSubmission>()
                        .equalTo("quizId", id)
                        .findAll()
                        .asCopy()
                }
        }
    }
}
