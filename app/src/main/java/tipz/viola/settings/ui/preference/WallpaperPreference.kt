// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.preference

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.elevation.SurfaceColors
import tipz.viola.R
import tipz.viola.databinding.PreferenceWallpaperBinding
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

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
            settingsPreference.setInt(SettingsKeys.startPageColor, 0)
            previewWallpaper.setImageResource(0)
            previewWallpaper.setBackgroundColor(0)
        }

        colorWallpaper.setOnClickListener {
            ColorPickerDialog
                .Builder(context)
                .setTitle(R.string.dialog_start_page_solid_color_picker)
                .setColorShape(ColorShape.SQAURE)
                .setColorListener { color, colorHex ->
                    settingsPreference.setInt(SettingsKeys.startPageColor, color)
                    setWallpaperPreview()
                }
                .show()
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) changeWallpaper.isVisible = false
        setWallpaperPreview()
    }

    @Suppress("DEPRECATION")
    fun setWallpaperPreview(
        uri: Uri = settingsPreference.getString(SettingsKeys.startPageWallpaper).toUri()
    ) {
        if (settingsPreference.getInt(SettingsKeys.startPageColor) != 0) {
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