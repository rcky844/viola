// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.preference

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import tipz.viola.R
import tipz.viola.databinding.PreferenceWallpaperBinding
import tipz.viola.ext.getFrameworkIdentifier
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import top.defaults.colorpicker.ColorPickerView

class WallpaperPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    private lateinit var binding: PreferenceWallpaperBinding
    internal val settingsPreference = SettingsSharedPreference.instance

    lateinit var previewWallpaper: AppCompatImageView
    lateinit var changeWallpaper: CardView
    lateinit var resetWallpaper: CardView
    lateinit var colorWallpaper: CardView

    init {
        layoutResource = R.layout.preference_wallpaper
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        binding = PreferenceWallpaperBinding.bind(holder.itemView)
        previewWallpaper = binding.previewWallpaperView
        changeWallpaper = binding.changeWallpaperCard
        resetWallpaper = binding.resetWallpaperCard
        colorWallpaper = binding.colorWallpaperCard

        listOf(changeWallpaper, resetWallpaper, colorWallpaper).forEach {
            it.setCardBackgroundColor(SurfaceColors.getColorForElevation(
                context, 1 * context.resources.displayMetrics.density))
        }

        changeWallpaper.setOnClickListener {
            if (onPreferenceChangeListener != null) {
                onPreferenceChangeListener!!.onPreferenceChange(this, null)
            }
        }

        resetWallpaper.setOnClickListener {
            settingsPreference.setString(SettingsKeys.startPageWallpaper, "")
            settingsPreference.setInt(SettingsKeys.startPageColor, -1)
            previewWallpaper.setImageResource(0)
            previewWallpaper.setBackgroundColor(0)
        }

        colorWallpaper.setOnClickListener {
            val picker = ColorPickerView(context)
            picker.setInitialColor(settingsPreference.getInt(SettingsKeys.startPageColor)
                .takeIf { it != -1 } ?: Color.WHITE)
            picker.setEnabledAlpha(true)
            picker.setEnabledBrightness(true)

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_start_page_solid_color_picker)
                .setPositiveButton(context.resources.getString(
                    context.getFrameworkIdentifier("date_time_set"))) { _, _ ->
                    settingsPreference.setInt(SettingsKeys.startPageColor, picker.color)
                    setWallpaperPreview()
                }
                .setNegativeButton(context.resources.getString(android.R.string.cancel), null)
                .setView(picker)
                .show()
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) changeWallpaper.isVisible = false
        setWallpaperPreview()
    }

    @Suppress("DEPRECATION")
    fun setWallpaperPreview(
        uri: Uri = settingsPreference.getString(SettingsKeys.startPageWallpaper).toUri()
    ) {
        if (settingsPreference.getInt(SettingsKeys.startPageColor) != -1) {
            previewWallpaper.setBackgroundColor(
                settingsPreference.getInt(SettingsKeys.startPageColor))
            return
        }
        previewWallpaper.setBackgroundColor(0)

        try {
            previewWallpaper.setImageURI(uri)
        } catch (_: Exception) {
            previewWallpaper.setImageResource(0)
        }
    }
}