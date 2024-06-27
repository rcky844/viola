package tipz.viola.download

import android.content.Context
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
        val downloadQueue = downloadQueue.value!!
        if (downloadQueue.isEmpty()) return@Observer

        downloadQueue.forEach {
            when (it.downloadMode) {
                DownloadMode.ANDROID_DOWNLOAD_MANAGER.value -> /* 0 */
                    AndroidDownloadManager(context).startDownload(it)
            }
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

            it.downloadMode = clientMode // TODO: Allow customizing download mode

            val listData = downloadQueue.value!!
            listData.add(it)
            downloadQueue.postValue(listData)
        }
    }

    fun destroy() {
        downloadQueue.removeObserver(downloadObserver)
    }
}