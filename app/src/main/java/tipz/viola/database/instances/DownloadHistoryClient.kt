// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.database.instances

import android.content.Context
import tipz.viola.Application
import tipz.viola.database.BrohaClient
import tipz.viola.settings.SettingsKeys

class DownloadHistoryClient(context: Context) : BrohaClient(context, "download") {
    init {
        val settingsPreference = (context.applicationContext as Application).settingsPreference
        val downloadApiVer = settingsPreference.getInt(SettingsKeys.downloadApi)
        if (downloadApiVer > LATEST_API || downloadApiVer <= -1) throw RuntimeException()
        settingsPreference.setInt(SettingsKeys.downloadApi, LATEST_API)
    }

    companion object {
        private const val LATEST_API = 0
    }
}