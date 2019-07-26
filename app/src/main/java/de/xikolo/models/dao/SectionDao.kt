package de.xikolo.models.dao

import androidx.lifecycle.LiveData
import de.xikolo.extensions.asCopy
import de.xikolo.extensions.asLiveData
import de.xikolo.models.Section
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.kotlin.where

class SectionDao(realm: Realm) : BaseDao<Section>(Section::class, realm) {

    override fun find(id: String?): LiveData<Section> =
        query()
            .equalTo("id", id)
            .findFirstAsync()
            .asLiveData()

    class Unmanaged {
        companion object {

            @JvmStatic
            fun find(id: String?): Section? =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Section>()
                        .equalTo("id", id)
                        .findFirst()
                        ?.asCopy()
                }

            fun allForCourse(courseId: String?): List<Section> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<Section>()
                        .equalTo("courseId", courseId)
                        .sort("position")
                        .findAll()
                        .asCopy()
                }

        }
    }

}
