package tipz.viola.download

import android.content.Context
import androidx.lifecycle.MutableLiveData

interface DownloadProvider {
    val context: Context
    val capabilities: List<DownloadCapabilities>

    fun startDownload(downloadObject: DownloadObject) {
        status.postValue(DownloadStatus.STARTED)
    }
    fun stopDownload() {
        status.postValue(DownloadStatus.STOPPED)
    }

    companion object {
        val status: MutableLiveData<DownloadStatus> = MutableLiveData<DownloadStatus>()

        fun getPreferredDownloadProvider(context: Context) = listOf(
            InternalDownloadProvider(context),
            AndroidDownloadProvider(context)
        )

        enum class DownloadStatus {
            STARTED, STOPPED
        }
    }
}