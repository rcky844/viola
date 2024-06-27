/*
 * Copyright (C) 2022-2023 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tipz.viola.utils

import android.app.DownloadManager
import android.content.Context
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tipz.viola.R
import tipz.viola.utils.CommonUtils.showMessage
import tipz.viola.webview.VJavaScriptInterface
import tipz.viola.webview.VWebViewActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URL

object DownloadUtils {
    private var LOG_TAG = "DownloadUtils"

    fun dmDownloadFile(
        context: Context, url: String,
        contentDisposition: String?,
        mimeType: String?, requestUrl: String?
    ) {
        dmDownloadFile(context, url, contentDisposition, mimeType, null, null, requestUrl)
    }

    /* TODO: Rewrite into our own download manager */
    @JvmStatic
    fun dmDownloadFile(
        context: Context, url: String,
        contentDisposition: String?,
        mimeType: String?, title: String?,
        customFilename: String?, requestUrl: String?
    ): Long {
        Log.w(LOG_TAG, "Using Android's DownloadManager will be deprecated in 7.x, please fix.")

        if (url.startsWith("http://") || url.startsWith("https://")) {
            val request = DownloadManager.Request(
                Uri.parse(UrlUtils.patchUrlForCVEMitigation(url))
            )

            // Let this downloaded file be scanned by MediaScanner - so that it can
            // show up in Gallery app, for example.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) request.allowScanningByMediaScanner()
            if (title != null) request.setTitle(title)

            // Referer header for some sites which use the same HTML link for the download link
            request.addRequestHeader("Referer", requestUrl ?: url)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // Notify client once download is completed!
            val filename =
                customFilename ?: UrlUtils.guessFileName(url, contentDisposition, mimeType)
            try {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            } catch (e: IllegalStateException) {
                showMessage(context, context.resources.getString(R.string.downloadFailed))
                return -1
            }
            request.setMimeType(
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(url)
                )
            )
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            try {
                return dm.enqueue(request)
            } catch (e: RuntimeException) {
                showMessage(context, context.resources.getString(R.string.downloadFailed))
            }
        } else {
            if (url.startsWith("data:")) {
                val path =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val dataInfo = url.substring(url.indexOf(":") + 1, url.indexOf(","))
                val filename = (System.currentTimeMillis().toString() + "."
                        + MimeTypeMap.getSingleton().getExtensionFromMimeType(
                    dataInfo.substring(
                        0,
                        if (dataInfo.contains(";")) dataInfo.indexOf(";") else dataInfo.length
                    )
                ))
                val file = File(path, filename)
                try {
                    if (!path.exists()) path.mkdirs()
                    if (!file.exists()) file.createNewFile()
                    val dataString = url.substring(url.indexOf(",") + 1)
                    val writableBytes = if (dataInfo.contains(";base64")) Base64.decode(
                        dataString,
                        Base64.DEFAULT
                    ) else dataString.toByteArray()
                    val os: OutputStream = FileOutputStream(file)
                    os.write(writableBytes)
                    os.close()

                    // Tell the media scanner about the new file so that it is immediately available to the user.
                    MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)
                    showMessage(
                        context,
                        context.resources.getString(
                            R.string.notification_download_successful,
                            filename
                        )
                    )
                } catch (ignored: IOException) {
                }
            } else if (url.startsWith("blob:")) { /* TODO: Make it actually handle blob: URLs */
                mimeType?.let { (context as VWebViewActivity).webview
                    .loadUrl(VJavaScriptInterface.getBase64StringFromBlobUrl(url, it)) }
            }
        }
        return -1
    }

    suspend fun startFileDownload(urlString: String?) =
        withContext(Dispatchers.IO) {
            if (urlString!!.startsWith("data:")) {
                val dataInfo = urlString.substring(urlString.indexOf(":") + 1, urlString.indexOf(","))
                val dataString = urlString.substring(urlString.indexOf(",") + 1)
                return@withContext if (dataInfo.contains(";base64")) Base64.decode(
                    dataString,
                    Base64.DEFAULT
                ) else dataString.toByteArray()
            } else if (urlString.startsWith("blob:")) { /* TODO: Make it actually handle blob: URLs */
                return@withContext null
            }
            val url = URL(urlString)
            return@withContext try {
                url.readBytes()
            } catch (_: Exception) {
                byteArrayOf() // Return empty byte array to not crash on Exception
            }
        }!!

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val n = cm.activeNetwork
            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            }
            return false
        } else {
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }
    }
}