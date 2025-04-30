// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.fragment

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.jetbrains.annotations.MustBeInvokedByOverriders
import tipz.viola.R
import tipz.viola.databinding.DialogDownloadLocationPickerBinding
import tipz.viola.ext.getFrameworkIdentifier
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.webview.buss.BussUtils

class WebXApiPickerPreference(
    private val context: Context,
    attrs: AttributeSet
) : Preference(context, attrs) {
    private val settingsPreference = SettingsSharedPreference(context)
    private fun getUrl() = settingsPreference.getString(SettingsKeys.bussApiUrl)

    init {
        setTitle(R.string.pref_webx_picker_title)
        setUrlSummary()
        setOnPreferenceClickListener {
            createPickerDialog()
            true
        }
    }

    @MustBeInvokedByOverriders
    fun setUrlSummary() {
        setSummary(getUrl())
    }

    private fun createPickerDialog() {
        val binding = DialogDownloadLocationPickerBinding.inflate(LayoutInflater.from(context))
        val textView = binding.pathEditText.apply {
            setText(getUrl())
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.pref_webx_picker_title)
            .setView(binding.root)
            .setPositiveButton(context.resources.getString(
                context.getFrameworkIdentifier("date_time_set")), null)
            .setNeutralButton(R.string.reset, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.run {
            setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    settingsPreference.setString(SettingsKeys.bussApiUrl, textView.text.toString())
                    setUrlSummary()
                    dialog.dismiss()
                }
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                    textView.setText(BussUtils.defaultApiUrl)
                }
            }
            show()
        }
    }
}