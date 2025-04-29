// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.migrations

import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

class FernandoMigrations(private val pref: SettingsSharedPreference) {
    init {
        if (pref.getInt(SettingsKeys.protocolVersion) == 4) {
            // Homepage kill switch defaults
            pref.setInt(SettingsKeys.useHomePage, 1)

            // Search suggestions kill switch defaults
            pref.setInt(SettingsKeys.useSearchSuggestions, 1)
        }
    }
}