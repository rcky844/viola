// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.ext

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import tipz.viola.R

fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

fun Context.pxToDp(px: Int): Int {
    return (px / resources.displayMetrics.density).toInt()
}

fun Context.shareUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return false

    val i = Intent(Intent.ACTION_SEND)
    i.type = "text/plain"
    i.putExtra(Intent.EXTRA_TEXT, url)
    startActivity(Intent.createChooser(i, resources.getString(R.string.share_url_dialog_title)))
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
        showMessage(R.string.copied_clipboard)
}

fun Context.setImmersiveMode(enable: Boolean) {
    val windowInsetsController = WindowCompat.getInsetsController(
        (this as AppCompatActivity).window,
        window.decorView
    )
    WindowCompat.setDecorFitsSystemWindows(window, !enable)
    if (enable) {
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    }
}

@Suppress("DEPRECATION")
fun Context.isOnline(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        cm.activeNetwork != null
    } else {
        val n = cm.activeNetworkInfo
        n != null && n.isAvailable
    }
}
