/*
 * Copyright (c) 2022-2024 Tipz Team
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
    private lateinit var appbar: AppBarLayout
    private lateinit var webviewContainer: RelativeLayout
    private var swipeRefreshLayoutEnabled = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsPreference = (applicationContext as Application).settingsPreference!!

        onBackPressedDispatcher.addCallback(this) {
            if (webview.canGoBack()) webview.goBack() else finish()
        }
    }

    override fun onStart() {
        appbar = findViewById(R.id.appbar)
        webviewContainer = findViewById(R.id.webviewContainer)

        /* Init VioWebView */
        webview.doSettingsCheck()

        // Setup swipe refresh layout
        swipeRefreshLayout.setOnRefreshListener { webview.webViewReload() }
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        // Setup start page
        startPageLayout?.findViewById<View>(R.id.startPageEditText)?.setOnClickListener { onStartPageEditTextPressed() }

        // Setup favicon
        faviconProgressBar?.setOnClickListener { favicon?.performClick() }
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        webview.onPause()
        webview.pauseTimers()
        webview.freeMemory()
    }

    override fun onResume() {
        super.onResume()
        webview.onResume()
        webview.resumeTimers()
        webview.freeMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        webview.destroy()
        webview.removeAllViews()
    }

    /**
     * Need Load Info Receiver
     *
     *
     * Receive needLoadUrl for loading.
     */
    @JvmField
    val mGetNeedLoad =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            doSettingsCheck()
            webview.loadUrl((if (result.data != null) result.data!!.getStringExtra("needLoadUrl") else CommonUtils.EMPTY_STRING)!!)
        }

    /**
     * Config Checker
     *
     *
     * Used to check if anything has been changed
     * after returning from settings.
     */
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
        if (settingsPreference.getString(SettingsKeys.startPageWallpaper).isNullOrEmpty()) {
            startPageLayout?.setBackgroundColor(resources.getColor(R.color.colorTopBarWebView))
        } else {
            try {
                val bitmap : Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(settingsPreference.getString(SettingsKeys.startPageWallpaper)) )
                startPageLayout?.background = BitmapDrawable(resources, bitmap)
            } catch (_: SecurityException) {
                startPageLayout?.setBackgroundColor(resources.getColor(R.color.colorTopBarWebView))
                settingsPreference.setString(SettingsKeys.startPageWallpaper, CommonUtils.EMPTY_STRING)
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
        if (settingsPreference.getIntBool(SettingsKeys.enableSwipeRefresh)) swipeRefreshLayout.isEnabled = swipeRefreshLayoutEnabled
    }

    @CallSuper
    open fun onPageLoadProgressChanged(progress: Int) {
        progressBar.progress = if (progress == 100) 0 else progress
    }

    private fun getTrueCSSValue(rawValue: String): String {
        var mValue = rawValue
        if (mValue.contains("\"")) mValue = mValue.replace("\"", "")
        if (mValue == "null") return "auto"
        val arrayValue: Array<String> =
            mValue.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return arrayValue[arrayValue.size - 1]
    }
}