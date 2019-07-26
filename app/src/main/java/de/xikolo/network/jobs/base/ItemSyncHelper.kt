package de.xikolo.network.jobs.base

import de.xikolo.models.*
import de.xikolo.network.sync.Sync
import io.realm.kotlin.where

class ItemSyncHelper {

    companion object {

        fun syncItemContent(item: Item.JsonModel) {
            syncItemContent(arrayOf(item))
        }

        fun syncItemContent(items: Array<Item.JsonModel>) {
            Sync.Included.with<RichText>(items)
                .saveOnly()
                .run()
            Sync.Included.with<Quiz>(items)
                .saveOnly()
                .run()
            Sync.Included.with<PeerAssessment>(items)
                .saveOnly()
                .run()
            Sync.Included.with<LtiExercise>(items)
                .saveOnly()
                .run()
            Sync.Included.with<Video>(items)
                .saveOnly()
                .setBeforeCommitCallback { realm, model ->
                    val localVideo = realm.where<Video>().equalTo("id", model.id).findFirst()
                    if (localVideo != null) model.progress = localVideo.progress
                }
                .run()
        }
    }

}
