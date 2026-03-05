// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.preference

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.preference.Preference
import tipz.viola.R
import tipz.viola.settings.SettingsSharedPreference

open class MaterialPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {
    private val settingsPreference = SettingsSharedPreference.instance
    private val mRequiredApi: Int

    init {
        // Get attrs
        val a = context.obtainStyledAttributes(attrs, R.styleable.MaterialSwitchPreference)
        mRequiredApi = a.getInteger(R.styleable.MaterialSwitchPreference_requiredApi, 1)
        a.recycle()

        // Check for required API
        if (mRequiredApi >= Build.VERSION.SDK_INT) this.isEnabled = false
    }
}