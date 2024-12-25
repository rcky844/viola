// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download

import android.net.Uri
import android.util.Base64
import android.webkit.MimeTypeMap
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.util.regex.Pattern

object DownloadUtils {
    fun dataStringToExtension(dataString: String) : String? {
        val dataInfo =
            dataString.substring(dataString.indexOf(":") + 1, dataString.indexOf(","))

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(
            dataInfo.substring(
                0,
                if (dataInfo.contains(";")) dataInfo.indexOf(";")
                else dataInfo.length
            ))
    }

    fun dataStringToByteArray(dataString: String) : ByteArray {
        val dataInfo =
            dataString.substring(dataString.indexOf(":") + 1, dataString.indexOf(","))


        val rawData = getRawDataFromDataUri(dataString)
        return if (dataInfo.contains(";base64"))
            base64StringToByteArray(rawData)
            else dataString.toByteArray()
    }

    fun getRawDataFromDataUri(dataString: String) : String =
        dataString.substring(dataString.indexOf(",") + 1)

    fun base64StringToByteArray(dataString: String) : ByteArray {
        return Base64.decode(dataString, Base64.DEFAULT)
    }

    /**
     * Guess the name of the file that should be downloaded.
     *
     *
     * This method is largely identical to [android.webkit.URLUtil.guessFileName]
     * which unfortunately does not implement RFC 5987.
     *
     * @param url                Url to the content
     * @param contentDisposition Content-Disposition HTTP header or `null`
     * @param mimeType           Mime-type of the content or `null`
     * @return file name including extension
     */
    fun guessFileName(url: String, contentDisposition: String?, mimeType: String?): String {
        val extractedFileName = extractFileNameFromUrl(contentDisposition, url)
        val sanitizedMimeType = sanitizeMimeType(mimeType)

        // Split filename between base and extension
        // Add an extension if filename does not have one
        return if (extractedFileName.contains(".")) {
            extractedFileName
        } else {
            extractedFileName + createExtension(sanitizedMimeType)
        }
    }

    // Some site add extra information after the mimetype, for example 'application/pdf; qs=0.001'
    // we just want to extract the mimeType and ignore the rest.
    private fun sanitizeMimeType(mimeType: String?): String? {
        return if (mimeType != null) {
            if (mimeType.contains(";")) {
                mimeType.substringBefore(';')
            } else {
                mimeType
            }
        } else {
            null
        }
    }

    private fun extractFileNameFromUrl(contentDisposition: String?, url: String): String {
        var filename: String? = null

        // Extract file name from content disposition header field
        if (contentDisposition != null) {
            filename = parseContentDisposition(contentDisposition)
            if (filename == null) filename = url.substringAfterLast("/")
            // Filenames can't contain "?", otherwise DownloadManager will
            // throw an exception.
            // Check whether filename contains "?", reset filename if present
            if (filename.contains("?")) filename = null
        }

        // If all the other http-related approaches failed, use the plain uri
        if (filename == null) {
            var decodedUrl = Uri.decode(url)
            decodedUrl = decodedUrl.substringBefore('?')
            if (!decodedUrl.endsWith("/")) {
                filename = decodedUrl.substringAfterLast("/")
            }
        }

        // Finally, if couldn't get filename from URI, get a generic filename
        if (filename == null) {
            filename = "download"
        }
        return filename
    }

    /**
     * This is the regular expression to match the content disposition type segment.
     *
     *
     * A content disposition header can start either with inline or attachment followed by comma;
     * For example: attachment; filename="filename.jpg" or inline; filename="filename.jpg"
     * (inline|attachment)\\s*; -> Match either inline or attachment, followed by zero o more
     * optional whitespaces characters followed by a comma.
     */
    private const val contentDispositionType = "(inline|attachment)\\s*;"

    /**
     * This is the regular expression to match filename* parameter segment.
     *
     *
     * A content disposition header could have an optional filename* parameter,
     * the difference between this parameter and the filename is that this uses
     * the encoding defined in RFC 5987.
     *
     *
     * Some examples:
     * filename*=utf-8''success.html
     * filename*=iso-8859-1'en'file%27%20%27name.jpg
     * filename*=utf-8'en'filename.jpg
     *
     *
     * For matching this section we use:
     * \\s*filename\\s*=\\s*= -> Zero or more optional whitespaces characters
     * followed by filename followed by any zero or more whitespaces characters and the equal sign;
     *
     *
     * (utf-8|iso-8859-1)-> Either utf-8 or iso-8859-1 encoding types.
     *
     *
     * '[^']*'-> Zero or more characters that are inside of single quotes '' that are not single
     * quote.
     *
     *
     * (\S*) -> Zero or more characters that are not whitespaces. In this group,
     * it's where we are going to have the filename.
     */
    private const val contentDispositionFileNameAsterisk =
        "\\s*filename\\*\\s*=\\s*(utf-8|iso-8859-1)'[^']*'([^;\\s]*)"

