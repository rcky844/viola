/*
 * Copyright (c) 2023-2024 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tipz.viola.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatEditText
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.R
import tipz.viola.search.SearchEngineEntries
import tipz.viola.utils.CommonUtils

class ListPickerAlertDialog(context: Context, settingsPreference: SettingsSharedPreference) :
    MaterialAlertDialogBuilder(context) {
    private var mListPickerObject: ListPickerObject = ListPickerObject()
    private var mSettingsPreference: SettingsSharedPreference = settingsPreference

    fun getListPickerObject(): ListPickerObject {
        return mListPickerObject
    }

    fun setupDialogForShowing() {
        val useNamePreference = mListPickerObject.namePreference != CommonUtils.EMPTY_STRING

        // Set checked item to current settings
        var checkedItem = if (useNamePreference) {
            mListPickerObject.nameToIdFunction(
                mSettingsPreference.getString(mListPickerObject.namePreference))
        } else {
            mSettingsPreference.getInt(mListPickerObject.idPreference)
        }

        setTitle(mListPickerObject.dialogTitle)
        setSingleChoiceItems(
            mListPickerObject.nameList, checkedItem
        ) { _: DialogInterface?, which: Int -> checkedItem = which }
        setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
            if (checkedItem == mListPickerObject.customIndex) {
                val layoutInflater = LayoutInflater.from(context)
                @SuppressLint("InflateParams") val root =
                    layoutInflater.inflate(R.layout.dialog_edittext, null)
                val customInput =
                    root.findViewById<AppCompatEditText>(R.id.edittext)
                MaterialAlertDialogBuilder(context) // TODO: Improve implementation?
                    .setTitle(mListPickerObject.dialogTitle)
                    .setMessage(mListPickerObject.dialogCustomMessage)
                    .setView(root)
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        if (customInput.text.toString().isNotEmpty()) {
                            mSettingsPreference.setString(
                                mListPickerObject.stringPreference,
                                customInput.text.toString()
                            )
                            if (useNamePreference) {
                                mSettingsPreference.setString(
                                    mListPickerObject.namePreference,
                                    SearchEngineEntries.getNameByIndex(checkedItem)
                                )
                            } else {
                                mSettingsPreference.setInt(
                                    mListPickerObject.idPreference,
                                    checkedItem
                                )
                            }

                            mListPickerObject.preference?.summary =
                                mListPickerObject.nameList!![checkedItem]
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show()
            }
            if (checkedItem != mListPickerObject.customIndex) {
                mSettingsPreference.setString(
                    mListPickerObject.stringPreference,
                    CommonUtils.EMPTY_STRING
                )
                if (useNamePreference) {
                    mSettingsPreference.setString(
                        mListPickerObject.namePreference,
                        SearchEngineEntries.getNameByIndex(checkedItem)
                    )
                } else {
                    mSettingsPreference.setInt(
                        mListPickerObject.idPreference,
                        checkedItem
                    )
                }

                mListPickerObject.preference?.summary = mListPickerObject.nameList!![checkedItem]
            }
        }
        setNegativeButton(android.R.string.cancel, null)
    }

    class ListPickerObject {
        var preference: Preference? = null // Preference for this dialog
        var nameList: Array<String>? = null // Array list consisting of names of options
        var idPreference = CommonUtils.EMPTY_STRING // Preference key for storing IDs
        var namePreference = CommonUtils.EMPTY_STRING // Preference key for storing names
        var nameToIdFunction : (name: String) -> Int = this::stubNameToIdFunction
        var stringPreference = CommonUtils.EMPTY_STRING // Preference key for storing strings
        var dialogTitle = CommonUtils.EMPTY_STRING // Dialog title
        var dialogCustomMessage = CommonUtils.EMPTY_STRING // Message for custom dialog
        var customIndex = 0 // Custom item index

        fun stubNameToIdFunction(name: String) : Int {
            Log.w(LOG_TAG, "stubNameToIdFunction(): " +
                    "$name using namePreference without any means to convert to index!")
            return 0
        }
    }

    companion object {
        private var LOG_TAG = "ListPickerActivity"
    }
}