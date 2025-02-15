// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download

import android.content.DialogInterface
import android.os.Environment
import android.text.Html
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
import tipz.viola.R.string
import tipz.viola.download.database.Droha
import tipz.viola.download.database.DrohaClient
import tipz.viola.download.providers.AndroidDownloadProvider
import tipz.viola.download.providers.InternalDownloadProvider
import tipz.viola.settings.SettingsKeys
import tipz.viola.webview.VWebView
import java.io.File
import java.net.URLDecoder

class DownloadClient(context: Application) {
    private val LOG_TAG = "DownloadClient"

    private var settingsPreference = (context.applicationContext as Application).settingsPreference
    private var clientMode = settingsPreference.getInt(SettingsKeys.downloadMgrMode)

    private var vWebView: VWebView? = null
    var drohaClient: DrohaClient = DrohaClient(context)
    var downloadQueue: MutableLiveData<MutableList<Droha>> = MutableLiveData(mutableListOf())
    private var currentTaskId = 0

    @Suppress("DEPRECATION")
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
                DownloadMode.AUTO_DOWNLOAD_PROVIDER -> { // TODO: Move auto detection to UI
                    var retProvider: DownloadProvider? = null
                    DownloadProvider.getPreferredDownloadProvider(context).forEach { itProvider ->
                        if (isProviderCapable(it, itProvider.capabilities)) retProvider = itProvider
                    }
                    retProvider // Return
                }
                DownloadMode.ANDROID_DOWNLOAD_PROVIDER -> AndroidDownloadProvider(context)
                DownloadMode.INTERNAL_DOWNLOAD_PROVIDER -> InternalDownloadProvider(context)
                else -> null
            } ?: return@forEach
            Log.i(LOG_TAG, "id=${it.taskId}: DownloadProvider found, provider=${provider.javaClass.name}")

            // Set-up for download provider
            it.vWebView = vWebView
            if (it.downloadPath == null) it.downloadPath = defaultDownloadPath
            provider.resolveFilename(it)

            // Start download
            it.downloadStatus = true
            val downloadActions = {
                provider.startDownload(it)
                commitToDroha(it) // Commit to Droha
            }

            if (it.showDialog)
                MaterialAlertDialogBuilder(ActivityManager.instance.currentActivity!!)
                    .setTitle(string.downloads_dialog_title)
                    .setMessage(Html.fromHtml(context.getString(
                        // Check for duplication
                        if (File(defaultDownloadPath, it.filename!!).exists())
                            string.downloads_dialog_duplicated_message
                        else string.downloads_dialog_message,
                        "<b>${it.filename}</b>")
                    ))
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        downloadActions()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show()
            else downloadActions()
        }
    }

    init {
        downloadQueue.observeForever(downloadObserver)
    }

    /* Private methods */
    private fun isProviderCapable(downloadObject: Droha,
                                  capabilities: List<DownloadCapabilities>) : Boolean {
        return capabilities.any { downloadObject.compareUriProtocol(it.value) }
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
            val matching = drohaClient.getWithFilename(droha.filename!!)
            if (matching.isNotEmpty()) matching.forEach { drohaClient.deleteById(it.id) }
            drohaClient.insert(droha)
        }
    }

    companion object {
        val defaultDownloadPath =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/"
    }
}