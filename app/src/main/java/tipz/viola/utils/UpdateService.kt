// Copyright (c) 2024-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.utils

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.download.MiniDownloadHelper
import tipz.viola.ext.dpToPx
import tipz.viola.ext.isOnline
import tipz.viola.ext.setMaterialDialogViewPadding
import tipz.viola.ext.uriFromFile
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.webview.pages.ProjectUrls
import java.io.File
import java.io.FileOutputStream

class UpdateService(private val activity: AppCompatActivity) {
    private val settingsPreference = SettingsSharedPreference.instance
    private val dirFile = File(activity.filesDir.path + "/updates")

    data class Response(val name: String, val code: Int, val revision: Int,
                        val date: String, val url: String, val isValid: Boolean)
    enum class Status {
        SUCCESS, LATEST_VERSION, NO_NETWORK, FAILURE
    }
    var updateResponse: MutableLiveData<Response> = MutableLiveData()

    init {
        // Auto clean the directory on start
        if (dirFile.exists() || dirFile.mkdirs()) {
            for (file in dirFile.listFiles()!!)
                if (!file.isDirectory)
                    file.delete()
        }

        // Observe update response values
        updateResponse.observe(activity, Observer {
            if (it.isValid) showInformationalDialog(it)
        })
    }

    private fun installApk(file: File) =
        MainScope().launch {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(activity.uriFromFile(file),
                "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                activity.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }

    private suspend fun getUpdateJson(): Pair<JSONObject?, Status> {
        // Check for internet access
        if (!activity.isOnline()) return Pair(null, Status.NO_NETWORK)

        // Here we go!
        val r = MiniDownloadHelper.startDownloadWithDialog(activity,
            ProjectUrls.updateJSONUrl,
            R.string.update_download_failed
        )
        return if (!r.successful) Pair(null, Status.FAILURE)
        else Pair(JSONObject(String(r.response)), Status.SUCCESS)
    }

    suspend fun fetchUpdate(): Status {
        // Get update JSON
        val jObject: JSONObject = getUpdateJson().let { it.first ?: return it.second }

        // Get update channel name
        val channelName = settingsPreference.getString(SettingsKeys.updateChannelName)
            .ifEmpty { BuildConfig.VERSION_BUILD_TYPE }.ifEmpty { return Status.FAILURE }

        // Process the selected update channel data
        jObject.getJSONObject(channelName).let {
            val jChannelDataString = "channel_data_${BuildConfig.FLAVOR}"
            if (it.has(jChannelDataString))
                return@let it.getJSONObject(jChannelDataString)
            else return Status.LATEST_VERSION // When update channel does not exist
        }.let {
            val remoteCode = it.optInt("code", 0)
            val remoteRev = it.optInt("revision", 0)

            // When local version is newer than remote
            if (BuildConfig.VERSION_CODE > remoteCode
                || (BuildConfig.VERSION_CODE == remoteCode
                        && BuildConfig.VERSION_BUILD_REVISION >= remoteRev))
                return Status.LATEST_VERSION

            // Return with response
            updateResponse.postValue(Response(it.getString("name"),
                remoteCode, remoteRev, it.getString("date"),
                it.getString("download_url"), true))
            return Status.SUCCESS
        }
    }

    suspend fun downloadUpdate(response: Response, apkFile: File) {
        val ar = MiniDownloadHelper.startDownloadWithDialog(activity,
            response.url, R.string.update_download_failed)
        if (ar.successful && (dirFile.exists() || dirFile.mkdirs()))
            withContext(Dispatchers.IO) {
                apkFile.createNewFile()
                FileOutputStream(apkFile).use {
                    it.write(ar.response)
                }
            }
    }

    private fun showInformationalDialog(response: Response) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.dialog_update_available_title)
            .setMessage(
                activity.resources.getString(
                    R.string.dialog_update_available_message, response.name,
                    if (response.revision > 0) "${response.code}.${response.revision}"
                    else response.code.toString()
                ) + "\n" + activity.resources.getString(
                    R.string.dialog_update_available_release_date_message,
                    response.date)
            )
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                // Show indeterminate progress bar dialog
                val progressDialog = MaterialAlertDialogBuilder(activity).apply {
                    setView(LinearLayoutCompat(activity).apply {
                        gravity = Gravity.CENTER_VERTICAL
                        addView(CircularProgressIndicator(activity).apply {
                            isIndeterminate = true
                        })
                        addView(AppCompatTextView(activity).apply {
                            setText(R.string.update_downloading)
                            updatePadding(left = context.dpToPx(20))
                        })
                        setMaterialDialogViewPadding()
                    })
                    setCancelable(false)
                }.create()
                progressDialog.show()

                // Download update
                val apkFile = File(dirFile,
                    response.url.substringAfterLast('/'))
                runBlocking(Dispatchers.IO) {
                    downloadUpdate(response, apkFile)
                }

                // Install application
                progressDialog.dismiss()
                installApk(apkFile)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create().show()
    }

    class UpdateChannel {
        var identifier: String = ""
        var displayName: String = ""
    }

    fun getAvailableUpdateChannels(): Pair<List<UpdateChannel>?, Status> {
        return runBlocking(Dispatchers.IO) {
            // Get update JSON
            val jObject: JSONObject = getUpdateJson().let {
                it.first ?: return@runBlocking Pair(null, it.second)
            }
            val updateChannelList = mutableListOf<UpdateChannel>()

            // Build list of update channels
            jObject.keys().forEach {
                updateChannelList.add(UpdateChannel().apply {
                    identifier = it

                    val channelObj = jObject.getJSONObject(it)
                    if (channelObj.has("channel_name")) {
                        displayName = channelObj.getString("channel_name")
                    }
                })
            }
            Pair(updateChannelList, Status.SUCCESS)
        }
    }
}