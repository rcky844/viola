// Copyright (c) 2023-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.R
import tipz.viola.download.DownloadObject
import tipz.viola.download.MiniDownloadHelper
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.UrlUtils
import tipz.viola.webviewui.BrowserActivity
import tipz.viola.widget.StringResAdapter

open class HitTestAlertDialog(context: Context) : MaterialAlertDialogBuilder(context) {
    private var arrayAdapter = StringResAdapter(context)

    open fun setupDialogForShowing(vWebView: VWebView, bundle: Bundle): Boolean {
        val hr = vWebView.hitTestResult
        val type = hr.type
        var url = bundle.getString("url") ?: return false
        val title = bundle.getString("title")
        val src = bundle.getString("src")

        // Perform checks on the type of content we are dealing with
        if (type == WebView.HitTestResult.UNKNOWN_TYPE || type == WebView.HitTestResult.EDIT_TEXT_TYPE) return false

        // Truncate url string to make things load faster
        url = if (url.length > 75) url.substring(0, 74) + "…" else url

        if (title.isNullOrBlank()) {
            setTitle(url)
        } else {
            val layoutInflater = LayoutInflater.from(context)
            @SuppressLint("InflateParams") val root =
                layoutInflater.inflate(R.layout.dialog_hit_test_title, null)

            root.findViewById<AppCompatTextView>(R.id.title).text = title.trim()
            root.findViewById<AppCompatTextView>(R.id.url).text = url

            val icon = root.findViewById<AppCompatImageView>(R.id.icon)
            if (src.isNullOrBlank()) icon.visibility = View.GONE
            else {
                CoroutineScope(Dispatchers.IO).launch {
                    val data = MiniDownloadHelper.startDownload(src)!!
                    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    if (bitmap != null)
                        CoroutineScope(Dispatchers.Main).launch {
                            icon.setImageBitmap(bitmap)
                        }
                }
            }

            this.setCustomTitle(root)
        }

        // Add items to array adapter
        arrayAdapter.addAll(R.string.open_in_new_tab, R.string.copy_url)
        if (title.isNullOrBlank()) arrayAdapter.add(R.string.copy_text_url)
        if (!src.isNullOrBlank()) arrayAdapter.addAll(
            R.string.download_image,
            R.string.copy_src_url,
            R.string.search_image
        )
        arrayAdapter.add(R.string.share_url)

        setAdapter(arrayAdapter) { _: DialogInterface?, which: Int ->
            when (arrayAdapter.getItemResId(which)) {
                R.string.copy_url -> CommonUtils.copyClipboard(context, url)
                R.string.copy_text_url -> CommonUtils.copyClipboard(context, title)
                R.string.copy_src_url -> CommonUtils.copyClipboard(context, src)

                R.string.download_image -> {
                    vWebView.downloadClient.addToQueue(DownloadObject().apply {
                        uriString = src ?: url
                    })
                }

                R.string.search_image -> {
                    val fileSearch = src ?: url
                    vWebView.loadUrl("http://images.google.com/searchbyimage?image_url=$fileSearch")
                }

                R.string.open_in_new_tab -> {
                    val intent = Intent(context, BrowserActivity::class.java)
                    intent.data = Uri.parse(UrlUtils.patchUrlForCVEMitigation(url))
                    context.startActivity(intent)
                }

                R.string.share_url -> CommonUtils.shareUrl(context, url)
            }
        }
        return true
    }
}