// Copyright (c) 2020-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("DEPRECATION")

package tipz.viola.webview

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_REQUIRE_DEFAULT
import android.content.Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.Settings
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.Application
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.broha.api.HistoryClient
import tipz.viola.broha.api.HistoryClient.UpdateHistoryState
import tipz.viola.broha.database.Broha
import tipz.viola.broha.database.IconHashClient
import tipz.viola.download.DownloadClient
import tipz.viola.download.DownloadObject
import tipz.viola.search.SearchEngineEntries
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.BussUtils
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.UrlUtils
import tipz.viola.webview.pages.ExportedUrls
import tipz.viola.webview.pages.PrivilegedPages
import tipz.viola.webviewui.BaseActivity


@SuppressLint("SetJavaScriptEnabled")
class VWebView(private val mContext: Context, attrs: AttributeSet?) : WebView(
    mContext, attrs
) {
    private var activity: VWebViewActivity = mContext as VWebViewActivity
    private lateinit var historyClient: HistoryClient
    val downloadClient: DownloadClient = (mContext.applicationContext as Application).downloadClient
    private val iconHashClient = IconHashClient(mContext)
    private val webSettings = this.settings
    private var currentBroha = Broha()
    private var historyState = UpdateHistoryState.STATE_COMMITTED_WAIT_TASK
    val settingsPreference =
        (mContext.applicationContext as Application).settingsPreference
    internal var adServersHandler: AdServersClient

    private val requestHeaders = HashMap<String, String>()

    private val titleHandler = Handler { message ->
        val webLongPress = HitTestAlertDialog(mContext)
        if (!webLongPress.setupDialogForShowing(this, message.data)) return@Handler false
        webLongPress.show()

        return@Handler true
    }

    enum class PageLoadState {
        PAGE_STARTED, PAGE_FINISHED, PAGE_ERROR,
        UPDATE_HISTORY, UPDATE_FAVICON, UPDATE_TITLE, UNKNOWN
    }

    init {
        /* User agent init code */
        downloadClient.vWebViewModuleInit(this)
        setUserAgent(UserAgentMode.MOBILE, UserAgentBundle().apply {
            noReload = true
        })

        /* Start the download manager service */
        setDownloadListener { vUrl: String?, _: String?, vContentDisposition: String?, vMimeType: String?, _: Long ->
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            )
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0
                )

            downloadClient.addToQueue(DownloadObject().apply {
                uriString = vUrl!!
                contentDisposition = vContentDisposition
                mimeType = vMimeType
                requestUrl = getRealUrl()
            })

            onPageInformationUpdated(PageLoadState.UNKNOWN, originalUrl!!, null)
            activity.onPageLoadProgressChanged(0)
            if (!canGoBack() && originalUrl == null && settingsPreference.getIntBool(SettingsKeys.closeAppAfterDownload))
                activity.finish()
        }
        addJavascriptInterface(VJavaScriptInterface(activity), VJavaScriptInterface.INTERFACE_NAME)

        setLayerType(LAYER_TYPE_HARDWARE, null)

        // Zoom controls
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        // Also increase text size to fill the viewport (this mirrors the behaviour of Firefox,
        // Chrome does this in the current Chrome Dev, but not Chrome release).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

        // Disable file access
        // Disabled as it no longer functions since Android 11
        webSettings.allowFileAccess = false
        webSettings.allowContentAccess = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.allowFileAccessFromFileURLs = false
            webSettings.allowUniversalAccessFromFileURLs = false
        }

        // Enable some HTML5 related settings
        webSettings.databaseEnabled = false // Disabled as no-op since Android 15
        webSettings.domStorageEnabled = true

        // Ad Server Hosts
        adServersHandler = AdServersClient(mContext, settingsPreference)

        this.webViewClient = VWebViewClient(mContext, this, adServersHandler)
        this.webChromeClient = VChromeWebClient(mContext, this)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_CLIENT_BASIC_USAGE))
            WebViewCompat.setWebViewRenderProcessClient(
                this,
                VWebViewRenderProcessClient(mContext, this)
            )

        /* Hit Test Menu */
        setOnCreateContextMenuListener { _: ContextMenu?, _: View?, _: ContextMenuInfo? ->
            val message = titleHandler.obtainMessage()
            this.requestFocusNodeHref(message)
        }
    }

    override fun destroy() {
        downloadClient.destroy()
        super.destroy()
    }

    @Suppress("deprecation")
    fun doSettingsCheck() {
        // Dark mode
        val darkMode = BaseActivity.getDarkMode(mContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && WebViewFeature.isFeatureSupported(
                WebViewFeature.ALGORITHMIC_DARKENING
            )
        )
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webSettings, darkMode)
        else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK))
            WebSettingsCompat.setForceDark(
                webSettings,
                if (darkMode) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
            )

        // Javascript
        webSettings.javaScriptEnabled =
            settingsPreference.getIntBool(SettingsKeys.isJavaScriptEnabled)
        webSettings.javaScriptCanOpenWindowsAutomatically =
            settingsPreference.getIntBool(SettingsKeys.isJavaScriptEnabled)

        // HTTPS enforce setting
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) webSettings.mixedContentMode =
            if (settingsPreference.getIntBool(SettingsKeys.enforceHttps)) WebSettings.MIXED_CONTENT_NEVER_ALLOW
            else WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Google's "Safe" Browsing
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE))
            WebSettingsCompat.setSafeBrowsingEnabled(
                webSettings,
                settingsPreference.getIntBool(SettingsKeys.enableGoogleSafeBrowse)
            )

        // WebView Debugging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(Settings.Secure.getInt(activity.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1)
        }

        // Do Not Track request
        requestHeaders["DNT"] = settingsPreference.getInt(SettingsKeys.sendDNT).toString()

        // Global Privacy Control
        requestHeaders["Sec-GPC"] = settingsPreference.getInt(SettingsKeys.sendSecGPC).toString()

        // Data Saver
        requestHeaders["Save-Data"] = settingsPreference.getInt(SettingsKeys.sendSaveData).toString()

        // Ad Servers Hosts
        if (settingsPreference.getIntBool(SettingsKeys.enableAdBlock))
            adServersHandler.importAdServers()

        // Setup history client
        if (historyState != UpdateHistoryState.STATE_DISABLED)
            historyClient = HistoryClient(activity)
    }

    fun setUpdateHistory(value: Boolean) {
        historyState = if (value) UpdateHistoryState.STATE_COMMITTED_WAIT_TASK
            else UpdateHistoryState.STATE_DISABLED
    }

    fun onSslErrorProceed() {
        activity.onSslErrorProceed()
    }

    override fun loadUrl(url: String) {
        if (url.isBlank()) return
        if (BussUtils.sendAndRequestResponse(this, url)) return

        // Check for privileged URLs
        val privilegedActualUrl = PrivilegedPages.getActualUrl(url)
        if (privilegedActualUrl != null) {
            loadRealUrl(privilegedActualUrl)
            return
        }

        // Check for view source
        if (url.startsWith(ExportedUrls.viewSourcePrefix)) loadRealUrl(url)

        // Handle App Links
        if (settingsPreference.getIntBool(SettingsKeys.checkAppLink)
            && UrlUtils.isUriLaunchable(url)) {
            var handled = false
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            webIntent.addCategory(Intent.CATEGORY_BROWSABLE)
            webIntent.setFlags(
                FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_REQUIRE_NON_BROWSER or
                        FLAG_ACTIVITY_REQUIRE_DEFAULT
            )
            val packageManager = activity.packageManager
            if (packageManager?.let { webIntent.resolveActivity(it) } != null) {
                val dialog = MaterialAlertDialogBuilder(activity)
                dialog.setTitle(R.string.dialog_open_external_title)
                    .setMessage(R.string.dialog_open_external_message)
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        try {
                            activity.startActivity(webIntent)
                            handled = true
                        } catch (e: ActivityNotFoundException) {
                            // Do not load actual url on failure
                        }
                    }
                    .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
                        // Load actual url if user cancelled the request
                        val checkedUrl = UrlUtils.toSearchOrValidUrl(settingsPreference, url)
                        super.loadUrl(checkedUrl, requestHeaders)
                    }
                    .create().show()
            }
            if (handled) return // Exit loadUrl() if handled
        } else {
            // If the URL has "viola://" prefix but hasn't been handled till here,
            // wire it up with the "chrome://" suffix.
            if (url.startsWith(ExportedUrls.violaPrefix)) {
                super.loadUrl(url.replace(ExportedUrls.violaPrefix, ExportedUrls.chromePrefix))
                return
            }

            // By this point, it is probably a webpage.
            val checkedUrl = UrlUtils.toSearchOrValidUrl(settingsPreference, url)
            onPageInformationUpdated(PageLoadState.UNKNOWN, checkedUrl, null)

            // Prevent creating duplicate entries
            if (currentBroha.url == checkedUrl && historyState != UpdateHistoryState.STATE_DISABLED)
                historyState = UpdateHistoryState.STATE_DISABLED_DUPLICATED

            super.loadUrl(checkedUrl, requestHeaders)
        }
    }

    // This should only be accessed by us!
    fun loadRealUrl(url: String) {
        super.loadUrl(url)
    }

    override fun reload() {
        if (currentBroha.url == getRealUrl() && historyState != UpdateHistoryState.STATE_DISABLED)
            historyState = UpdateHistoryState.STATE_DISABLED_DUPLICATED // Prevent duplicate entries
        loadUrl(getRealUrl())
    }

    override fun getUrl(): String {
        val superUrl = super.getUrl()
        return if (superUrl.isNullOrBlank()) ExportedUrls.aboutBlankUrl
        else filterUrl(superUrl)
    }

    fun filterUrl(url: String): String {
        return if (url.startsWith(ExportedUrls.viewSourcePrefix)) url
        else if (PrivilegedPages.shouldShowEmptyUrl(url)) CommonUtils.EMPTY_STRING
        else PrivilegedPages.getDisplayUrl(url) ?: url
    }

    fun getRealUrl(): String = super.getUrl() ?: ExportedUrls.aboutBlankUrl

    override fun goBack() {
        activity.onDropDownDismissed()
        super.goBack()
    }

    override fun goForward() {
        activity.onDropDownDismissed()
        super.goForward()
    }

    fun evaluateJavascript(script: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            super.evaluateJavascript(script, null)
        else
            super.loadUrl("javascript:function(){${script}}()");
    }

    fun onPageInformationUpdated(state: PageLoadState, url: String?, favicon: Bitmap?) {
        onPageInformationUpdated(state, url, favicon, null)
    }

    fun onPageInformationUpdated(state: PageLoadState, url: String?,
                                 favicon: Bitmap?, description: String?) {
        val currentUrl = this.url
        val newUrl = if (!url.isNullOrBlank()) filterUrl(url) else currentUrl

        when (state) {
            PageLoadState.PAGE_STARTED -> {
                if (currentUrl.startsWith(ExportedUrls.viewSourcePrefix)) return
                activity.onFaviconProgressUpdated(true)
                activity.onPageLoadProgressChanged(-1)
            }

            PageLoadState.PAGE_FINISHED -> {
                activity.onFaviconProgressUpdated(false)
                activity.onPageLoadProgressChanged(0)
                activity.onSslCertificateUpdated()
                activity.onSwipeRefreshLayoutRefreshingUpdated(false)
            }

            PageLoadState.PAGE_ERROR -> {
                var errorContent = template
                for (i in 0..5) errorContent = errorContent.replace(
                    "$$i",
                    mContext.resources.getStringArray(R.array.errMsg)[i]
                )
                errorContent = errorContent.replace("$6", "$description")

                CoroutineScope(Dispatchers.Main).launch {
                    // TODO: Figure out issue with failingUrl & historyUrl
                    // Although it is not stored in history, it could actually be an issue long term
                    loadDataWithBaseURL(
                        url, errorContent,
                        "text/html", "UTF-8", url
                    )
                }
                stopLoading()
            }

            PageLoadState.UPDATE_HISTORY -> {
                if (currentUrl.isBlank() || getRealUrl() == ExportedUrls.aboutBlankUrl) return
                if (historyState == UpdateHistoryState.STATE_COMMITTED_WAIT_TASK) {
                    currentBroha = Broha(title, currentUrl)
                    historyState = UpdateHistoryState.STATE_URL_UPDATED
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    CookieManager.getInstance().flush()
                else CookieSyncManager.getInstance().sync()
                activity.onSwipeRefreshLayoutRefreshingUpdated(false)
            }

            PageLoadState.UPDATE_FAVICON -> {
                if (currentUrl.isBlank() || getRealUrl() == ExportedUrls.aboutBlankUrl) return
                if (historyState == UpdateHistoryState.STATE_URL_UPDATED) {
                    CoroutineScope(Dispatchers.IO).launch {
                        currentBroha.iconHash = iconHashClient.save(favicon!!)
                        historyClient.insert(currentBroha)
                    }
                    historyState = UpdateHistoryState.STATE_COMMITTED_WAIT_TASK
                }
                if (historyState == UpdateHistoryState.STATE_DISABLED_DUPLICATED)
                    historyState = UpdateHistoryState.STATE_COMMITTED_WAIT_TASK
                activity.onSwipeRefreshLayoutRefreshingUpdated(false)
            }

            PageLoadState.UPDATE_TITLE -> {
                if (currentUrl.isBlank() || getRealUrl() == ExportedUrls.aboutBlankUrl) return
                activity.onTitleUpdated(
                    if (this.visibility == View.GONE) resources.getString(
                        R.string.start_page
                    ) else title?.trim()
                )
                activity.onSwipeRefreshLayoutRefreshingUpdated(false)
            }

            PageLoadState.UNKNOWN -> {
            }
        }

        activity.onUrlUpdated(newUrl)
        activity.onFaviconUpdated(favicon, false)
        activity.onDropDownDismissed()
    }

    fun onPageLoadProgressChanged(progress: Int) {
        activity.onPageLoadProgressChanged(progress)
    }

    fun setUserAgent(agentMode: UserAgentMode, dataBundle: UserAgentBundle) {
        if (agentMode == UserAgentMode.CUSTOM && dataBundle.userAgentString.isBlank()) return

        val targetResId = {
            when (agentMode) {
                UserAgentMode.MOBILE -> R.drawable.smartphone
                UserAgentMode.DESKTOP -> R.drawable.desktop
                UserAgentMode.CUSTOM -> R.drawable.custom
            }
        }
        val mobile = if (agentMode == UserAgentMode.MOBILE) "Mobile" else CommonUtils.EMPTY_STRING
        val userAgentHolder = when (agentMode) {
            UserAgentMode.MOBILE, UserAgentMode.DESKTOP -> {
                "Mozilla/5.0 (Linux) AppleWebKit/537.36 KHTML, like Gecko) Chrome/${
                    WebViewCompat.getCurrentWebViewPackage(mContext)?.versionName}" +
                        "$mobile Safari/537.36 Viola/${BuildConfig.VERSION_NAME}" + "." +
                                (BuildConfig.VERSION_BUILD_ID ?: BuildConfig.VERSION_BUILD_GIT_REVISION)
            }
            UserAgentMode.CUSTOM -> {
                dataBundle.userAgentString
            }
        }

        if (agentMode == UserAgentMode.DESKTOP) dataBundle.enableDesktop = true

        webSettings.userAgentString = userAgentHolder
        if (dataBundle.iconView != null) {
            dataBundle.iconView!!.setImageResource(targetResId())
            dataBundle.iconView!!.tag = targetResId()
        }
        webSettings.loadWithOverviewMode = dataBundle.enableDesktop
        webSettings.useWideViewPort = dataBundle.enableDesktop
        super.setScrollBarStyle(if (dataBundle.enableDesktop) SCROLLBARS_OUTSIDE_OVERLAY else SCROLLBARS_INSIDE_OVERLAY)

        if (!dataBundle.noReload) reload()
    }

    enum class UserAgentMode {
        MOBILE, DESKTOP, CUSTOM
    }

    class UserAgentBundle {
        var userAgentString = CommonUtils.EMPTY_STRING
        var iconView: AppCompatImageView? = null
        var enableDesktop = false // Defaults to true with UserAgentMode.DESKTOP
        var noReload = false
    }

    fun loadHomepage(useStartPage : Boolean) {
        if (useStartPage) {
            loadRealUrl(ExportedUrls.actualStartUrl)
        } else {
            loadUrl(SearchEngineEntries.getDefaultHomeUrl(settingsPreference))
        }

    }

    fun loadViewSourcePage(url: String?): Boolean {
        val currentUrl = if (url.isNullOrBlank()) getUrl() else url
        if (PrivilegedPages.isPrivilegedPage(url)) return false
        if (currentUrl.startsWith(ExportedUrls.viewSourcePrefix)) return false // TODO: Allow changing behaviour
        loadRealUrl("${ExportedUrls.viewSourcePrefix}$currentUrl")
        return true
    }

    companion object {
        private const val template =
            "<html>\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n<style>\np { font-family:sans-serif; font-size: 16px; }\nli { font-family:sans-serif; font-size: 16px; }\n</style>\n<head>\n<title>$0</title>\n</head>\n<body>\n<div style=\"padding-left: 8vw; padding-top: 12vh;\">\n<div>\n<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"96\" viewBox=\"0 -960 960 960\" width=\"96\">\n<path d=\"M480.134-120q-74.673 0-140.41-28.339-65.737-28.34-114.365-76.922-48.627-48.582-76.993-114.257Q120-405.194 120-479.866q0-74.673 28.339-140.41 28.34-65.737 76.922-114.365 48.582-48.627 114.257-76.993Q405.194-840 479.866-840q74.673 0 140.41 28.339 65.737 28.34 114.365 76.922 48.627 48.582 76.993 114.257Q840-554.806 840-480.134q0 74.673-28.339 140.41-28.34 65.737-76.922 114.365-48.582 48.627-114.257 76.993Q554.806-120 480.134-120ZM440-162v-78q-33 0-56.5-23.5T360-320v-40L168-552q-3 18-5.5 36t-2.5 36q0 121 79.5 212T440-162Zm276-102q20-22 36-47.5t26.5-53q10.5-27.5 16-56.5t5.5-59q0-98.58-54.115-180.059Q691.769-741.538 600-777.538V-760q0 33-23.5 56.5T520-680h-80v80q0 17-11.5 28.5T400-560h-80v80h240q17 0 28.5 11.5T600-440v120h40q26 0 47 15.5t29 40.5Z\"></path>\n</svg>\n</div>\n<div>\n<p style=\"font-weight: bold; font-size: 24px; margin-bottom: 8px;\">$1</p>\n<p style=\"margin-top: 8px; margin-bottom: 24px;\">$2</p>\n<p style=\"font-weight: bold;\">$3</p>\n<ul style=\"\">\n<li>$4</li>\n<li>$5</li>\n</ul>\n<p style=\"color: #808080;\">$6</p>\n</div>\n</div>\n</body>\n</html>"
    }
}