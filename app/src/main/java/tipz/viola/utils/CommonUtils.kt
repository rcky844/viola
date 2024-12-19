// Copyright (c) 2021-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.TextUtils
import java.util.Locale

object CommonUtils {
    private const val DEFAULT_LANGUAGE = "en-US"

    val language: String
        get() {
            var language = Locale.getDefault().language
            val country = Locale.getDefault().country
            if (TextUtils.isEmpty(language)) language = DEFAULT_LANGUAGE
            return "$language-$country"
        }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val icon = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(icon)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return icon
    }
}