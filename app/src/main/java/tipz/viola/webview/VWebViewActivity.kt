/*
 * Copyright (C) 2022-2023 Tipz Team
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
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import tipz.viola.Application
import tipz.viola.BaseActivity
import tipz.viola.R
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsUtils
import tipz.viola.utils.CommonUtils

open class VWebViewActivity : BaseActivity() {
    lateinit var pref: SharedPreferences
    lateinit var webview: VWebView
    lateinit var favicon: AppCompatImageView
    lateinit var faviconProgressBar: CircularProgressIndicator
    lateinit var progressBar: LinearProgressIndicator
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var startPageLayout: View
    private lateinit var appbar: AppBarLayout
    lateinit var toolsContainer: RelativeLayout
    private lateinit var webviewContainer: RelativeLayout
    private var swipeRefreshLayoutEnabled = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = (applicationContext as Application).pref!!

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
        startPageLayout.findViewById<View>(R.id.startPageEditText)
            .setOnClickListener { onStartPageEditTextPressed() }

        // Setup favicon
        faviconProgressBar.setOnClickListener { favicon.performClick() }
        super.onStart()
    }

    @Suppress("DEPRECATION")
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) webview.freeMemory()
    }

    /**
     * Need Load Info Receiver
     *
     *
     * Receive needLoadUrl for loading.
     */
    @JvmField
    val mGetNeedLoad = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
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
        if (swipeRefreshLayoutEnabled) {
            swipeRefreshLayout.isEnabled = CommonUtils.isIntStrOne(
                SettingsUtils.getPrefNum(
                    pref,
                    SettingsKeys.enableSwipeRefresh
                )
            )
        }

        // Favicon
        favicon.visibility = if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.showFavicon))) View.VISIBLE else View.GONE
        if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.showFavicon)) && faviconProgressBar.visibility == View.VISIBLE)
            favicon.visibility = View.GONE

        // Reach mode
        reachModeCheck()
    }

    fun reachModeCheck() {
        val appBarParams = appbar.layoutParams as CoordinatorLayout.LayoutParams
        val toolsContainerParams: CoordinatorLayout.LayoutParams?
        val webviewContainerParams =
            webviewContainer.layoutParams as CoordinatorLayout.LayoutParams
        toolsContainerParams =
            toolsContainer.layoutParams as CoordinatorLayout.LayoutParams

        val actionBarSize = resources.getDimension(R.dimen.actionbar_view_height).toInt()
        val toolsContainerSize = resources.getDimension(R.dimen.toolbar_icon_size).toInt()
        var margin = actionBarSize
        if (toolsContainer.visibility == View.VISIBLE) margin += toolsContainerSize
        if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.reverseLayout))) {
            if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.reverseOnlyActionBar))) {
                appBarParams.gravity = Gravity.TOP
                toolsContainer.visibility = View.VISIBLE
                toolsContainerParams.setMargins(0, 0, 0, 0)
                webviewContainerParams.setMargins(0, actionBarSize, 0, toolsContainerSize)
            } else {
                appBarParams.gravity = Gravity.BOTTOM
                toolsContainerParams.setMargins(0, 0, 0, actionBarSize)
                webviewContainerParams.setMargins(0, 0, 0, margin)
            }
            toolsContainerParams.gravity = Gravity.BOTTOM
        } else {
            appBarParams.gravity = Gravity.TOP
            toolsContainerParams.gravity = Gravity.TOP
            toolsContainerParams.setMargins(0, actionBarSize, 0, 0)
            webviewContainerParams.setMargins(0, margin, 0, 0)
        }
        appbar.layoutParams = appBarParams
        appbar.invalidate()
        toolsContainer.layoutParams = toolsContainerParams
        toolsContainer.invalidate()
        webviewContainer.layoutParams = webviewContainerParams
        webviewContainer.invalidate()
    }

    open fun onUrlUpdated(url: String?) {}
    open fun onUrlUpdated(url: String?, position: Int) {}
    @Suppress("DEPRECATION")
    @CallSuper
    open fun onTitleUpdated(title: String?) {
        if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.updateRecentsIcon))
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        ) {
            val description = TaskDescription(title)
            setTaskDescription(description)
        }
    }

    open fun onDropDownDismissed() {}
    open fun onStartPageEditTextPressed() {}
    @Suppress("DEPRECATION")
    @CallSuper
    fun onFaviconUpdated(icon: Bitmap?, checkInstance: Boolean) {
        if (checkInstance && favicon.drawable is BitmapDrawable) return
        if (icon == null) favicon.setImageResource(R.drawable.default_favicon) else favicon.setImageBitmap(
            icon
        )
        if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.updateRecentsIcon))
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val description = TaskDescription(webview.title, icon)
            setTaskDescription(description)
        }
    }

    @CallSuper
    fun onFaviconProgressUpdated(isLoading: Boolean) {
        if (!CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.showFavicon))) return
        if (isLoading) {
            favicon.visibility = View.GONE
            faviconProgressBar.visibility = View.VISIBLE
        } else {
            favicon.visibility = View.VISIBLE
            faviconProgressBar.visibility = View.GONE
        }
    }

    @CallSuper
    fun onSwipeRefreshLayoutRefreshingUpdated(isRefreshing: Boolean) {
        swipeRefreshLayout.isRefreshing = isRefreshing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webview.evaluateJavascript("getComputedStyle(document.body).getPropertyValue('overflow-y')") { value1: String ->
                updateSwipeRefreshLayoutEnabled(getTrueCSSValue(value1) != "hidden")
                if (swipeRefreshLayoutEnabled) webview.evaluateJavascript("getComputedStyle(document.body).getPropertyValue('overscroll-behavior-y')") { value2: String ->
                    updateSwipeRefreshLayoutEnabled(
                        getTrueCSSValue(value2) == "auto"
                    )
                }
            }
        }
    }

    private fun updateSwipeRefreshLayoutEnabled(isEnabled: Boolean) {
        swipeRefreshLayoutEnabled = isEnabled
        if (CommonUtils.isIntStrOne(
                SettingsUtils.getPrefNum(
                    pref,
                    SettingsKeys.enableSwipeRefresh
                )
            )
        ) swipeRefreshLayout.isEnabled = swipeRefreshLayoutEnabled
    }

    @CallSuper
    fun onPageLoadProgressChanged(progress: Int) {
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