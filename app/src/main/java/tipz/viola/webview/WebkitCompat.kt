// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("DEPRECATION")

package tipz.viola.webview

import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.webkit.WebIconDatabase
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewFeature.WebViewSupportFeature

object WebkitCompat {
    fun setDefaultsForCompat(webView: VWebView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // Remove exposed JavaScript interfaces on older Android SDKs
            // in order to prevent exploitation
            listOf("searchBoxJavaBridge_", /* CVE-2014-1939 */
                "accessibility", "accessibilityTraversal" /* CVE-2014-7224 */
            ).forEach {
                webView.removeJavascriptInterface(it)
            }

            // Open legacy favicon paths to ensure proper usage
            WebIconDatabase.getInstance().open(webView.context.getDir("icons", MODE_PRIVATE).path)
        }
    }

    fun isFeatureSupported(@WebViewSupportFeature feature: String): Boolean {
        // Fail the test if running Android versions older than KitKat, as they do not
        // have Chromium WebView in framework nor in an APK file.
        // KitKat did not have updatable WebView, but it shipped with Chromium in framework,
        // so WebViewFeature#isFeatureSupported(feature) does not cause any issues.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return false

        // Run the devil itself.
        return WebViewFeature.isFeatureSupported(feature)
    }
}