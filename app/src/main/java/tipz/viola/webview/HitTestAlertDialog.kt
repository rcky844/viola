// Copyright (c) 2023-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import tipz.viola.R
import tipz.viola.databinding.DialogHitTestTitleBinding
import tipz.viola.download.MiniDownloadHelper
import tipz.viola.download.database.Droha
import tipz.viola.ext.copyClipboard
import tipz.viola.ext.shareUrl
import tipz.viola.webview.activity.BrowserActivity
import tipz.viola.widget.StringResAdapter
import androidx.core.net.toUri

open class HitTestAlertDialog(context: Context) : MaterialAlertDialogBuilder(context) {
    private var arrayAdapter = StringResAdapter(context)

    private fun getHitTestItemsList(hasLinkText: Boolean, hasSrc: Boolean): Collection<Int> {
        val array = mutableListOf<Int>()
        array.add(R.string.hit_test_open_in_new_tab)
        array.add(R.string.menu_copy_link)
        array.add(R.string.hit_test_download_link)

        // Add link text option
        if (hasLinkText) array.add(R.string.hit_test_copy_link_text)

        // Add source related options
        if (hasSrc) {
            array.add(R.string.hit_test_download_image)
            array.add(R.string.hit_test_copy_image_link)
            array.add(R.string.hit_test_search_image)
        }

        array.add(R.string.hit_test_share_link)

        return array
    }

    open fun setupDialogForShowing(view: VWebView, bundle: Bundle): Boolean {
        val hr = view.hitTestResult
        val type = hr.type
        val url = bundle.getString("url") ?: return false
        val title = bundle.getString("title")
        var src = bundle.getString("src")
        if (src == null && url != hr.extra) src = hr.extra

        // Perform checks on the type of content we are dealing with
        if (type == WebView.HitTestResult.UNKNOWN_TYPE
            || type == WebView.HitTestResult.EDIT_TEXT_TYPE) return false

        if (title.isNullOrBlank()) {
            // Truncate url string to make things load faster
            setTitle(if (url.length > 100) url.substring(0, 99) + "…" else url)
        } else {
            val binding: DialogHitTestTitleBinding =
                DialogHitTestTitleBinding.inflate(LayoutInflater.from(context))
            val mView = binding.root

            binding.title.text = title.trim()
            binding.url.text = url

            val icon = binding.icon
            if (src.isNullOrBlank()) icon.visibility = View.GONE
            else {
                CoroutineScope(Dispatchers.IO).launch {
                    val data = MiniDownloadHelper.startDownload(src).response
                    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    if (bitmap != null) MainScope().launch { icon.setImageBitmap(bitmap) }
                }
            }

            this.setCustomTitle(mView)
        }

        // Add items to array adapter
        getHitTestItemsList(!title.isNullOrBlank(), !src.isNullOrBlank()).forEach {
            arrayAdapter.add(it)
        }

        setAdapter(arrayAdapter) { _: DialogInterface?, which: Int ->
            when (arrayAdapter.getItemResId(which)) {
                R.string.menu_copy_link -> context.copyClipboard(url)

                R.string.hit_test_copy_link_text -> context.copyClipboard(title?.trim())

                R.string.hit_test_download_link -> {
                    view.downloadClient.launchDownload(Droha().apply {
                        uriString = url
                    })
                }

                R.string.hit_test_copy_image_link -> context.copyClipboard(src)

                R.string.hit_test_download_image -> {
                    view.downloadClient.launchDownload(Droha().apply {
                        uriString = src ?: url
                    })
                }

                R.string.hit_test_search_image -> {
                    val fileSearch = src ?: url
                    view.loadUrl("http://images.google.com/searchbyimage?image_url=$fileSearch")
                }

                R.string.hit_test_open_in_new_tab -> {
                    val intent = Intent(context, BrowserActivity::class.java)
                    intent.data = url.toUri()
                    context.startActivity(intent)
                }

                R.string.hit_test_share_link -> context.shareUrl(url)
            }
        }
        return true
    }
}