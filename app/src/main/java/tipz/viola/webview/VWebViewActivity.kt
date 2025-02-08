// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.app.ActivityManager.TaskDescription
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import tipz.viola.Application
import tipz.viola.R
import tipz.viola.ext.showMessage
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.webview.activity.BaseActivity


open class VWebViewActivity : BaseActivity() {
    lateinit var settingsPreference: SettingsSharedPreference
    lateinit var swipeRefreshLayout: VSwipeRefreshLayout
    lateinit var webview: VWebView
    var favicon: AppCompatImageView? = null
    var faviconProgressBar: CircularProgressIndicator? = null
    lateinit var progressBar: LinearProgressIndicator
    internal lateinit var appbar: AppBarLayout
    internal lateinit var webviewContainer: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsPreference = (applicationContext as Application).settingsPreference

        onBackPressedDispatcher.addCallback(this) {
            if (webview.canGoBack()) webview.goBack() else finish()
        }
    }

    override fun onStart() {
        // Init VioWebView
        webview.doSettingsCheck()

        // Swipe to Refresh
        swipeRefreshLayout.initialize(webview)

        // Setup favicon
        faviconProgressBar?.setOnClickListener { favicon?.performClick() }
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        webview.onPause()
        webview.pauseTimers()
    }

    override fun onResume() {
        super.onResume()
        webview.onResume()
        webview.resumeTimers()
    }

    override fun onDestroy() {
        super.onDestroy()
        webview.destroy()
        webview.removeAllViews()
    }

    val mGetNeedLoad =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            doSettingsCheck()
            if (result.data == null) return@registerForActivityResult

            // FROM: SettingsActivity
            result.data!!.getStringExtra(SettingsKeys.needLoadUrl)?.let { webview.loadUrl(it) }

            if (result.data!!.getBooleanExtra(SettingsKeys.needReload, false))
                webview.reload()

            if (result.data!!.getIntExtra(SettingsKeys.updateAdServers, 0) != 0) {
                webview.adServersHandler.downloadAdServers {
                    showMessage(R.string.toast_ad_servers_finished)
                }
            }
        }

    @Suppress("DEPRECATION")
    override fun doSettingsCheck() {
        super.doSettingsCheck()

        // Favicon
        if (favicon != null) {
            favicon!!.visibility =
                if (settingsPreference.getIntBool(SettingsKeys.showFavicon)) View.VISIBLE else View.GONE
            if (settingsPreference.getIntBool(SettingsKeys.showFavicon) && faviconProgressBar?.visibility == View.VISIBLE)
                favicon!!.visibility = View.GONE
        }
    }

    open fun onUrlUpdated(url: String?) {}
    open fun onUrlUpdated(url: String?, position: Int) {}

    @Suppress("DEPRECATION")
    @CallSuper
    open fun onTitleUpdated(title: String?) {
        if (settingsPreference.getIntBool(SettingsKeys.updateRecentsIcon)
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val description = TaskDescription(title)
            setTaskDescription(description)
        }
    }

    open fun onDropDownDismissed() {}

    open fun onSslCertificateUpdated() {}

    open fun onSslErrorProceed() {}

    @Suppress("DEPRECATION")
    @CallSuper
    open fun onFaviconUpdated(icon: Bitmap?, checkInstance: Boolean) {
        if (checkInstance && favicon?.drawable is BitmapDrawable) return
        if (icon == null) favicon?.setImageResource(R.drawable.default_favicon) else favicon?.setImageBitmap(
            icon
        )
        if (settingsPreference.getIntBool(SettingsKeys.updateRecentsIcon) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val description = TaskDescription(webview.title, icon)
            setTaskDescription(description)
        }
    }

    @CallSuper
    open fun onFaviconProgressUpdated(isLoading: Boolean) {
        if (!settingsPreference.getIntBool(SettingsKeys.showFavicon)) return
        if (isLoading) {
            favicon?.visibility = View.GONE
            faviconProgressBar?.visibility = View.VISIBLE
        } else {
            favicon?.visibility = View.VISIBLE
            faviconProgressBar?.visibility = View.GONE
        }
    }

    @CallSuper
    open fun onPageLoadProgressChanged(progress: Int) {
        progressBar.progress = if (progress == 100) 0 else progress
    }
}