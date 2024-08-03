// Copyright (c) 2023-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.SwitchPreferenceCompat
import tipz.viola.Application
import tipz.viola.R

class MaterialSwitchPreference(context: Context, attrs: AttributeSet?) :
    SwitchPreferenceCompat(context, attrs) {
    private val settingsPreference: SettingsSharedPreference =
        (context.applicationContext as Application).settingsPreference
    private val mPreferenceTag: String?
    private val mNeedReload: Boolean
    private val mRequiredApi: Int

    init {
        // Get attrs
        val a = context.obtainStyledAttributes(attrs, R.styleable.MaterialSwitchPreference)
        mPreferenceTag = a.getString(R.styleable.MaterialSwitchPreference_preferenceTag)
        mNeedReload = a.getBoolean(R.styleable.MaterialSwitchPreference_needReload, false)
        mRequiredApi = a.getInteger(R.styleable.MaterialSwitchPreference_requiredApi, 1)
        a.recycle()

        // Handle checkbox
        isChecked = settingsPreference.getIntBool(mPreferenceTag!!)
        onPreferenceClickListener = OnPreferenceClickListener {
            settingsPreference.setIntBool(mPreferenceTag, isChecked)
            SettingsActivity.SettingsPrefHandler.needReload = mNeedReload
            true
        }
        if (mRequiredApi > Build.VERSION.SDK_INT) this.isEnabled = false

        // Use material switch
        widgetLayoutResource = R.layout.preference_material_switch
    }
}