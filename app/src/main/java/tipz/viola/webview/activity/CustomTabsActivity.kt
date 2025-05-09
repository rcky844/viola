// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import tipz.viola.databinding.ActivityCustomTabBinding
import tipz.viola.ext.finishAndRemoveTaskExt
import tipz.viola.ext.shareUrl
import tipz.viola.webview.VWebViewActivity
import androidx.core.net.toUri

class CustomTabsActivity : VWebViewActivity(true) {
    private lateinit var binding: ActivityCustomTabBinding
    private lateinit var title: AppCompatTextView
    private lateinit var host: AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomTabBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        /* Appbar */
        appbar = binding.appbar
        webviewContainer = binding.webviewContainer

        /* Back button */
        val actionBarBack : AppCompatImageView = binding.close
        actionBarBack.setOnClickListener { finishAndRemoveTaskExt() }

        /* Title and Host */
        title = binding.title
        host = binding.host

        /* Share */
        val share = binding.share
        share.setOnClickListener { shareUrl(webview.url) }

        /* Open in Browser */
        val openBrowser = binding.openBrowser
        openBrowser.setOnClickListener {
            val url = webview.url
            val intent = Intent(this, BrowserActivity::class.java)
            intent.data = url.toUri()
            startActivity(intent)
            finishAndRemoveTaskExt()
        }

        /* Progress Bar */
        progressBar = binding.webviewProgressBar

        /* Swipe Refresh Layout */
        swipeRefreshLayout = binding.layoutWebview.swipe

        // Setup Web App Mode
        if (intent.getBooleanExtra(EXTRA_LAUNCH_AS_WEBAPP, false))
            appbar.visibility = View.GONE

        /* WebView */
        webview = binding.layoutWebview.webview
        webview.setUpdateHistory(false)

        // Finally, load webpage
        super.onStart()
        val dataUri = intent.data
        if (dataUri != null) webview.loadUrl(dataUri.toString())
    }

    override fun onUrlUpdated(url: String?) {
        host.text = url!!.toUri().host
    }

    override fun onTitleUpdated(title: String?) {
        super.onTitleUpdated(title)
        this.title.text = title
    }

    companion object {
        const val EXTRA_LAUNCH_AS_WEBAPP = "launchAsWebApp"
    }
}
