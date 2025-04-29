// Copyright (c) 2023-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import tipz.viola.Application
import tipz.viola.settings.migrations.ExoticMigrations
import tipz.viola.settings.migrations.FernandoMigrations
import tipz.viola.settings.migrations.InitialMigrations

class SettingsSharedPreference(context: Context) {
    private var preference: SharedPreferences

    init {
        preference =
            context.getSharedPreferences(SettingsKeys.configDataStore, Activity.MODE_PRIVATE)!!
        if (context is Application) settingsInit()
    }

    private fun settingsInit() {
        // Migration modules
        InitialMigrations(this)
        ExoticMigrations(this)
        FernandoMigrations(this)

        // Sets CURRENT_PROTOCOL_VERSION, currently 4
        // Must be non-zero, as zero is defined as uninitialized
        // TODO: Update to version 5 for Fernando
        this.setInt(SettingsKeys.protocolVersion, 4)
    }

    fun getInt(prefName: String): Int {
        return preference.getInt(prefName, 0)
    }

    fun getIntOnOff(prefName: String): String {
        return if (getIntBool(prefName)) "on" else "off"
    }

    fun setInt(prefName: String, value: Int) {
        preference.edit().putInt(prefName, value).apply()
    }

    fun getString(prefName: String): String {
        return preference.getString(prefName, null) ?: ""
    }

    fun setString(prefName: String, value: String) {
        preference.edit().putString(prefName, value).apply()
    }

    fun getIntBool(prefName: String): Boolean {
        return this.getInt(prefName) == 1
    }

    fun setIntBool(prefName: String, value: Boolean) {
        this.setInt(prefName, if (value) 1 else 0)
    }

    fun remove(prefName: String) {
        preference.edit().remove(prefName).apply()
    }
}