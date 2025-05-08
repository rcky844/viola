// Copyright (c) 2023-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.activity

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.databinding.DialogEdittextBinding
import tipz.viola.settings.SettingsSharedPreference

class ListPickerAlertDialog(context: Context, settingsPreference: SettingsSharedPreference,
                            private val listPickerObject: ListPickerObject
) :
    MaterialAlertDialogBuilder(context) {
    private var settingsPreference: SettingsSharedPreference = settingsPreference

    init {
        listPickerObject.apply {
            val useNamePreference = namePreference != ""

            // Set checked item to current settings
            var checkedItem = getCheckedItem(this@ListPickerAlertDialog.settingsPreference)

            if (dialogTitleResId != 0) setTitle(dialogTitleResId)
            else setTitle(dialogTitle)
            setSingleChoiceItems(
                displayList.takeUnless { it == null } ?: nameList, checkedItem
            ) { _: DialogInterface?, which: Int -> checkedItem = which }
            setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                if (customIndexEnabled && checkedItem == customIndex) createCustomDialog(checkedItem)
                else {
                    if (!stringPreference.isNullOrBlank())
                        this@ListPickerAlertDialog.settingsPreference.setString(stringPreference!!, "")

                    if (useNamePreference) {
                        this@ListPickerAlertDialog.settingsPreference.setString(namePreference, nameList!![checkedItem])
                    } else {
                        this@ListPickerAlertDialog.settingsPreference.setInt(idPreference, checkedItem)
                    }

                    preference?.summary = (displayList ?: nameList)!![checkedItem]
                }
                dialogPositivePressed()
            }
            setNegativeButton(android.R.string.cancel, null)
        }
    }

    private fun createCustomDialog(checkedItem: Int) {
        val binding: DialogEdittextBinding =
            DialogEdittextBinding.inflate(LayoutInflater.from(context))
        val view = binding.root
        val customInput = binding.edittext

        // TODO: Improve implementation?
        val dialog = MaterialAlertDialogBuilder(context)
        listPickerObject.apply {
            if (dialogTitleResId != 0)
                dialog.setTitle(dialogTitleResId)
            else dialog.setTitle(dialogTitle)

            if (dialogCustomMessageResId == 0) {
                if (!dialogCustomMessage.isNullOrBlank())
                    dialog.setMessage(dialogCustomMessage)
            } else dialog.setMessage(dialogCustomMessageResId)

            dialog.setView(view)
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    if (customInput.text?.trim().toString().isNotEmpty()) {
                        if (!stringPreference.isNullOrBlank())
                            settingsPreference.setString(stringPreference!!,
                                customInput.text.toString())
                        if (getUseNamePreference()) {
                            settingsPreference.setString(namePreference, nameList!![checkedItem])
                        } else {
                            settingsPreference.setInt(idPreference, checkedItem)
                        }

                        preference?.summary = (displayList ?: nameList)!![checkedItem]
                    }
                    dialogPositivePressed()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
        }
    }

    class ListPickerObject {
        var preference: Preference? = null // Preference for this dialog
        var nameList: Array<String>? = null // Array list consisting of names of options
        var displayList: Array<String>? = null // Array list consisting of display names of options
        var idPreference = "" // Preference key for storing IDs
        var namePreference = "" // Preference key for storing names
        var nameToIdFunction: (name: String) -> Int = this::stubNameToIdFunction
        var stringPreference: String? = null // Preference key for storing strings
        var dialogTitle: String? = null // Dialog title
        var dialogTitleResId = 0 // Dialog title resource ID
        var dialogCustomMessage: String? = null // Message for custom dialog
        var dialogCustomMessageResId = 0 // Resource ID of message for custom dialog
        var dialogPositivePressed: () -> Unit = { } // Ran when a positive button is pressed
        var customIndexEnabled = false // Uses custom item index
        var customIndex = 0 // Custom item index

        fun getUseNamePreference() = namePreference != ""
        fun getCheckedItem(pref: SettingsSharedPreference) = if (getUseNamePreference()) {
            nameToIdFunction(pref.getString(namePreference))
        } else {
            pref.getInt(idPreference)
        }

        fun stubNameToIdFunction(name: String) : Int {
            Log.w(
                LOG_TAG, "stubNameToIdFunction(): " +
                    "$name using namePreference without any means to convert to index!")
            return 0
        }
    }

    companion object {
        private var LOG_TAG = "ListPickerActivity"
    }
}