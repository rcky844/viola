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
package tipz.viola.utils;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Locale;

import tipz.viola.R;

public class CommonUtils {
    public static final String EMPTY_STRING = "";
    private static final String DEFAULT_LANGUAGE = "en-US";

    public static String LINE_SEPARATOR() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2 ?
                System.getProperty("line.separator") : System.lineSeparator();
    }

    public static void shareUrl(Context context, @NonNull String url) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, url);
        context.startActivity(Intent.createChooser(i, context.getResources().getString(R.string.share_url_dialog_title)));
    }

    /**
     * Show Toast
     *
     * @param s is supplied for what to show
     */
    public static void showMessage(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    /**
     * Copy to Clipboard
     *
     * @param s string to copy
     */
    public static void copyClipboard(Context context, String s) {
        ((ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", s));
        showMessage(context, context.getResources().getString(R.string.copied_clipboard));
    }

    public static boolean isIntStrOne(Object obj) {
        return obj.equals(obj instanceof String ? "1" : 1);
    }

    public static String getLanguage() {
        String language = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        if (TextUtils.isEmpty(language))
            language = DEFAULT_LANGUAGE;
        return language + "-" + country;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap icon = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(icon);

        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return icon;
    }

    public static float getDisplayMetrics(Context context, int measuredDp) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                measuredDp,
                r.getDisplayMetrics()
        );
    }
}