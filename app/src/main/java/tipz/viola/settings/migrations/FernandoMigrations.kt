// Copyright (c) 2025-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.migrations

import tipz.viola.download.DownloadClient
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.webview.buss.BussUtils

object FernandoMigrations : Migration(4) {
    override val keyPairsUpdate: Array<Pair<String, Any>> = arrayOf(
        // Homepage kill switch defaults
        Pair(SettingsKeys.useHomePage, 1),

        // Search suggestions kill switch defaults
        Pair(SettingsKeys.useSearchSuggestions, 1),

        // Downloads preferences
        Pair(SettingsKeys.enableDownloads, 1),
        Pair(SettingsKeys.requireDownloadConformation, 1),
        Pair(SettingsKeys.downloadLocationDefault, DownloadClient.defaultInitialDownloadPath),

        // Bussin WebX
        Pair(SettingsKeys.bussApiUrl, BussUtils.defaultApiUrl),

        // Cookies
        Pair(SettingsKeys.isCookiesEnabled, 1),
    )
    override val keysRemoval: Array<String> = arrayOf()

    override fun process(pref: SettingsSharedPreference) {
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
