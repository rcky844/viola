// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download.providers

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import tipz.viola.R
import tipz.viola.download.DownloadCapabilities
import tipz.viola.download.DownloadProvider
import tipz.viola.download.DownloadUtils
import tipz.viola.download.database.Droha
import tipz.viola.utils.CommonUtils
import tipz.viola.webview.VJavaScriptInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class InternalDownloadProvider(override val context: Context) : DownloadProvider {
    override val capabilities = listOf(
        // TODO: Enable for http/https/file/ftp downloads
        // DownloadCapabilities.PROTOCOL_HTTP,
        // DownloadCapabilities.PROTOCOL_HTTPS,
        // DownloadCapabilities.PROTOCOL_FILE.
        // DownloadCapabilities.PROTOCOL_FTP.
        DownloadCapabilities.PROTOCOL_DATA,
        DownloadCapabilities.PROTOCOL_BLOB
    )
    override var statusListener: DownloadProvider.Companion.DownloadStatusListener? = null

    override fun resolveFilename(downloadObject: Droha) {
        downloadObject.apply {
            filename = when (DownloadCapabilities.fromString(getUriProtocol())) {
                DownloadCapabilities.PROTOCOL_DATA -> {
                    (System.currentTimeMillis().toString() + "."
                            + DownloadUtils.dataStringToExtension(uriString))
                }

                DownloadCapabilities.PROTOCOL_BLOB ->
                    "blob" // FIXME: Actual file name needed

                else -> return@apply // TODO: Implement all
            }
        }
    }

    override fun startDownload(downloadObject: Droha) {
        super.startDownload(downloadObject)
        downloadObject.apply {
            when (DownloadCapabilities.fromString(downloadObject.getUriProtocol())) {
                DownloadCapabilities.PROTOCOL_DATA -> {
                    byteArrayToFile(context,
                        DownloadUtils.dataStringToByteArray(uriString), filename!!)
                }

                DownloadCapabilities.PROTOCOL_BLOB -> mimeType?.let {
                    if (vWebView != null)
                        vWebView!!.evaluateJavascript(
                            VJavaScriptInterface.getBase64StringFromBlobUrl(uriString, it),
                            null
                        )
                }

                else -> return@apply // TODO: Implement all
            }
        }
    }

    companion object {
        private val LOG_TAG = "IntDownloadProvider"

        fun byteArrayToFile(context: Context, barr: ByteArray, filename: String) {
            Log.i(LOG_TAG, "byteArrayToFile(): filename=${filename}")

            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(path, filename)

            try {
                if (!path.exists()) path.mkdirs()
                if (!file.exists()) file.createNewFile()

                val os: OutputStream = FileOutputStream(file)
                os.write(barr)
                os.close()

                // Tell the media scanner about the new file so that it is immediately available to the user.
                MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)
                CommonUtils.showMessage(context, context.resources.getString(
                    R.string.notification_download_successful, filename))
            } catch (ignored: IOException) {
            }
        }
    }
}