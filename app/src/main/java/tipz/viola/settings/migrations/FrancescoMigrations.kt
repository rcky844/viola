// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.migrations

import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

class FrancescoMigrations(private val pref: SettingsSharedPreference) {
    init {
        if (pref.getInt(SettingsKeys.protocolVersion) <= 5) {

        }
    }
}