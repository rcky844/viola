// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.fragment

import android.os.Bundle
import androidx.preference.Preference
import tipz.build.info.BuildInfo
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.activity.ListPickerAlertDialog
import tipz.viola.settings.activity.MaterialSwitchPreference
import tipz.viola.utils.UpdateService

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

        findPreference<Preference>(PREF_UPDATE_CHANNEL)?.run {
            val availableUpdateChannels = UpdateService(settingsActivity, false)
                .getAvailableUpdateChannels().toTypedArray()

            setOnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = it
                    nameList = availableUpdateChannels
                    namePreference = SettingsKeys.updateChannelName
                    nameToIdFunction = { name ->
                        availableUpdateChannels.indexOfFirst { i ->
                            name.takeUnless { it.isEmpty() }?.let { i == it }
                                ?: (i == BuildConfig.VERSION_BUILD_TYPE)
                        }
                    }
                    dialogTitleResId = R.string.pref_update_channel_title
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
                true
            }
            summary = settingsPreference.getString(SettingsKeys.updateChannelName)
                .ifEmpty { BuildConfig.VERSION_BUILD_TYPE }
        }

        findPreference<Preference>(PREF_BUILD_NUMBER)?.setSummary(
            BuildInfo().getProductBuildTag() ?: ""
        )
    }

    companion object {
        private const val PREF_REMOTE_DEBUGGING = "remote_debugging"
        private const val PREF_UPDATE_CHANNEL = "update_channel"
        private const val PREF_BUILD_NUMBER = "build_number"
    }
}