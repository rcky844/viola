// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import tipz.viola.ext.isDarkMode
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference


open class BaseActivity : AppCompatActivity() {
    val settingsPreference = SettingsSharedPreference.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
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
        lateinit var windowInsetsController: WindowInsetsControllerCompat

        fun performThemeModeChecks(context: Context) {
            val mode = when (SettingsSharedPreference.instance.getInt(SettingsKeys.themeId)) {
                1 -> AppCompatDelegate.MODE_NIGHT_NO
                2 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
                        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                    else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            }
            AppCompatDelegate.setDefaultNightMode(mode)


            windowInsetsController.isAppearanceLightStatusBars = !context.isDarkMode()
            windowInsetsController.isAppearanceLightNavigationBars = !context.isDarkMode()
        }
    }
}