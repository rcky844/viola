// Copyright (c) 2020-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("DEPRECATION")

package tipz.viola.webview

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import tipz.viola.Application
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.database.Broha
import tipz.viola.database.instances.HistoryClient
import tipz.viola.database.instances.HistoryClient.UpdateHistoryState
import tipz.viola.database.instances.IconHashClient
import tipz.viola.download.DownloadClient
import tipz.viola.download.database.Droha
import tipz.viola.ext.Matcher
import tipz.viola.ext.isDarkMode
import tipz.viola.ext.matchAndExec
import tipz.viola.ext.showMessage
import tipz.viola.search.SearchEngineEntries
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.utils.UrlUtils
import tipz.viola.webview.activity.BrowserActivity
import tipz.viola.webview.buss.BussUtils
import tipz.viola.webview.pages.BrowserUrls
import tipz.viola.webview.pages.PrivilegedPages
import tipz.viola.webview.pages.ProjectUrls
import java.util.regex.Pattern

@SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface", "RequiresFeature")
class VWebView(private val context: Context, attrs: AttributeSet?) : WebView(
    context, attrs
) {
    private val LOG_TAG = "VWebView"

    private var activity: VWebViewActivity = context as VWebViewActivity
    private var activeSnackBar: Snackbar? = null
    val downloadClient: DownloadClient = (context.applicationContext as Application).downloadClient
    val webSettings = this.settings
    private var historyState = UpdateHistoryState.STATE_COMMITTED_WAIT_TASK
    private var insecureAllow = false
    var loadProgress = 100
    val settingsPreference = SettingsSharedPreference.instance
    internal var adServersHandler: AdServersClient
    private val initialUserAgent = settings.userAgentString
    private var pageError = false

    internal val requestHeaders = HashMap<String, String>()
    var consoleLogging = false
        set(value) {
            field = value
            if (!value) consoleMessages.clear()
        }
    val consoleMessages = mutableListOf<ConsoleMessage>()

    // Broha related (history & favorites)
    private lateinit var historyClient: HistoryClient
    private val iconHashClient = IconHashClient(context)
    var faviconExt: Bitmap? = null

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
        UPDATE_HISTORY, UPDATE_TITLE, UNKNOWN
    }

    init {
        /* User agent init code */
        downloadClient.vWebViewModuleInit(this)
        setUserAgent(UserAgentMode.MOBILE, UserAgentBundle().apply {
            noReload = true
        })

        /* Start the download manager service */
        setDownloadListener { vUrl, _, vContentDisposition, vMimeType, _ ->
            Log.d(LOG_TAG, """
                Incoming download request
                URL: $vUrl
                Content Disposition: $vContentDisposition
                MIME Type: $vMimeType
            """.trimIndent())

            if (!settingsPreference.getIntBool(SettingsKeys.enableDownloads)) {
                Log.i(LOG_TAG, "Downloads disabled by user, request dropped.")
                return@setDownloadListener
            }

            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)

            downloadClient.launchDownload(Droha().apply {
                uriString = vUrl!!
                contentDisposition = vContentDisposition
                mimeType = vMimeType
                requestUrl = getRealUrl()
                showDialog = settingsPreference.getIntBool(SettingsKeys.requireDownloadConformation)
                dialogPositiveButtonClickListener = {
                    if (!canGoBack() && originalUrl == null
                        && settingsPreference.getIntBool(SettingsKeys.closeAppAfterDownload))
                        activity.finish()
                }
            })

            onPageInformationUpdated(PageLoadState.UNKNOWN, originalUrl ?: "")
            onPageLoadProgressChanged(0)
        }

        // Features for legacy
        WebkitCompat.setDefaultsForCompat(this)

        // JavaScript interface
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
        webSettings.savePassword = false

        // Ad Server Hosts
        adServersHandler = AdServersClient(context, settingsPreference)

        this.webViewClient = VWebViewClient(activity, this, adServersHandler)
        this.webChromeClient = VChromeWebClient(activity, this)
        if (WebkitCompat.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_CLIENT_BASIC_USAGE)) {
            WebViewCompat.setWebViewRenderProcessClient(
                this, VWebViewRenderProcessClient(this)
            )
        }

        /* Hit Test Menu */
        setOnCreateContextMenuListener { _, _, _ ->
            val message = titleHandler.obtainMessage()
            this.requestFocusNodeHref(message)
        }
    }

    @Suppress("deprecation")
    fun doSettingsCheck() {
        // Dark mode
        val darkMode = context.isDarkMode()
        val forceDark = settingsPreference.getIntBool(SettingsKeys.useForceDark)
        if (WebkitCompat.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webSettings, darkMode && forceDark)
        } else if (WebkitCompat.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            if (darkMode && forceDark) WebSettingsCompat.FORCE_DARK_ON
            else WebSettingsCompat.FORCE_DARK_OFF
        }

        // Cookies
        CookieManager.getInstance().setAcceptCookie(
            settingsPreference.getIntBool(SettingsKeys.isCookiesEnabled)
        )

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
        if (WebkitCompat.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE))
            WebSettingsCompat.setSafeBrowsingEnabled(
                webSettings,
                settingsPreference.getIntBool(SettingsKeys.enableGoogleSafeBrowse)
            )

        // WebView Debugging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(
                settingsPreference.getIntBool(SettingsKeys.remoteDebugging))
        }

        // Do Not Track request
        requestHeaders["DNT"] = settingsPreference.getInt(SettingsKeys.sendDNT).toString()

        // Global Privacy Control
        requestHeaders["Sec-GPC"] = settingsPreference.getInt(SettingsKeys.sendSecGPC).toString()

        // Data Saver
        requestHeaders["Save-Data"] = settingsPreference.getIntOnOff(SettingsKeys.sendSaveData)

        // Always-on logging
        val alwaysOnLogging = settingsPreference.getIntBool(SettingsKeys.alwaysOnLogging)
        if (!(consoleLogging && !alwaysOnLogging)) {
            consoleLogging = alwaysOnLogging
        }

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
            else Intent(Intent.ACTION_VIEW, url.toUri())
        if (intent.resolveActivity(context.packageManager) != null) {
            activeSnackBar = Snackbar.make(
                activity.webviewContainer,
                R.string.snackbar_open_external_message,
                Snackbar.LENGTH_INDEFINITE
            ).setBehavior(BaseTransientBottomBar.Behavior().apply {
                setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY)
            }).setAction(R.string.snackbar_open_external_action) {
                context.startActivity(intent)
            }.apply {
                show()
            }
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

        // Reset insecure connection override
        insecureAllow = false

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
                .substringBefore('/')

            // Browser mode specific URLs
            val act = activity as BrowserActivity?
            val match = handlingSuffix.matchAndExec(listOf(
                // Bookmarks
                Matcher(BrowserUrls.bookmarksChromeSuffix, {
                    act?.itemSelected(null, R.drawable.favorites)
                }),

                // Favorites
                // "favorites" does not exist as "chrome://" prefix,
                // so limit to "viola://" prefix.
                Matcher(BrowserUrls.favouritesViolaSuffix, {
                    if (isViolaUrl) act?.itemSelected(null, R.drawable.favorites)
                }),

                // History
                Matcher(BrowserUrls.historyChromeSuffix, {
                    act?.itemSelected(null, R.drawable.history)
                }),

                // New Tab
                Matcher(BrowserUrls.newTabChromeSuffix, {
                    loadHomepage()
                }),

                // New Tab Page
                Matcher(BrowserUrls.newTabPageChromeSuffix, {
                    loadHomepage(true)
                }),

                // New Tab Page (Third party)
                Matcher(BrowserUrls.newTabPageThirdPartyChromeSuffix, {
                    loadHomepage(false)
                }),

                // Quit
                Matcher(BrowserUrls.quitChromeSuffix, {
                    act?.finish()
                }),
            ))
            if (!match) super.loadUrl("${BrowserUrls.chromePrefix}$handlingSuffix")

            return
        }

        // By this point, it is probably a webpage or a search query.
        val checkedUrl = UrlUtils.UrlOrSearchValidator.validate(settingsPreference, inUrl)
        if (UrlUtils.UrlOrSearchValidator.isSearch) {
            // Handle App Links
            if (loadAppLinkUrl(url, true)) return
        }
        onPageInformationUpdated(PageLoadState.UNKNOWN, checkedUrl)

        // Prevent creating duplicate entries
        if (getRealUrl() == checkedUrl && historyState != UpdateHistoryState.STATE_DISABLED)
            historyState = UpdateHistoryState.STATE_DISABLED_DUPLICATED

        super.loadUrl(checkedUrl, requestHeaders)
    }

    // This should only be accessed by us!
    fun loadRealUrl(url: String) {
        if (url.isBlank()) super.loadUrl(BrowserUrls.aboutBlankUrl)
        super.loadUrl(url)
    }

    override fun reload() {
        if (historyState != UpdateHistoryState.STATE_DISABLED)
            historyState = UpdateHistoryState.STATE_DISABLED_DUPLICATED // Prevent duplicate entries

        // Handling for page error conditions
        // TODO: Replace with other solutions
        if (pageError) {
            super.goBack()
            pageError = false
            return
        }

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

        // Apply go back twice of error
        // TODO: Replace with other solutions
        if (pageError) {
            super.goBack()
            pageError = false
        }
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

    private suspend fun commitHistory(newUrl: String) {
        val currentTitle = runBlocking {
            withContext(Dispatchers.Main) { title }
        }
        val iconHash = faviconExt.takeUnless { it == null }?.let { iconHashClient.save(it) }
        historyClient.insert(Broha(iconHash, currentTitle, newUrl))
        Log.d(LOG_TAG, "History committed, url=$newUrl")
    }

    fun onPageInformationUpdated(state: PageLoadState, url: String? = null,
                                 favicon: Bitmap? = null, description: String? = null) {
        val currentUrl = this.url
        val newUrl = if (!url.isNullOrBlank()) filterUrl(url) else currentUrl

        // Update favicon
        this.faviconExt = favicon

        when (state) {
            PageLoadState.PAGE_STARTED -> {
                if (currentUrl.startsWith(BrowserUrls.viewSourcePrefix)) return
                if (currentUrl.startsWith(UrlUtils.httpPrefix)
                    && settingsPreference.getIntBool(SettingsKeys.enforceHttps)
                    && !insecureAllow) {
                    stopLoading()
                    MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.dialog_insecure_connection_title)
                        .setMessage(resources.getString(
                            R.string.dialog_insecure_connection_message, currentUrl.toUri().host))
                        .setPositiveButton(R.string.dialog_insecure_connection_continue_to_site) { _, _ ->
                            insecureAllow = true
                            loadRealUrl(currentUrl)
                        }.setNegativeButton(R.string.dialog_insecure_connection_go_back) { _, _ ->
                            goBack()
                        }.show()
                    return
                }

                onPageLoadProgressChanged(-1)
                activity.onPageStateChanged(true)
                consoleMessages.clear()
                activeSnackBar.takeUnless { it == null }?.dismiss()
            }

            PageLoadState.PAGE_FINISHED -> {
                onPageLoadProgressChanged(0)
                activity.onPageStateChanged(false)
                activity.onSslCertificateUpdated()
                activity.swipeRefreshLayout.setRefreshing(false)
            }

            PageLoadState.PAGE_ERROR -> {
                pageError = true

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
                if (historyState == UpdateHistoryState.STATE_COMMITTED_WAIT_TASK)
                    historyState = UpdateHistoryState.STATE_URL_UPDATED
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    CookieManager.getInstance().flush()
                else CookieSyncManager.getInstance().sync()
                activity.swipeRefreshLayout.setRefreshing(false)

                CoroutineScope(Dispatchers.IO).launch {
                    Log.d(LOG_TAG, "History commit job START")
                    runBlocking {
                        withTimeoutOrNull(25000L) {
                            while (faviconExt == null) {
                                delay(1000)
                                continue
                            }
                        }
                    }

                    if (historyState == UpdateHistoryState.STATE_URL_UPDATED) {
                        historyState = UpdateHistoryState.STATE_COMMITTED_WAIT_TASK
                        commitHistory(newUrl)
                    }
                    MainScope().launch { activity.onFaviconUpdated(faviconExt) }
                }
            }

            PageLoadState.UPDATE_TITLE -> {
                if (currentUrl.isBlank() || getRealUrl() == BrowserUrls.aboutBlankUrl) return
                activity.onTitleUpdated(
                    if (isGone) resources.getString(R.string.start_page)
                    else title?.trim()
                )
                activity.swipeRefreshLayout.setRefreshing(false)
            }

            PageLoadState.UNKNOWN -> {
            }
        }
        Log.v(LOG_TAG, "onPageInformationUpdated(): state=${state.name}")

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

    fun loadHomepage(useStartPage: Boolean = !settingsPreference.getIntBool(SettingsKeys.useWebHomePage)) {
        if (!settingsPreference.getIntBool(SettingsKeys.useHomePage)) {
            activity.onPageLoadProgressChanged(0) // Reset page load progress
            return
        }

        if (useStartPage) {
            loadRealUrl(ProjectUrls.actualStartUrl)
        } else {
            loadUrl(SearchEngineEntries.getPreferredUrl(
                settingsPreference, SearchEngineEntries.EngineInfoType.HOMEPAGE))
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