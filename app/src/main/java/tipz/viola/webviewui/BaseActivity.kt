/*
 * Copyright (c) 2022-2024 Tipz Team
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
package tipz.viola.webviewui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import tipz.viola.Application
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference


open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsPreference = (applicationContext as Application).settingsPreference
        windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    }

    public override fun onStart() {
        super.onStart()
        doSettingsCheck()
    }

    @CallSuper
    open fun doSettingsCheck() {
        // Dark Mode
        darkModeCheck(this)
    }

    companion object {
        var settingsPreference: SettingsSharedPreference? = null
        var windowInsetsController: WindowInsetsControllerCompat? = null

        fun darkModeCheck(context: Context) {
            // Dark mode
            if (settingsPreference!!.getInt(SettingsKeys.themeId) == 0) AppCompatDelegate.setDefaultNightMode(
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else AppCompatDelegate.setDefaultNightMode(
                if (settingsPreference!!.getInt(SettingsKeys.themeId) == 2) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )

            windowInsetsController!!.isAppearanceLightStatusBars = !getDarkMode(context)
            windowInsetsController!!.isAppearanceLightNavigationBars = !getDarkMode(context)
        }

        fun getDarkMode(context: Context): Boolean {
            return context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES
        }
    }
}