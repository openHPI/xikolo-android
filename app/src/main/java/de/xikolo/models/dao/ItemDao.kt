package de.xikolo.models.dao

import de.xikolo.extensions.asCopy
import de.xikolo.models.*
import de.xikolo.models.Item.*
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.RealmObject
import io.realm.Sort
import io.realm.kotlin.where

class ItemDao(realm: Realm) : BaseDao<Item>(Item::class, realm) {

    init {
        defaultSort = "position" to Sort.ASCENDING
    }

    fun allAccessibleForCourse(courseId: String?) =
        all("courseId" to courseId, "accessible" to true)

    fun allAccessibleForSection(sectionId: String?) =
        all("sectionId" to sectionId, "accessible" to true)

    class Unmanaged {
        companion object {

            @JvmStatic
            fun find(id: String?): Item? =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Item>()
                        .equalTo("id", id)
                        .findFirst()
                        ?.asCopy()
                }

            @JvmStatic
            fun findContent(id: String?): RealmObject? =
                Realm.getDefaultInstance().use { realm ->
                    val item = find(id)
                    when (item?.contentType) {
                        TYPE_TEXT  -> realm.where<RichText>().equalTo("id", item.contentId).findFirst()?.asCopy()
                        TYPE_VIDEO -> realm.where<Video>().equalTo("id", item.contentId).findFirst()?.asCopy()
                        TYPE_QUIZ  -> realm.where<Quiz>().equalTo("id", item.contentId).findFirst()?.asCopy()
                        TYPE_LTI   -> realm.where<LtiExercise>().equalTo("id", item.contentId).findFirst()?.asCopy()
                        TYPE_PEER  -> realm.where<PeerAssessment>().equalTo("id", item.contentId).findFirst()?.asCopy()
                        else       -> null
                    }
            }

            @JvmStatic
            fun allAccessibleForSection(sectionId: String?): List<Item> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Item>()
                        .equalTo("sectionId", sectionId)
                        .equalTo("accessible", true)
                        .sort("position")
                        .findAll()
                        .asCopy()
                }

            @JvmStatic
            fun allAccessibleVideosForSection(sectionId: String?): List<Item> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Item>()
                        .equalTo("sectionId", sectionId)
                        .equalTo("accessible", true)
                        .equalTo("contentType", Item.TYPE_VIDEO)
                        .sort("position")
                        .findAll()
                        .asCopy()
                }

        }
    }

}
