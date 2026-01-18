// Copyright (c) 2024-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.migrations

import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

object ExoticMigrations : Migration(1) {
    /* In Exotic 7.0, index based search IDs are ditched over
     * using more identifiable names representing the search
     * engine. This will need to be migrated using a map of
     * the old engines list.
     *
     * For custom urls in this update, they also migrated to
     * a more properly named version.
     */
    private val oldEnginesMapping = arrayOf("google", "baidu", "duckduckgo",
        "bing", "yahoo", "ecosia", "yandex", "brave", "startpage", "whoogle",
        "swisscows", "qwant", "sogou", "so360", ""
    )

    override val keyPairsUpdate: Array<Pair<String, Any>> = arrayOf(
        Pair(SettingsKeys.showFavicon, 0),
        Pair(SettingsKeys.useForceDark, 1),
        Pair(SettingsKeys.enableHistoryStorage, 1),
    )

    override val keysRemoval: Array<String> = arrayOf(
        SettingsKeys.defaultHomePage, SettingsKeys.defaultHomePageId,
        SettingsKeys.defaultSearch, SettingsKeys.defaultSearchId,
        SettingsKeys.defaultSuggestions, SettingsKeys.defaultSuggestionsId,
    )

    override fun process(pref: SettingsSharedPreference) {
        pref.setString(SettingsKeys.homePageName,
            oldEnginesMapping[pref.getInt(SettingsKeys.defaultHomePageId)])
        pref.setString(SettingsKeys.homePageCustomUrl,
            pref.getString(SettingsKeys.defaultHomePage))

        pref.setString(SettingsKeys.searchName,
            oldEnginesMapping[pref.getInt(SettingsKeys.defaultSearchId)])
        pref.setString(SettingsKeys.searchCustomUrl,
            pref.getString(SettingsKeys.defaultSearch))

        pref.setString(SettingsKeys.suggestionsName,
            oldEnginesMapping[pref.getInt(SettingsKeys.defaultSuggestionsId)])
        pref.setString(SettingsKeys.suggestionsCustomUrl,
            pref.getString(SettingsKeys.defaultSuggestions))
    }
}

object ExoticMR1Migrations : Migration(2) {
    override val keyPairsUpdate: Array<Pair<String, Any>> = arrayOf(
        Pair(SettingsKeys.showFullscreenWarningDialog, 1),
    )
    override val keysRemoval: Array<String> = arrayOf()
    override fun process(pref: SettingsSharedPreference) { }
}

object ExoticMR2Migrations : Migration(3) {
    override val keyPairsUpdate: Array<Pair<String, Any>> = arrayOf()
    override val keysRemoval: Array<String> = arrayOf(SettingsKeys.checkAppLink)
    override fun process(pref: SettingsSharedPreference) { }
}
