/*
 * Copyright (C) 2021-2023 Tipz Team
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
package tipz.browservio.settings;

public class SettingsKeys {
    /* browservio_saver */
    public static final String browservio_saver = "browservio.cfg"; /* Pref file name */
    public static final String adBlockListServer = "adBlockListServer"; /* STRING */
    public static final String centerActionBar = "centerActionBar"; /* INTEGER */
    public static final String closeAppAfterDownload = "closeAppAfterDownload"; /* INTEGER */
    public static final String defaultHomePage = "defaultHomePage"; /* STRING */ /* FIXME: Rename to customHomePage */
    public static final String defaultHomePageId = "defaultHomePageId"; /* INTEGER */
    public static final String defaultSearch = "defaultSearch"; /* STRING */ /* FIXME: Rename to customSearch */
    public static final String defaultSearchId = "defaultSearchId"; /* INTEGER */
    public static final String defaultSuggestions = "defaultSuggestions"; /* STRING */ /* FIXME: Rename to customSuggestions */
    public static final String defaultSuggestionsId = "defaultSuggestionsId"; /* INTEGER */
    public static final String enableAdBlock = "enableAdBlock"; /* INTEGER */
    public static final String enableGoogleSafeBrowse = "enableGoogleSafeBrowse"; /* INTEGER */
    public static final String enableSwipeRefresh = "enableSwipeRefresh"; /* INTEGER */
    public static final String enforceHttps = "enforceHttps"; /* INTEGER */
    public static final String favApi = "favApi"; /* INTEGER */ /* Was in bookmarks */
    public static final String historyApi = "historyApi"; /* INTEGER */ /* Was in history */
    public static final String isFirstLaunch = "isFirstLaunch"; /* STRING */
    public static final String isJavaScriptEnabled = "isJavaScriptEnabled"; /* INTEGER */
    public static final String protocolVersion = "protocolVersion"; /* INTEGER */
    public static final String reverseLayout = "reverseLayout"; /* INTEGER */
    public static final String reverseOnlyActionBar = "reverseOnlyActionBar"; /* INTEGER */
    public static final String sendDNT = "sendDNT"; /* INTEGER */
    public static final String showFavicon = "showFavicon"; /* INTEGER */
    public static final String themeId = "themeId"; /* INTEGER */
    public static final String useCustomTabs = "useCustomTabs"; /* INTEGER */
    public static final String updateRecentsIcon = "updateRecentsIcon"; /* INTEGER */
}