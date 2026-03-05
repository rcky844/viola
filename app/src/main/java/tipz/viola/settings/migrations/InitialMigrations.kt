// Copyright (c) 2024-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.migrations

import android.annotation.SuppressLint
import tipz.viola.BuildConfig
import tipz.viola.download.DownloadClient
import tipz.viola.search.SearchEngineEntries
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.webview.buss.BussUtils

// Initialize when protocol version is 0, as it is defined as uninitialized
@SuppressLint("DiscouragedApi")
object InitialMigrations : Migration(0) {
    override val keyPairsUpdate: Array<Pair<String, Any>> = arrayOf(
        // Bussin WebX
        Pair(SettingsKeys.bussApiUrl, BussUtils.defaultApiUrl),

        // Downloads
        Pair(SettingsKeys.closeAppAfterDownload, 1),
        Pair(SettingsKeys.downloadLocationDefault, DownloadClient.defaultInitialDownloadPath),
        Pair(SettingsKeys.downloadMgrMode, 0),
        Pair(SettingsKeys.enableDownloads, 1),
        Pair(SettingsKeys.requireDownloadConformation, 1),

        // Homepage
        Pair(SettingsKeys.useHomePage, 1),
        Pair(SettingsKeys.homePageName, SearchEngineEntries.defaultEngineName),

        // Search
        Pair(SettingsKeys.searchName, SearchEngineEntries.defaultEngineName),
        Pair(SettingsKeys.suggestionsName, SearchEngineEntries.defaultEngineName),
        Pair(SettingsKeys.useSearchSuggestions, 1),

        // Updates
        Pair(SettingsKeys.updateChannelName, BuildConfig.VERSION_BUILD_TYPE),

        // Miscellaneous
        Pair(SettingsKeys.adServerId, 0),
        Pair(SettingsKeys.autoFadeToolbar, 1),
        Pair(SettingsKeys.enableHistoryStorage, 1),
        Pair(SettingsKeys.isCookiesEnabled, 1),
        Pair(SettingsKeys.isJavaScriptEnabled, 1),
        Pair(SettingsKeys.enableAdBlock, 0),
        Pair(SettingsKeys.enableGoogleSafeBrowse, 0),
        Pair(SettingsKeys.enableSwipeRefresh, 1),
        Pair(SettingsKeys.enforceHttps, 1),
        Pair(SettingsKeys.reverseAddressBar, 0),
        Pair(SettingsKeys.sendDNT, 0),
        Pair(SettingsKeys.sendSaveData, 0),
        Pair(SettingsKeys.sendSecGPC, 0),
        Pair(SettingsKeys.showFavicon, 0),
        Pair(SettingsKeys.showFullscreenWarningDialog, 1),
        Pair(SettingsKeys.themeId, 0),
        Pair(SettingsKeys.useCustomTabs, 1),
        Pair(SettingsKeys.useForceDark, 1),
        Pair(SettingsKeys.updateRecentsIcon, 1),
    )
    override val keysRemoval: Array<String> = arrayOf()
    override fun process(pref: SettingsSharedPreference) {}
}
