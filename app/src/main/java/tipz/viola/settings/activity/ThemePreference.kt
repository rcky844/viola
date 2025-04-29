// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.activity

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.elevation.SurfaceColors
import tipz.viola.R
import tipz.viola.databinding.PreferenceThemeBinding
import tipz.viola.settings.SettingsKeys
import tipz.viola.webview.activity.BaseActivity.Companion.settingsPreference

class ThemePreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    private lateinit var binding: PreferenceThemeBinding

    init {
        layoutResource = R.layout.preference_theme
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        binding = PreferenceThemeBinding.bind(holder.itemView)
        updateUi()
    }

    private fun updateThemeCard(card: CardView, theme: Int) {
        val density = context.resources.displayMetrics.density
        val surfaceColor = SurfaceColors.getColorForElevation(context, 1 * density)
        val surfaceColorActive = SurfaceColors.getColorForElevation(context, 32 * density)
        val activeTheme = settingsPreference.getInt(SettingsKeys.themeId)
        card.setCardBackgroundColor(if (theme == activeTheme) surfaceColorActive else surfaceColor)
        card.setOnClickListener { _ ->
            settingsPreference.setInt(SettingsKeys.themeId, theme)
            if (onPreferenceChangeListener != null) {
                onPreferenceChangeListener!!.onPreferenceChange(this, theme)
            }
            updateUi()
        }
    }

    private fun updateUi() {
        updateThemeCard(binding.themeSystemCard, 0)
        updateThemeCard(binding.themeLightCard, 1)
        updateThemeCard(binding.themeDarkCard, 2)
    }
}