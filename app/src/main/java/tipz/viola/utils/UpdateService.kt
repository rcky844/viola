// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import tipz.viola.Application
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.download.MiniDownloadHelper
import tipz.viola.ext.isOnline
import tipz.viola.ext.showMessage
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.webview.pages.ProjectUrls
import java.io.File
import java.io.FileOutputStream

class UpdateService(private val context: Context, private val silent: Boolean) {
    private var settingsPreference: SettingsSharedPreference =
        (context.applicationContext as Application).settingsPreference
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

    private fun uriFromFile(file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        ) else Uri.fromFile(file)
    }

    private fun installApplication(file: File) =
        MainScope().launch {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uriFromFile(file), "application/vnd.android.package-archive")
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

    fun checkUpdates() = coroutineScope.launch {
        // Check for internet access
        if (!context.isOnline()) {
            showMessage(R.string.toast_network_unavailable)
        }

        // Here we go!
        val r = MiniDownloadHelper.startDownloadWithDialog(context,
            ProjectUrls.updateJSONUrl,
            R.string.update_download_failed
        )
        if (!r.successful) return@launch
        val jObject = JSONObject(String(r.response))

        // Get update channel name
        val updateChannelName = settingsPreference.getString(SettingsKeys.updateChannelName)
            .ifEmpty { BuildConfig.VERSION_BUILD_TYPE }
        if (!jObject.has(updateChannelName)) {
            showMessage(R.string.update_download_failed)
            return@launch
        }

        // Process the selected update channel data
        val jChannelObject = jObject.getJSONObject(updateChannelName)
        val jChannelDataString =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) "channel_data_modern"
            else "channel_data_legacy"

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
                            installApplication(apkFile)
                        }
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
        }
    }
}