// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import tipz.viola.R
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.ui.preference.ThemePreference
import tipz.viola.settings.ui.preference.WallpaperPreference
import tipz.viola.webview.activity.BaseActivity.Companion.performThemeModeChecks

class AppearanceFragment : ExtPreferenceFragment(R.string.pref_main_appearance) {
    private lateinit var startPageWallpaper: WallpaperPreference

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@registerForActivityResult
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            settingsActivity.contentResolver.takePersistableUriPermission(uri, flag)
            settingsPreference.setString(SettingsKeys.startPageWallpaper, uri.toString())
            startPageWallpaper.setWallpaperPreview(uri)
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_appearance, rootKey)

        findPreference<ThemePreference>(PREF_THEME_PICKER)?.run {
            setOnPreferenceChangeListener { _, _ ->
                performThemeModeChecks(settingsActivity)
                true
            }
        }

        startPageWallpaper = findPreference<WallpaperPreference>(PREF_START_PAGE_WALLPAPER)!!
        startPageWallpaper.run {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return@run

            setOnPreferenceChangeListener { _, _ ->
                if (settingsPreference.getString(SettingsKeys.startPageWallpaper).isEmpty()) {
                    pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } else {
                    // TODO: Add reset button & remove
                    settingsPreference.setString(SettingsKeys.startPageWallpaper, "")
                    setWallpaperPreview()
                }
                true
            }
        }
    }

    companion object {
        private const val PREF_THEME_PICKER = "theme"
        private const val PREF_START_PAGE_WALLPAPER = "start_page_wallpaper"
    }
}