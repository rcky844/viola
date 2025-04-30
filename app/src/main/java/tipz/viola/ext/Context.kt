// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.ext

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import tipz.viola.R

fun Context.dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
fun Context.pxToDp(px: Int): Int = (px / resources.displayMetrics.density).toInt()

fun Context.shareUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return false

    val i = Intent(Intent.ACTION_SEND)
    i.type = "text/plain"
    i.putExtra(Intent.EXTRA_TEXT, url)
    startActivity(Intent.createChooser(i, resources.getString(R.string.dialog_share_title)))
    return true
}

fun Context.showMessage(s: String?) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}

fun Context.showMessage(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun Context.copyClipboard(s: String?) {
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
        ClipData.newPlainText("clipboard", s)
    )

    // Starting in Android 13, the system displays a new visual confirmation when
    // content is added to the clipboard, effectively rendering this toast pointless.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
        showMessage(R.string.toast_copied_clipboard)
}

fun Context.setImmersiveMode(enable: Boolean) =
    WindowCompat.getInsetsController((this as AppCompatActivity).window, window.decorView).run {
        if (enable) {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            show(WindowInsetsCompat.Type.systemBars())
        }
    }

@Suppress("DEPRECATION")
fun Context.isOnline(): Boolean =
    (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activeNetwork != null
        } else {
            activeNetworkInfo.run { this != null && isAvailable }
        }
    }

@ColorInt
fun Context.getOnSurfaceColor(): Int {
    val attrs = theme.obtainStyledAttributes(
        intArrayOf(com.google.android.material.R.attr.colorOnSurface)
    )
    return attrs.getColor(0, 0)
}

@Dimension
fun Context.getMinTouchTargetSize(): Int {
    val attrs = theme.obtainStyledAttributes(
        intArrayOf(com.google.android.material.R.attr.minTouchTargetSize)
    )
    return attrs.getDimensionPixelSize(0, 0)
}

@SuppressLint("DiscouragedApi")
fun Context.getFrameworkIdentifier(name: String): Int =
    resources.getIdentifier(name, "string", "android")