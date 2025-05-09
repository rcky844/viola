// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.fragment

import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity.ACTIVITY_SERVICE
import androidx.preference.Preference
import tipz.viola.R
import tipz.viola.ext.showMessage
import tipz.viola.search.SearchEngineEntries
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.ui.preference.MaterialPreferenceDialogFragmentCompat.MaterialDialogPreferenceListener
import tipz.viola.settings.ui.preference.ListPickerAlertDialog
import tipz.viola.settings.ui.preference.MaterialDialogPreference
import tipz.viola.webview.VWebStorage
import java.io.IOException

class PrivacySecurityFragment : ExtPreferenceFragment(R.string.pref_main_privacy_security) {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_privacy_security, rootKey)


        val adBlockerHostsEntries =
            settingsActivity.resources.getStringArray(R.array.ad_blocker_hosts_entries)
        findPreference<Preference>(PREF_AD_BLOCKER_SOURCE)?.run {
            setOnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = it
                    nameList = adBlockerHostsEntries
                    idPreference = SettingsKeys.adServerId
                    stringPreference = SettingsKeys.adServerUrl
                    dialogTitleResId = R.string.pref_ad_blocker_source_title
                    customIndexEnabled = true
                    customIndex = SearchEngineEntries.customIndex
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
                true
            }
            summary = adBlockerHostsEntries[settingsPreference.getInt(SettingsKeys.adServerId)]
        }

        findPreference<Preference>(PREF_AD_BLOCKER_DOWNLOAD)?.setOnPreferenceClickListener {
            val intent = Intent()
            intent.putExtra(SettingsKeys.updateAdServers, 1)
            settingsActivity.setResult(0, intent)
            settingsActivity.finish()
            true
        }

        findPreference<MaterialDialogPreference>(PREF_CLEAR_BROWSING_DATA)?.materialDialogPreferenceListener =
            object : MaterialDialogPreferenceListener {
                override fun onDialogClosed(positiveResult: Boolean) {
                    if (!positiveResult) return
                    VWebStorage.deleteBrowsingData {
                        settingsActivity.showMessage(R.string.toast_cleared)
                    }
                }
            }

        findPreference<MaterialDialogPreference>(PREF_RESET_TO_DEFAULT)?.materialDialogPreferenceListener =
            object : MaterialDialogPreferenceListener {
                override fun onDialogClosed(positiveResult: Boolean) {
                    if (!positiveResult) return
                    settingsActivity.showMessage(R.string.toast_reset_complete)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        (settingsActivity.getSystemService(ACTIVITY_SERVICE)
                                as ActivityManager).clearApplicationUserData()
                    } else {
                        val packageName = settingsActivity.packageName
                        val runtime = Runtime.getRuntime()
                        try {
                            runtime.exec("pm clear $packageName")
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
    }

    companion object {
        private const val PREF_AD_BLOCKER_SOURCE = "adBlockerSource"
        private const val PREF_AD_BLOCKER_DOWNLOAD = "adBlockerDownload"

        private const val PREF_CLEAR_BROWSING_DATA = "clear_browsing_data"
        private const val PREF_RESET_TO_DEFAULT = "reset_to_default"
    }
}