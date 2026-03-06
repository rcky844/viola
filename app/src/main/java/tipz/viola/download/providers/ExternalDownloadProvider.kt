// Copyright (c) 2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download.providers

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import tipz.viola.download.DownloadCapabilities
import tipz.viola.download.DownloadProvider
import tipz.viola.download.database.Droha

class ExternalDownloadProvider(override val context: Context) : DownloadProvider {
    override val capabilities = listOf(
        DownloadCapabilities.PROTOCOL_HTTP,
        DownloadCapabilities.PROTOCOL_HTTPS
    )
    override var statusListener: DownloadProvider.Companion.DownloadStatusListener? = null

    override fun resolveFilename(downloadObject: Droha) {
        AndroidDownloadProvider(context).resolveFilename(downloadObject)
    }

    override fun startDownload(downloadObject: Droha) {
        super.startDownload(downloadObject)
        downloadObject.apply {
            // FIXME: Very rudimentary implementation to pass to other apps
            val intent = Intent(Intent.ACTION_VIEW,
                downloadObject.uriString.toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addCategory(Intent.CATEGORY_BROWSABLE)
            context.startActivity(intent)
        }
    }
}