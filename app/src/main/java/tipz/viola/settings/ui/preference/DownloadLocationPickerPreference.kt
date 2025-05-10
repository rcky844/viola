// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.preference

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.jetbrains.annotations.MustBeInvokedByOverriders
import tipz.viola.R
import tipz.viola.databinding.DialogDownloadLocationPickerBinding
import tipz.viola.download.DownloadClient
import tipz.viola.ext.getFrameworkIdentifier
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import java.io.File

class DownloadLocationPickerPreference(
    private val context: Context,
    attrs: AttributeSet
) : Preference(context, attrs) {
    private val settingsPreference = SettingsSharedPreference(context)
    private fun getPath() = settingsPreference.getString(SettingsKeys.downloadLocationDefault)

    init {
        setTitle(R.string.download_location)
        setPathSummary()
        setOnPreferenceClickListener {
            createPickerDialog()
            true
        }
    }

    @MustBeInvokedByOverriders
    fun setPathSummary() {
        setSummary(getPath())
    }

    private fun createPickerDialog() {
        val binding = DialogDownloadLocationPickerBinding.inflate(LayoutInflater.from(context))
        val textView = binding.pathEditText.apply {
            setText(getPath())
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.download_location)
            .setView(binding.root)
            .setPositiveButton(context.resources.getString(
                context.getFrameworkIdentifier("date_time_set")), null)
            .setNeutralButton(R.string.reset, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.run {
            setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    textView.text.toString().takeUnless { it.isEmpty() }?.let {
                        if (File(it).exists()) {
                            settingsPreference.setString(SettingsKeys.downloadLocationDefault, it)
                            setPathSummary()
                            dialog.dismiss()
                        } else {
                            textView.error = context.resources.getString(R.string.path_not_found)
                        }
                    }
                }
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                    textView.setText(DownloadClient.defaultInitialDownloadPath)
                }
            }
            show()
        }
    }

    companion object {
        private const val REQUEST_CODE_PICKER = 1000
    }
}