// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.migrations

import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

// For version 7.0.x
class ExoticMigrations(private val pref: SettingsSharedPreference) {
    private val deletedKeys = arrayOf(
        SettingsKeys.defaultHomePage, SettingsKeys.defaultHomePageId,
        SettingsKeys.defaultSearch, SettingsKeys.defaultSearchId,
        SettingsKeys.defaultSuggestions, SettingsKeys.defaultSuggestionsId,
    )

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

    private fun migrateSearchIndex() {
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

    private fun migrateDefaultBoolean() {
        pref.setInt(SettingsKeys.showFavicon, 0)
        pref.setInt(SettingsKeys.useForceDark, 1)
    }

    init {
        // Apply migrations only to the previous protocol version
        if (pref.getInt(SettingsKeys.protocolVersion) == 1) {
            // Migrations
            migrateSearchIndex()
            migrateDefaultBoolean()

            // Remove deleted keys
            deletedKeys.forEach { pref.remove(it) }
        }
    }
}