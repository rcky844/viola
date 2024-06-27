package tipz.viola.download

import android.content.Context

interface DownloadProvider {
    val context: Context

    fun startDownload(downloadObject: DownloadObject) : DownloadObject
}