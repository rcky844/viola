// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download

import android.util.Log
import androidx.annotation.DrawableRes
import tipz.viola.R
import tipz.viola.download.database.Droha


enum class FileFormat(val mimeType: String?, val extensions: Array<String>) {
    FORMAT_UNKNOWN(null, arrayOf()),

    /* Application */
    FORMAT_ANDROID_PACKAGE(
        "application/vnd.android.package-archive",
        arrayOf("apk", "apks", "aab", "xapk", "apkm", "akp")
    ),
    FORMAT_BINARY_DATA("application/octet-stream", arrayOf()),
    FORMAT_EPUB("application/epub+zip", arrayOf("epub")),
    FORMAT_MS_WORD("application/msword",
        arrayOf("doc", "dot",
                "docx", "dotx", "docm", "dotm")),
    FORMAT_MS_PPT("application/vnd.ms-powerpoint",
        arrayOf("ppt", "pot", "pps", "ppa",
                "pptx", "potx", "ppsx", "ppam", "pptm", "potm", "ppsm")),
    FORMAT_MS_EXCEL("application/vnd.ms-excel",
        arrayOf("xls", "xlt", "xla",
                "xlsx", "xltx", "xlsm", "xltm", "xlam", "xlsb")),
    FORMAT_MS_WORKS("application/vnd.ms-works",
        arrayOf("wcm", "wdb", "wks", "wps", "xlr")),
    FORMAT_OPENDOCUMENT("application/vnd.oasis.opendocument.",
        arrayOf("odt", "ods", "odp", "odg", "odf", "fodt", "fods", "fodp", "fodg")),
    FORMAT_PDF("application/pdf", arrayOf("pdf")),
    FORMAT_APPLICATION_GENERIC("application/", arrayOf()),

    /* Application: Archives */
    FORMAT_BZIP2("application/x-bzip2", arrayOf("bz2")),
    FORMAT_GZIP("application/gzip", arrayOf("gz")),
    FORMAT_RAR("application/vnd.rar", arrayOf("rar", "rev")),
    FORMAT_TAR("application/x-tar", arrayOf("tar")),
    FORMAT_XZ("application/x-xz", arrayOf("xz")),
    FORMAT_ZIP("application/zip", arrayOf("zip", "zipx")),
    FORMAT_7Z("application/x-7z-compressed", arrayOf("7z")),

    /* Audio */
    FORMAT_AUDIO_GENERIC("audio/",
        arrayOf("mp3", "aac", /* Lossy */
                "ogg", "oga", "ogv", "ogx", "opus", /* Lossy: Ogg & Opus */
                "mid", "midi", "flac", "m4a", /* Lossless */
                "wav", "aiff", "pcm" /* Uncompressed */)
    ),

    /* Image */
    FORMAT_IMAGE_GENERIC("image/",
        arrayOf("jpg", "jpeg", /* Lossy */
                "png", "apng", "bmp", "gif", "ico", /* Lossless */
                "webp", "avif", "tiff" /* Both */)
    ),

    /* Video */
    FORMAT_VIDEO_GENERIC("video/",
        arrayOf("mp4", "mpeg", "webm", "mkv", "avi", "mov", "wmv", "3gp", "3g2")
    );

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

        fun determineFileFormat(obj: Droha): FileFormat {
            if (obj.filename.isNullOrBlank()) return FORMAT_BINARY_DATA // Say we have binary data
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
        fun getFileDrawableResId(obj: Droha): Int {
            return when (determineFileFormat(obj)) {
                FORMAT_UNKNOWN -> R.drawable.document_generic // Also represents generic documents

                /* Application */
                FORMAT_ANDROID_PACKAGE -> R.drawable.document_apk
                FORMAT_BINARY_DATA -> R.drawable.document_application
                FORMAT_EPUB -> R.drawable.document_book
                FORMAT_MS_WORD, FORMAT_MS_PPT, FORMAT_MS_EXCEL, FORMAT_MS_WORKS, FORMAT_OPENDOCUMENT
                    -> R.drawable.document
                FORMAT_PDF -> R.drawable.document_pdf
                FORMAT_APPLICATION_GENERIC -> R.drawable.document_application

                /* Application: Archives */
                FORMAT_BZIP2, FORMAT_GZIP, FORMAT_RAR,
                FORMAT_TAR, FORMAT_XZ, FORMAT_ZIP, FORMAT_7Z
                    -> R.drawable.document_zip

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