// Copyright (c) 2023-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

open class MaterialPreferenceDialogFragmentCompat(private val mMaterialPreferenceDialogListener: MaterialDialogPreferenceListener) :
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