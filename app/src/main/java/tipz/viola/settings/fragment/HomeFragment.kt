// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.fragment

import android.os.Bundle
import androidx.preference.Preference
import tipz.viola.R
import tipz.viola.search.SearchEngineEntries
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.activity.ListPickerAlertDialog

class HomeFragment : ExtPreferenceFragment(R.string.pref_main_home) {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_home, rootKey)

        val searchHomePageList = SearchEngineEntries.getEngineDisplayList(requireContext())
        findPreference<Preference>(PREF_HOMEPAGE)?.run {
            setOnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = it
                    nameList = SearchEngineEntries.getEngineNameList()
                    displayList = searchHomePageList
                    namePreference = SettingsKeys.homePageName
                    nameToIdFunction = SearchEngineEntries::getIndexByName
                    stringPreference = SettingsKeys.homePageCustomUrl
                    dialogTitleResId = R.string.homepage
                    customIndexEnabled = true
                    customIndex = SearchEngineEntries.customIndex
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
                true
            }
            summary = searchHomePageList[SearchEngineEntries.getIndexByName(
                settingsPreference.getString(SettingsKeys.homePageName)
            )]
        }
    }

    companion object {
        private const val PREF_HOMEPAGE = "homepage"
    }
}