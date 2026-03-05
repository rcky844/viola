// Copyright (c) 2023-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.preference

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.R
import tipz.viola.ext.restartApplication
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.settings.ui.fragment.ExtPreferenceFragment

class MaterialSwitchPreference(context: Context, attrs: AttributeSet?) :
    SwitchPreferenceCompat(context, attrs) {
    private val settingsPreference = SettingsSharedPreference.instance
    private val preferenceTag: String?
    private val needReload: Boolean
    private val needRestart: Boolean
    private val requiredApi: Int

    init {
        // Get attrs
        val a = context.obtainStyledAttributes(attrs, R.styleable.MaterialSwitchPreference)
        preferenceTag = a.getString(R.styleable.MaterialSwitchPreference_preferenceTag)
        needReload = a.getBoolean(R.styleable.MaterialSwitchPreference_needReload, false)
        needRestart = a.getBoolean(R.styleable.MaterialSwitchPreference_needRestart, false)
        requiredApi = a.getInteger(R.styleable.MaterialSwitchPreference_requiredApi, 1)
        a.recycle()

        // Handle checkbox
        isChecked = settingsPreference.getIntBool(preferenceTag!!)
        onPreferenceClickListener = OnPreferenceClickListener {
            settingsPreference.setIntBool(preferenceTag, isChecked)
            ExtPreferenceFragment.needReload = needReload

            if (needRestart) {
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.dialog_settings_restart_apply_title)
                    .setMessage(R.string.dialog_settings_restart_apply_message)
                    .setPositiveButton(R.string.dialog_settings_restart_apply_positive) { _, _ ->
                        context.restartApplication()
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> isChecked = !isChecked }
                    .setCancelable(false)
                    .create().show()
            }
            true
        }
        if (requiredApi >= Build.VERSION.SDK_INT) this.isEnabled = false

        // Use material switch
        widgetLayoutResource = R.layout.preference_material_switch
    }
}