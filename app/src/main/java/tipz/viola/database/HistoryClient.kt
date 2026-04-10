// Copyright (c) 2022-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.database

import android.content.Context
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

class HistoryClient(private val context: Context) {
    enum class UpdateHistoryState {
        STATE_DISABLED, STATE_DISABLED_DUPLICATED, STATE_WAIT_TASK, STATE_PENDING_COMMIT, STATE_COMMITTED
    }

    val dao: BrohaDao
        // if the INSTANCE is not null, then return it,
        // if it is, then create the DAO
        get() = INSTANCE ?: synchronized(this) {
            val instance = BrohaDatabase.getDatabase(
                context, "history").brohaDao()
            INSTANCE = instance
            // return instance
            instance
        }

    init {
        val settingsPreference = SettingsSharedPreference.instance
        val historyApiVer = settingsPreference.getInt(SettingsKeys.historyApi)
        if (historyApiVer > LATEST_API || historyApiVer <= -1) throw RuntimeException()
        settingsPreference.setInt(SettingsKeys.historyApi, LATEST_API)
    }

    companion object {
        private const val LATEST_API = 3

        // Singleton prevents multiple instances of DAO opening at the
        // same time.
        @Volatile
        private var INSTANCE: BrohaDao? = null
    }
}
