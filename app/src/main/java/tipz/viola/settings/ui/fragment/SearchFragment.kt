// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.fragment

import android.os.Bundle
import androidx.preference.Preference
import tipz.viola.R
import tipz.viola.search.SearchEngineEntries
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.ui.preference.ListPickerAlertDialog

class SearchFragment : ExtPreferenceFragment(R.string.pref_main_search) {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_search, rootKey)

        val searchHomePageList = SearchEngineEntries.getEngineDisplayList(requireContext())
        findPreference<Preference>(PREF_SEARCH_ENGINE)?.run {
            setOnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = it
                    nameList = SearchEngineEntries.getEngineNameList()
                    displayList = searchHomePageList
                    namePreference = SettingsKeys.searchName
                    nameToIdFunction = SearchEngineEntries::getIndexByName
                    stringPreference = SettingsKeys.searchCustomUrl
                    dialogTitleResId = R.string.search_engine
                    dialogCustomMessageResId = R.string.search_dialog_custom_message
                    customIndexEnabled = true
                    customIndex = SearchEngineEntries.customIndex
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
                true
            }
            summary = searchHomePageList[SearchEngineEntries.getIndexByName(
                settingsPreference.getString(SettingsKeys.searchName)
            )]
        }

        findPreference<Preference>(PREF_SEARCH_SUGGESTIONS)?.run {
            setOnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = it
                    nameList = SearchEngineEntries.getEngineNameList()
                    displayList = searchHomePageList
                    namePreference = SettingsKeys.suggestionsName
                    nameToIdFunction = SearchEngineEntries::getIndexByName
                    stringPreference = SettingsKeys.suggestionsCustomUrl
                    dialogTitleResId = R.string.search_suggestions_title
                    dialogCustomMessageResId = R.string.search_dialog_custom_message
                    customIndexEnabled = true
                    customIndex = SearchEngineEntries.customIndex
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
                true
            }
            summary = searchHomePageList[SearchEngineEntries.getIndexByName(
                settingsPreference.getString(SettingsKeys.suggestionsName)
            )]
        }
    }

    companion object {
        private const val PREF_SEARCH_ENGINE = "search_engine"
        private const val PREF_SEARCH_SUGGESTIONS = "search_suggestions"
    }
}