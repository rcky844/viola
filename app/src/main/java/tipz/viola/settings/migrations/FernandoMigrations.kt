// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.migrations

import tipz.viola.download.DownloadClient
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.webview.buss.BussUtils

class FernandoMigrations(private val pref: SettingsSharedPreference) {
    init {
        if (pref.getInt(SettingsKeys.protocolVersion) == 4) {
            // Homepage kill switch defaults
            pref.setInt(SettingsKeys.useHomePage, 1)

            // Search suggestions kill switch defaults
            pref.setInt(SettingsKeys.useSearchSuggestions, 1)

            // Downloads preferences
            pref.setInt(SettingsKeys.enableDownloads, 1)
            pref.setInt(SettingsKeys.requireDownloadConformation, 1)
            pref.setString(SettingsKeys.downloadLocationDefault, DownloadClient.defaultInitialDownloadPath)

            // Bussin WebX
            pref.setString(SettingsKeys.bussApiUrl, BussUtils.defaultApiUrl)
        }
    }
}