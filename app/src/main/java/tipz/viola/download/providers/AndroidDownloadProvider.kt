// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download.providers

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import tipz.viola.R
import tipz.viola.download.DownloadCapabilities
import tipz.viola.download.DownloadClient.Companion.defaultDownloadPath
import tipz.viola.download.DownloadProvider
import tipz.viola.download.DownloadUtils
import tipz.viola.download.database.Droha
import tipz.viola.ext.isOnline
import tipz.viola.ext.showMessage
import java.io.File

class AndroidDownloadProvider(override val context: Context) : DownloadProvider {
    private var downloadID: Long = 0
    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) stopDownload()
        }
    }

    override val capabilities = listOf(
        DownloadCapabilities.PROTOCOL_HTTP,
        DownloadCapabilities.PROTOCOL_HTTPS
    )
    override var statusListener: DownloadProvider.Companion.DownloadStatusListener? = null

    override fun resolveFilename(downloadObject: Droha) {
        downloadObject.apply {
            try {
                if (filename == null) {
                    Log.d(LOG_TAG, "id=${taskId}: uriString=${uriString}")
                    Log.d(LOG_TAG, "id=${taskId}: contentDisposition=${contentDisposition}")
                    Log.d(LOG_TAG, "id=${taskId}: mimeType=${mimeType}")
                    filename = DownloadUtils.guessFileName(uriString, contentDisposition, mimeType)
                }
                Log.d(LOG_TAG, "id=${taskId}: Resolved filename=${filename}")
            } catch (e: IllegalStateException) {
                context.showMessage(R.string.downloads_toast_failed)
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun startDownload(downloadObject: Droha) {
        super.startDownload(downloadObject)
        downloadObject.apply {
            if (checkIsOnline && !context.isOnline()) {
                statusListener!!.post(DownloadProvider.Companion.DownloadStatus.NO_INTERNET)
                return
            }

            val request = DownloadManager.Request(Uri.parse(uriString))

            Log.i(LOG_TAG, "startDownload(): uriString=${uriString}")

            // Let this downloaded file be scanned by MediaScanner - so that it can
            // show up in Gallery app, for example.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) request.allowScanningByMediaScanner()

            // Referer header for some sites which use the same HTML link for the download link
            request.addRequestHeader("Referer", requestUrl ?: uriString)

            // Notify client once download is completed
            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            // Delete file if exists
            val file = File(defaultDownloadPath, filename!!)
            if (file.exists()) file.delete()

            request.setDestinationUri(file.toUri())
            request.setMimeType(
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(uriString)
                )
            )

            // Set user agent and Cookies
            request.addRequestHeader("User-Agent", userAgent)
            CookieManager.getInstance().getCookie(uriString)?.takeUnless { it.isEmpty() }?.let {
                request.addRequestHeader("Cookie", it)
            }

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            try {
                downloadID = dm.enqueue(request)
            } catch (e: RuntimeException) {
                context.showMessage(R.string.downloads_toast_failed)
                return@apply
            }

            // Setup download complete receiver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    onDownloadComplete,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_EXPORTED
                )
            } else {
                context.registerReceiver(
                    onDownloadComplete,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }
        }
    }

    companion object {
        private val LOG_TAG = "AndroidDownloadProvider"
    }
}