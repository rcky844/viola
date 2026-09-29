// Copyright (c) 2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import tipz.viola.R
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

class RenderingLayersPreference(
    private val context: Context,
    attrs: AttributeSet
) : Preference(context, attrs) {
    private val settingsPreference = SettingsSharedPreference(context)

    init {
        setTitle(R.string.pref_rendering_layers_title)
        setSummary(R.string.pref_rendering_layers_summary)
        setOnPreferenceClickListener {
            val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                displayList = arrayOf("LAYER_TYPE_NONE", "LAYER_TYPE_SOFTWARE", "LAYER_TYPE_HARDWARE")
                idPreference = SettingsKeys.renderingLayers
                dialogTitleResId = R.string.pref_rendering_layers_title
            }

            ListPickerAlertDialog(context, settingsPreference, listPickerObject)
                .create().show()
            true
        }
    }
}