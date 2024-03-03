package tipz.viola.webview

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewClientCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.Application
import tipz.viola.R
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.UrlUtils
import java.io.ByteArrayInputStream
import java.net.MalformedURLException
import java.net.URL

open class VWebViewClient(private val mContext: Context, private val mVWebView: VWebView, private val adServersHandler : AdServersHandler) : WebViewClientCompat() {
    private val settingsPreference =
        (mContext.applicationContext as Application).settingsPreference!!

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        mVWebView.onPageInformationUpdated(VWebView.PageLoadState.PAGE_STARTED, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        mVWebView.onPageInformationUpdated(VWebView.PageLoadState.PAGE_FINISHED, url, null)
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        mVWebView.onPageInformationUpdated(VWebView.PageLoadState.UPDATE_HISTORY, url, null)
    }

    @Deprecated("Deprecated in Java")
    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String
    ) {
        var errorContent = template
        for (i in 0..5) errorContent = errorContent.replace(
            "$$i",
            mContext.resources.getStringArray(R.array.errMsg)[i]
        )
        errorContent = errorContent.replace("$6", description)

        view.evaluateJavascript("""document.documentElement.innerHTML = `$errorContent`""", null)
        view.stopLoading()
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        if (UrlUtils.isUriLaunchable(url)) return false
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(UrlUtils.cve_2017_13274(url)))
            mContext.startActivity(intent)
        } catch (ignored: ActivityNotFoundException) {
            CommonUtils.showMessage(
                mContext,
                mContext.resources.getString(R.string.toast_no_app_to_handle)
            )
        }
        return true
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        val dialog = MaterialAlertDialogBuilder(mContext)
        var contentSummary = mContext.resources.getString(R.string.ssl_certificate_unknown)
        when (error.primaryError) {
            SslError.SSL_DATE_INVALID -> contentSummary =
                mContext.resources.getString(R.string.ssl_certificate_date_invalid)

            SslError.SSL_INVALID -> contentSummary =
                mContext.resources.getString(R.string.ssl_certificate_invalid)

            SslError.SSL_EXPIRED -> contentSummary =
                mContext.resources.getString(R.string.ssl_certificate_expired)

            SslError.SSL_IDMISMATCH -> contentSummary =
                mContext.resources.getString(R.string.ssl_certificate_idmismatch)

            SslError.SSL_NOTYETVALID -> contentSummary =
                mContext.resources.getString(R.string.ssl_certificate_notyetvalid)

            SslError.SSL_UNTRUSTED -> contentSummary =
                mContext.resources.getString(R.string.ssl_certificate_untrusted)
        }
        dialog.setTitle(mContext.resources.getString(R.string.ssl_certificate_error_dialog_title))
            .setMessage(
                mContext.resources.getString(
                    R.string.ssl_certificate_error_dialog_content,
                    contentSummary
                )
            )
            .setPositiveButton(mContext.resources.getString(android.R.string.ok)) { _: DialogInterface?, _: Int -> handler.proceed() }
            .setNegativeButton(mContext.resources.getString(android.R.string.cancel)) { _: DialogInterface?, _: Int -> handler.cancel() }
            .create().show()
    }

    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        if (adServersHandler.adServers.isNullOrEmpty()) {
            adServersHandler.downloadAdServers()
            return super.shouldInterceptRequest(view, url)
        }
        try {
            if (adServersHandler.adServers!!.contains(" " + URL(url).host) && settingsPreference.getInt(
                    SettingsKeys.enableAdBlock
                ) == 1
            )
                return WebResourceResponse(
                    "text/plain", "utf-8",
                    ByteArrayInputStream(CommonUtils.EMPTY_STRING.toByteArray())
                )
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return super.shouldInterceptRequest(view, url)
    }

    companion object {
        private const val template =
            "<html>\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n<head>\n<title>$0</title>\n</head>\n<body>\n<div style=\"padding-left: 8vw; padding-top: 12vh;\">\n<div>\n<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"96\" viewBox=\"0 -960 960 960\" width=\"96\" fill=\"currentColor\">\n<path d=\"M480.134-120q-74.673 0-140.41-28.339-65.737-28.34-114.365-76.922-48.627-48.582-76.993-114.257Q120-405.194 120-479.866q0-74.673 28.339-140.41 28.34-65.737 76.922-114.365 48.582-48.627 114.257-76.993Q405.194-840 479.866-840q74.673 0 140.41 28.339 65.737 28.34 114.365 76.922 48.627 48.582 76.993 114.257Q840-554.806 840-480.134q0 74.673-28.339 140.41-28.34 65.737-76.922 114.365-48.582 48.627-114.257 76.993Q554.806-120 480.134-120ZM440-162v-78q-33 0-56.5-23.5T360-320v-40L168-552q-3 18-5.5 36t-2.5 36q0 121 79.5 212T440-162Zm276-102q20-22 36-47.5t26.5-53q10.5-27.5 16-56.5t5.5-59q0-98.58-54.115-180.059Q691.769-741.538 600-777.538V-760q0 33-23.5 56.5T520-680h-80v80q0 17-11.5 28.5T400-560h-80v80h240q17 0 28.5 11.5T600-440v120h40q26 0 47 15.5t29 40.5Z\"/>\n</svg>\n</div>\n<div>\n<p style=\"font-family:sans-serif; font-weight: bold; font-size: 24px; margin-top: 24px; margin-bottom: 8px;\">$1</p>\n<p style=\"font-family:sans-serif; font-size: 16px; margin-top: 8px; margin-bottom: 24px;\">$2</p>\n<p style=\"font-family:sans-serif; font-weight: bold; font-size: 16px; margin-bottom: 8px;\">$3</p>\n<ul style=\"font-family:sans-serif; font-size: 16px; margin-top: 0px; margin-bottom: 0px;\">\n<li>$4</li>\n<li>$5</li>\n</ul>\n<p style=\"font-family:sans-serif; font-size: 12px; margin-bottom: 8px; color: #808080;\">$6</p>\n</div>\n</div>\n</body>\n</html>"
    }
}