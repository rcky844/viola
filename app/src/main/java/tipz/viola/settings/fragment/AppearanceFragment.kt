// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.fragment

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import androidx.preference.Preference
import tipz.viola.R
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.activity.ThemePreference
import tipz.viola.webview.activity.BaseActivity.Companion.performThemeModeChecks

class AppearanceFragment : ExtPreferenceFragment(R.string.pref_main_appearance) {
    private lateinit var startPageWallpaper: Preference

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@registerForActivityResult
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            settingsActivity.contentResolver.takePersistableUriPermission(uri, flag)
            settingsPreference.setString(SettingsKeys.startPageWallpaper, uri.toString())
            startPageWallpaper.summary = resources.getString(
                R.string.pref_start_page_wallpaper_summary,
                DocumentFile.fromSingleUri(settingsActivity, uri)?.name
            )
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_appearance, rootKey)

        findPreference<ThemePreference>(PREF_THEME_PICKER)?.run {
            setOnPreferenceChangeListener { _, _ ->
                performThemeModeChecks(settingsActivity)
                true
            }
        }

        startPageWallpaper = findPreference(PREF_START_PAGE_WALLPAPER)!!
        startPageWallpaper.run {
            setOnPreferenceClickListener {
                if (settingsPreference.getString(SettingsKeys.startPageWallpaper).isEmpty()) {
                    pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } else {
                    startPageWallpaper.setSummary(
                        resources.getString(
                            R.string.pref_start_page_wallpaper_summary,
                            resources.getString(R.string.default_res)
                        )
                    )
                    settingsPreference.setString(SettingsKeys.startPageWallpaper, "")
                }
                true
            }
            summary = resources.getString(
                R.string.pref_start_page_wallpaper_summary,
                if (settingsPreference.getString(SettingsKeys.startPageWallpaper).isEmpty()) {
                    resources.getString(R.string.default_res)
                } else {
                    DocumentFile.fromSingleUri(
                        settingsActivity,
                        Uri.parse(settingsPreference.getString(SettingsKeys.startPageWallpaper))
                    )?.name
                }
            )
        }
    }

    companion object {
        private const val PREF_THEME_PICKER = "theme"
        private const val PREF_START_PAGE_WALLPAPER = "start_page_wallpaper"
    }
}