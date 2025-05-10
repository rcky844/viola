// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.fragment

import android.os.Bundle
import tipz.viola.R
import tipz.viola.settings.ui.preference.DownloadLocationPickerPreference

class DownloadsFragment : ExtPreferenceFragment(R.string.pref_main_downloads) {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_downloads, rootKey)
    }

    override fun onStart() {
        super.onStart()
        findPreference<DownloadLocationPickerPreference>(PREF_DOWNLOAD_LOCATION_PICKER)?.setPathSummary()
    }

    companion object {
        private const val PREF_DOWNLOAD_LOCATION_PICKER = "download_location_default"
    }
}