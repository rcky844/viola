// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.app.ActivityManager.TaskDescription
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatImageView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import tipz.viola.Application
import tipz.viola.R
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.utils.CommonUtils
import tipz.viola.webviewui.BaseActivity

open class VWebViewActivity : BaseActivity() {
    lateinit var settingsPreference: SettingsSharedPreference
    lateinit var webview: VWebView
    var favicon: AppCompatImageView? = null
    var faviconProgressBar: CircularProgressIndicator? = null
    lateinit var progressBar: LinearProgressIndicator
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    var startPageLayout: View? = null
    internal lateinit var appbar: AppBarLayout
    internal lateinit var webviewContainer: RelativeLayout
    internal lateinit var toolsContainer: RelativeLayout
    private var swipeRefreshLayoutEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsPreference = (applicationContext as Application).settingsPreference

        onBackPressedDispatcher.addCallback(this) {
            if (webview.canGoBack()) webview.goBack() else finish()
        }
    }

    override fun onStart() {
        try {
            appbar = findViewById(R.id.appbar)
            webviewContainer = findViewById(R.id.webviewContainer)
            toolsContainer = findViewById(R.id.toolsContainer)
        } catch (_: NullPointerException) {
        }

        // Init VioWebView
        webview.doSettingsCheck()

        // Setup swipe refresh layout
        swipeRefreshLayout.setOnRefreshListener { webview.reload() }

        // Setup start page
        startPageLayout?.findViewById<View>(R.id.startPageEditText)
            ?.setOnClickListener { onStartPageEditTextPressed() }

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

            result.data!!.getStringExtra(SettingsKeys.needLoadUrl)?.let { webview.loadUrl(it) } // FROM: SettingsActivity

            if (result.data!!.getIntExtra(SettingsKeys.needReload, 0) != 0)
                webview.reload()

            if (result.data!!.getIntExtra(SettingsKeys.updateAdServers, 0) != 0)
                webview.adServersHandler.downloadAdServers() // TODO: Add dialogs to show progress
        }

    @Suppress("DEPRECATION")
    override fun doSettingsCheck() {
        super.doSettingsCheck()

        // Pull to Refresh
        if (swipeRefreshLayoutEnabled) swipeRefreshLayout.isEnabled =
            settingsPreference.getIntBool(SettingsKeys.enableSwipeRefresh)

        // Favicon
        if (favicon != null) {
            favicon!!.visibility =
                if (settingsPreference.getIntBool(SettingsKeys.showFavicon)) View.VISIBLE else View.GONE
            if (settingsPreference.getIntBool(SettingsKeys.showFavicon) && faviconProgressBar?.visibility == View.VISIBLE)
                favicon!!.visibility = View.GONE
        }

        // Start Page Wallpaper
        if (settingsPreference.getString(SettingsKeys.startPageWallpaper).isEmpty()) {
            startPageLayout?.setBackgroundResource(0)
        } else {
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                    this.contentResolver,
                    Uri.parse(settingsPreference.getString(SettingsKeys.startPageWallpaper))
                )
                startPageLayout?.background = BitmapDrawable(resources, bitmap)
            } catch (_: SecurityException) {
                startPageLayout?.setBackgroundResource(0)
                settingsPreference.setString(
                    SettingsKeys.startPageWallpaper,
                    CommonUtils.EMPTY_STRING
                )
            }
        }
    }

    open fun onUrlUpdated(url: String?) {}
    open fun onUrlUpdated(url: String?, position: Int) {}

    @Suppress("DEPRECATION")
    @CallSuper
    open fun onTitleUpdated(title: String?) {
        if (settingsPreference.getIntBool(SettingsKeys.updateRecentsIcon) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val description = TaskDescription(title)
            setTaskDescription(description)
        }
    }

    open fun onDropDownDismissed() {}

    open fun onSslCertificateUpdated() {}

    open fun onSslErrorProceed() {}

    open fun onStartPageEditTextPressed() {}

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
    open fun onSwipeRefreshLayoutRefreshingUpdated(isRefreshing: Boolean) {
        swipeRefreshLayout.isRefreshing = isRefreshing
        webview.evaluateJavascript("getComputedStyle(document.body).getPropertyValue('overflow-y')") { value1: String ->
            updateSwipeRefreshLayoutEnabled(getTrueCSSValue(value1) != "hidden")
            if (swipeRefreshLayoutEnabled) webview.evaluateJavascript("getComputedStyle(document.body).getPropertyValue('overscroll-behavior-y')") { value2: String ->
                updateSwipeRefreshLayoutEnabled(
                    getTrueCSSValue(value2) == "auto"
                )
            }
        }
    }

    private fun updateSwipeRefreshLayoutEnabled(isEnabled: Boolean) {
        swipeRefreshLayoutEnabled = isEnabled
        if (settingsPreference.getIntBool(SettingsKeys.enableSwipeRefresh)) swipeRefreshLayout.isEnabled =
            swipeRefreshLayoutEnabled
    }

    private fun getTrueCSSValue(rawValue: String): String {
        var mValue = rawValue
        if (mValue.contains("\"")) mValue = mValue.replace("\"", "")
        if (mValue == "null") return "auto"
        val arrayValue: Array<String> =
            mValue.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return arrayValue[arrayValue.size - 1]
    }

    @CallSuper
    open fun onPageLoadProgressChanged(progress: Int) {
        progressBar.progress = if (progress == 100) 0 else progress
    }
}