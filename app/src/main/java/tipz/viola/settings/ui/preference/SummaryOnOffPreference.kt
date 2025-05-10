// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import org.jetbrains.annotations.MustBeInvokedByOverriders
import tipz.viola.Application
import tipz.viola.R
import tipz.viola.settings.SettingsSharedPreference

class SummaryOnOffPreference(context: Context,
                             private val attrs: AttributeSet?) : Preference(context, attrs) {
    private val settingsPreference = SettingsSharedPreference.instance
    private val preferenceTag: String?

    init {
        // Get attrs
        val a = context.obtainStyledAttributes(attrs, R.styleable.MaterialSwitchPreference)
        preferenceTag = a.getString(R.styleable.MaterialSwitchPreference_preferenceTag)
        a.recycle()

        // Handle preference tag
        if (preferenceTag == null)
            throw IllegalStateException("Preference tag must be provided, but none were given.")
        setOnOffSummary()
    }

    @MustBeInvokedByOverriders
    fun setOnOffSummary() {
        setSummary(
            if (settingsPreference.getIntBool(preferenceTag!!)) R.string.text_on
            else R.string.text_off
        )
    }
}