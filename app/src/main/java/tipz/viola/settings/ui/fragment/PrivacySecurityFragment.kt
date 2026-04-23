// Copyright (c) 2022-2026 Tipz Team
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
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.ui.preference.MaterialDialogPreference
import tipz.viola.settings.ui.preference.MaterialPreferenceDialogFragmentCompat.MaterialDialogPreferenceListener
import tipz.viola.webview.VWebStorage
import java.io.IOException

class PrivacySecurityFragment : ExtPreferenceFragment(R.string.pref_main_privacy_security) {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_privacy_security, rootKey)

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
        private const val PREF_AD_BLOCKER_DOWNLOAD = "adBlockerDownload"

        private const val PREF_CLEAR_BROWSING_DATA = "clear_browsing_data"
        private const val PREF_RESET_TO_DEFAULT = "reset_to_default"
    }
}