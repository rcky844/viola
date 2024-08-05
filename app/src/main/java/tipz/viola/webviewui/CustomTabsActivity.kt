// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webviewui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import tipz.viola.databinding.ActivityCustomTabBinding
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.UrlUtils
import tipz.viola.webview.VWebViewActivity

class CustomTabsActivity : VWebViewActivity() {
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

        /* Back button */
        val actionBarBack : AppCompatImageView = binding.close
        actionBarBack.setOnClickListener { finish() }

        /* Title and Host */
        title = binding.title
        host = binding.host

        /* Share */
        val share = binding.share
        share.setOnClickListener { CommonUtils.shareUrl(this, webview.url) }

        /* Open in Browser */
        val openBrowser = binding.openBrowser
        openBrowser.setOnClickListener {
            val url = webview.url
            val intent = Intent(this, BrowserActivity::class.java)
            intent.data = Uri.parse(UrlUtils.patchUrlForCVEMitigation(url))
            startActivity(intent)
            finish()
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

        // Finally, load webpge
        super.onStart()
        val dataUri = intent.data
        if (dataUri != null) webview.loadUrl(dataUri.toString())
    }

    override fun onUrlUpdated(url: String?) {
        host.text = Uri.parse(UrlUtils.patchUrlForCVEMitigation(url!!)).host
    }

    override fun onTitleUpdated(title: String?) {
        super.onTitleUpdated(title)
        this.title.text = title
    }

    companion object {
        const val EXTRA_LAUNCH_AS_WEBAPP = "launchAsWebApp"
    }
}
