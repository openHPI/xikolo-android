package de.xikolo.models.dao

import de.xikolo.extensions.asCopy
import de.xikolo.models.QuizQuestion
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.kotlin.where

class QuizQuestionDao(realm: Realm) : BaseDao<QuizQuestion>(QuizQuestion::class, realm) {

    fun allForQuiz(quizId: String) = all("quizId" to quizId)

    class Unmanaged {
        companion object {

            @JvmStatic
            fun find(id: String?): QuizQuestion? =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<QuizQuestion>()
                        .equalTo("id", id)
                        .findFirst()
                        ?.asCopy()
                }

            @JvmStatic
            fun allForQuiz(id: String?): List<QuizQuestion> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<QuizQuestion>()
                        .equalTo("quizId", id)
                        .findAll()
                        .asCopy()
                }
        }
    }
}
