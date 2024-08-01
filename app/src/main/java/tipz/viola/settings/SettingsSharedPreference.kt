/*
 * Copyright (c) 2023-2024 Tipz Team
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
import tipz.viola.Application
import tipz.viola.settings.migrations.ExoticMigrations
import tipz.viola.settings.migrations.InitialMigrations
import tipz.viola.utils.CommonUtils

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

        // Sets CURRENT_PROTOCOL_VERSION, currently 2
        // Must be non-zero, as zero is defined as uninitialized
        this.setInt(SettingsKeys.protocolVersion, 2)
    }

    fun getInt(prefName: String): Int {
        return preference.getInt(prefName, 0)
    }

    fun setInt(prefName: String, value: Int) {
        preference.edit().putInt(prefName, value).apply()
    }

    fun getString(prefName: String): String {
        return preference.getString(prefName, null) ?: CommonUtils.EMPTY_STRING
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