// Copyright (c) 2025-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.migrations

import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

object FrancescoMigrations : Migration(5) {
    override val keyPairsUpdate: Array<Pair<String, Any>> = arrayOf(
        Pair(SettingsKeys.startPageColor, -1),
        Pair(SettingsKeys.autoFadeToolbar, 1),
        Pair(SettingsKeys.legacyToolbar, 0),
        Pair(SettingsKeys.historySearchSuggestions, 1),
    )
    override val keysRemoval: Array<String> = arrayOf(
        SettingsKeys.adServerId, SettingsKeys.adServerUrl)
    override fun process(pref: SettingsSharedPreference) { }
}
