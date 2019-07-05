package de.xikolo.managers

import de.xikolo.App
import de.xikolo.events.LogoutEvent
import de.xikolo.models.User
import de.xikolo.network.jobs.CreateAccessTokenJob
import de.xikolo.network.jobs.GetUserWithProfileJob
import de.xikolo.network.jobs.base.RequestJobCallback
import de.xikolo.storages.ApplicationPreferences
import de.xikolo.storages.UserStorage
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmObject
import org.greenrobot.eventbus.EventBus

class UserManager {

    fun login(email: String, password: String, callback: RequestJobCallback) {
        CreateAccessTokenJob(email, password, callback).run()
    }

    fun requestUserWithProfile(callback: RequestJobCallback) {
        GetUserWithProfileJob(callback).run()
    }

    fun getUser(realm: Realm, listener: RealmChangeListener<User>?): RealmObject? {
        if (listener == null) {
            throw IllegalArgumentException("RealmChangeListener should not be null for async queries.")
        }

        if (!UserManager.isAuthorized) {
            return null
        }

        val userPromise = realm
            .where(User::class.java)
            .equalTo("id", UserManager.userId)
            .findFirstAsync()

        userPromise.addChangeListener(listener)

        return userPromise
    }

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

            EventBus.getDefault().post(LogoutEvent())
        }
    }

}
