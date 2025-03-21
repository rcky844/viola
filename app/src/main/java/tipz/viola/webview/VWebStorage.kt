// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebStorage
import androidx.webkit.WebStorageCompat
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object VWebStorage {
    private val instance = WebStorage.getInstance()

    @SuppressLint("RequiresFeature")
    @Suppress("DEPRECATION")
    fun deleteBrowsingData(doneCallback: () -> Any) {
        if (WebkitCompat.isFeatureSupported(WebViewFeature.DELETE_BROWSING_DATA)) {
            // Compatibility class for supported WebView
            WebStorageCompat.deleteBrowsingData(instance) { doneCallback() }
        } else {
            // Use traditional method
            instance.deleteAllData()

            // Delete Cookies
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            } else {
                val cookieSyncMgr = CookieSyncManager.getInstance()
                val cookieManager = CookieManager.getInstance()
                cookieSyncMgr.startSync()
                cookieManager.removeAllCookie()
                cookieManager.removeSessionCookie()
                cookieSyncMgr.stopSync()
                cookieSyncMgr.sync()
            }

            // Respond to callback
            CoroutineScope(Dispatchers.Main).launch { doneCallback() }
        }
    }
}
