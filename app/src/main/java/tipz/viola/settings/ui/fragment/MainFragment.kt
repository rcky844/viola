// Copyright (c) 2025-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.preference.Preference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.build.info.BuildInfoActivity
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.ext.showMessage
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.ui.preference.SummaryOnOffPreference
import tipz.viola.utils.UpdateService
import tipz.viola.utils.UpdateService.Status

class MainFragment : ExtPreferenceFragment(R.string.settings_title) {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_main, rootKey)

        findPreference<SummaryOnOffPreference>(PREF_SCREEN_HOME)?.setOnPreferenceClickListener {
            settingsActivity.openScreen(R.xml.preference_settings_home)
            true
        }
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

        findPreference<Preference>(PREF_SCREEN_DOWNLOADS)?.setOnPreferenceClickListener {
            settingsActivity.openScreen(R.xml.preference_settings_downloads)
            true
        }
        findPreference<Preference>(PREF_SCREEN_WEB_FEATURES)?.setOnPreferenceClickListener {
            settingsActivity.openScreen(R.xml.preference_settings_web_features)
            true
        }
        findPreference<Preference>(PREF_SCREEN_DEVELOPMENT)?.setOnPreferenceClickListener {
            settingsActivity.openScreen(R.xml.preference_settings_development)
            true
        }

        findPreference<Preference>(PREF_CHECK_FOR_UPDATES)?.setOnPreferenceClickListener {
            val service = UpdateService(settingsActivity)
            val status = MutableLiveData<Status>()

            status.observe(settingsActivity, Observer {
                // Handle all other cases
                if (it != Status.SUCCESS) {
                    settingsActivity.showMessage(
                        when (it) {
                            Status.LATEST_VERSION -> R.string.toast_version_latest
                            Status.NO_NETWORK -> R.string.toast_network_unavailable
                            else -> R.string.update_download_failed
                        }
                    )
                    return@Observer
                }
            })

            // Start fetching update
            CoroutineScope(Dispatchers.IO).launch {
                status.postValue(service.fetchUpdate())
            }

            true
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

    override fun onStart() {
        super.onStart()
        findPreference<SummaryOnOffPreference>(PREF_SCREEN_HOME)?.setOnOffSummary()
        findPreference<SummaryOnOffPreference>(PREF_SCREEN_DOWNLOADS)?.setOnOffSummary()

        // Perform checks for update channel
        val identifier = mutableListOf<String>()
        UpdateService(settingsActivity)
            .getAvailableUpdateChannels().first?.forEach {
                identifier.add(it.identifier)
            }

        findPreference<Preference>(PREF_CHECK_FOR_UPDATES)?.isEnabled =
            identifier.any { it == settingsPreference.getString(SettingsKeys.updateChannelName)
                    || it == BuildConfig.VERSION_BUILD_TYPE }
    }

    companion object {
        private const val PREF_SCREEN_HOME = "home"
        private const val PREF_SCREEN_SEARCH = "search"
        private const val PREF_SCREEN_PRIVACY_SECURITY = "privacy_security"
        private const val PREF_SCREEN_APPEARANCE = "appearance"

        private const val PREF_SCREEN_DOWNLOADS = "downloads"
        private const val PREF_SCREEN_WEB_FEATURES = "web_features"
        private const val PREF_SCREEN_DEVELOPMENT = "development"

        private const val PREF_CHECK_FOR_UPDATES = "check_for_updates"
        private const val PREF_ABOUT = "about"
    }
}