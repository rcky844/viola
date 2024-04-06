/*
 * Copyright (c) 2021-2024 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tipz.viola.settings

object SettingsKeys {

    /*
     * Configuration keys for shared preferences.
     *
     * Includes all possibly used keys. Comment next to the definition
     * indicates data type.
     */
    const val configDataStore = "config" /* Pref file name */ /* TODO: Migrate to something newer? */

    const val adServerId = "adServerId" /* STRING */
    const val adServerUrl = "adServerUrl" /* STRING */
    const val closeAppAfterDownload = "closeAppAfterDownload" /* INTEGER */
    const val defaultHomePage = "defaultHomePage" /* STRING */ /* FIXME: Rename to customHomePage */
    const val defaultHomePageId = "defaultHomePageId" /* INTEGER */
    const val defaultSearch = "defaultSearch" /* STRING */ /* FIXME: Rename to customSearch */
    const val defaultSearchId = "defaultSearchId" /* INTEGER */
    const val defaultSuggestions = "defaultSuggestions" /* STRING */ /* FIXME: Rename to customSuggestions */
    const val defaultSuggestionsId = "defaultSuggestionsId" /* INTEGER */
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

    /*
     * Intent keys used in SettingsActivity communication
     *
     * Includes all possibly used keys. Comment next to the definition
     * indicates data type.
     */
    const val needLoadUrl = "needLoadUrl"  /* STRING */
}