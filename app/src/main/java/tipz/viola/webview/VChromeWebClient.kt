// Copyright (c) 2020-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.Manifest
import android.app.Activity
import android.content.Context
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
import tipz.viola.utils.CommonUtils
import java.util.Objects

open class VChromeWebClient(private val mContext: Context,
                            private val mVWebView: VWebView) : WebChromeClient() {
    private var mCustomView: View? = null
    private var mCustomViewCallback: CustomViewCallback? = null

    private var mUploadMessage: ValueCallback<Array<Uri>>? = null
    private val mFileChooser =
        (mContext as AppCompatActivity).registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (null == mUploadMessage || uri == null) return@registerForActivityResult
            mUploadMessage!!.onReceiveValue(arrayOf(uri))
            mUploadMessage = null
        }

    override fun onShowCustomView(paramView: View, viewCallback: CustomViewCallback) {
        if (mCustomView != null) {
            onHideCustomView()
            return
        }
        mCustomView = paramView
        (mContext as AppCompatActivity).requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        mCustomViewCallback = viewCallback
        CommonUtils.setImmersiveMode(mContext, true)
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
        CommonUtils.setImmersiveMode(mContext, false)
        mContext.requestedOrientation = mContext.resources.configuration.orientation
        mCustomViewCallback!!.onCustomViewHidden()
        mCustomViewCallback = null
    }

    override fun onProgressChanged(view: WebView, progress: Int) {
        mVWebView.onPageLoadProgressChanged(progress)
    }

    override fun onReceivedIcon(view: WebView, favicon: Bitmap) {
        mVWebView.onPageInformationUpdated(VWebView.PageLoadState.UPDATE_FAVICON, null, favicon)
    }

    override fun onReceivedTitle(view: WebView, title: String) {
        mVWebView.onPageInformationUpdated(VWebView.PageLoadState.UPDATE_TITLE, null, null)
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

    private fun jsDialog(
        url: String,
        message: String,
        defaultValue: String?,
        result: JsResult,
        titleResId: Int
    ) {
        val binding: DialogEdittextBinding =
            DialogEdittextBinding.inflate(LayoutInflater.from(mContext))
        val view = binding.root

        val jsMessage = binding.edittext
        val dialog = MaterialAlertDialogBuilder(mContext)
        dialog.setTitle(mContext.resources.getString(titleResId, url))
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                if (defaultValue == null) result.confirm()
                else (result as JsPromptResult).confirm(
                    Objects.requireNonNull(jsMessage.text).toString())
            }
            .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
                result.cancel()
                mVWebView.onPageInformationUpdated(VWebView.PageLoadState.PAGE_FINISHED, null, null)
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
        if (mVWebView.consoleLogging && consoleMessage != null)
            mVWebView.consoleMessages.append(consoleMessage.message())
        return super.onConsoleMessage(consoleMessage)
    }
}