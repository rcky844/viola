// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.Gravity
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.updatePadding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.CoroutineScope
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
import tipz.viola.ext.showMessage
import tipz.viola.ext.uriFromFile
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.webview.pages.ProjectUrls
import java.io.File
import java.io.FileOutputStream

class UpdateService(private val context: Context, private val silent: Boolean) {
    private val settingsPreference = SettingsSharedPreference.instance
    private val dirFile = File(context.filesDir.path + "/updates")
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        // Auto clean the directory on start
        if (dirFile.exists() || dirFile.mkdirs()) {
            for (file in dirFile.listFiles()!!)
                if (!file.isDirectory)
                    file.delete()
        }
    }

    private fun installApplication(file: File) =
        MainScope().launch {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(context.uriFromFile(file), "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }

    private fun showMessage(@StringRes resId: Int) =
        MainScope().launch { if (!silent) context.showMessage(resId) }

    private fun getUpdateJson(): JSONObject? {
        // Check for internet access
        if (!context.isOnline()) {
            showMessage(R.string.toast_network_unavailable)
            return null
        }

        // Here we go!
        val r = MiniDownloadHelper.startDownloadWithDialog(context,
            ProjectUrls.updateJSONUrl,
            R.string.update_download_failed
        )
        return if (!r.successful) null
        else JSONObject(String(r.response))
    }

    fun checkUpdates() = coroutineScope.launch {
        // Get update JSON
        val jObject = getUpdateJson() ?: return@launch

        // Get update channel name
        val updateChannelName = settingsPreference.getString(SettingsKeys.updateChannelName)
            .ifEmpty { BuildConfig.VERSION_BUILD_TYPE }
        if (!jObject.has(updateChannelName)) {
            showMessage(R.string.update_download_failed)
            return@launch
        }

        // Process the selected update channel data
        val jChannelObject = jObject.getJSONObject(updateChannelName)
        val jChannelDataString = "channel_data_${BuildConfig.FLAVOR}"

        if (!jChannelObject.has(jChannelDataString)) {
            showMessage(R.string.toast_version_latest)
            return@launch
        }

        // Process the update channel object
        val jChannelUpdateObject = jChannelObject.getJSONObject(jChannelDataString)

        val remoteVerCode = jChannelUpdateObject.optInt("code", 0)
        val remoteVerRev = jChannelUpdateObject.optInt("revision", 0)
        if (BuildConfig.VERSION_CODE > remoteVerCode
            || (BuildConfig.VERSION_CODE == remoteVerCode
                    && BuildConfig.VERSION_BUILD_REVISION >= remoteVerRev)) {
            showMessage(R.string.toast_version_latest)
            return@launch
        }

        withContext(Dispatchers.Main) {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_update_available_title)
                .setMessage(
                    context.resources.getString(
                        R.string.dialog_update_available_message,
                        jChannelUpdateObject.getString("name"),
                        if (remoteVerRev > 0) "$remoteVerCode.$remoteVerRev" else remoteVerCode.toString()
                    ) + "\n" + context.resources.getString(
                                R.string.dialog_update_available_release_date_message,
                                jChannelUpdateObject.getString("date"))
                )
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    // Show indeterminate progress bar dialog
                    val progressDialog = MaterialAlertDialogBuilder(context).apply {
                        setView(LinearLayoutCompat(context).apply {
                            gravity = Gravity.CENTER_VERTICAL
                            addView(CircularProgressIndicator(context).apply {
                                isIndeterminate = true
                            })
                            addView(AppCompatTextView(context).apply {
                                setText(R.string.update_downloading)
                                updatePadding(left = context.dpToPx(20))
                            })
                            setMaterialDialogViewPadding()
                        })
                        setCancelable(false)
                    }.create()
                    progressDialog.show()

                    // Download update
                    val filename = jChannelUpdateObject.getString("download_url")
                        .substringAfterLast('/')
                    val dirFile = File(context.filesDir.path + "/updates")
                    val apkFile = File(dirFile, filename)

                    coroutineScope.launch {
                        val ar = MiniDownloadHelper.startDownloadWithDialog(context,
                            jChannelUpdateObject.getString("download_url"),
                            R.string.update_download_failed
                        )
                        if (ar.successful && (dirFile.exists() || dirFile.mkdirs())) {
                            apkFile.createNewFile()
                            val fos = FileOutputStream(apkFile)
                            fos.write(ar.response)
                            fos.close()
                            progressDialog.dismiss()
                            installApplication(apkFile)
                        }
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
        }
    }

    class UpdateChannel {
        var identifier: String = ""
        var displayName: String = ""
    }

    fun getAvailableUpdateChannels(): List<UpdateChannel> = runBlocking(coroutineScope.coroutineContext) {
        // Get update JSON
        val jObject = getUpdateJson() ?: return@runBlocking listOf()
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
        return@runBlocking updateChannelList
    }
}