// Copyright (c) 2025-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.migrations

import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

object FrancescoMigrations : Migration(5) {
    override val keyPairsUpdate: Array<Pair<String, Any>> = arrayOf(
        Pair(SettingsKeys.autoFadeToolbar, 1),
        Pair(SettingsKeys.legacyToolbar, 0),
    )
    override val keysRemoval: Array<String> = arrayOf()
    override fun process(pref: SettingsSharedPreference) { }
}
