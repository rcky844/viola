// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.preference.Preference
import tipz.build.info.BuildInfo
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.ui.preference.ListPickerAlertDialog
import tipz.viola.settings.ui.preference.MaterialSwitchPreference
import tipz.viola.utils.UpdateService
import java.text.SimpleDateFormat
import java.util.Date

class DevelopmentFragment : ExtPreferenceFragment(R.string.pref_main_development_title) {
    @SuppressLint("SimpleDateFormat")
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
            val identifier = mutableListOf<String>()
            val display = mutableListOf<String>()
            UpdateService(settingsActivity, false)
                .getAvailableUpdateChannels().forEach {
                    identifier.add(it.identifier)
                    display.add(
                        if (it.displayName.isEmpty()) it.identifier
                        else "${it.displayName} (${it.identifier})"
                    )
                }

            setOnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = it
                    nameList = identifier.toTypedArray()
                    displayList = display.toTypedArray()
                    namePreference = SettingsKeys.updateChannelName
                    nameToIdFunction = { name ->
                        identifier.indexOfFirst { i ->
                            name.takeUnless { it.isEmpty() }?.let { i == it }
                                ?: (i == BuildConfig.VERSION_BUILD_TYPE)
                        }
                    }
                    dialogTitleResId = R.string.pref_update_channel_title
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .setNeutralButton(R.string.reset) { _, _ ->
                        settingsPreference.setString(
                            SettingsKeys.updateChannelName, BuildConfig.VERSION_BUILD_TYPE)
                        setSummary(BuildConfig.VERSION_BUILD_TYPE)
                    }
                    .create().show()
                true
            }
            summary = settingsPreference.getString(SettingsKeys.updateChannelName)
                .ifEmpty { BuildConfig.VERSION_BUILD_TYPE }
        }

        findPreference<Preference>(PREF_BUILD_INFO)?.setSummary(
            BuildInfo().run {
                resources.getString(R.string.buildinfo_pref_build_info_summary,
                    getProductBuildTag() ?: "",
                        productBuildBranch, productBuildGitRevision,
                    SimpleDateFormat("E MMM dd HH:mm:ss z yyyy").format(Date(productBuildTimestamp))
                )
            }
        )

        findPreference<Preference>(PREF_PROTOCOL_INFO)?.setSummary(
            settingsPreference.run {
                resources.getString(R.string.buildinfo_pref_protocol_info_summary,
                    getInt(SettingsKeys.protocolVersion),
                    getInt(SettingsKeys.favApi),
                    getInt(SettingsKeys.historyApi)
                )
            }
        )
    }

    companion object {
        private const val PREF_REMOTE_DEBUGGING = "remote_debugging"
        private const val PREF_UPDATE_CHANNEL = "update_channel"
        private const val PREF_BUILD_INFO = "build_info"
        private const val PREF_PROTOCOL_INFO = "protocol_info"
    }
}