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
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.View
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
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

@SuppressLint("SetJavaScriptEnabled")
class VWebView(private val mContext: Context, attrs: AttributeSet?) : WebView(
    mContext, attrs
) {
    private var mVioWebViewActivity: VWebViewActivity? = null
    private val iconHashClient = (mContext.applicationContext as Application).iconHashClient!!
    private val webSettings = this.settings
    private var currentUrl: String? = null
    private var currentBroha: Broha? = null
    private var updateHistory = true
    private var historyCommitted = false
    private val settingsPreference =
        (mContext.applicationContext as Application).settingsPreference!!
    private var adServersHandler: AdServersHandler

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

    enum class PageLoadState {
        PAGE_STARTED, PAGE_FINISHED, UPDATE_HISTORY, UPDATE_FAVICON, UPDATE_TITLE, UNKNOWN
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
            onPageInformationUpdated(PageLoadState.UNKNOWN, originalUrl!!, null)
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

        // Ad Server Hosts
        adServersHandler = AdServersHandler(mContext, settingsPreference)

        /* HTML5 API flags */
        webSettings.databaseEnabled = false
        webSettings.domStorageEnabled = true

        this.webViewClient = VWebViewClient(mContext, this, adServersHandler)
        this.webChromeClient = VChromeWebClient(mContext, this)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_CLIENT_BASIC_USAGE))
            WebViewCompat.setWebViewRenderProcessClient(this,
                VWebViewRenderProcessClient(mContext, this)
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

        // Ad Servers Hosts
        if (settingsPreference.getIntBool(SettingsKeys.enableAdBlock))
            adServersHandler.importAdServers()
    }

    fun notifyViewSetup() {
        mVioWebViewActivity = mContext as VWebViewActivity
    }

    fun setUpdateHistory(value: Boolean) {
        updateHistory = value
    }

    fun onSslErrorProceed() {
        mVioWebViewActivity?.onSslErrorProceed()
    }

    override fun loadUrl(url: String) {
        if (url.isEmpty()) return
        if (url == InternalUrls.aboutBlankUrl) {
            super.loadUrl(url)
            return
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

        if (url == InternalUrls.updateAdServersHostsUrl) {
            adServersHandler.downloadAdServers() // TODO: Add dialogs to show progress
            return
        }

        // Update to start page layout
        val startPageLayout = mVioWebViewActivity?.startPageLayout
        if (url == InternalUrls.startUrl) {
            this.loadUrl(InternalUrls.aboutBlankUrl)
            this.visibility = GONE
            mVioWebViewActivity?.swipeRefreshLayout?.visibility = GONE
            startPageLayout?.visibility = VISIBLE
            mVioWebViewActivity?.onSslCertificateUpdated()
            return
        }
        if (this.visibility == GONE) {
            this.visibility = VISIBLE
            mVioWebViewActivity?.swipeRefreshLayout?.visibility = VISIBLE
            startPageLayout?.visibility = GONE
        }

        val checkedUrl = UrlUtils.toSearchOrValidUrl(mContext, url)
        onPageInformationUpdated(PageLoadState.UNKNOWN, checkedUrl, null)

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

    fun onPageInformationUpdated(state : PageLoadState, url: String?, favicon: Bitmap?) {
        if (url == InternalUrls.aboutBlankUrl) return
        if (url != null) currentUrl = url

        when (state) {
            PageLoadState.PAGE_STARTED -> {
                mVioWebViewActivity!!.onFaviconProgressUpdated(true)
                mVioWebViewActivity!!.onPageLoadProgressChanged(-1)
            }
            PageLoadState.PAGE_FINISHED -> {
                mVioWebViewActivity!!.onFaviconProgressUpdated(false)
                mVioWebViewActivity!!.onPageLoadProgressChanged(0)
                mVioWebViewActivity!!.onSslCertificateUpdated()
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

            PageLoadState.UPDATE_FAVICON -> {
                if (!historyCommitted && updateHistory) {
                    CoroutineScope(Dispatchers.IO).launch {
                        currentBroha!!.iconHash = iconHashClient.save(favicon!!)
                        if (HistoryUtils.lastUrl(mContext) != currentUrl) {
                            HistoryApi.historyBroha(mContext)!!.insertAll(currentBroha!!)
                        }
                    }
                    historyCommitted = true
                }
            }

            PageLoadState.UPDATE_TITLE -> {
                mVioWebViewActivity!!.onTitleUpdated(if (this.visibility == View.GONE) resources.getString(R.string.start_page) else title)
            }

            PageLoadState.UNKNOWN -> {
            }
        }

        if (url != null) mVioWebViewActivity!!.onUrlUpdated(url)
        mVioWebViewActivity!!.onFaviconUpdated(favicon, false)
        mVioWebViewActivity!!.onDropDownDismissed()
    }

    fun onPageLoadProgressChanged(progress : Int) {
        mVioWebViewActivity!!.onPageLoadProgressChanged(progress)
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
        if (currentUrl.isNullOrBlank() || currentUrl == InternalUrls.aboutBlankUrl) return
        super.loadUrl(currentUrl!!)
    }
}