// Copyright (c) 2020-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.util.Log
import android.view.LayoutInflater
import android.webkit.HttpAuthHandler
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.StringRes
import androidx.webkit.WebViewClientCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.R
import tipz.viola.databinding.DialogAuthBinding
import tipz.viola.ext.showMessage
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.UrlUtils
import tipz.viola.webview.VWebView.PageLoadState
import java.io.ByteArrayInputStream

open class VWebViewClient(
    private val context: Context, private val vWebView: VWebView,
    private val adServersHandler: AdServersClient
) : WebViewClientCompat() {
    private val LOG_TAG = "VWebViewClient"

    private val settingsPreference = vWebView.settingsPreference
    private val unsecureURLSet = ArrayList<String>()
    private val unsecureURLErrorSet = ArrayList<SslError>()

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        vWebView.onPageInformationUpdated(PageLoadState.PAGE_STARTED, url, favicon)
        vWebView.checkHomePageVisibility()
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        vWebView.onPageInformationUpdated(PageLoadState.PAGE_FINISHED, url, null)
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        vWebView.onPageInformationUpdated(PageLoadState.UPDATE_HISTORY, url, null)
    }

    @Deprecated("Deprecated in Java")
    override fun onReceivedError(
        view: WebView, errorCode: Int, description: String, failingUrl: String
    ) {
        if (description.contains("ERR_SSL_PROTOCOL_ERROR")) {
            getSslDialog(ERROR_FAILED_SSL_HANDSHAKE)
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    vWebView.loadRealUrl(
                        failingUrl.replaceFirst(UrlUtils.httpsPrefix, UrlUtils.httpPrefix))
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
        }
        vWebView.onPageInformationUpdated(PageLoadState.PAGE_ERROR, failingUrl,
            null, description)
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView, handler: HttpAuthHandler,
        host: String, realm: String
    ) {
        val binding: DialogAuthBinding = DialogAuthBinding.inflate(LayoutInflater.from(context))
        val editView = binding.root

        val usernameEditText = binding.usernameEditText
        val passwordEditText = binding.passwordEditText
        binding.message.text = context.getString(R.string.dialog_auth_detail, vWebView.url)

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dialog_auth_title)
            .setView(editView)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                handler.proceed(usernameEditText.text.toString(), passwordEditText.text.toString())
            }
            .setNegativeButton(android.R.string.cancel, { _: DialogInterface?, _: Int ->
                handler.cancel()
            })
            .create().show()
    }

    private fun getSslDialog(error: Int): MaterialAlertDialogBuilder {
        val dialog = MaterialAlertDialogBuilder(context)
        @StringRes val stringResId = when (error) {
            SslError.SSL_DATE_INVALID -> R.string.ssl_certificate_date_invalid
            SslError.SSL_INVALID -> R.string.ssl_certificate_invalid
            SslError.SSL_EXPIRED -> R.string.ssl_certificate_expired
            SslError.SSL_IDMISMATCH -> R.string.ssl_certificate_idmismatch
            SslError.SSL_NOTYETVALID -> R.string.ssl_certificate_notyetvalid
            SslError.SSL_UNTRUSTED -> R.string.ssl_certificate_untrusted
            ERROR_FAILED_SSL_HANDSHAKE -> R.string.ssl_failed_handshake
            else -> R.string.ssl_certificate_unknown
        }

        dialog.setTitle(R.string.ssl_certificate_error_dialog_title)
            .setMessage(
                context.resources.getString(
                    R.string.ssl_certificate_error_dialog_content,
                    context.resources.getString(stringResId)
                )
            )
        return dialog
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        // Handle SSL errors
        if (unsecureURLSet.contains(url)) {
            getSslDialog(unsecureURLErrorSet[unsecureURLSet.indexOf(url)].primaryError)
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    run {
                        view.loadUrl(url)
                        vWebView.onSslErrorProceed()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
            return true
        }

        // Do not override loading if URL scheme is supported
        if (UrlUtils.isUriSupported(url)) return false

        // Handle open in app
        Log.i(LOG_TAG, "Handling possible App Link, url=$url")
        if (!settingsPreference.getIntBool(SettingsKeys.checkAppLink)) {
            Log.i(LOG_TAG, "App Link checking is disabled.")
            return true
        }
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
        } else {
            if (view.progress == 100) {
                context.showMessage(R.string.toast_no_app_to_handle)
            }
            Log.w(LOG_TAG, "Found no application to handle App Link!")
        }
        return true
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        unsecureURLErrorSet.add(error)
        getSslDialog(error.primaryError)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                run {
                    handler.proceed()
                    vWebView.onSslErrorProceed()
                    unsecureURLSet.add(error.url)
                }
            }
            .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
                handler.cancel()
            }
            .create().show()
    }

    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        return false
    }

    @Suppress("DEPRECATION") // Kept as it is easier to handle
    @Deprecated("Deprecated in Java")
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        if (settingsPreference.getIntBool(SettingsKeys.enableAdBlock)) {
            if (adServersHandler.adServers.isNullOrEmpty()) {
                // TODO: Add dialog to warn users of this issue
                return super.shouldInterceptRequest(view, url)
            }
            if (adServersHandler.adServers!!.contains(" ${Uri.parse(url).host}"))
                return WebResourceResponse(
                    "text/plain", "utf-8",
                    ByteArrayInputStream("".toByteArray())
                )
        }

        return super.shouldInterceptRequest(view, url)
    }
}
