package de.xikolo.managers

import de.xikolo.App
import de.xikolo.storages.ApplicationPreferences
import de.xikolo.storages.UserStorage
import io.realm.Realm

class UserManager {

    companion object {

        val TAG: String = UserManager::class.java.simpleName

        @JvmStatic
        val token: String?
            get() {
                return UserStorage().accessToken
            }

        @JvmStatic
        val userId: String?
            get() {
                return UserStorage().userId
            }

        @JvmStatic
        val isAuthorized: Boolean
            get() {
                return UserStorage().accessToken != null
            }

        @JvmStatic
        fun logout() {
            val application = App.instance
            application.clearCookieSyncManager()

            val userStorage = UserStorage()
            userStorage.delete()

            val appPreferences = ApplicationPreferences()
            appPreferences.delete()

            application.lanalytics.deleteData()

            val realm = Realm.getDefaultInstance()
            realm.executeTransaction { it.deleteAll() }
            realm.close()

            App.instance.state.login.loggedOut()
        }
    }

}
