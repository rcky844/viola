// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity.components

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.setPadding
import com.google.android.material.progressindicator.CircularProgressIndicator
import tipz.viola.R
import tipz.viola.ext.dpToPx
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

class FavIconView(
    context: Context, attributeSet: AttributeSet
) : CoordinatorLayout(context, attributeSet) {
    private val settingsPreference = SettingsSharedPreference.instance

    val imageView = AppCompatImageView(context)
    val progressBar = CircularProgressIndicator(context)

    var isDisplayed = true
        set(value) {
            field = value
            visibility = if (value) VISIBLE else GONE
        }
    fun updateIsDisplayed() {
        isDisplayed = settingsPreference.getIntBool(SettingsKeys.showFavicon)
    }

    var isLoading = false
        set(value) {
            field = value
            if (!isDisplayed) return
            imageView.visibility = if (value) GONE else VISIBLE
            progressBar.visibility = if (value) VISIBLE else GONE
        }

    init {
        imageView.apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setPadding(context.dpToPx(8))
            setImageResource(R.drawable.default_favicon)
        }
        progressBar.apply {
            visibility = GONE
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setPadding(context.dpToPx(8))
            indicatorSize = context.dpToPx(24)
            isIndeterminate = true
        }

        addView(imageView)
        addView(progressBar)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        imageView.setOnClickListener(l)
        progressBar.setOnClickListener(l)
    }

    fun setImageResource(@DrawableRes resId: Int) = imageView.setImageResource(resId)

    fun setImageBitmap(bm: Bitmap?) {
        if (bm == null) setImageResource(R.drawable.default_favicon)
        else imageView.setImageBitmap(bm)
    }
}