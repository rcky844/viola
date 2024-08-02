// Copyright (c) 2021-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import tipz.viola.R
import java.util.Locale

object CommonUtils {
    const val EMPTY_STRING = ""
    private const val DEFAULT_LANGUAGE = "en-US"

    fun shareUrl(context: Context, url: String?): Boolean {
        if (url.isNullOrBlank()) return false

        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, url)
        context.startActivity(
            Intent.createChooser(
                i,
                context.resources.getString(R.string.share_url_dialog_title)
            )
        )
        return true
    }

    /**
     * Show Toast
     *
     * @param s is supplied for what to show
     */
    fun showMessage(context: Context?, s: String?) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
    }

    fun showMessage(context: Context?, @StringRes resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    /**
     * Copy to Clipboard
     *
     * @param s string to copy
     */
    fun copyClipboard(context: Context, s: String?) {
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
            ClipData.newPlainText("clipboard", s)
        )
        showMessage(context, R.string.copied_clipboard)
    }

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

    fun getDisplayMetrics(context: Context, measuredDp: Int): Float {
        val r = context.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            measuredDp.toFloat(),
            r.displayMetrics
        )
    }

    fun setImmersiveMode(context: Context, enable: Boolean) {
        val windowInsetsController = WindowCompat.getInsetsController(
            (context as AppCompatActivity).window,
            context.window.decorView
        )
        WindowCompat.setDecorFitsSystemWindows(context.window, !enable)
        if (enable) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}