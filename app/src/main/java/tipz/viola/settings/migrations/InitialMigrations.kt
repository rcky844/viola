// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.migrations

import tipz.viola.BuildConfig
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

// For first start
class InitialMigrations(pref: SettingsSharedPreference) {
    init {
        // Initialize only protocol version 0, as it is defined as uninitialized
        if (pref.getInt(SettingsKeys.protocolVersion) == 0) {
            // Search
            pref.setString(SettingsKeys.homePageName, "brave")
            pref.setString(SettingsKeys.searchName, "brave")
            pref.setString(SettingsKeys.suggestionsName, "brave")

            // Updates
            pref.setString(SettingsKeys.updateChannelName, BuildConfig.VERSION_BUILD_TYPE)

            // Miscellaneous
            pref.setInt(SettingsKeys.adServerId, 0)
            pref.setInt(SettingsKeys.closeAppAfterDownload, 1)
            pref.setInt(SettingsKeys.downloadMgrMode, 0)
            pref.setInt(SettingsKeys.enableHistoryStorage, 1)
            pref.setInt(SettingsKeys.isJavaScriptEnabled, 1)
            pref.setInt(SettingsKeys.enableAdBlock, 0)
            pref.setInt(SettingsKeys.enableGoogleSafeBrowse, 0)
            pref.setInt(SettingsKeys.enableSwipeRefresh, 1)
            pref.setInt(SettingsKeys.enforceHttps, 1)
            pref.setInt(SettingsKeys.reverseAddressBar, 0)
            pref.setInt(SettingsKeys.sendDNT, 0)
            pref.setInt(SettingsKeys.sendSaveData, 0)
            pref.setInt(SettingsKeys.sendSecGPC, 0)
            pref.setInt(SettingsKeys.showFavicon, 0)
            pref.setInt(SettingsKeys.showFullscreenWarningDialog, 1)
            pref.setInt(SettingsKeys.themeId, 0)
            pref.setInt(SettingsKeys.useCustomTabs, 1)
            pref.setInt(SettingsKeys.useForceDark, 1)
            pref.setInt(SettingsKeys.updateRecentsIcon, 1)
        }
    }
}