// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.preference

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
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

    init {
        layoutResource = R.layout.preference_wallpaper
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        binding = PreferenceWallpaperBinding.bind(holder.itemView)
        previewWallpaper = binding.previewWallpaperView
        changeWallpaper = binding.changeWallpaperCard

        val density = context.resources.displayMetrics.density
        changeWallpaper.setCardBackgroundColor(
            SurfaceColors.getColorForElevation(context, 1 * density))
        changeWallpaper.setOnClickListener {
            if (onPreferenceChangeListener != null) {
                onPreferenceChangeListener!!.onPreferenceChange(this, null)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) setWallpaperPreview()
        else changeWallpaper.isVisible = false
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun setWallpaperPreview(
        uri: Uri = settingsPreference.getString(SettingsKeys.startPageWallpaper).toUri()
    ) {
        try {
            previewWallpaper.setImageURI(uri)
        } catch (_: Exception) {
            previewWallpaper.setImageResource(0)
        }
    }
}