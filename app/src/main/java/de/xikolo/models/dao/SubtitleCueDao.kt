package de.xikolo.models.dao

import de.xikolo.extensions.asCopy
import de.xikolo.models.SubtitleCue
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.kotlin.where

class SubtitleCueDao(realm: Realm) : BaseDao<SubtitleCue>(SubtitleCue::class, realm) {

    class Unmanaged {
        companion object {

            @JvmStatic
            fun allForTrack(subtitleTrackId: String?): List<SubtitleCue> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<SubtitleCue>()
                        .equalTo("subtitleId", subtitleTrackId)
                        .sort("identifier")
                        .findAll()
                        .asCopy()
                }

        }
    }

}
