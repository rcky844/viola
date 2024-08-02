// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download

import android.util.Log
import androidx.annotation.DrawableRes
import tipz.viola.R

enum class FileFormat(val mimeType: String?, val extensions: Array<String>) {
    FORMAT_UNKNOWN(null, arrayOf()),

    /* Application */
    FORMAT_ANDROID_PACKAGE(
        "application/vnd.android.package-archive",
        arrayOf("apk", "apks", "aab", "xapk", "apkm", "akp")
    ),
    FORMAT_PDF("application/pdf", arrayOf("pdf")),
    FORMAT_APPLICATION_GENERIC("application/", arrayOf()),

    /* Audio */
    FORMAT_AUDIO_GENERIC("audio/", arrayOf()),

    /* Image */
    FORMAT_IMAGE_GENERIC("image/", arrayOf()),

    /* Video */
    FORMAT_VIDEO_GENERIC("video/", arrayOf());

    companion object {
        private val LOG_TAG = "FileFormat"

        fun getEnum(mimeSpec: String?, extensionSpec: String): FileFormat {
            for (it in entries) {
                if (!mimeSpec.isNullOrBlank() && it.mimeType != null) {
                    if ((it.mimeType == mimeSpec || (it.mimeType.endsWith("/")
                                && mimeSpec.startsWith(it.mimeType)))) {
                        return it
                    }
                }

                if (it.extensions.any { ext -> ext == extensionSpec }) {
                    return it
                }
            }
            return FORMAT_UNKNOWN
        }

        fun determineFileFormat(obj: DownloadObject): FileFormat {
            val extensionSpec = obj.filename!!.substringAfterLast(".")
            Log.d(LOG_TAG, "determineFileFormat(): extensionSpec=${extensionSpec}")

            var mimeSpec = obj.mimeType
            Log.d(LOG_TAG, "determineFileFormat(): mineSpec=${mimeSpec}")

            if (!mimeSpec.isNullOrBlank()) {
                mimeSpec = mimeSpec.substringBefore(";")
                Log.d(LOG_TAG, "determineFileFormat(): Cleaned up, mineSpec=${mimeSpec}")
            }

            return getEnum(mimeSpec, extensionSpec)
        }

        @DrawableRes
        fun getFileDrawableResId(obj: DownloadObject): Int {
            return when (determineFileFormat(obj)) {
                FORMAT_UNKNOWN -> R.drawable.document // Also represents generic documents

                /* Application */
                FORMAT_ANDROID_PACKAGE -> R.drawable.document_apk
                FORMAT_PDF -> R.drawable.document_pdf
                FORMAT_APPLICATION_GENERIC -> R.drawable.document_application

                /* Audio */
                FORMAT_AUDIO_GENERIC -> R.drawable.document_audio

                /* Image */
                FORMAT_IMAGE_GENERIC -> R.drawable.document_image

                /* Video */
                FORMAT_VIDEO_GENERIC -> R.drawable.document_video
            }
        }
    }
}