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

class SettingsInit(context: Context) {
    val pref: SharedPreferences

    init {
        pref = context.getSharedPreferences(SettingsKeys.configDataStore, Activity.MODE_PRIVATE)

        if (SettingsUtils.getPrefNum(pref, SettingsKeys.protocolVersion) == 0) {
            SettingsUtils.setPrefNum(pref, SettingsKeys.closeAppAfterDownload, 1)
            SettingsUtils.setPrefNum(pref, SettingsKeys.defaultHomePageId, 7)
            SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSearchId, 7)
            SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSuggestionsId, 7)
            SettingsUtils.setPrefNum(pref, SettingsKeys.isJavaScriptEnabled, 1)
            SettingsUtils.setPrefNum(pref, SettingsKeys.enableAdBlock, 0)
            SettingsUtils.setPrefNum(pref, SettingsKeys.enableGoogleSafeBrowse, 0)
            SettingsUtils.setPrefNum(pref, SettingsKeys.enableSwipeRefresh, 1)
            SettingsUtils.setPrefNum(pref, SettingsKeys.enforceHttps, 1)
            SettingsUtils.setPrefNum(pref, SettingsKeys.reverseAddressBar, 0)
            SettingsUtils.setPrefNum(pref, SettingsKeys.sendDNT, 0)
            SettingsUtils.setPrefNum(pref, SettingsKeys.showFavicon, 1)
            SettingsUtils.setPrefNum(pref, SettingsKeys.themeId, 0)
            SettingsUtils.setPrefNum(pref, SettingsKeys.useCustomTabs, 1)
            SettingsUtils.setPrefNum(pref, SettingsKeys.updateRecentsIcon, 1)
        }
        // Sets CURRENT_PROTOCOL_VERSION, currently 1
        // Must be non-zero, as zero is defined as uninitialized
        SettingsUtils.setPrefNum(
            pref,
            SettingsKeys.protocolVersion,
            1
        )
    }
}