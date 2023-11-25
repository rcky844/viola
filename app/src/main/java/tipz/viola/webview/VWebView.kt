/*
 * Copyright (C) 2020-2023 Tipz Team
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
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Handler
import android.os.Message
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
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebIconDatabase
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewRenderProcess
import androidx.webkit.WebViewRenderProcessClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.Application
import tipz.viola.BaseActivity
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.broha.api.HistoryApi
import tipz.viola.broha.api.HistoryUtils
import tipz.viola.broha.database.Broha
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsUtils
import tipz.viola.utils.BrowservioURLs
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.DownloadUtils
import tipz.viola.utils.DownloaderThread
import tipz.viola.utils.UrlUtils
import java.io.ByteArrayInputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.Objects
import java.util.Scanner

@SuppressLint("SetJavaScriptEnabled")
class VWebView(private val mContext: Context, attrs: AttributeSet?) : WebView(
    mContext, attrs
) {
    private var mVioWebViewActivity: VWebViewActivity? = null
    private val iconHashClient = (mContext.applicationContext as Application).iconHashClient!!
    private val webSettings = this.settings
    private val mWebViewRenderProcess = if (WebViewFeature.isFeatureSupported(WebViewFeature.GET_WEB_VIEW_RENDERER)) WebViewCompat.getWebViewRenderProcess(
        this
    ) else null
    private var currentUrl: String? = null
    private var adServers: String? = null
    private var currentBroha: Broha? = null
    private var updateHistory = true
    private var historyCommitted = false
    private val pref = (mContext.applicationContext as Application).pref!!
    private var mUploadMessage: ValueCallback<Array<Uri>>? = null
    val mFileChooser = (mContext as AppCompatActivity).registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (null == mUploadMessage || uri == null) return@registerForActivityResult
        mUploadMessage!!.onReceiveValue(arrayOf(uri))
        mUploadMessage = null
    }
    private val mRequestHeaders = HashMap<String, String>()
    private fun userAgentFull(mode: Double): String {
        val info = WebViewCompat.getCurrentWebViewPackage(mContext)
        val webkitVersion = if (info == null) "534.30" else "537.36"
        return "Mozilla/5.0 (" + "Linux; Device with Viola " + BuildConfig.VERSION_NAME + ") AppleWebKit/" + webkitVersion + " KHTML, like Gecko) Chrome/" + if (info == null) "12.0.742" else info.versionName + if (mode == 0.0) " Mobile " else " Safari/$webkitVersion"
    }

    init {
        /* User agent init code */
        setPrebuiltUAMode(null, 0.0, true)

        /* Start the download manager service */
        setDownloadListener { url: String?, _: String?, contentDisposition: String?, mimeType: String?, _: Long ->
            DownloadUtils.dmDownloadFile(
                mContext, url, contentDisposition,
                mimeType, currentUrl
            )
            updateCurrentUrl(originalUrl)
            mVioWebViewActivity!!.onPageLoadProgressChanged(0)
            if (!canGoBack() && originalUrl == null && CommonUtils.isIntStrOne(
                    SettingsUtils.getPrefNum(
                        pref, SettingsKeys.closeAppAfterDownload
                    )
                )
            ) mVioWebViewActivity!!.finish()
        }
        setLayerType(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) LAYER_TYPE_HARDWARE else LAYER_TYPE_SOFTWARE,
            null
        )

        /* zoom related stuff - From SCMPNews project */webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true

        // Also increase text size to fill the viewport (this mirrors the behaviour of Firefox,
        // Chrome does this in the current Chrome Dev, but not Chrome release).
        webSettings.layoutAlgorithm =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING else WebSettings.LayoutAlgorithm.NORMAL
        webSettings.displayZoomControls = false
        webSettings.allowFileAccess = false
        webSettings.allowContentAccess = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.allowFileAccessFromFileURLs = false
            webSettings.allowUniversalAccessFromFileURLs = false
        }

        /* HTML5 API flags */webSettings.databaseEnabled = false
        webSettings.domStorageEnabled = true
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) WebIconDatabase.getInstance()
            .open(mContext.getDir("icons", Context.MODE_PRIVATE).path)
        this.webViewClient = WebClient()
        this.webChromeClient = ChromeWebClient()
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_CLIENT_BASIC_USAGE) && mWebViewRenderProcess != null) WebViewCompat.setWebViewRenderProcessClient(
            this,
            RenderClient()
        )
        removeJavascriptInterface("searchBoxJavaBridge_") /* CVE-2014-1939 */
        removeJavascriptInterface("accessibility") /* CVE-2014-7224 */
        removeJavascriptInterface("accessibilityTraversal") /* CVE-2014-7224 */

        /* Hit Test Menu */setOnCreateContextMenuListener { _: ContextMenu?, _: View?, _: ContextMenuInfo? ->
            val hr = this.hitTestResult
            val url = hr.extra
            val type = hr.type
            if (type == HitTestResult.UNKNOWN_TYPE || type == HitTestResult.EDIT_TEXT_TYPE) return@setOnCreateContextMenuListener
            val webLongPress = MaterialAlertDialogBuilder(mContext)
            webLongPress.setTitle(if (url!!.length > 75) url.substring(0, 74) + "…" else url)
            val arrayAdapter = ArrayAdapter<String>(mContext, R.layout.recycler_list_item_1)
            if (type == HitTestResult.SRC_ANCHOR_TYPE) arrayAdapter.add(resources.getString(R.string.open_in_new_tab))
            if (type == HitTestResult.IMAGE_TYPE || type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                arrayAdapter.add(resources.getString(R.string.download_image))
                arrayAdapter.add(resources.getString(R.string.search_image))
            }
            arrayAdapter.add(resources.getString(R.string.copy_url))
            arrayAdapter.add(resources.getString(R.string.share_url))
            webLongPress.setAdapter(arrayAdapter) { _: DialogInterface?, which: Int ->
                when (arrayAdapter.getItem(which)) {
                    resources.getString(R.string.copy_url) -> {
                        CommonUtils.copyClipboard(mContext, url)
                    }
                    resources.getString(R.string.download_image) -> {
                        DownloadUtils.dmDownloadFile(
                            mContext, url,
                            null, null, url
                        )
                    }
                    resources.getString(R.string.search_image) -> {
                        this.loadUrl("http://images.google.com/searchbyimage?image_url=$url")
                    }
                    resources.getString(R.string.open_in_new_tab) -> {
                        val intent = Intent(mContext, BrowserActivity::class.java)
                        intent.putExtra(Intent.EXTRA_TEXT, url)
                            .setAction(Intent.ACTION_SEND).type = UrlUtils.TypeSchemeMatch[1]
                        mContext.startActivity(intent)
                    }
                    resources.getString(R.string.share_url) -> {
                        CommonUtils.shareUrl(mContext, url)
                    }
                }
            }
            webLongPress.show()
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
            CommonUtils.isIntStrOne(
                SettingsUtils.getPrefNum(
                    pref,
                    SettingsKeys.isJavaScriptEnabled
                )
            )
        webSettings.javaScriptCanOpenWindowsAutomatically =
            CommonUtils.isIntStrOne(
                SettingsUtils.getPrefNum(
                    pref,
                    SettingsKeys.isJavaScriptEnabled
                )
            )

        // HTTPS enforce setting
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) webSettings.mixedContentMode =
            if (CommonUtils.isIntStrOne(
                    SettingsUtils.getPrefNum(pref, SettingsKeys.enforceHttps)
                )
            ) WebSettings.MIXED_CONTENT_NEVER_ALLOW else WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Google's "Safe" Browsing
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) WebSettingsCompat.setSafeBrowsingEnabled(
            webSettings,
            CommonUtils.isIntStrOne(
                SettingsUtils.getPrefNum(
                    pref,
                    SettingsKeys.enableGoogleSafeBrowse
                )
            )
        )

        // Do Not Track request
        mRequestHeaders["DNT"] = SettingsUtils.getPrefNum(pref, SettingsKeys.sendDNT).toString()
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
        val urlIdentify = URLIdentify(url)
        if (urlIdentify == CommonUtils.EMPTY_STRING) return
        val checkedUrl = UrlUtils.toSearchOrValidUrl(mContext, urlIdentify)
        updateCurrentUrl(checkedUrl)

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

    private fun updateCurrentUrl(url: String?) {
        mVioWebViewActivity!!.onUrlUpdated(url)
        currentUrl = url
    }

    /**
     * WebViewClient
     */
    inner class WebClient : WebViewClientCompat() {
        private fun UrlSet(url: String) {
            if (currentUrl != url && urlShouldSet(url) || currentUrl == null) updateCurrentUrl(url)
        }

        override fun onPageStarted(view: WebView, url: String, icon: Bitmap?) {
            UrlSet(url)
            mVioWebViewActivity!!.onFaviconProgressUpdated(true)
            mVioWebViewActivity!!.onFaviconUpdated(null, false)
            mVioWebViewActivity!!.onDropDownDismissed()
        }

        override fun onPageFinished(view: WebView, url: String) {
            if (view.originalUrl == null || view.originalUrl == url) doUpdateVisitedHistory(
                view,
                url,
                true
            )
            mVioWebViewActivity!!.onFaviconProgressUpdated(false)
        }

        override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
            UrlSet(url)
            if (updateHistory) {
                currentBroha = Broha(title, currentUrl)
                historyCommitted = false
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) CookieSyncManager.getInstance()
                .sync() else CookieManager.getInstance().flush()
            mVioWebViewActivity!!.onFaviconUpdated(null, true)
            mVioWebViewActivity!!.onSwipeRefreshLayoutRefreshingUpdated(false)
        }

        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String
        ) {
            var returnVal = template
            for (i in 0..5) returnVal = returnVal.replace(
                "$$i",
                mContext.resources.getStringArray(R.array.errMsg)[i]
            )
            returnVal = returnVal.replace("$6", description)
            view.loadDataWithBaseURL(null, returnVal, "text/html", "UTF-8", null)
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (UrlUtils.isUriLaunchable(url)) return false
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(UrlUtils.cve_2017_13274(url)))
                mContext.startActivity(intent)
            } catch (ignored: ActivityNotFoundException) {
                CommonUtils.showMessage(
                    mContext,
                    resources.getString(R.string.toast_no_app_to_handle)
                )
            }
            return true
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            val dialog = MaterialAlertDialogBuilder(mContext)
            var content_summary = resources.getString(R.string.ssl_certificate_unknown)
            when (error.primaryError) {
                SslError.SSL_DATE_INVALID -> content_summary =
                    resources.getString(R.string.ssl_certificate_date_invalid)

                SslError.SSL_INVALID -> content_summary =
                    resources.getString(R.string.ssl_certificate_invalid)

                SslError.SSL_EXPIRED -> content_summary =
                    resources.getString(R.string.ssl_certificate_expired)

                SslError.SSL_IDMISMATCH -> content_summary =
                    resources.getString(R.string.ssl_certificate_idmismatch)

                SslError.SSL_NOTYETVALID -> content_summary =
                    resources.getString(R.string.ssl_certificate_notyetvalid)

                SslError.SSL_UNTRUSTED -> content_summary =
                    resources.getString(R.string.ssl_certificate_untrusted)
            }
            dialog.setTitle(resources.getString(R.string.ssl_certificate_error_dialog_title))
                .setMessage(
                    resources.getString(
                        R.string.ssl_certificate_error_dialog_content,
                        content_summary
                    )
                )
                .setPositiveButton(resources.getString(android.R.string.ok)) { _: DialogInterface?, _: Int -> handler.proceed() }
                .setNegativeButton(resources.getString(android.R.string.cancel)) { _: DialogInterface?, _: Int -> handler.cancel() }
                .create().show()
        }

        override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
            return false
        }

        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
            if (adServers == null) updateAdServerList()
            try {
                if (adServers != null) if (adServers!!.contains(" " + URL(url).host) && SettingsUtils.getPrefNum(
                        pref,
                        SettingsKeys.enableAdBlock
                    ) == 1
                ) return WebResourceResponse(
                    "text/plain",
                    "utf-8",
                    ByteArrayInputStream(CommonUtils.EMPTY_STRING.toByteArray())
                )
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
            return super.shouldInterceptRequest(view, url)
        }
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
            if (!historyCommitted && updateHistory) {
                currentBroha!!.iconHash = iconHashClient.save(icon)
                currentBroha!!.title = title // For making sure title is up to date
                if (HistoryUtils.lastUrl(mContext) != currentUrl) HistoryApi.historyBroha(mContext)
                    .insertAll(currentBroha)
                historyCommitted = true
            }
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            mVioWebViewActivity!!.onTitleUpdated(title)
        }

        override fun onGeolocationPermissionsShowPrompt(
            origin: String,
            callback: GeolocationPermissions.Callback
        ) {
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
        var dialog = MaterialAlertDialogBuilder(mContext)
            .setTitle(R.string.dialog_page_unresponsive_title)
            .setMessage(R.string.dialog_page_unresponsive_message)
            .setPositiveButton(R.string.dialog_page_unresponsive_wait, null)
            .setNegativeButton(R.string.dialog_page_unresponsive_terminate) { _dialog: DialogInterface?, _which: Int -> mWebViewRenderProcess!!.terminate() }
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

    /* Function to update the list of Ad servers */
    private fun updateAdServerList() {
        adServers = CommonUtils.EMPTY_STRING
        val mHandlerThread = DownloaderThread("adServers")
        mHandlerThread.start()
        mHandlerThread.setCallerHandler(object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    DownloaderThread.TYPE_SUCCESS -> {
                        val data = msg.data.getString(DownloaderThread.MSG_RESPONSE)
                        if (data != null) {
                            val scanner = Scanner(data)
                            val builder = StringBuilder()
                            while (scanner.hasNextLine()) {
                                val line = scanner.nextLine()
                                if (line.startsWith("127.0.0.1 ")) builder.append(line)
                                    .append(CommonUtils.LINE_SEPARATOR())
                            }
                            adServers = builder.toString()
                        }
                    }

                    DownloaderThread.TYPE_FAILED -> adServers = null
                }
                mHandlerThread.quit()
                super.handleMessage(msg)
            }
        })
        mHandlerThread.startDownload("https://raw.githubusercontent.com/AdAway/adaway.github.io/master/hosts.txt")
    }

    private fun urlShouldSet(url: String): Boolean {
        return !(url == "about:blank" || url.startsWith(BrowservioURLs.prefix))
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

    fun setPrebuiltUAMode(view: AppCompatImageView?, mode: Double, noReload: Boolean) {
        setUA(
            view,
            mode == 1.0,
            userAgentFull(mode),
            if (mode == 0.0) R.drawable.smartphone else R.drawable.desktop,
            noReload
        )
    }

    fun webViewReload() {
        if (currentUrl.isNullOrBlank()) return
        super.loadUrl(currentUrl!!)
    }

    /**
     * URL identify module
     *
     *
     * This module/function identifies a supplied
     * URL to check for it's nature.
     *
     * @param url is supplied for the url to check
     * @return url to load
     */
    private fun URLIdentify(url: String): String {
        if (url == BrowservioURLs.licenseUrl || url == BrowservioURLs.realLicenseUrl) return BrowservioURLs.realLicenseUrl
        if (url == BrowservioURLs.reloadUrl) {
            webViewReload()
            return CommonUtils.EMPTY_STRING
        }
        val startPageLayout = mVioWebViewActivity!!.startPageLayout
        if (url == BrowservioURLs.startUrl) {
            this.visibility = GONE
            startPageLayout.visibility = VISIBLE
            return CommonUtils.EMPTY_STRING
        }
        if (this.visibility == GONE) {
            this.visibility = VISIBLE
            startPageLayout.visibility = GONE
        }
        return url
    }

    companion object {
        private const val template =
            "<html>\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n<head>\n<title>$0</title>\n</head>\n<body>\n<div style=\"padding-left: 8vw; padding-top: 12vh;\">\n<div>\n<svg xmlns=\"http://www.w3.org/2000/svg\" enable-background=\"new 0 0 24 24\" height=\"96px\" viewBox=\"0 0 24 24\" width=\"96px\" fill=\"currentColor\">\n<g>\n<rect fill=\"none\" height=\"24\" width=\"24\"/>\n<path d=\"M11,8.17L6.49,3.66C8.07,2.61,9.96,2,12,2c5.52,0,10,4.48,10,10c0,2.04-0.61,3.93-1.66,5.51l-1.46-1.46 C19.59,14.87,20,13.48,20,12c0-3.35-2.07-6.22-5-7.41V5c0,1.1-0.9,2-2,2h-2V8.17z M21.19,21.19l-1.41,1.41l-2.27-2.27 C15.93,21.39,14.04,22,12,22C6.48,22,2,17.52,2,12c0-2.04,0.61-3.93,1.66-5.51L1.39,4.22l1.41-1.41L21.19,21.19z M11,18 c-1.1,0-2-0.9-2-2v-1l-4.79-4.79C4.08,10.79,4,11.38,4,12c0,4.08,3.05,7.44,7,7.93V18z\"/>\n</g>\n</svg>\n</div>\n<div>\n<p style=\"font-family:sans-serif; font-weight: bold; font-size: 24px; margin-top: 24px; margin-bottom: 8px;\">$1</p>\n<p style=\"font-family:sans-serif; font-size: 16px; margin-top: 8px; margin-bottom: 24px;\">$2</p>\n<p style=\"font-family:sans-serif; font-weight: bold; font-size: 16px; margin-bottom: 8px;\">$3</p>\n<ul style=\"font-family:sans-serif; font-size: 16px; margin-top: 0px; margin-bottom: 0px;\">\n<li>$4</li>\n<li>$5</li>\n</ul>\n<p style=\"font-family:sans-serif; font-size: 12px; margin-bottom: 8px; color: #808080;\">$6</p>\n</div>\n</div>\n</body>\n</html>"
    }
}