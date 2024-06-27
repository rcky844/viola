package tipz.viola.download

import android.content.Context

interface DownloadProvider {
    val context: Context
    val capabilities: List<DownloadCapabilities>

    fun startDownload(downloadObject: DownloadObject) : DownloadObject
}