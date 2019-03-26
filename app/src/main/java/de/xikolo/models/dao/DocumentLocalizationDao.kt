package de.xikolo.models.dao

import de.xikolo.extensions.asCopy
import de.xikolo.models.DocumentLocalization
import de.xikolo.models.dao.base.BaseDao
import io.realm.Realm
import io.realm.kotlin.where

class DocumentLocalizationDao(realm: Realm) : BaseDao<DocumentLocalization>(DocumentLocalization::class, realm) {

    class Unmanaged {
        companion object {

            fun allForDocument(documentId: String?): List<DocumentLocalization> =
                Realm.getDefaultInstance().use { realm ->
                    realm.where<DocumentLocalization>()
                        .equalTo("documentId", documentId)
                        .findAll()
                        .asCopy()
                }

        }
    }

}
