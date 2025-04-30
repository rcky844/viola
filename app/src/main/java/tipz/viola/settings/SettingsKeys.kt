// Copyright (c) 2021-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings

object SettingsKeys {

    /*
     * Configuration keys for shared preferences.
     *
     * Includes all possibly used keys. Comment next to the definition
     * indicates data type.
     */
    const val configDataStore = "config" /* Pref file name */ /* TODO: Migrate to something newer? */
    const val protocolVersion = "protocolVersion" /* INTEGER */ /* Protocol of settings config */

    /* Ad blocker */
    const val enableAdBlock = "enableAdBlock" /* INTEGER */
    const val adServerId = "adServerId" /* STRING */
    const val adServerUrl = "adServerUrl" /* STRING */

    /* Broha */
    const val favApi = "favApi" /* INTEGER */
    const val historyApi = "historyApi" /* INTEGER */
    const val enableHistoryStorage = "enableHistoryStorage" /* INTEGER */

    /* Buss */
    const val bussApiUrl = "bussApiUrl" /* STRING */

    /* Custom Tabs */
    const val useCustomTabs = "useCustomTabs" /* INTEGER */

    /* Download Manager */
    const val downloadApi = "downloadApi" /* INTEGER */
    const val closeAppAfterDownload = "closeAppAfterDownload" /* INTEGER */
    const val downloadLocationDefault = "downloadLocationDefault" /* STRING */
    const val downloadMgrMode = "downloadMgrMode" /* INTEGER */
    const val enableDownloads = "enableDownloads" /* INTEGER */
    const val requireDownloadConformation = "requireDownloadConformation" /* INTEGER */

    /* Development settings */
    const val remoteDebugging = "remoteDebugging" /* STRING */
    const val alwaysOnLogging = "alwaysOnLogging" /* INTEGER */

    /* Search & Startpage */
    /* TODO: Remove custom settings when support creating custom EngineObjects */
    const val homePageName = "homePageName" /* STRING */
    const val homePageCustomUrl = "homePageCustomUrl" /* STRING */
    const val searchName = "searchName" /* STRING */
    const val searchCustomUrl = "searchCustomUrl" /* STRING */
    const val suggestionsName = "suggestionsName" /* STRING */
    const val suggestionsCustomUrl = "suggestionsCustomUrl" /* STRING */
    const val useHomePage = "useHomePage" /* INTEGER */
    const val useWebHomePage = "useWebHomePage" /* INTEGER */
    const val useSearchSuggestions = "useSearchSuggestions" /* INTEGER */

    /* Visuals & Theming */
    const val themeId = "themeId" /* INTEGER */
    const val useForceDark = "useForceDark" /* INTEGER */
    const val showFavicon = "showFavicon" /* INTEGER */
    const val startPageWallpaper = "startPageWallpaper" /* STRING */
    const val reverseAddressBar = "reverseAddressBar" /* INTEGER */
    const val updateRecentsIcon = "updateRecentsIcon" /* INTEGER */
    const val showFullscreenWarningDialog = "showFullscreenWarningDialog" /* INTEGER */

    /* Updater */
    const val updateChannelName = "updateChannelName" /* STRING */

    /* Miscellaneous */
    const val enableGoogleSafeBrowse = "enableGoogleSafeBrowse" /* INTEGER */
    const val enableSwipeRefresh = "enableSwipeRefresh" /* INTEGER */
    const val enforceHttps = "enforceHttps" /* INTEGER */
    const val isJavaScriptEnabled = "isJavaScriptEnabled" /* INTEGER */
    const val sendDNT = "sendDNT" /* INTEGER */
    const val sendSaveData = "sendSaveData" /* INTEGER */
    const val sendSecGPC = "sendSecGPC" /* INTEGER */

    /* Removed in 7.1.x */
    const val checkAppLink = "checkAppLink" /* INTEGER */

    /* Removed in 7.0.x */
    const val defaultHomePage = "defaultHomePage" /* STRING */
    const val defaultHomePageId = "defaultHomePageId" /* INTEGER */
    const val defaultSearch = "defaultSearch" /* STRING */
    const val defaultSearchId = "defaultSearchId" /* INTEGER */
    const val defaultSuggestions = "defaultSuggestions" /* STRING */
    const val defaultSuggestionsId = "defaultSuggestionsId" /* INTEGER */

    /*
     * Intent keys used in inter-activity communication
     *
     * Includes all possibly used keys. Comment next to the definition
     * indicates data type.
     */
    const val needLoadUrl = "needLoadUrl"  /* STRING */
    const val needReload = "needReload" /* INTEGER */
    const val updateAdServers = "updateAdServers" /* INTEGER */
}