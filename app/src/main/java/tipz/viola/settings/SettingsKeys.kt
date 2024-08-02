// Copyright (c) 2021-2024 Tipz Team
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

    /* Search */
    /* TODO: Remove custom settings when support creating custom EngineObjects */
    const val homePageName = "homePageName" /* STRING */
    const val homePageCustomUrl = "homePageCustomUrl" /* STRING */
    const val searchName = "searchName" /* STRING */
    const val searchCustomUrl = "searchCustomUrl" /* STRING */
    const val suggestionsName = "suggestionsName" /* STRING */
    const val suggestionsCustomUrl = "suggestionsCustomUrl" /* STRING */

    /* Miscellaneous */
    const val adServerId = "adServerId" /* STRING */
    const val adServerUrl = "adServerUrl" /* STRING */
    const val checkAppLink = "checkAppLink" /* INTEGER */
    const val closeAppAfterDownload = "closeAppAfterDownload" /* INTEGER */
    const val downloadMgrMode = "downloadMgrMode" /* INTEGER */
    const val enableAdBlock = "enableAdBlock" /* INTEGER */
    const val enableGoogleSafeBrowse = "enableGoogleSafeBrowse" /* INTEGER */
    const val enableSwipeRefresh = "enableSwipeRefresh" /* INTEGER */
    const val enforceHttps = "enforceHttps" /* INTEGER */
    const val favApi = "favApi" /* INTEGER */
    const val historyApi = "historyApi" /* INTEGER */
    const val isJavaScriptEnabled = "isJavaScriptEnabled" /* INTEGER */
    const val protocolVersion = "protocolVersion" /* INTEGER */
    const val reverseAddressBar = "reverseAddressBar" /* INTEGER */
    const val sendDNT = "sendDNT" /* INTEGER */
    const val sendSaveData = "sendSaveData" /* INTEGER */
    const val sendSecGPC = "sendSecGPC" /* INTEGER */
    const val showFavicon = "showFavicon" /* INTEGER */
    const val startPageWallpaper = "startPageWallpaper" /* STRING */
    const val themeId = "themeId" /* INTEGER */
    const val useCustomTabs = "useCustomTabs" /* INTEGER */
    const val useWebHomePage = "useWebHomePage" /* INTEGER */
    const val updateChannelName = "updateChannelName" /* STRING */
    const val updateRecentsIcon = "updateRecentsIcon" /* INTEGER */

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