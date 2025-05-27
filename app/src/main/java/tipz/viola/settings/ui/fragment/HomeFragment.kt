// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.fragment

import android.os.Bundle
import androidx.preference.Preference
import tipz.viola.R
import tipz.viola.search.SearchEngineEntries
import tipz.viola.search.SearchEngineEntries.EngineInfoType.HOMEPAGE
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.ui.preference.ListPickerAlertDialog

class HomeFragment : ExtPreferenceFragment(R.string.pref_main_home) {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_home, rootKey)

        val homePageNameList = SearchEngineEntries.getEngineNameList(HOMEPAGE)
        val homePageDisplayList = SearchEngineEntries.getEngineDisplayList(requireContext(), HOMEPAGE)
        findPreference<Preference>(PREF_HOMEPAGE)?.run {
            setOnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = it
                    nameList = homePageNameList
                    displayList = homePageDisplayList
                    namePreference = SettingsKeys.homePageName
                    nameToIdFunction = { name -> homePageNameList.indexOfFirst { it == name } }
                    stringPreference = SettingsKeys.homePageCustomUrl
                    dialogTitleResId = R.string.homepage
                    customIndexEnabled = true
                    customIndex = homePageNameList.size - 1
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
                true
            }
            summary = homePageDisplayList[homePageNameList.indexOfFirst {
                it == settingsPreference.getString(SettingsKeys.homePageName)
            }]
        }
    }

    companion object {
        private const val PREF_HOMEPAGE = "homepage"
    }
}