    /**
     * Format as defined in RFC 2616 and RFC 5987
     * Both inline and attachment types are supported.
     * More details can be found
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition
     *
     *
     * The first segment is the [contentDispositionType], there you can find the documentation,
     * Next, it's the filename segment, where we have a filename="filename.ext"
     * For example, all of these could be possible in this section:
     * filename="filename.jpg"
     * filename="file\"name.jpg"
     * filename="file\\name.jpg"
     * filename="file\\\"name.jpg"
     * filename=filename.jpg
     *
     *
     * For matching this section we use:
     * \\s*filename\\s*=\\s*= -> Zero or more whitespaces followed by filename followed
     * by zero or more whitespaces and the equal sign.
     *
     *
     * As we want to extract the the content of filename="THIS", we use:
     *
     *
     * \\s* -> Zero or more whitespaces
     *
     *
     * (\"((?:\\\\.|[^|"\\\\])*)\" -> A quotation mark, optional : or \\ or any character,
     * and any non quotation mark or \\\\ zero or more times.
     *
     *
     * For example: filename="file\\name.jpg", filename="file\"name.jpg" and filename="file\\\"name.jpg"
     *
     *
     * We don't want to match after ; appears, For example filename="filename.jpg"; foo
     * we only want to match before the semicolon, so we use. |[^;]*)
     *
     *
     * \\s* ->  Zero or more whitespaces.
     *
     *
     * For supporting cases, where we have both filename and filename*, we use:
     * "(?:;$contentDispositionFileNameAsterisk)?"
     *
     *
     * Some examples:
     *
     *
     * attachment; filename="_.jpg"; filename*=iso-8859-1'en'file%27%20%27name.jpg
     * attachment; filename="_.jpg"; filename*=iso-8859-1'en'file%27%20%27name.jpg
     */
    private val contentDispositionPattern = Pattern.compile(
        contentDispositionType +
                "\\s*filename\\s*=\\s*(\"((?:\\\\.|[^\"\\\\])*)\"|[^;]*)\\s*" +
                "(?:;" + contentDispositionFileNameAsterisk + ")?",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * This is an alternative content disposition pattern where only filename* is available
     */
    private val fileNameAsteriskContentDispositionPattern = Pattern.compile(
        contentDispositionType +
                contentDispositionFileNameAsterisk, Pattern.CASE_INSENSITIVE
    )

    /**
     * Keys for the capture groups inside CONTENT_DISPOSITION_PATTERN
     */
    private const val ENCODED_FILE_NAME_GROUP = 5
    private const val ENCODING_GROUP = 4
    private const val QUOTED_FILE_NAME_GROUP = 3
    private const val UNQUOTED_FILE_NAME = 2

    /**
     * Belongs to the [fileNameAsteriskContentDispositionPattern]
     */
    private const val ALTERNATIVE_FILE_NAME_GROUP = 3
    private const val ALTERNATIVE_ENCODING_GROUP = 2
    private fun parseContentDisposition(contentDisposition: String): String? {
        try {
            val filename = parseContentDispositionWithFileName(contentDisposition)
            return filename ?: parseContentDispositionWithFileNameAsterisk(contentDisposition)
        } catch (ex: IllegalStateException) {
            // This function is defined as returning null when it can't parse the header
        }
        return null
    }

    private fun parseContentDispositionWithFileName(contentDisposition: String): String? {
        val m = contentDispositionPattern.matcher(contentDisposition)
        if (m.find()) {
            val encodedFileName = m.group(ENCODED_FILE_NAME_GROUP)
            val encoding = m.group(ENCODING_GROUP)
            if (encodedFileName != null && encoding != null) {
                try {
                    return decodeHeaderField(encodedFileName, encoding)
                } catch (e: UnsupportedEncodingException) {
                    // Do nothing
                }
            } else {
                // Return quoted string if available and replace escaped characters.
                val quotedFileName = m.group(QUOTED_FILE_NAME_GROUP)
                return quotedFileName?.replace("\\\\(.)".toRegex(), "$1") ?: m.group(
                    UNQUOTED_FILE_NAME
                )
            }
        }
        return null
    }

    private fun parseContentDispositionWithFileNameAsterisk(contentDisposition: String): String? {
        val alternative = fileNameAsteriskContentDispositionPattern.matcher(contentDisposition)
        if (alternative.find()) {
            val encoding = alternative.group(ALTERNATIVE_ENCODING_GROUP)
            val fileName = alternative.group(ALTERNATIVE_FILE_NAME_GROUP)
            if (encoding == null || fileName == null) return null
            try {
                return decodeHeaderField(fileName, encoding)
            } catch (e: UnsupportedEncodingException) {
                // Do nothing
            }
        }
        return null
    }

    /**
     * Definition as per RFC 5987, section 3.2.1. (value-chars)
     */
    private val encodedSymbolPattern =
        Pattern.compile("%[0-9a-f]{2}|[0-9a-z!#$&+-.^_`|~]", Pattern.CASE_INSENSITIVE)

    @Throws(UnsupportedEncodingException::class)
    private fun decodeHeaderField(field: String, encoding: String): String {
        val m = encodedSymbolPattern.matcher(field)
        val stream = ByteArrayOutputStream()
        while (m.find()) {
            val symbol = m.group()
            if (symbol.startsWith("%")) {
                stream.write(symbol.substring(1).toInt(16))
            } else {
                stream.write(symbol[0].code)
            }
        }
        return stream.toString(encoding)
    }

    /**
     * Guess the extension for a file using the mime type.
     */
    private fun createExtension(mimeType: String?): String {
        var extension: String? = null
        if (mimeType != null) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            if (extension != null) {
                extension = ".$extension"
            }
        }
        if (extension == null) {
            // checking startsWith to ignoring encoding value such as "text/html; charset=utf-8"
            extension = if (mimeType != null && mimeType.lowercase().startsWith("text/")) {
                if (mimeType.equals("text/html", ignoreCase = true)) {
                    ".html"
                } else {
                    ".txt"
                }
            } else {
                // If there's no mime type assume binary data
                ".bin"
            }
        }
        return extension
    }
}