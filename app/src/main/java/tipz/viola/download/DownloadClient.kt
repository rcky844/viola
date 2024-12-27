// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download

import android.content.DialogInterface
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.ActivityManager
import tipz.viola.Application
import tipz.viola.R
import tipz.viola.download.database.Droha
import tipz.viola.download.database.DrohaClient
import tipz.viola.download.providers.AndroidDownloadProvider
import tipz.viola.download.providers.InternalDownloadProvider
import tipz.viola.settings.SettingsKeys
import tipz.viola.webview.VWebView
import java.net.URLDecoder

class DownloadClient(context: Application) {
    private val LOG_TAG = "DownloadClient"

    private var settingsPreference = (context.applicationContext as Application).settingsPreference
    private var clientMode = settingsPreference.getInt(SettingsKeys.downloadMgrMode)

    private var vWebView: VWebView? = null
    var drohaClient: DrohaClient = DrohaClient(context)
    var downloadQueue: MutableLiveData<MutableList<Droha>> = MutableLiveData(mutableListOf())
    private var currentTaskId = 0

    private val downloadObserver = Observer<MutableList<Droha>> {
        Log.i(LOG_TAG, "Queue updated")

        val downloadQueue = downloadQueue.value!!
        if (downloadQueue.isEmpty()) return@Observer

        downloadQueue.forEach {
            if (it.downloadStatus) return@forEach

            // Decode URL string
            it.uriString = URLDecoder.decode(it.uriString, "UTF-8")

            // Match download provider
            val provider : DownloadProvider = when (it.downloadMode) {
                DownloadMode.AUTO_DOWNLOAD_PROVIDER.value -> { // TODO: Move auto detection to UI
                    var retProvider: DownloadProvider? = null
                    DownloadProvider.getPreferredDownloadProvider(context).forEach { itProvider ->
                        if (isProviderCapable(it, itProvider.capabilities)) retProvider = itProvider
                    }
                    retProvider // Return
                }
                DownloadMode.ANDROID_DOWNLOAD_PROVIDER.value -> AndroidDownloadProvider(context) /* 0 */
                DownloadMode.INTERNAL_DOWNLOAD_PROVIDER.value -> InternalDownloadProvider(context) /* 1 */
                else -> null
            } ?: return@forEach
            Log.i(LOG_TAG, "id=${it.taskId}: DownloadProvider found, provider=${provider.javaClass.name}")

            // Set-up for download provider
            it.vWebView = vWebView
            if (it.downloadPath == null) it.downloadPath = defaultDownloadPath
            provider.resolveFilename(it)

            // Start download
            it.downloadStatus = true
            if (it.showDialog)
                CoroutineScope(Dispatchers.Main).launch {
                    MaterialAlertDialogBuilder(ActivityManager.instance.currentActivity!!)
                        .setTitle(R.string.download_title)
                        .setMessage(context.getString(R.string.download_message, it.filename))
                        .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                            provider.startDownload(it)
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show()
                }
            else provider.startDownload(it)

            // Commit to Droha
            commitToDroha(it)
        }
    }

    init {
        downloadQueue.observeForever(downloadObserver)
    }

    /* Private methods */
    private fun isProviderCapable(
        downloadObject: Droha,
        capabilities: List<DownloadCapabilities>
    ) : Boolean {
        var isProviderCapable = false
        capabilities.forEach {
            if (downloadObject.compareUriProtocol(it.value)) isProviderCapable = true
        }
        return isProviderCapable
    }

    /* Public methods */
    fun addToQueue(vararg downloadObject: Droha) {
        downloadObject.forEach {
            it.taskId = currentTaskId
            currentTaskId++

            val listData = downloadQueue.value!!
            listData.add(it)
            downloadQueue.postValue(listData)
        }
    }

    fun vWebViewModuleInit(webView: VWebView) {
        vWebView = webView
    }

    fun destroy() {
        downloadQueue.removeObserver(downloadObserver)
    }

    fun commitToDroha(droha: Droha) {
        CoroutineScope(Dispatchers.IO).launch {
            drohaClient.insert(droha)
        }
    }

    companion object {
        val defaultDownloadPath =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/"
    }
}