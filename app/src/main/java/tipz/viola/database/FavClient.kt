// Copyright (c) 2022-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.database

import android.content.Context
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

class FavClient(private val context: Context) {
    val dao: BrohaDao
        // if the INSTANCE is not null, then return it,
        // if it is, then create the DAO
        get() = INSTANCE ?: synchronized(this) {
            val instance = BrohaDatabase.getDatabase(
                context, "bookmarks").brohaDao()
            INSTANCE = instance
            // return instance
            instance
        }

    init {
        val settingsPreference = SettingsSharedPreference.instance
        val favApiVer = settingsPreference.getInt(SettingsKeys.favApi)
        if (favApiVer > LATEST_API || favApiVer <= -1) throw RuntimeException()
        settingsPreference.setInt(SettingsKeys.favApi, LATEST_API)
    }

    companion object {
        private const val LATEST_API = 2

        // Singleton prevents multiple instances of DAO opening at the
        // same time.
        @Volatile
        private var INSTANCE: BrohaDao? = null
    }
}