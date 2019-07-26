package de.xikolo.viewmodels.shared

import de.xikolo.network.jobs.ListSectionsWithItemsJob
import de.xikolo.network.jobs.base.NetworkStateLiveData
import io.realm.Realm

class SectionDelegate(realm: Realm, private val courseId: String) {

    fun requestSectionListWithItems(networkState: NetworkStateLiveData, userRequest: Boolean) {
        ListSectionsWithItemsJob(courseId, networkState, userRequest).run()
    }

}
