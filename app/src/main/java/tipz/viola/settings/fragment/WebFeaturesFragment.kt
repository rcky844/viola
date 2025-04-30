// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.fragment

import android.os.Bundle
import tipz.viola.R

class WebFeaturesFragment : ExtPreferenceFragment(R.string.pref_main_web_features) {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_web_features, rootKey)
    }
}