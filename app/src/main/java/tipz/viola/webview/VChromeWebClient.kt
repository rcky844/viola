package tipz.viola.webview

import android.Manifest
import android.annotation.SuppressLint
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
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.R
import java.util.Objects

open class VChromeWebClient(private val mContext: Context, private val mVWebView: VWebView) :
    WebChromeClient() {
    private var mCustomView: View? = null
    private var mCustomViewCallback: CustomViewCallback? = null

    private var mUploadMessage: ValueCallback<Array<Uri>>? = null
    val mFileChooser =
        (mContext as AppCompatActivity).registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (null == mUploadMessage || uri == null) return@registerForActivityResult
            mUploadMessage!!.onReceiveValue(arrayOf(uri))
            mUploadMessage = null
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
                mVWebView.onPageInformationUpdated(VWebView.PageLoadState.PAGE_FINISHED, null, null)
            }
        if (defaultValue != null) dialog.setView(root)
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
}