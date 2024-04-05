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

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.SwitchPreferenceCompat
import tipz.viola.Application
import tipz.viola.R

class MaterialSwitchPreference(context: Context, attrs: AttributeSet?) :
    SwitchPreferenceCompat(context, attrs) {
    private val settingsPreference: SettingsSharedPreference =
        (getContext().applicationContext as Application).settingsPreference!!
    private val mPreferenceTag: String?
    private val mNeedReload: Boolean

    init {
        // Get attrs
        val a = getContext().obtainStyledAttributes(attrs, R.styleable.MaterialSwitchPreference)
        mPreferenceTag = a.getString(R.styleable.MaterialSwitchPreference_preferenceTag)
        mNeedReload = a.getBoolean(R.styleable.MaterialSwitchPreference_needReload, false)
        a.recycle()

        // Handle checkbox
        isChecked = settingsPreference.getIntBool(mPreferenceTag!!)
        onPreferenceClickListener = OnPreferenceClickListener {
            settingsPreference.setIntBool(mPreferenceTag, isChecked)
            SettingsActivity.SettingsPrefHandler.needReload = mNeedReload
            true
        }

        // Use material switch
        widgetLayoutResource = R.layout.preference_material_switch
    }
}