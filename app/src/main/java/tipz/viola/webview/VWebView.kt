// Copyright (c) 2020-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("DEPRECATION")

package tipz.viola.webview

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import tipz.viola.Application
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.database.Broha
import tipz.viola.database.instances.HistoryClient
import tipz.viola.database.instances.HistoryClient.UpdateHistoryState
import tipz.viola.database.instances.IconHashClient
import tipz.viola.download.DownloadClient
import tipz.viola.download.database.Droha
import tipz.viola.ext.showMessage
import tipz.viola.search.SearchEngineEntries
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.UrlUtils
import tipz.viola.webview.activity.BaseActivity
import tipz.viola.webview.activity.BrowserActivity
import tipz.viola.webview.buss.BussUtils
import tipz.viola.webview.pages.BrowserUrls
import tipz.viola.webview.pages.PrivilegedPages
import tipz.viola.webview.pages.ProjectUrls
import java.util.regex.Pattern

@SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
class VWebView(private val context: Context, attrs: AttributeSet?) : WebView(
    context, attrs
) {
    private val LOG_TAG = "VWebView"

    private var activity: VWebViewActivity = context as VWebViewActivity
    private lateinit var historyClient: HistoryClient
    val downloadClient: DownloadClient = (context.applicationContext as Application).downloadClient
    private val iconHashClient = IconHashClient(context)
    val webSettings = this.settings
    private var currentBroha = Broha()
    var currentFavicon: Bitmap? = null
    private var historyState = UpdateHistoryState.STATE_COMMITTED_WAIT_TASK
    var loadProgress = 100
    val settingsPreference =
        (context.applicationContext as Application).settingsPreference
    internal var adServersHandler: AdServersClient
    private val initialUserAgent = settings.userAgentString

    private val requestHeaders = HashMap<String, String>()
    var consoleLogging = false
    val consoleMessages = StringBuilder()

    private val titleHandler = Handler { message ->
        val webLongPress = HitTestAlertDialog(context)
        if (!webLongPress.setupDialogForShowing(this, message.data)) return@Handler false
        webLongPress.show()

        return@Handler true
    }

    private val template = context.assets.open("error.html")
        .bufferedReader().use { it.readText() }

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
        setDownloadListener { vUrl: String?, _: String?,
                              vContentDisposition: String?, vMimeType: String?, _: Long ->
            Log.d(LOG_TAG, """
                Incoming download request
                URL: $vUrl
                Content Disposition: $vContentDisposition
                MIME Type: $vMimeType
            """.trimIndent())

            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)

            downloadClient.launchDownload(Droha().apply {
                uriString = vUrl!!
                contentDisposition = vContentDisposition
                mimeType = vMimeType
                requestUrl = getRealUrl()
                showDialog = true
            })

            onPageInformationUpdated(PageLoadState.UNKNOWN, originalUrl!!, null)
            onPageLoadProgressChanged(0)
            if (!canGoBack() && originalUrl == null && settingsPreference.getIntBool(SettingsKeys.closeAppAfterDownload))
                activity.finish()
        }

        // JavaScript interface
        addJavascriptInterface(VJavaScriptInterface(activity), VJavaScriptInterface.INTERFACE_NAME)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            removeJavascriptInterface("searchBoxJavaBridge_") /* CVE-2014-1939 */
            removeJavascriptInterface("accessibility") /* CVE-2014-7224 */
            removeJavascriptInterface("accessibilityTraversal") /* CVE-2014-7224 */
        }

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
        webSettings.savePassword = false

        // Ad Server Hosts
        adServersHandler = AdServersClient(context, settingsPreference)

        this.webViewClient = VWebViewClient(context, this, adServersHandler)
        this.webChromeClient = VChromeWebClient(activity, this)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_CLIENT_BASIC_USAGE))
            WebViewCompat.setWebViewRenderProcessClient(
                this,
                VWebViewRenderProcessClient(this)
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
        val darkMode = BaseActivity.isDarkMode(context)
        val forceDark = settingsPreference.getIntBool(SettingsKeys.useForceDark)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webSettings, darkMode && forceDark)
        } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            if (darkMode && forceDark) WebSettingsCompat.FORCE_DARK_ON
            else WebSettingsCompat.FORCE_DARK_OFF
        }

        // Javascript
        settingsPreference.getIntBool(SettingsKeys.isJavaScriptEnabled).apply {
            webSettings.javaScriptEnabled = this
            webSettings.javaScriptCanOpenWindowsAutomatically = this

        }

        // HTTPS enforce setting
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.mixedContentMode =
                if (settingsPreference.getIntBool(SettingsKeys.enforceHttps))
                    WebSettings.MIXED_CONTENT_NEVER_ALLOW
                else WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        // Google's "Safe" Browsing
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE))
            WebSettingsCompat.setSafeBrowsingEnabled(
                webSettings,
                settingsPreference.getIntBool(SettingsKeys.enableGoogleSafeBrowse)
            )

        // WebView Debugging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(Settings.Global.getInt(context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1)
        }

        // Do Not Track request
        requestHeaders["DNT"] = settingsPreference.getInt(SettingsKeys.sendDNT).toString()

        // Global Privacy Control
        requestHeaders["Sec-GPC"] = settingsPreference.getInt(SettingsKeys.sendSecGPC).toString()

        // Data Saver
        requestHeaders["Save-Data"] = settingsPreference.getInt(SettingsKeys.sendSaveData).toString()

        // Setup history client
        if (historyState != UpdateHistoryState.STATE_DISABLED) {
            historyClient = HistoryClient(context)
            historyClient.doSettingsCheck()
        }
    }

    fun setUpdateHistory(value: Boolean) {
        historyState = if (value) UpdateHistoryState.STATE_COMMITTED_WAIT_TASK
        else UpdateHistoryState.STATE_DISABLED
    }

    fun onSslErrorProceed() {
        activity.onSslErrorProceed()
    }

    @SuppressLint("InlinedApi")
    fun loadAppLinkUrl(url: String, noToast: Boolean = false): Boolean {
        if (UrlUtils.isUriSupported(url)) return false

        Log.i(LOG_TAG, "Checking for possible App Link, url=$url")
        val intent =
            if (url.startsWith("intent://")) Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            else Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(context.packageManager) != null) {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_open_external_title)
                .setMessage(R.string.dialog_open_external_message)
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    context.startActivity(intent)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
            return true
        } else {
            if (!noToast && (loadProgress == 0 || loadProgress == 100)) {
                Log.v(LOG_TAG, "App Link not handled and page loaded, showing toast")
                context.showMessage(R.string.toast_no_app_to_handle)
            }
            Log.w(LOG_TAG, "Found no application to handle App Link!")
            return false
        }
    }

    override fun loadUrl(inUrl: String) = inUrl.trim().let { url ->
        if (url.isEmpty()) return
        if (BussUtils.sendAndRequestResponse(this, url)) return

        // Check for privileged URLs
        if (PrivilegedPages.isPrivilegedPage(url)) {
            super.loadUrl(url)
            return
        }

        val privilegedActualUrl = PrivilegedPages.getActualUrl(url)
        if (privilegedActualUrl != null) {
            loadRealUrl(privilegedActualUrl)
            return
        }

        // Check for view source
        if (url.startsWith(BrowserUrls.viewSourcePrefix)) loadRealUrl(url)

        // Handle "chrome://" and "viola://" prefix (i.e. browser handling URLs)
        val isViolaUrl = url.startsWith(BrowserUrls.violaPrefix)
        val isChromeUrl = url.startsWith(BrowserUrls.chromePrefix)
        if (isViolaUrl || isChromeUrl) {
            // If the URL has "viola://" prefix but hasn't been handled till here,
            // wire it up with the "chrome://" prefix.
            val handlingSuffix = url
                .replaceFirst(BrowserUrls.violaPrefix, "", true)
                .replaceFirst(BrowserUrls.chromePrefix, "", true)

            // Browser mode specific URLs
            activity.takeIf { it is BrowserActivity }?.let { it as BrowserActivity
                // Bookmarks / Favorites
                // "favorites" does not exist as "chrome://" prefix,
                // so limit to "viola://" prefix.
                if (handlingSuffix.startsWith(BrowserUrls.bookmarksChromeSuffix)
                    || (isViolaUrl && handlingSuffix.startsWith(BrowserUrls.favouritesViolaSuffix))) {
                    it.itemSelected(null, R.drawable.favorites)
                    return
                }

                // History
                if (handlingSuffix.startsWith(BrowserUrls.historyChromeSuffix)) {
                    it.itemSelected(null, R.drawable.history)
                    return
                }

                // New Tab Page
                if (handlingSuffix.startsWith(BrowserUrls.newTabPageChromeSuffix)) {
                    loadHomepage(true)
                    return
                }

                // New Tab Page (Third party)
                if (handlingSuffix.startsWith(BrowserUrls.newTabPageThirdPartyChromeSuffix)) {
                    loadHomepage(false)
                    return
                }
            }

            // Quit
            if (handlingSuffix.startsWith(BrowserUrls.quitChromeSuffix)) {
                activity.finish()
                return
            }

            super.loadUrl("${BrowserUrls.chromePrefix}$handlingSuffix")
            return
        }

        // By this point, it is probably a webpage or a search query.
        val checkedUrl = UrlUtils.UrlOrSearchValidator.validate(settingsPreference, inUrl)
        if (UrlUtils.UrlOrSearchValidator.isSearch) {
            // Handle App Links
            if (loadAppLinkUrl(url, true)) return
        }
        onPageInformationUpdated(PageLoadState.UNKNOWN, checkedUrl, null)

        // Prevent creating duplicate entries
        if (currentBroha.url == checkedUrl && historyState != UpdateHistoryState.STATE_DISABLED)
            historyState = UpdateHistoryState.STATE_DISABLED_DUPLICATED

        super.loadUrl(checkedUrl, requestHeaders)
    }

    // This should only be accessed by us!
    fun loadRealUrl(url: String) {
        if (url.isBlank()) super.loadUrl(BrowserUrls.aboutBlankUrl)
        super.loadUrl(url)
    }

    override fun reload() {
        if (currentBroha.url == getRealUrl() && historyState != UpdateHistoryState.STATE_DISABLED)
            historyState = UpdateHistoryState.STATE_DISABLED_DUPLICATED // Prevent duplicate entries
        loadUrl(getUrl())
    }

    override fun getUrl(): String {
        val superUrl = super.getUrl()
        return if (superUrl.isNullOrBlank()) BrowserUrls.aboutBlankUrl
        else filterUrl(superUrl)
    }

    fun filterUrl(url: String): String {
        return if (url.startsWith(BrowserUrls.viewSourcePrefix))
            // TODO: This causes reload button to show cross icon, why?
            url.replace(BrowserUrls.viewSourcePrefix, "")
        else if (PrivilegedPages.shouldShowEmptyUrl(url)) ""
        else PrivilegedPages.getDisplayUrl(url) ?: url
    }

    fun getRealUrl(): String = super.getUrl() ?: BrowserUrls.aboutBlankUrl

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
                if (currentUrl.startsWith(BrowserUrls.viewSourcePrefix)) return
                onPageLoadProgressChanged(-1)
                activity.onFaviconProgressUpdated(true)
                consoleMessages.clear()
            }

            PageLoadState.PAGE_FINISHED -> {
                onPageLoadProgressChanged(0)
                activity.onFaviconProgressUpdated(false)
                activity.onSslCertificateUpdated()
                activity.swipeRefreshLayout.setRefreshing(false)
            }

            PageLoadState.PAGE_ERROR -> {
                var errorContent = template
                for (i in 0..5) errorContent = errorContent.replace(
                    "$$i",
                    context.resources.getStringArray(R.array.errMsg)[i]
                )
                errorContent = errorContent.replace("$6", "$description")

                MainScope().launch {
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
                if (currentUrl.isBlank() || getRealUrl() == BrowserUrls.aboutBlankUrl) return
                if (historyState == UpdateHistoryState.STATE_COMMITTED_WAIT_TASK) {
                    currentBroha = Broha(title, currentUrl)
                    historyState = UpdateHistoryState.STATE_URL_UPDATED
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    CookieManager.getInstance().flush()
                else CookieSyncManager.getInstance().sync()
                activity.swipeRefreshLayout.setRefreshing(false)
            }

            PageLoadState.UPDATE_FAVICON -> {
                if (currentUrl.isBlank() || getRealUrl() == BrowserUrls.aboutBlankUrl) return
                if (historyState == UpdateHistoryState.STATE_URL_UPDATED) {
                    CoroutineScope(Dispatchers.IO).launch {
                        currentBroha.iconHash = iconHashClient.save(favicon!!)
                        historyClient.insert(currentBroha)
                    }
                    historyState = UpdateHistoryState.STATE_COMMITTED_WAIT_TASK
                }
                if (historyState == UpdateHistoryState.STATE_DISABLED_DUPLICATED)
                    historyState = UpdateHistoryState.STATE_COMMITTED_WAIT_TASK
                activity.swipeRefreshLayout.setRefreshing(false)
            }

            PageLoadState.UPDATE_TITLE -> {
                if (currentUrl.isBlank() || getRealUrl() == BrowserUrls.aboutBlankUrl) return
                activity.onTitleUpdated(
                    if (this.visibility == View.GONE) resources.getString(R.string.start_page)
                    else title?.trim()
                )
                activity.swipeRefreshLayout.setRefreshing(false)
            }

            PageLoadState.UNKNOWN -> {
            }
        }

        // Update favicon
        currentFavicon = favicon
        activity.onFaviconUpdated(favicon, state == PageLoadState.UPDATE_FAVICON)

        activity.onUrlUpdated(newUrl)
        activity.onDropDownDismissed()
    }

    fun onPageLoadProgressChanged(progress: Int) {
        loadProgress = progress
        activity.onPageLoadProgressChanged(progress)
    }

    fun setUserAgent(agentMode: UserAgentMode, dataBundle: UserAgentBundle) {
        if (agentMode == UserAgentMode.CUSTOM && dataBundle.userAgentString.isBlank()) return

        // Set user agent string
        val userAgentBuilder = StringBuilder()
        if (agentMode == UserAgentMode.CUSTOM) {
            userAgentBuilder.append(dataBundle.userAgentString)
        } else {
            val pattern = Pattern.compile("\\(.*?\\)\\s|.*?/.*?(\\s|\$)")
            val matcher = pattern.matcher(initialUserAgent)
            while (matcher.find()) {
                var group = matcher.group()
                when (agentMode) {
                    UserAgentMode.MOBILE -> { }
                    UserAgentMode.DESKTOP -> {
                        // Replace Android version and build number
                        if (group.contains("Linux; Android "))
                            group = "(Linux) "

                        // Remove references to Mobile Safari
                        if (group.startsWith("Mobile Safari"))
                            group = group.replace("Mobile ", "")
                    }
                    else -> { }
                }

                // Don't declare ourselves as a WebView, this breaks many sites
                // as they may thing we can only provide a simple WebView.
                // "wv" and "Version/4.0" is removed.
                val wvRegex = "\\((.*)?;\\s?wv((;\\s.*)?)\\)".toRegex()
                if (group.contains(wvRegex))
                    group = group.replace(wvRegex, "(\$1\$2)")

                if (group.contains("Version/4.0"))
                    continue

                // Add to builder
                userAgentBuilder.append(group)
            }
            userAgentBuilder.append(" Viola/${BuildConfig.VERSION_NAME}")
        }
        webSettings.userAgentString = userAgentBuilder.toString()

        // Handle view related things
        val targetResId = {
            when (agentMode) {
                UserAgentMode.MOBILE -> R.drawable.smartphone
                UserAgentMode.DESKTOP -> R.drawable.desktop
                UserAgentMode.CUSTOM -> R.drawable.custom
            }
        }
        if (agentMode == UserAgentMode.DESKTOP) dataBundle.enableDesktop = true

        if (dataBundle.iconView != null) {
            dataBundle.iconView!!.setImageResource(targetResId())
            dataBundle.iconView!!.tag = targetResId()
        }
        webSettings.loadWithOverviewMode = dataBundle.enableDesktop
        webSettings.useWideViewPort = dataBundle.enableDesktop
        super.setScrollBarStyle(if (dataBundle.enableDesktop)
            SCROLLBARS_OUTSIDE_OVERLAY else SCROLLBARS_INSIDE_OVERLAY)

        if (!dataBundle.noReload) reload()
    }

    enum class UserAgentMode {
        MOBILE, DESKTOP, CUSTOM
    }

    class UserAgentBundle {
        var userAgentString = ""
        var iconView: AppCompatImageView? = null
        var enableDesktop = false // Defaults to true with UserAgentMode.DESKTOP
        var noReload = false
    }

    fun checkHomePageVisibility() {
        if (activity is BrowserActivity) {
            return (activity as BrowserActivity).checkHomePageVisibility()
        } else {
            return
        }
    }

    fun loadHomepage(useStartPage : Boolean) {
        if (useStartPage) {
            loadRealUrl(ProjectUrls.actualStartUrl)
        } else {
            loadUrl(SearchEngineEntries.getPreferredHomePageUrl(settingsPreference))
        }
    }

    fun loadViewSourcePage(url: String?): Boolean {
        val currentUrl = if (url.isNullOrBlank()) getUrl() else url
        if (PrivilegedPages.isPrivilegedPage(getRealUrl())) return false
        if (currentUrl.startsWith(BrowserUrls.viewSourcePrefix)) return false // TODO: Allow changing behaviour
        loadRealUrl("${BrowserUrls.viewSourcePrefix}$currentUrl")
        return true
    }
}