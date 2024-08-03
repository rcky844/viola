// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download

import android.util.Log
import tipz.viola.utils.BussUtils.LOG_TAG
import java.net.URL

class MiniDownloadHelper {
    private val LOG_TAG = "MiniDownloadHelper"

    /* Public methods */
    companion object {
        suspend fun startDownload(uriString: String): ByteArray? {
            if (uriString.startsWith("data:")) {
                DownloadUtils.base64StringToByteArray(DownloadUtils.getRawDataFromDataUri(uriString))
            } else if (uriString.startsWith("blob:")) { /* TODO: Make it actually handle blob: URLs */
                return null
            }
            val url = URL(uriString)
            return try {
                url.readBytes()
            } catch (_: Exception) {
                Log.e(LOG_TAG, "Request failed, url=${url}")
                byteArrayOf() // Return empty byte array to not crash on Exception
            }
        }
    }
}