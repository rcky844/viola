package tipz.viola.download

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import tipz.viola.Application
import tipz.viola.settings.SettingsKeys

class DownloadClient(context: Context) {
    private val LOG_TAG = "DownloadClient"

    private var settingsPreference = (context.applicationContext as Application).settingsPreference
    private var clientMode = settingsPreference.getInt(SettingsKeys.downloadMgrMode)

    private var downloadQueue: MutableLiveData<MutableList<DownloadObject>> = MutableLiveData(mutableListOf())
    private var currentTaskId = 0

    private val downloadObserver = Observer<MutableList<DownloadObject>> {
        Log.i(LOG_TAG, "Queue updated")

        val downloadQueue = downloadQueue.value!!
        if (downloadQueue.isEmpty()) return@Observer

        downloadQueue.forEach {
            // Match download manager
            val provider : DownloadProvider = when (it.downloadMode) {
                DownloadMode.AUTO_DOWNLOAD_PROVIDER.value -> InternalDownloadProvider(context) // FIXME /* 0 */
                DownloadMode.ANDROID_DOWNLOAD_PROVIDER.value -> AndroidDownloadProvider(context) /* 1 */
                DownloadMode.INTERNAL_DOWNLOAD_PROVIDER.value -> InternalDownloadProvider(context) /* 2 */
                else -> null
            } ?: return@forEach
            Log.i(LOG_TAG, "id=${it.taskId}: DownloadProvider found, provider=${provider.javaClass.name}")

            // Check capabilities
            Log.i(LOG_TAG, "id=${it.taskId}: URI protocol: ${it.getUriProtocol()}")
            var isProviderCapable = false
            provider.capabilities.forEach { cap ->
                if (it.compareUriProtocol(cap.value)) isProviderCapable = true
            }
            if (!isProviderCapable) return@forEach
            Log.i(LOG_TAG, "id=${it.taskId}: URI protocol matched")

            // Start download
            provider.startDownload(it)
        }
    }

    init {
        downloadQueue.observeForever(downloadObserver)
    }

    /* Public methods */
    fun addToQueue(vararg downloadObject: DownloadObject) {
        downloadObject.forEach {
            it.taskId = currentTaskId
            currentTaskId++

            val listData = downloadQueue.value!!
            listData.add(it)
            downloadQueue.postValue(listData)
        }
    }

    fun destroy() {
        downloadQueue.removeObserver(downloadObserver)
    }
}