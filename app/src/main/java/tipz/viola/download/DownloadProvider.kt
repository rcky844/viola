// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download

import android.content.Context
import tipz.viola.download.database.Droha
import tipz.viola.download.providers.AndroidDownloadProvider
import tipz.viola.download.providers.InternalDownloadProvider

interface DownloadProvider {
    val context: Context
    val capabilities: List<DownloadCapabilities>
    var statusListener: DownloadStatusListener?

    fun resolveFilename(downloadObject: Droha) {
    }

    fun startDownload(downloadObject: Droha) {
        if (downloadObject.statusListener != null) {
            statusListener = downloadObject.statusListener!!
            statusListener!!.post(DownloadStatus.STARTED)
        }
    }
    fun stopDownload() {
        if (statusListener != null)
            statusListener!!.post(DownloadStatus.STOPPED)
    }

    companion object {
        fun getPreferredDownloadProvider(context: Context) = listOf(
            InternalDownloadProvider(context),
            AndroidDownloadProvider(context)
        )

        enum class DownloadStatus {
            STARTED, STOPPED, NO_INTERNET
        }

        fun interface DownloadStatusListener {
            fun post(status: DownloadStatus)
        }
    }
}