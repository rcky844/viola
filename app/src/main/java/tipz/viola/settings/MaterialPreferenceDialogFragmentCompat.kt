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

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MaterialPreferenceDialogFragmentCompat(private val mMaterialPreferenceDialogListener: MaterialDialogPreferenceListener) :
    PreferenceDialogFragmentCompat() {
    /** Which button was clicked.  */
    private var mWhichButtonClicked = 0
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mWhichButtonClicked = DialogInterface.BUTTON_NEGATIVE
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(preference.dialogTitle)
            .setIcon(preference.dialogIcon)
            .setPositiveButton(preference.positiveButtonText, this)
            .setNegativeButton(preference.negativeButtonText, this)
        val contentView = onCreateDialogView(requireContext())
        if (contentView != null) {
            onBindDialogView(contentView)
            builder.setView(contentView)
        } else {
            builder.setMessage(preference.dialogMessage)
        }
        onPrepareDialogBuilder(builder)
        return builder.create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        mWhichButtonClicked = which
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDialogClosed(mWhichButtonClicked == DialogInterface.BUTTON_POSITIVE)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        mMaterialPreferenceDialogListener.onDialogClosed(positiveResult)
    }

    interface MaterialDialogPreferenceListener {
        fun onDialogClosed(positiveResult: Boolean)
    }

    companion object {
        @JvmStatic
        fun newInstance(
            key: String?,
            materialPreferenceDialogListener: MaterialDialogPreferenceListener
        ): MaterialPreferenceDialogFragmentCompat {
            val fragment = MaterialPreferenceDialogFragmentCompat(materialPreferenceDialogListener)
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }
}