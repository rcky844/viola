package tipz.viola.download

import android.content.Context

interface DownloadProvider {
    val context: Context
    val capabilities: List<DownloadCapabilities>

    fun startDownload(downloadObject: DownloadObject) : DownloadObject
    companion object {
        fun getPreferredDownloadProvider(context: Context) = listOf(
            InternalDownloadProvider(context),
            AndroidDownloadProvider(context)
        )
    }
}