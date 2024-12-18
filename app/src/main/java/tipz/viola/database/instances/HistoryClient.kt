// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.database.instances

import android.content.Context
import tipz.viola.Application
import tipz.viola.database.BrohaClient
import tipz.viola.settings.SettingsKeys

class HistoryClient(context: Context) : BrohaClient(context, "history") {
    enum class UpdateHistoryState {
        STATE_DISABLED, STATE_DISABLED_DUPLICATED, STATE_URL_UPDATED, STATE_COMMITTED_WAIT_TASK
    }

    val settingsPreference = (context.applicationContext as Application).settingsPreference

    init {
        val historyApiVer = settingsPreference.getInt(SettingsKeys.historyApi)
        if (historyApiVer > LATEST_API || historyApiVer <= -1) throw RuntimeException()
        settingsPreference.setInt(SettingsKeys.historyApi, LATEST_API)
    }

    fun doSettingsCheck() {
        brohaEnabled = settingsPreference.getIntBool(SettingsKeys.enableHistoryStorage)
    }

    companion object {
        private const val LATEST_API = 0
    }
}