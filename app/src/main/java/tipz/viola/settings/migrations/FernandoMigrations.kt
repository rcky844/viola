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

            // Cookies
            pref.setInt(SettingsKeys.isCookiesEnabled, 1)

            // Disable FrogFind as suggestions provider
            // Automatic removal of unsupported entries was added by build 105,
            // migrate users who set FrogFind as the suggestions provider to the
            // default.
            if (pref.getString(SettingsKeys.suggestionsName) == "frogfind")
                pref.setString(SettingsKeys.suggestionsName, "brave")

            // Migrate Whoogle users to the default
            if (pref.getString(SettingsKeys.searchName) == "whoogle")
                pref.setString(SettingsKeys.searchName, "brave")
            if (pref.getString(SettingsKeys.homePageName) == "whoogle")
                pref.setString(SettingsKeys.homePageName, "brave")
            if (pref.getString(SettingsKeys.suggestionsName) == "whoogle")
                pref.setString(SettingsKeys.suggestionsName, "brave")
        }
    }
}