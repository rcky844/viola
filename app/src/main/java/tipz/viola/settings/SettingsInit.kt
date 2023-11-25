/*
 * Copyright (C) 2022-2023 Tipz Team
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

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import tipz.viola.utils.CommonUtils

class SettingsInit(context: Context) {
    val pref: SharedPreferences

    init {
        pref = context.getSharedPreferences(SettingsKeys.configDataStore, Activity.MODE_PRIVATE)

        /* A bloopers fix for migrating from old versions */if (SettingsUtils.isFirstLaunch(pref) && SettingsUtils.getPrefNum(
                pref,
                SettingsKeys.protocolVersion
            ) == 0 && SettingsUtils.getPref(pref, SettingsKeys.isJavaScriptEnabled)!!.isNotEmpty()
        ) SettingsUtils.setPref(pref, SettingsKeys.isFirstLaunch, "0")
        if (SettingsUtils.isFirstLaunch(pref)) {
            SettingsUtils.setPrefNum(pref, SettingsKeys.centerActionBar, 1)
            SettingsUtils.setPrefNum(pref, SettingsKeys.closeAppAfterDownload, 1)
            SettingsUtils.setPrefNum(pref, SettingsKeys.defaultHomePageId, 7)
            SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSearchId, 7)
            SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSuggestionsId, 7)
            SettingsUtils.setPrefNum(pref, SettingsKeys.isJavaScriptEnabled, 1)
            SettingsUtils.setPrefNum(pref, SettingsKeys.enableAdBlock, 0)
            SettingsUtils.setPrefNum(pref, SettingsKeys.enableGoogleSafeBrowse, 0)
            SettingsUtils.setPrefNum(pref, SettingsKeys.enableSwipeRefresh, 1)
            SettingsUtils.setPrefNum(pref, SettingsKeys.enforceHttps, 1)
            SettingsUtils.setPrefNum(pref, SettingsKeys.reverseLayout, 0)
            SettingsUtils.setPrefNum(pref, SettingsKeys.reverseOnlyActionBar, 0)
            SettingsUtils.setPrefNum(pref, SettingsKeys.sendDNT, 0)
            SettingsUtils.setPrefNum(pref, SettingsKeys.showFavicon, 1)
            SettingsUtils.setPrefNum(pref, SettingsKeys.themeId, 0)
            SettingsUtils.setPrefNum(pref, SettingsKeys.useCustomTabs, 1)
            SettingsUtils.setPrefNum(pref, SettingsKeys.updateRecentsIcon, 1)
        } else {
            protoVer0To1()
            protoVer1To2()
        }
        SettingsUtils.setPrefNum(
            pref,
            SettingsKeys.protocolVersion,
            2
        ) /* CURRENT_PROTOCOL_VERSION */
        if (SettingsUtils.isFirstLaunch(pref)) SettingsUtils.setPref(
            pref,
            SettingsKeys.isFirstLaunch,
            "0"
        )
    }

    private fun protoVer0To1() {
        if (SettingsUtils.getPrefNum(pref, SettingsKeys.protocolVersion) == 0) {
            /* 2c15e330: java: urls: Add Brave Search */
            if (SettingsUtils.getPrefNum(
                    pref,
                    SettingsKeys.defaultHomePageId
                ) == 7
            ) SettingsUtils.setPrefNum(pref, SettingsKeys.defaultHomePageId, 8)
            if (SettingsUtils.getPrefNum(
                    pref,
                    SettingsKeys.defaultSearchId
                ) == 7
            ) SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSearchId, 8)
            /* 8cdfc371: java: settings: Move most settings to integer and add protocol version */SettingsUtils.setPrefNum(
                pref,
                SettingsKeys.isJavaScriptEnabled,
                SettingsUtils.getPref(pref, SettingsKeys.isJavaScriptEnabled)!!.toInt()
            )
            SettingsUtils.setPrefNum(
                pref,
                SettingsKeys.sendDNT,
                SettingsUtils.getPref(pref, SettingsKeys.sendDNT)!!.toInt()
            )
            SettingsUtils.setPrefNum(
                pref,
                SettingsKeys.showFavicon,
                SettingsUtils.getPref(pref, SettingsKeys.showFavicon)!!.toInt()
            )
        }
    }

    private fun protoVer1To2() {
        if (SettingsUtils.getPrefNum(pref, SettingsKeys.protocolVersion) == 1) {
            /* 4bb92167: java: settings: Allow enabling or disabling pull to refresh */
            SettingsUtils.setPrefNum(pref, SettingsKeys.enableSwipeRefresh, 1)
            /* 1fd6ea58: java: main: Add experimental support for enforcing HTTPS */SettingsUtils.setPrefNum(
                pref,
                SettingsKeys.enforceHttps,
                1
            )
            /* cc6cb8ea: java: search: Rewrite search engine code */if (SettingsUtils.getPrefNum(
                    pref,
                    SettingsKeys.defaultHomePageId
                ) != 8
            ) SettingsUtils.setPref(pref, SettingsKeys.defaultHomePage, CommonUtils.EMPTY_STRING)
            if (SettingsUtils.getPrefNum(
                    pref,
                    SettingsKeys.defaultSearchId
                ) != 8
            ) SettingsUtils.setPref(pref, SettingsKeys.defaultSearch, CommonUtils.EMPTY_STRING)
            SettingsUtils.setPref(pref, SettingsKeys.defaultSuggestions, CommonUtils.EMPTY_STRING)
            /* 8f2ca067: java: settings: Allow the user to choose if they want Custom Tabs */SettingsUtils.setPrefNum(
                pref,
                SettingsKeys.useCustomTabs,
                1
            )
            /* 2d6ce244: java: webview: Finish if launched page is a download */SettingsUtils.setPrefNum(
                pref,
                SettingsKeys.closeAppAfterDownload,
                1
            )
            /* 89b55613: java: browser: Add support for reverse layout */SettingsUtils.setPrefNum(
                pref,
                SettingsKeys.reverseLayout,
                0
            )
            /* 3cc75065: java: search: Add support for DuckDuckGo search suggestions */if (SettingsUtils.getPrefNum(
                    pref,
                    SettingsKeys.defaultSuggestionsId
                ) >= 2
            ) SettingsUtils.setPrefNum(
                pref,
                SettingsKeys.defaultSuggestionsId,
                SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSuggestionsId + 1)
            )
            /* 9410799f: java: settings: Add switch for updating task label description */SettingsUtils.setPrefNum(
                pref,
                SettingsKeys.updateRecentsIcon,
                1
            )
            /* bac9b451: java: webview: Allow disabling Google's "Safe" Browsing feature */SettingsUtils.setPrefNum(
                pref,
                SettingsKeys.enableGoogleSafeBrowse,
                0
            )
        }
    }

    private fun protoVer2To3() {
        if (SettingsUtils.getPrefNum(pref, SettingsKeys.protocolVersion) == 2) {
            /* 4bb92167: java: settings: Add the option to reverse only action bar */
            SettingsUtils.setPrefNum(pref, SettingsKeys.reverseOnlyActionBar, 0)
        }
    }
}