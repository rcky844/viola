// Copyright (c) 2023-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import tipz.viola.Application
import tipz.viola.settings.migrations.ExoticMigrations
import tipz.viola.settings.migrations.FernandoMigrations
import tipz.viola.settings.migrations.InitialMigrations

/* TODO: Migrate to something newer? */
class SettingsSharedPreference(context: Context) {
    private var preference: SharedPreferences

    init {
        preference =
            context.getSharedPreferences(CONFIG_DATA_STORE_NAME, Activity.MODE_PRIVATE)!!
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
        preference.edit { putInt(prefName, value) }
    }

    fun getString(prefName: String): String {
        return preference.getString(prefName, null) ?: ""
    }

    fun setString(prefName: String, value: String) {
        preference.edit { putString(prefName, value) }
    }

    fun getIntBool(prefName: String): Boolean {
        return this.getInt(prefName) == 1
    }

    fun setIntBool(prefName: String, value: Boolean) {
        this.setInt(prefName, if (value) 1 else 0)
    }

    fun remove(prefName: String) {
        preference.edit { remove(prefName) }
    }

    companion object {
        lateinit var instance: SettingsSharedPreference
        private const val CONFIG_DATA_STORE_NAME = "config" /* Pref file name */
    }
}