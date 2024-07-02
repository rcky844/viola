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
import tipz.viola.broha.database.IconHashUtils
import tipz.viola.download.DownloadClient
import tipz.viola.download.DownloadObject
import tipz.viola.search.SearchEngineEntries
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.InternalUrls
import tipz.viola.utils.UrlUtils
import tipz.viola.webviewui.BaseActivity


@SuppressLint("SetJavaScriptEnabled")
class VWebView(private val mContext: Context, attrs: AttributeSet?) : WebView(
    mContext, attrs
) {
    private var activity: VWebViewActivity = mContext as VWebViewActivity
    private lateinit var historyClient: HistoryClient
    val downloadClient: DownloadClient = (mContext.applicationContext as Application).downloadClient
    private val iconHashClient = IconHashUtils(mContext)
    private val webSettings = this.settings
    private var currentBroha = Broha()
    private var historyState = UpdateHistoryState.STATE_COMMITTED_WAIT_TASK
    private val settingsPreference =
        (mContext.applicationContext as Application).settingsPreference
    internal var adServersHandler: AdServersHandler

    private val requestHeaders = HashMap<String, String>()

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
        downloadClient.vWebViewModuleInit(this)
        setUserAgent(UserAgentMode.MOBILE, UserAgentBundle())

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
                requestUrl = getUrl()
            })

            onPageInformationUpdated(PageLoadState.UNKNOWN, originalUrl!!, null)
            activity.onPageLoadProgressChanged(0)
            if (!canGoBack() && originalUrl == null && settingsPreference.getIntBool(SettingsKeys.closeAppAfterDownload))
                activity.finish()
        }
        addJavascriptInterface(VJavaScriptInterface(context), VJavaScriptInterface.INTERFACE_NAME)

        setLayerType(LAYER_TYPE_HARDWARE, null)

        // Zoom controls
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        // Also increase text size to fill the viewport (this mirrors the behaviour of Firefox,
        // Chrome does this in the current Chrome Dev, but not Chrome release).
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

        // Disable file access
        // Disabled as it no longer functions since Android 11
        webSettings.allowFileAccess = false
        webSettings.allowContentAccess = false
        webSettings.allowFileAccessFromFileURLs = false
        webSettings.allowUniversalAccessFromFileURLs = false

        // Enable some HTML5 related settings
        webSettings.databaseEnabled = false // Disabled as no-op since Android 15
        webSettings.domStorageEnabled = true

        // Ad Server Hosts
        adServersHandler = AdServersHandler(mContext, settingsPreference)

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
            if (settingsPreference.getIntBool(SettingsKeys.enforceHttps)) WebSettings.MIXED_CONTENT_NEVER_ALLOW else WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Google's "Safe" Browsing
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE))
            WebSettingsCompat.setSafeBrowsingEnabled(
                webSettings,
                settingsPreference.getIntBool(SettingsKeys.enableGoogleSafeBrowse)
            )

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
        if (url == InternalUrls.aboutBlankUrl) {
            super.loadUrl(url)
            return
        }

        // Check for internal URLs
        if (url == InternalUrls.violaLicenseUrl) {
            super.loadUrl(InternalUrls.licenseUrl)
            return
        }

        // Handle App Links
        if (settingsPreference.getIntBool(SettingsKeys.checkAppLink)
            && UrlUtils.isUriLaunchable(url)) {
            var handled = false
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            webIntent.addCategory(Intent.CATEGORY_BROWSABLE);
            webIntent.setFlags(
                FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_REQUIRE_NON_BROWSER or
                        FLAG_ACTIVITY_REQUIRE_DEFAULT
            )
            val packageManager = activity.packageManager;
            if (packageManager?.let { webIntent.resolveActivity(it) } != null) {
                val dialog = MaterialAlertDialogBuilder(activity)
                dialog.setTitle(resources.getString(R.string.dialog_open_external_title))
                    .setMessage(resources.getString(R.string.dialog_open_external_message))
                    .setPositiveButton(resources.getString(android.R.string.ok)) { _: DialogInterface?, _: Int ->
                        try {
                            activity.startActivity(webIntent)
                            handled = true
                        } catch (e: ActivityNotFoundException) {
                            // Do not load actual url on failure
                        }
                    }
                    .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
                        // Load actual url if user cancelled the request
                        val checkedUrl = UrlUtils.toSearchOrValidUrl(mContext, url)
                        super.loadUrl(checkedUrl, requestHeaders)
                    }
                    .create().show()
            }
            if (handled) return // Exit loadUrl() if handled
        } else {
            // Update to start page layout
            val startPageLayout = activity.startPageLayout
            if (url == InternalUrls.violaStartUrl) {
                this.loadUrl(InternalUrls.aboutBlankUrl)
                this.visibility = GONE
                activity.swipeRefreshLayout.visibility = GONE
                startPageLayout?.visibility = VISIBLE
                activity.onSslCertificateUpdated()
                return
            }
            if (this.visibility == GONE) {
                this.visibility = VISIBLE
                activity.swipeRefreshLayout.visibility = VISIBLE
                startPageLayout?.visibility = GONE
            }

            // If the URL has "viola://" prefix but hasn't been handled till here,
            // wire it up with the "chrome://" suffix.
            if (url.startsWith(InternalUrls.violaPrefix)) {
                super.loadUrl(url.replace(InternalUrls.violaPrefix, InternalUrls.chromePrefix))
                return
            }

            // By this point, it is probably a webpage.
            val checkedUrl = UrlUtils.toSearchOrValidUrl(mContext, url)
            onPageInformationUpdated(PageLoadState.UNKNOWN, checkedUrl, null)

            // Prevent creating duplicate entries
            if (currentBroha.url == checkedUrl && historyState != UpdateHistoryState.STATE_DISABLED)
                historyState = UpdateHistoryState.STATE_DISABLED_DUPLICATED

            super.loadUrl(checkedUrl, requestHeaders)
        }
    }

    override fun reload() {
        if (currentBroha.url == getUrl() && historyState != UpdateHistoryState.STATE_DISABLED)
            historyState = UpdateHistoryState.STATE_DISABLED_DUPLICATED // Prevent duplicate entries
        loadUrl(getUrl())
    }

    override fun getUrl(): String {
        val superUrl = super.getUrl()
        return if (superUrl.isNullOrBlank()) InternalUrls.aboutBlankUrl else superUrl
    }

    override fun goBack() {
        activity.onDropDownDismissed()
        super.goBack()
    }

    override fun goForward() {
        activity.onDropDownDismissed()
        super.goForward()
    }

    fun onPageInformationUpdated(state: PageLoadState, url: String?, favicon: Bitmap?) {
        if (url == InternalUrls.aboutBlankUrl) return
        val currentUrl = getUrl()

        when (state) {
            PageLoadState.PAGE_STARTED -> {
                if (currentUrl.startsWith(InternalUrls.viewSourcePrefix)) return
                activity.onFaviconProgressUpdated(true)
                activity.onPageLoadProgressChanged(-1)
            }

            PageLoadState.PAGE_FINISHED -> {
                activity.onFaviconProgressUpdated(false)
                activity.onPageLoadProgressChanged(0)
                activity.onSslCertificateUpdated()
            }

            PageLoadState.UPDATE_HISTORY -> {
                if (historyState == UpdateHistoryState.STATE_COMMITTED_WAIT_TASK) {
                    currentBroha = Broha(title, currentUrl)
                    historyState = UpdateHistoryState.STATE_URL_UPDATED
                }
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) CookieSyncManager.getInstance()
                    .sync() else CookieManager.getInstance().flush()
                activity.onSwipeRefreshLayoutRefreshingUpdated(false)
            }

            PageLoadState.UPDATE_FAVICON -> {
                if (historyState == UpdateHistoryState.STATE_URL_UPDATED) {
                    CoroutineScope(Dispatchers.IO).launch {
                        currentBroha.iconHash = iconHashClient.save(favicon!!)
                        historyClient.insert(currentBroha)
                    }
                    historyState = UpdateHistoryState.STATE_COMMITTED_WAIT_TASK
                }
                if (historyState == UpdateHistoryState.STATE_DISABLED_DUPLICATED)
                    historyState = UpdateHistoryState.STATE_COMMITTED_WAIT_TASK
            }

            PageLoadState.UPDATE_TITLE -> {
                activity.onTitleUpdated(
                    if (this.visibility == View.GONE) resources.getString(
                        R.string.start_page
                    ) else title?.trim()
                )
            }

            PageLoadState.UNKNOWN -> {
            }
        }

        if (url != null) activity.onUrlUpdated(url)
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
                    WebViewCompat.getCurrentWebViewPackage(
                        mContext
                    )?.versionName
                } $mobile Safari/537.36 Viola/${BuildConfig.VERSION_NAME + BuildConfig.VERSION_NAME_EXTRA}"
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
            loadUrl(InternalUrls.violaStartUrl)
        } else {
            loadUrl(
                SearchEngineEntries.getHomePageUrl(
                settingsPreference, settingsPreference.getInt(SettingsKeys.defaultHomePageId)))
        }

    }

    fun loadViewSourcePage(url: String?): Boolean {
        val currentUrl = if (url.isNullOrBlank()) getUrl() else url
        if (currentUrl == InternalUrls.aboutBlankUrl) return false
        if (currentUrl.startsWith(InternalUrls.viewSourcePrefix)) return false // TODO: Allow changing behaviour
        loadUrl("${InternalUrls.viewSourcePrefix}$currentUrl")
        return true
    }
}