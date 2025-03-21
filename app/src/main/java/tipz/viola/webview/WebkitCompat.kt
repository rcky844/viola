// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.os.Build
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewFeature.WebViewSupportFeature

object WebkitCompat {
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