// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.database.instances

import android.content.Context
import tipz.viola.database.BrohaClient
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

class FavClient(context: Context) : BrohaClient(context, "bookmarks") {
    init {
        val settingsPreference = SettingsSharedPreference.instance
        val favApiVer = settingsPreference.getInt(SettingsKeys.favApi)
        if (favApiVer > LATEST_API || favApiVer <= -1) throw RuntimeException()
        settingsPreference.setInt(SettingsKeys.favApi, LATEST_API)
    }

    companion object {
        private const val LATEST_API = 1
    }
}