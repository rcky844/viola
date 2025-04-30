// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.fragment

import android.os.Bundle
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.settings.activity.MaterialSwitchPreference

class DevelopmentFragment : ExtPreferenceFragment(R.string.pref_main_development_title) {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_development, rootKey)

        findPreference<MaterialSwitchPreference>(PREF_REMOTE_DEBUGGING)?.run {
            if (BuildConfig.DEBUG) {
                setSummary(R.string.pref_remote_debugging_summary_debuggable)
                summaryOn = null
                summaryOff = null
                isEnabled = false
            }
        }
    }

    companion object {
        private const val PREF_REMOTE_DEBUGGING = "remote_debugging"
    }
}