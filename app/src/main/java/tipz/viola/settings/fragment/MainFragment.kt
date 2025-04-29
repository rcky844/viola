// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.fragment

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.build.info.BuildInfoActivity
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.databinding.DialogEdittextBinding
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.UpdateService

class MainFragment : ExtPreferenceFragment(R.string.settings_title) {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_main, rootKey)

        findPreference<Preference>(PREF_SCREEN_SEARCH)?.setOnPreferenceClickListener {
            settingsActivity.openScreen(R.xml.preference_settings_search)
            true
        }
        findPreference<Preference>(PREF_SCREEN_PRIVACY_SECURITY)?.setOnPreferenceClickListener {
            settingsActivity.openScreen(R.xml.preference_settings_privacy_security)
            true
        }
        findPreference<Preference>(PREF_SCREEN_APPEARANCE)?.setOnPreferenceClickListener {
            settingsActivity.openScreen(R.xml.preference_settings_appearance)
            true
        }

        findPreference<Preference>(PREF_CHECK_FOR_UPDATES)?.setOnPreferenceClickListener {
            UpdateService(settingsActivity, false).checkUpdates()
            true
        }

        // TODO: Load update channels from online JSON
        findPreference<Preference>(PREF_UPDATE_CHANNEL)?.run {
            setOnPreferenceClickListener {
                val binding: DialogEdittextBinding = DialogEdittextBinding.inflate(layoutInflater)
                val view = binding.root

                val editText = binding.edittext
                editText.setText(settingsPreference.getString(SettingsKeys.updateChannelName))

                MaterialAlertDialogBuilder(settingsActivity)
                    .setTitle(R.string.pref_update_channel_title)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        settingsPreference.setString(
                            SettingsKeys.updateChannelName,
                            editText.text.toString().trim()
                        )
                        it.summary = editText.text.toString().trim()
                            .ifEmpty { BuildConfig.VERSION_BUILD_TYPE }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show()
                true
            }
            summary = settingsPreference.getString(SettingsKeys.updateChannelName)
                .ifEmpty { BuildConfig.VERSION_BUILD_TYPE }
        }

        findPreference<Preference>(PREF_ABOUT)?.run {
            setOnPreferenceClickListener {
                val intent = Intent(context, BuildInfoActivity::class.java)
                getNeedLoadFromNonMain.launch(intent)
                true
            }
            summary = resources.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME
        }
    }

    companion object {
        private const val PREF_SCREEN_SEARCH = "search"
        private const val PREF_SCREEN_PRIVACY_SECURITY = "privacy_security"
        private const val PREF_SCREEN_APPEARANCE = "appearance"

        private const val PREF_CHECK_FOR_UPDATES = "check_for_updates"
        private const val PREF_UPDATE_CHANNEL = "update_channel"
        private const val PREF_ABOUT = "about"
    }
}