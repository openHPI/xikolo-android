package de.xikolo.models.dao

import de.xikolo.extensions.asCopy
import de.xikolo.models.SubtitleTrack
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.kotlin.where

class SubtitleTrackDao(realm: Realm) : BaseDao<SubtitleTrack>(SubtitleTrack::class, realm) {

    class Unmanaged {
        companion object {

            @JvmStatic
            fun allForVideo(videoId: String?): List<SubtitleTrack> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<SubtitleTrack>()
                        .equalTo("videoId", videoId)
                        .findAll()
                        .asCopy()
                }

        }
    }

}
