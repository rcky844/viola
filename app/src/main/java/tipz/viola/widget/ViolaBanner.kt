// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.widget

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import com.google.android.material.elevation.SurfaceColors
import tipz.viola.R
import tipz.viola.ext.dpToPx

fun Context.makeViolaBanner(): CardView {
    val appBannerCard = CardView(this).apply {
        id = R.id.app_banner_card
        radius = context.dpToPx(64).toFloat()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            setContentPadding(context.dpToPx(8), context.dpToPx(16),
                context.dpToPx(24), context.dpToPx(16))
        else
            setContentPadding(0, context.dpToPx(8),
            context.dpToPx(16), context.dpToPx(8))

        setCardBackgroundColor(SurfaceColors.getColorForElevation(
            context, 1 * context.resources.displayMetrics.density))
        cardElevation = 0f
        background.alpha = 120
    }
    val appBanner = AppCompatTextView(this).apply {
        id = R.id.app_banner
        gravity = Gravity.CENTER
        setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.app_logo, 0, 0, 0)
        setText(R.string.app_name_display)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        setTypeface(null, Typeface.BOLD)
    }
    appBannerCard.addView(appBanner)

    return appBannerCard
}