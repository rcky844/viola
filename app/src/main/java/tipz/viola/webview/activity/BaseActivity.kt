// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        settingsPreference = (applicationContext as Application).settingsPreference
        windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    }

    public override fun onStart() {
        super.onStart()
        doSettingsCheck()
    }

    /*
     * Settings config checker
     *
     * This function is called every time a user returns to the app, where
     * onStart() is executed. It checks all configuration items and performs
     * actions to perform the actions required. Activities that implement us
     * will need to call this function.
     */
    @CallSuper
    open fun doSettingsCheck() {
        // Dark Mode
        performThemeModeChecks(this)
    }

    companion object {
        lateinit var settingsPreference: SettingsSharedPreference
        lateinit var windowInsetsController: WindowInsetsControllerCompat

        fun performThemeModeChecks(context: Context) {
            val mode = when (settingsPreference.getInt(SettingsKeys.themeId)) {
                1 -> AppCompatDelegate.MODE_NIGHT_NO
                2 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
                        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                    else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            }
            AppCompatDelegate.setDefaultNightMode(mode)


            windowInsetsController.isAppearanceLightStatusBars = !isDarkMode(context)
            windowInsetsController.isAppearanceLightNavigationBars = !isDarkMode(context)
        }

        fun isDarkMode(context: Context): Boolean {
            return context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES
        }
    }
}