// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import tipz.viola.R
import tipz.viola.utils.UrlUtils.UriScheme
import java.net.URL

class MiniDownloadHelper {
    class Response {
        var response = byteArrayOf()
        var successful = true
        var failure = ""
    }

    /* Public methods */
    companion object {
        const val LOG_TAG = "MiniDownloadHelper"
        const val UNSUPPORTED = "unsupported"

        fun startDownload(uriString: String): Response {
            val r = Response()

            when (UriScheme.getUriScheme(uriString.substringBefore(":"))) {
                UriScheme.SCHEME_HTTP, UriScheme.SCHEME_HTTPS -> {
                    try {
                        r.response = URL(uriString).readBytes()
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Request failed, url=${uriString}")
                        e.printStackTrace()

                        // Reflect failure of response
                        r.successful = false
                        r.failure = e.toString()
                    }
                }

                UriScheme.SCHEME_DATA -> {
                    r.response = DownloadUtils.base64StringToByteArray(
                        DownloadUtils.getRawDataFromDataUri(uriString))
                }

                /* TODO: Make it actually handle blob: URLs */
                else -> {
                    r.failure = UNSUPPORTED
                    r.successful = false
                }
            }
            return r
        }

        fun startDownloadWithDialog(
            context: Context, uriString: String,
            @StringRes titleMessage: Int = R.string.downloads_dialog_error_details_title
        ): Response {
            val r = startDownload(uriString)
            if (!r.successful) {
                MainScope().launch {
                    MaterialAlertDialogBuilder(context)
                        .setTitle(titleMessage)
                        .setMessage(r.failure)
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show()
                }
            }
            return r
        }
    }
}