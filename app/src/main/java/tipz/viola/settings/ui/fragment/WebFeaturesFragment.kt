// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.fragment

import android.os.Bundle
import tipz.viola.R
import tipz.viola.settings.ui.preference.WebXApiPickerPreference

class WebFeaturesFragment : ExtPreferenceFragment(R.string.pref_main_web_features) {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_web_features, rootKey)
    }

    override fun onStart() {
        super.onStart()
        findPreference<WebXApiPickerPreference>(PREF_WEBX_API_PICKER)?.setUrlSummary()
    }

    companion object {
        private const val PREF_WEBX_API_PICKER = "webx_api"
    }
}