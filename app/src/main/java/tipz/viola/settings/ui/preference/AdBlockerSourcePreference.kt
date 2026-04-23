// Copyright (c) 2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.preference

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.R
import tipz.viola.databinding.DialogEditTextBinding
import tipz.viola.ext.dpToPx
import tipz.viola.ext.getFrameworkIdentifier
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.webview.AdServersClient

class AdBlockerSourcePreference(
    private val context: Context,
    attrs: AttributeSet
) : Preference(context, attrs) {
    private val settingsPreference = SettingsSharedPreference(context)

    init {
        setTitle(R.string.pref_ad_blocker_source_title)
        setOnPreferenceClickListener {
            createDialog()
            true
        }
    }

    private fun createDialog() {
        val binding: DialogEditTextBinding =
            DialogEditTextBinding.inflate(LayoutInflater.from(context))
        val view = binding.root
        val input = binding.edittext

        input.run {
            gravity = Gravity.TOP
            minHeight = context.dpToPx(240)
            maxHeight = context.dpToPx(240)
            isSingleLine = false
            setHorizontallyScrolling(true)
            setText(settingsPreference.getString(SettingsKeys.adServerUrls))
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.pref_ad_blocker_source_title)
            .setMessage(R.string.pref_ad_blocker_source_message)
            .setView(view)
            .setPositiveButton(context.resources.getString(
                context.getFrameworkIdentifier("date_time_set")), null)
            .setNeutralButton(R.string.reset, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.run {
            setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    if (input.text?.trim().toString().isNotEmpty())
                        settingsPreference.setString(SettingsKeys.adServerUrls,
                            input.text.toString())
                }
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                    input.setText(AdServersClient.defaultAdServers)
                }
            }
            show()
        }
    }
}