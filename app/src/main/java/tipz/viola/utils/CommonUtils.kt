/*
 * Copyright (C) 2021-2023 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tipz.viola.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.TypedValue
import android.widget.Toast
import tipz.viola.R
import java.util.Locale

object CommonUtils {
    const val EMPTY_STRING = ""
    private const val DEFAULT_LANGUAGE = "en-US"

    fun shareUrl(context: Context, url: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, url)
        context.startActivity(
            Intent.createChooser(
                i,
                context.resources.getString(R.string.share_url_dialog_title)
            )
        )
    }

    /**
     * Show Toast
     *
     * @param s is supplied for what to show
     */
    @JvmStatic
    fun showMessage(context: Context?, s: String?) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
    }

    /**
     * Copy to Clipboard
     *
     * @param s string to copy
     */
    @JvmStatic
    fun copyClipboard(context: Context, s: String?) {
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
            ClipData.newPlainText("clipboard", s)
        )
        showMessage(context, context.resources.getString(R.string.copied_clipboard))
    }

    @JvmStatic
    fun isIntStrOne(obj: Any): Boolean {
        return obj == if (obj is String) "1" else 1
    }

    @JvmStatic
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
}