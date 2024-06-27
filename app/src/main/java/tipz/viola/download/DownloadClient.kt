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
                DownloadMode.AUTO_DOWNLOAD_PROVIDER.value -> { // TODO: Move auto detection to UI
                    var retProvider: DownloadProvider? = null
                    DownloadProvider.getPreferredDownloadProvider(context).forEach { itProvider ->
                        if (isProviderCapable(it, itProvider.capabilities)) retProvider = itProvider
                    }
                    retProvider // Return
                }
                DownloadMode.ANDROID_DOWNLOAD_PROVIDER.value -> AndroidDownloadProvider(context) /* 1 */
                DownloadMode.INTERNAL_DOWNLOAD_PROVIDER.value -> InternalDownloadProvider(context) /* 2 */
                else -> null
            } ?: return@forEach
            Log.i(LOG_TAG, "id=${it.taskId}: DownloadProvider found, provider=${provider.javaClass.name}")

            // Start download
            provider.startDownload(it)
        }
    }

    init {
        downloadQueue.observeForever(downloadObserver)
    }

    /* Private methods */
    private fun isProviderCapable(
        downloadObject: DownloadObject,
        capabilities: List<DownloadCapabilities>
    ) : Boolean {
        var isProviderCapable = false
        capabilities.forEach {
            if (downloadObject.compareUriProtocol(it.value)) isProviderCapable = true
        }
        return isProviderCapable
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