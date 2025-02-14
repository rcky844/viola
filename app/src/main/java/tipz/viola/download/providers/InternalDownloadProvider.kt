// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download.providers

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import tipz.viola.Application
import tipz.viola.R
import tipz.viola.download.DownloadCapabilities
import tipz.viola.download.DownloadClient
import tipz.viola.download.DownloadProvider
import tipz.viola.download.DownloadUtils
import tipz.viola.download.database.Droha
import tipz.viola.ext.showMessage
import tipz.viola.webview.VJavaScriptInterface.Companion.INTERFACE_NAME
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class InternalDownloadProvider(override val context: Context) : DownloadProvider {
    private val ketch = (context as Application).downloadClient.ketch

    override val capabilities = listOf(
        DownloadCapabilities.PROTOCOL_HTTP,
        DownloadCapabilities.PROTOCOL_HTTPS,
        DownloadCapabilities.PROTOCOL_DATA,
        DownloadCapabilities.PROTOCOL_BLOB
    )
    override var statusListener: DownloadProvider.Companion.DownloadStatusListener? = null

    override fun resolveFilename(downloadObject: Droha) {
        downloadObject.apply {
            if (filename != null) return
            Log.d(LOG_TAG, "id=${taskId}: uriString=${uriString}")
            Log.d(LOG_TAG, "id=${taskId}: contentDisposition=${contentDisposition}")
            Log.d(LOG_TAG, "id=${taskId}: mimeType=${mimeType}")

            when (DownloadCapabilities.fromString(getUriProtocol())) {
                DownloadCapabilities.PROTOCOL_DATA ->
                    filename = "${System.currentTimeMillis()}.${DownloadUtils.dataStringToExtension(uriString)}"

                DownloadCapabilities.PROTOCOL_BLOB ->
                    filename = "blob" // FIXME: Actual file name needed

                else -> try {
                    filename = DownloadUtils.guessFileName(uriString, contentDisposition, mimeType)
                } catch (e: Exception) {
                    context.showMessage(R.string.downloads_toast_failed)
                }
            }
            Log.d(LOG_TAG, "id=${taskId}: Resolved filename=${filename}")
        }
    }

    override fun startDownload(downloadObject: Droha) {
        super.startDownload(downloadObject)
        downloadObject.apply {
            when (DownloadCapabilities.fromString(downloadObject.getUriProtocol())) {
                DownloadCapabilities.PROTOCOL_DATA -> byteArrayToFile(context,
                    DownloadUtils.dataStringToByteArray(uriString), filename!!)

                DownloadCapabilities.PROTOCOL_BLOB -> mimeType?.let {
                    if (vWebView == null) return
                    vWebView!!.evaluateJavascript(getBase64StringFromBlobUrl(uriString, it))
                }

                else -> {
                    ketch.download(
                        url = uriString,
                        path = DownloadClient.defaultDownloadPath,
                        fileName = filename!!
                    )
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = "IntDownloadProvider"

        /**
         * Method to convert blobUrl to Blob, then process Base64 data on native side
         *
         * 1. Download Blob URL as Blob object
         * 2. Convert Blob object to Base64 data
         * 3. Pass Base64 data to Android layer for processing
         */
        fun getBase64StringFromBlobUrl(blobUrl: String, mimeType: String): String {
            // Script to convert blob URL to Base64 data in Web layer, then process it in Android layer
            return """
                var xhr = new XMLHttpRequest();
                xhr.open('GET', '${blobUrl}', true);
                xhr.setRequestHeader('Content-type','${mimeType}');
                xhr.responseType = 'blob';
                xhr.onload = function(e) {
                    if (this.status == 200) {
                        var blobFile = this.response;
                        var reader = new FileReader();
                        reader.readAsDataURL(blobFile);
                        reader.onloadend = function() {
                            uriString = reader.result;
                            $INTERFACE_NAME.getBase64FromBlobData(uriString, '${mimeType}');
                        };
                    };
                };
                xhr.send();
            """.trimIndent()
        }

        @Throws(IOException::class)
        fun getBase64FromBlobData(context: Context, uriString: String, mimeType: String) {
            Log.i(LOG_TAG, "getBase64FromBlobData(): mimeType=${mimeType}")
            byteArrayToFile(context,
                DownloadUtils.base64StringToByteArray(DownloadUtils.getRawDataFromDataUri(uriString)),
                "${System.currentTimeMillis()}.${DownloadUtils.dataStringToExtension(uriString)}"
            )
        }

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
            } catch (ignored: IOException) {
            }

            // Tell the media scanner about the new file so that it is immediately available to the user.
            MediaScannerConnection.scanFile(context, arrayOf(file.toString()),
                null, null)
            context.showMessage(context.resources.getString(
                R.string.downloads_toast_success, filename))
        }
    }
}