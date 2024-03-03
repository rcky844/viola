/*
 * Copyright (c) 2020-2024 Tipz Team
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
@file:Suppress("DEPRECATION")

package tipz.viola.webview

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewRenderProcess
import androidx.webkit.WebViewRenderProcessClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.Application
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.broha.api.HistoryApi
import tipz.viola.broha.api.HistoryUtils
import tipz.viola.broha.database.Broha
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.DownloadUtils
import tipz.viola.utils.InternalUrls
import tipz.viola.utils.UrlUtils
import tipz.viola.webviewui.BaseActivity
import java.util.Objects

@SuppressLint("SetJavaScriptEnabled")
class VWebView(private val mContext: Context, attrs: AttributeSet?) : WebView(
    mContext, attrs
) {
    private var mVioWebViewActivity: VWebViewActivity? = null
    private val iconHashClient = (mContext.applicationContext as Application).iconHashClient!!
    private val webSettings = this.settings
    private val mWebViewRenderProcess =
        if (WebViewFeature.isFeatureSupported(WebViewFeature.GET_WEB_VIEW_RENDERER)) WebViewCompat.getWebViewRenderProcess(
            this
        ) else null
    private var currentUrl: String? = null
    private var currentBroha: Broha? = null
    private var updateHistory = true
    private var historyCommitted = false
    private val settingsPreference =
        (mContext.applicationContext as Application).settingsPreference!!
    private var mUploadMessage: ValueCallback<Array<Uri>>? = null
    val mFileChooser =
        (mContext as AppCompatActivity).registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (null == mUploadMessage || uri == null) return@registerForActivityResult
            mUploadMessage!!.onReceiveValue(arrayOf(uri))
            mUploadMessage = null
        }
    private val mRequestHeaders = HashMap<String, String>()
    private fun userAgentFull(mode: Int): String {
        val mobile = if (mode == 0) "Mobile" else CommonUtils.EMPTY_STRING
        return "Mozilla/5.0 (Linux) AppleWebKit/537.36 KHTML, like Gecko) Chrome/${
            WebViewCompat.getCurrentWebViewPackage(
                mContext
            )?.versionName
        } $mobile Safari/537.36 Viola/${BuildConfig.VERSION_NAME}"
    }
    private val titleHandler = Handler { message ->
        val webLongPress = HitTestAlertDialog(mContext)
        if (!webLongPress.setupDialogForShowing(this, message.data)) return@Handler false
        webLongPress.show()

        return@Handler true
    }

    public enum class PageLoadState {
        PAGE_STARTED, PAGE_FINISHED, UPDATE_HISTORY, URL_UPDATE
    }

    init {
        /* User agent init code */
        setPrebuiltUAMode(null, 0, true)

        /* Start the download manager service */
        setDownloadListener { url: String?, _: String?, contentDisposition: String?, mimeType: String?, _: Long ->
            if (ContextCompat.checkSelfPermission(
                    mVioWebViewActivity!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED)
                ActivityCompat.requestPermissions(mVioWebViewActivity!!,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)

            DownloadUtils.dmDownloadFile(
                mContext, url!!, contentDisposition,
                mimeType, currentUrl
            )
            onPageInformationUpdated(PageLoadState.URL_UPDATE, originalUrl!!, null)
            mVioWebViewActivity!!.onPageLoadProgressChanged(0)
            if (!canGoBack() && originalUrl == null && settingsPreference.getIntBool(SettingsKeys.closeAppAfterDownload))
                mVioWebViewActivity!!.finish()
        }
        setLayerType(LAYER_TYPE_HARDWARE, null)

        /* zoom related stuff - From SCMPNews project */webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true

        // Also increase text size to fill the viewport (this mirrors the behaviour of Firefox,
        // Chrome does this in the current Chrome Dev, but not Chrome release).
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        webSettings.displayZoomControls = false
        webSettings.allowFileAccess = false
        webSettings.allowContentAccess = false
        webSettings.allowFileAccessFromFileURLs = false
        webSettings.allowUniversalAccessFromFileURLs = false

        /* HTML5 API flags */webSettings.databaseEnabled = false
        webSettings.domStorageEnabled = true
        this.webViewClient = VWebViewClient(mContext, this, AdServersHandler(settingsPreference))
        this.webChromeClient = ChromeWebClient()
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_CLIENT_BASIC_USAGE) && mWebViewRenderProcess != null) WebViewCompat.setWebViewRenderProcessClient(
            this,
            RenderClient()
        )

        /* Hit Test Menu */
        setOnCreateContextMenuListener { _: ContextMenu?, _: View?, _: ContextMenuInfo? ->
            val message = titleHandler.obtainMessage()
            this.requestFocusNodeHref(message)
        }
    }

    @Suppress("deprecation")
    fun doSettingsCheck() {
        // Dark mode
        val darkMode = BaseActivity.getDarkMode(mContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && WebViewFeature.isFeatureSupported(
                WebViewFeature.ALGORITHMIC_DARKENING
            )
        ) WebSettingsCompat.setAlgorithmicDarkeningAllowed(
            webSettings,
            darkMode
        ) else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) WebSettingsCompat.setForceDark(
            webSettings,
            if (darkMode) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
        )

        // Settings check
        webSettings.javaScriptEnabled =
            settingsPreference.getIntBool(SettingsKeys.isJavaScriptEnabled)
        webSettings.javaScriptCanOpenWindowsAutomatically =
            settingsPreference.getIntBool(SettingsKeys.isJavaScriptEnabled)

        // HTTPS enforce setting
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) webSettings.mixedContentMode =
            if (settingsPreference.getIntBool(SettingsKeys.enforceHttps)) WebSettings.MIXED_CONTENT_NEVER_ALLOW else WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Google's "Safe" Browsing
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE))
            WebSettingsCompat.setSafeBrowsingEnabled(
                webSettings,
                settingsPreference.getIntBool(SettingsKeys.enableGoogleSafeBrowse)
            )

        // Do Not Track request
        mRequestHeaders["DNT"] = settingsPreference.getInt(SettingsKeys.sendDNT).toString()
    }

    fun notifyViewSetup() {
        mVioWebViewActivity = mContext as VWebViewActivity
        doSettingsCheck()
    }

    fun setUpdateHistory(value: Boolean) {
        updateHistory = value
    }

    override fun loadUrl(url: String) {
        if (url.isEmpty()) return
        if (url == InternalUrls.aboutBlankUrl) {
            super.loadUrl(url)
            return
        }

        // Update to start page layout
        val startPageLayout = mVioWebViewActivity?.startPageLayout
        if (url == InternalUrls.startUrl) {
            this.loadUrl(InternalUrls.aboutBlankUrl)
            this.visibility = GONE
            startPageLayout?.visibility = VISIBLE
            return
        }
        if (this.visibility == GONE) {
            this.visibility = VISIBLE
            startPageLayout?.visibility = GONE
        }

        // Check for internal URLs
        if (url == InternalUrls.licenseUrl) {
            super.loadUrl(InternalUrls.realLicenseUrl)
            return
        }

        if (url == InternalUrls.reloadUrl) {
            webViewReload()
            return
        }

        // TODO: Remove, see InternalUrls.kt
        if (BuildConfig.DEBUG && url == InternalUrls.tempTest1226Url) {
            if (settingsPreference.getInt(SettingsKeys.adServerId) == 0) settingsPreference.setInt(SettingsKeys.adServerId, 1)
            if (settingsPreference.getInt(SettingsKeys.adServerId) == 1) settingsPreference.setInt(SettingsKeys.adServerId, 0)
            CommonUtils.showMessage(mVioWebViewActivity, "Triggered!")
            return
        }

        val checkedUrl = UrlUtils.toSearchOrValidUrl(mContext, url)
        onPageInformationUpdated(PageLoadState.URL_UPDATE, checkedUrl, null)

        // Load URL
        super.loadUrl(checkedUrl, mRequestHeaders)
    }

    override fun getUrl(): String? {
        return currentUrl
    }

    override fun goBack() {
        mVioWebViewActivity!!.onDropDownDismissed()
        super.goBack()
    }

    override fun goForward() {
        mVioWebViewActivity!!.onDropDownDismissed()
        super.goForward()
    }

    fun onPageInformationUpdated(state : PageLoadState, url: String, favicon: Bitmap?) {
        if (url == InternalUrls.aboutBlankUrl) return
        currentUrl = url

        when (state) {
            PageLoadState.PAGE_STARTED -> {
                mVioWebViewActivity!!.onFaviconProgressUpdated(true)
                mVioWebViewActivity!!.onPageLoadProgressChanged(-1)
            }
            PageLoadState.PAGE_FINISHED -> {
                mVioWebViewActivity!!.onFaviconProgressUpdated(false)
                mVioWebViewActivity!!.onPageLoadProgressChanged(0)
            }
            PageLoadState.UPDATE_HISTORY -> {
                if (updateHistory && currentUrl != null) {
                    currentBroha = Broha(title, currentUrl!!)
                    historyCommitted = false
                }
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) CookieSyncManager.getInstance()
                    .sync() else CookieManager.getInstance().flush()
                mVioWebViewActivity!!.onSwipeRefreshLayoutRefreshingUpdated(false)
            }

            PageLoadState.URL_UPDATE -> {
            }
        }

        mVioWebViewActivity!!.onUrlUpdated(url)
        mVioWebViewActivity!!.onFaviconUpdated(favicon, false)
        mVioWebViewActivity!!.onDropDownDismissed()
    }

    private fun setImmersiveMode(enable: Boolean) {
        val windowInsetsController = WindowCompat.getInsetsController(
            (mContext as AppCompatActivity).window,
            mContext.window.decorView
        )
        WindowCompat.setDecorFitsSystemWindows(mContext.window, !enable)
        if (enable) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    /**
     * WebChromeClient
     */
    inner class ChromeWebClient : WebChromeClient() {
        private var mCustomView: View? = null
        private var mCustomViewCallback: CustomViewCallback? = null
        override fun onShowCustomView(paramView: View, viewCallback: CustomViewCallback) {
            if (mCustomView != null) {
                onHideCustomView()
                return
            }
            mCustomView = paramView
            (mContext as AppCompatActivity).requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            mCustomViewCallback = viewCallback
            setImmersiveMode(true)
            (mContext.window.decorView as FrameLayout).addView(
                mCustomView,
                FrameLayout.LayoutParams(-1, -1)
            )
            mContext.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        override fun onHideCustomView() {
            (mContext as AppCompatActivity).window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            ((mContext as Activity).window.decorView as FrameLayout).removeView(mCustomView)
            mCustomView = null
            setImmersiveMode(false)
            mContext.requestedOrientation = resources.configuration.orientation
            mCustomViewCallback!!.onCustomViewHidden()
            mCustomViewCallback = null
        }

        override fun onProgressChanged(view: WebView, progress: Int) {
            mVioWebViewActivity!!.onPageLoadProgressChanged(progress)
        }

        override fun onReceivedIcon(view: WebView, icon: Bitmap) {
            mVioWebViewActivity!!.onFaviconUpdated(icon, false)
            val currentTitle = title
            if (!historyCommitted && updateHistory) {
                CoroutineScope(Dispatchers.IO).launch {
                    currentBroha!!.iconHash = iconHashClient.save(icon)
                    currentBroha!!.title = currentTitle // For making sure title is up to date
                    if (HistoryUtils.lastUrl(mContext) != currentUrl) {
                        HistoryApi.historyBroha(mContext)!!.insertAll(currentBroha!!)
                    }
                }
                historyCommitted = true
            }
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            mVioWebViewActivity!!.onTitleUpdated(if (view.visibility == View.GONE) resources.getString(R.string.start_page) else title)
        }

        override fun onGeolocationPermissionsShowPrompt(
            origin: String,
            callback: GeolocationPermissions.Callback
        ) {
            if (ContextCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_DENIED
            ) ActivityCompat.requestPermissions(mVioWebViewActivity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0)

            if (ContextCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) callback.invoke(origin, true, false)
        }

        override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            if (mUploadMessage != null) mUploadMessage!!.onReceiveValue(null)
            mUploadMessage = filePathCallback
            mFileChooser.launch("*/*")
            return true
        }

        override fun onJsAlert(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {
            jsDialog(url, message, null, result, R.string.js_page_says)
            return true
        }

        override fun onJsBeforeUnload(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {
            jsDialog(url, message, null, result, R.string.js_leave_page_prompt)
            return true
        }

        override fun onJsConfirm(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {
            jsDialog(url, message, null, result, R.string.js_page_says)
            return true
        }

        override fun onJsPrompt(
            view: WebView,
            url: String,
            message: String,
            defaultValue: String,
            result: JsPromptResult
        ): Boolean {
            jsDialog(url, message, defaultValue, result, R.string.js_page_says)
            return true
        }
    }

    /**
     * WebViewRenderProcessClient
     */
    inner class RenderClient : WebViewRenderProcessClient() {
        private var dialog = MaterialAlertDialogBuilder(mContext)
            .setTitle(R.string.dialog_page_unresponsive_title)
            .setMessage(R.string.dialog_page_unresponsive_message)
            .setPositiveButton(R.string.dialog_page_unresponsive_wait, null)
            .setNegativeButton(R.string.dialog_page_unresponsive_terminate) { _: DialogInterface?, _: Int -> mWebViewRenderProcess!!.terminate() }
            .create()

        override fun onRenderProcessUnresponsive(view: WebView, renderer: WebViewRenderProcess?) {
            dialog.show()
        }

        override fun onRenderProcessResponsive(view: WebView, renderer: WebViewRenderProcess?) {
            dialog.dismiss()
        }
    }

    private fun jsDialog(
        url: String,
        message: String,
        defaultValue: String?,
        result: JsResult,
        titleResId: Int
    ) {
        val layoutInflater = LayoutInflater.from(mContext)
        @SuppressLint("InflateParams") val root =
            layoutInflater.inflate(R.layout.dialog_edittext, null)
        val jsMessage = root.findViewById<AppCompatEditText>(R.id.edittext)
        val dialog = MaterialAlertDialogBuilder(mContext)
        dialog.setTitle(mContext.resources.getString(titleResId, url))
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                if (defaultValue == null) result.confirm() else (result as JsPromptResult).confirm(
                    Objects.requireNonNull(jsMessage.text).toString()
                )
            }
            .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
                result.cancel()
                mVioWebViewActivity!!.onFaviconProgressUpdated(false)
                mVioWebViewActivity!!.onPageLoadProgressChanged(0)
            }
        if (defaultValue != null) dialog.setView(root)
        dialog.create().show()
    }

    fun setUA(
        view: AppCompatImageView?,
        enableDesktop: Boolean,
        ua: String?,
        image: Int?,
        noReload: Boolean
    ) {
        webSettings.userAgentString = ua
        webSettings.loadWithOverviewMode = enableDesktop
        webSettings.useWideViewPort = enableDesktop
        super.setScrollBarStyle(if (enableDesktop) SCROLLBARS_OUTSIDE_OVERLAY else SCROLLBARS_INSIDE_OVERLAY)
        if (view != null) {
            view.setImageResource(image!!)
            view.tag = image
        }
        if (!noReload) webViewReload()
    }

    fun setPrebuiltUAMode(view: AppCompatImageView?, mode: Int, noReload: Boolean) {
        setUA(
            view,
            mode == 1,
            userAgentFull(mode),
            if (mode == 0) R.drawable.smartphone else R.drawable.desktop,
            noReload
        )
    }

    fun webViewReload() {
        if (currentUrl.isNullOrBlank()) return
        super.loadUrl(currentUrl!!)
    }
}