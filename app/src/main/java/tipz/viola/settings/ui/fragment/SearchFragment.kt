// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.fragment

import android.os.Bundle
import androidx.preference.Preference
import tipz.viola.R
import tipz.viola.search.SearchEngineEntries
import tipz.viola.search.SearchEngineEntries.EngineInfoType.SEARCH
import tipz.viola.search.SearchEngineEntries.EngineInfoType.SUGGESTION
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.ui.preference.ListPickerAlertDialog

class SearchFragment : ExtPreferenceFragment(R.string.pref_main_search) {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_search, rootKey)

        val searchNameList = SearchEngineEntries.getEngineNameList(SEARCH)
        val searchDisplayList = SearchEngineEntries.getEngineDisplayList(requireContext(), SEARCH)
        findPreference<Preference>(PREF_SEARCH_ENGINE)?.run {
            setOnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = it
                    nameList = searchNameList
                    displayList = searchDisplayList
                    namePreference = SettingsKeys.searchName
                    nameToIdFunction = { name -> searchNameList.indexOfFirst { it == name } }
                    stringPreference = SettingsKeys.searchCustomUrl
                    dialogTitleResId = R.string.search_engine
                    dialogCustomMessageResId = R.string.search_dialog_custom_message
                    customIndexEnabled = true
                    customIndex = searchNameList.size - 1
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
                true
            }
            summary = searchDisplayList[searchNameList.indexOfFirst {
                it == settingsPreference.getString(SettingsKeys.searchName)
            }]
        }
        val suggestionNameList = SearchEngineEntries.getEngineNameList(SUGGESTION)
        val suggestionDisplayList = SearchEngineEntries.getEngineDisplayList(requireContext(), SUGGESTION)
        findPreference<Preference>(PREF_SEARCH_SUGGESTIONS)?.run {
            setOnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = it
                    nameList = suggestionNameList
                    displayList = suggestionDisplayList
                    namePreference = SettingsKeys.suggestionsName
                    nameToIdFunction = { name -> suggestionNameList.indexOfFirst { it == name } }
                    stringPreference = SettingsKeys.suggestionsCustomUrl
                    dialogTitleResId = R.string.search_suggestions_title
                    dialogCustomMessageResId = R.string.search_dialog_custom_message
                    customIndexEnabled = true
                    customIndex = suggestionNameList.size - 1
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
                true
            }
            summary = suggestionDisplayList[suggestionNameList.indexOfFirst {
                it == settingsPreference.getString(SettingsKeys.suggestionsName)
            }]
        }
    }

    companion object {
        private const val PREF_SEARCH_ENGINE = "search_engine"
        private const val PREF_SEARCH_SUGGESTIONS = "search_suggestions"
    }
}