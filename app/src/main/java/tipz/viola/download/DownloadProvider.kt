// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download

import android.content.Context

interface DownloadProvider {
    val context: Context
    val capabilities: List<DownloadCapabilities>
    var statusListener: DownloadStatusListener?

    fun resolveFilename(downloadObject: DownloadObject) {
    }

    fun startDownload(downloadObject: DownloadObject) {
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