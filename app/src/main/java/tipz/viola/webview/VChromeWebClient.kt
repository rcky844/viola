// Copyright (c) 2020-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.R
import tipz.viola.databinding.DialogEdittextBinding
import tipz.viola.ext.setImmersiveMode
import java.util.Objects

open class VChromeWebClient(private val context: Activity,
                            private val vWebView: VWebView) : WebChromeClient() {
    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null

    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private val fileChooser =
        (context as AppCompatActivity).registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (null == uploadMessage || uri == null) return@registerForActivityResult
            uploadMessage!!.onReceiveValue(arrayOf(uri))
            uploadMessage = null
        }

    override fun onShowCustomView(paramView: View, viewCallback: CustomViewCallback) {
        if (customView != null) {
            onHideCustomView()
            return
        }
        customView = paramView
        context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        customViewCallback = viewCallback
        context.setImmersiveMode(true)
        (context.window.decorView as FrameLayout).addView(
            customView,
            FrameLayout.LayoutParams(-1, -1)
        )
        context.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onHideCustomView() {
        context.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        (context.window.decorView as FrameLayout).removeView(customView)
        customView = null
        context.setImmersiveMode(false)
        context.requestedOrientation = context.resources.configuration.orientation
        customViewCallback!!.onCustomViewHidden()
        customViewCallback = null
    }

    override fun onProgressChanged(view: WebView, progress: Int) {
        vWebView.onPageLoadProgressChanged(progress)
    }

    override fun onReceivedIcon(view: WebView, favicon: Bitmap) {
        vWebView.onPageInformationUpdated(VWebView.PageLoadState.UPDATE_FAVICON, null, favicon)
    }

    override fun onReceivedTitle(view: WebView, title: String) {
        vWebView.onPageInformationUpdated(VWebView.PageLoadState.UPDATE_TITLE, null, null)
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String,
        callback: GeolocationPermissions.Callback
    ) {
        // FIXME: Re-enable permission dialog
        /*
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
         */

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) callback.invoke(origin, true, false)
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        if (uploadMessage != null) uploadMessage!!.onReceiveValue(null)
        uploadMessage = filePathCallback
        fileChooser.launch("*/*")
        return true
    }

    private fun jsDialog(
        url: String,
        message: String,
        defaultValue: String?,
        result: JsResult,
        titleResId: Int
    ) {
        val binding: DialogEdittextBinding =
            DialogEdittextBinding.inflate(LayoutInflater.from(context))
        val view = binding.root

        val jsMessage = binding.edittext
        val dialog = MaterialAlertDialogBuilder(context)
        dialog.setTitle(context.resources.getString(titleResId, url))
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                if (defaultValue == null) result.confirm()
                else (result as JsPromptResult).confirm(
                    Objects.requireNonNull(jsMessage.text).toString())
            }
            .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
                result.cancel()
                vWebView.onPageInformationUpdated(VWebView.PageLoadState.PAGE_FINISHED, null, null)
            }
        if (defaultValue != null) dialog.setView(view)
        dialog.create().show()
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

    // TODO: Add more comprehensive information to the console
    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        if (vWebView.consoleLogging && consoleMessage != null)
            vWebView.consoleMessages.append(consoleMessage.message())
        return super.onConsoleMessage(consoleMessage)
    }
}