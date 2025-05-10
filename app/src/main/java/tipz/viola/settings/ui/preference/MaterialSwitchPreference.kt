// Copyright (c) 2023-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.preference

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.SwitchPreferenceCompat
import tipz.viola.R
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.settings.ui.fragment.ExtPreferenceFragment

class MaterialSwitchPreference(context: Context, attrs: AttributeSet?) :
    SwitchPreferenceCompat(context, attrs) {
    private val settingsPreference = SettingsSharedPreference.instance
    private val preferenceTag: String?
    private val needReload: Boolean
    private val requiredApi: Int

    init {
        // Get attrs
        val a = context.obtainStyledAttributes(attrs, R.styleable.MaterialSwitchPreference)
        preferenceTag = a.getString(R.styleable.MaterialSwitchPreference_preferenceTag)
        needReload = a.getBoolean(R.styleable.MaterialSwitchPreference_needReload, false)
        requiredApi = a.getInteger(R.styleable.MaterialSwitchPreference_requiredApi, 1)
        a.recycle()

        // Handle checkbox
        isChecked = settingsPreference.getIntBool(preferenceTag!!)
        onPreferenceClickListener = OnPreferenceClickListener {
            settingsPreference.setIntBool(preferenceTag, isChecked)
            ExtPreferenceFragment.needReload = needReload
            true
        }
        if (requiredApi > Build.VERSION.SDK_INT) this.isEnabled = false

        // Use material switch
        widgetLayoutResource = R.layout.preference_material_switch
    }
}