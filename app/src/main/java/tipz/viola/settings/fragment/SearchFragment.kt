// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.fragment

import android.os.Bundle
import androidx.preference.Preference
import tipz.viola.R
import tipz.viola.search.SearchEngineEntries
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.activity.ListPickerAlertDialog

class SearchFragment : ExtPreferenceFragment(R.string.pref_main_search) {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_search, rootKey)

        val searchHomePageList = resources.getStringArray(R.array.search_entries)
        findPreference<Preference>(PREF_SEARCH_ENGINE)?.run {
            setOnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = it
                    nameList = searchHomePageList
                    namePreference = SettingsKeys.searchName
                    nameToIdFunction = SearchEngineEntries::getIndexByName
                    idToNameFunction = SearchEngineEntries::getNameByIndex
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
                    nameList = searchHomePageList
                    namePreference = SettingsKeys.suggestionsName
                    nameToIdFunction = SearchEngineEntries::getIndexByName
                    idToNameFunction = SearchEngineEntries::getNameByIndex
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