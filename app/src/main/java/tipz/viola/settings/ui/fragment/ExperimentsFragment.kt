// Copyright (c) 2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.fragment

import android.os.Bundle
import tipz.viola.R

class ExperimentsFragment : ExtPreferenceFragment(R.string.pref_experiments_title) {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_experiments, rootKey)
    }
}