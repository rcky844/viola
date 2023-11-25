/*
 * Copyright (C) 2021-2023 Tipz Team
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
package tipz.viola.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tipz.viola.Application;
import tipz.viola.search.SearchEngineEntries;
import tipz.viola.settings.SettingsKeys;
import tipz.viola.settings.SettingsUtils;

public class UrlUtils {
    /**
     * An array used for intent filtering
     */
    public static final String[] TypeSchemeMatch = {
            "text/html", "text/plain", "application/xhtml+xml", "application/vnd.wap.xhtml+xml",
            "http", "https", "ftp", "file"};

    private static final String httpUrlRegex = "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&\\\\=]*)(/.*)?";

    /**
     * Some revisions of Android (before 2018-04-01 SPL) before Android Pie has
     * security flaws in producing correct host name from url string in android.net.Uri,
     * patch it ourselves.
     * <p>
     * Ref: CVE-2017-13274
     *
     * @param url supplied url to check.
     * @return fixed up url
     */
    public static String cve_2017_13274(String url) {
        if (url.contains("\\") && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
            return url.replace("\\", "/");
        return url;
    }

    public static boolean isUriLaunchable(String uri) {
        return uri.matches(httpUrlRegex) || uri.startsWith("data:");
    }

    /**
     * URL Checker
     * <p>
     * Checks if URL is valid, if not, make it a search term.
     *
     * @param input the input to check.
     * @return result
     */
    public static String toSearchOrValidUrl(Context context, String input) {
        SharedPreferences pref = ((Application) context.getApplicationContext()).pref;
        String trimmedInput = cve_2017_13274(input.trim());

        Uri uri = Uri.parse(trimmedInput);
        if (uri.isRelative()) {
            uri = Uri.parse((CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.enforceHttps)) ?
                    "https://" : "http://").concat(trimmedInput));
            if (!uri.toString().matches(httpUrlRegex)) {
                return SearchEngineEntries.getSearchUrl(pref,
                        SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSearchId),
                        input, CommonUtils.getLanguage());
            }
        }
        return uri.toString();
    }

    /**
     * Keep aligned with desktop generic content types:
     * https://searchfox.org/mozilla-central/source/browser/components/downloads/DownloadsCommon.jsm#208
     */
    private static final String[] GENERIC_CONTENT_TYPES = {
            "application/octet-stream",
            "binary/octet-stream",
            "application/unknown"
    };

    /**
     * Guess the name of the file that should be downloaded.
     * <p>
     * This method is largely identical to {@link android.webkit.URLUtil#guessFileName}
     * which unfortunately does not implement RFC 5987.
     *
     * @param url                Url to the content
     * @param contentDisposition Content-Disposition HTTP header or {@code null}
     * @param mimeType           Mime-type of the content or {@code null}
     * @return file name including extension
     */
    public static String guessFileName(String url, @Nullable String contentDisposition, @Nullable String mimeType) {
        String extractedFileName = extractFileNameFromUrl(contentDisposition, url);
        String sanitizedMimeType = sanitizeMimeType(mimeType);

        // Split filename between base and extension
        // Add an extension if filename does not have one
        if (extractedFileName.contains(".")) {
            if (Arrays.asList(GENERIC_CONTENT_TYPES).contains(mimeType)) {
                return extractedFileName;
            } else {
                return changeExtension(extractedFileName, sanitizedMimeType);
            }
        } else {
            return extractedFileName + createExtension(sanitizedMimeType);
        }
    }

    // Some site add extra information after the mimetype, for example 'application/pdf; qs=0.001'
    // we just want to extract the mimeType and ignore the rest.
    private static String sanitizeMimeType(String mimeType) {
        if (mimeType != null) {
            if (mimeType.contains(";")) {
                return StringUtils.substringBefore(mimeType, ';');
            } else {
                return mimeType;
            }
        } else {
            return null;
        }
    }

    private static String extractFileNameFromUrl(String contentDisposition, String url) {
        String filename = null;

        // Extract file name from content disposition header field
        if (contentDisposition != null) {
            filename = parseContentDisposition(contentDisposition);
            if (filename == null)
                filename = StringUtils.substringAfterLast(url, "/");
            // Filenames can't contain "?", otherwise DownloadManager will
            // throw an exception.
            // Check whether filename contains "?", reset filename if present
            if (filename.contains("?"))
                filename = null;
        }

        // If all the other http-related approaches failed, use the plain uri
        if (filename == null) {
            String decodedUrl = Uri.decode(url);
            decodedUrl = StringUtils.substringBefore(decodedUrl, '?');
            decodedUrl = StringUtils.substringBefore(decodedUrl, ';');
            if (!decodedUrl.endsWith("/")) {
                filename = StringUtils.substringAfterLast(decodedUrl, "/");
            }
        }

        // Finally, if couldn't get filename from URI, get a generic filename
        if (filename == null) {
            filename = "download";
        }

        return filename;
    }

    /**
     * This is the regular expression to match the content disposition type segment.
     * <p>
     * A content disposition header can start either with inline or attachment followed by comma;
     * For example: attachment; filename="filename.jpg" or inline; filename="filename.jpg"
     * (inline|attachment)\\s*; -> Match either inline or attachment, followed by zero o more
     * optional whitespaces characters followed by a comma.
     */
    private static final String contentDispositionType = "(inline|attachment)\\s*;";

    /**
     * This is the regular expression to match filename* parameter segment.
     * <p>
     * A content disposition header could have an optional filename* parameter,
     * the difference between this parameter and the filename is that this uses
     * the encoding defined in RFC 5987.
     * <p>
     * Some examples:
     * filename*=utf-8''success.html
     * filename*=iso-8859-1'en'file%27%20%27name.jpg
     * filename*=utf-8'en'filename.jpg
     * <p>
     * For matching this section we use:
     * \\s*filename\\s*=\\s*= -> Zero or more optional whitespaces characters
     * followed by filename followed by any zero or more whitespaces characters and the equal sign;
     * <p>
     * (utf-8|iso-8859-1)-> Either utf-8 or iso-8859-1 encoding types.
     * <p>
     * '[^']*'-> Zero or more characters that are inside of single quotes '' that are not single
     * quote.
     * <p>
     * (\S*) -> Zero or more characters that are not whitespaces. In this group,
     * it's where we are going to have the filename.
     */
    private static final String contentDispositionFileNameAsterisk =
            "\\s*filename\\*\\s*=\\s*(utf-8|iso-8859-1)'[^']*'([^;\\s]*)";

    /**
     * Format as defined in RFC 2616 and RFC 5987
     * Both inline and attachment types are supported.
     * More details can be found
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition
     * <p>
     * The first segment is the [contentDispositionType], there you can find the documentation,
     * Next, it's the filename segment, where we have a filename="filename.ext"
     * For example, all of these could be possible in this section:
     * filename="filename.jpg"
     * filename="file\"name.jpg"
     * filename="file\\name.jpg"
     * filename="file\\\"name.jpg"
     * filename=filename.jpg
     * <p>
     * For matching this section we use:
     * \\s*filename\\s*=\\s*= -> Zero or more whitespaces followed by filename followed
     * by zero or more whitespaces and the equal sign.
     * <p>
     * As we want to extract the the content of filename="THIS", we use:
     * <p>
     * \\s* -> Zero or more whitespaces
     * <p>
     * (\"((?:\\\\.|[^|"\\\\])*)\" -> A quotation mark, optional : or \\ or any character,
     * and any non quotation mark or \\\\ zero or more times.
     * <p>
     * For example: filename="file\\name.jpg", filename="file\"name.jpg" and filename="file\\\"name.jpg"
     * <p>
     * We don't want to match after ; appears, For example filename="filename.jpg"; foo
     * we only want to match before the semicolon, so we use. |[^;]*)
     * <p>
     * \\s* ->  Zero or more whitespaces.
     * <p>
     * For supporting cases, where we have both filename and filename*, we use:
     * "(?:;$contentDispositionFileNameAsterisk)?"
     * <p>
     * Some examples:
     * <p>
     * attachment; filename="_.jpg"; filename*=iso-8859-1'en'file%27%20%27name.jpg
     * attachment; filename="_.jpg"; filename*=iso-8859-1'en'file%27%20%27name.jpg
     */
    private static final Pattern contentDispositionPattern =
            Pattern.compile(contentDispositionType +
                            "\\s*filename\\s*=\\s*(\"((?:\\\\.|[^\"\\\\])*)\"|[^;]*)\\s*" +
                            "(?:;" + contentDispositionFileNameAsterisk + ")?",
                    Pattern.CASE_INSENSITIVE);

    /**
     * This is an alternative content disposition pattern where only filename* is available
     */
    private static final Pattern fileNameAsteriskContentDispositionPattern =
            Pattern.compile(contentDispositionType +
                    contentDispositionFileNameAsterisk, Pattern.CASE_INSENSITIVE);

    /**
     * Keys for the capture groups inside CONTENT_DISPOSITION_PATTERN
     */
    private static final int ENCODED_FILE_NAME_GROUP = 5;
    private static final int ENCODING_GROUP = 4;
    private static final int QUOTED_FILE_NAME_GROUP = 3;
    private static final int UNQUOTED_FILE_NAME = 2;

    /**
     * Belongs to the [fileNameAsteriskContentDispositionPattern]
     */
    private static final int ALTERNATIVE_FILE_NAME_GROUP = 3;
    private static final int ALTERNATIVE_ENCODING_GROUP = 2;

    @Nullable
    private static String parseContentDisposition(String contentDisposition) {
        try {
            String filename = parseContentDispositionWithFileName(contentDisposition);
            if (filename == null)
                return parseContentDispositionWithFileNameAsterisk(contentDisposition);
            else
                return filename;
        } catch (IllegalStateException ex) {
            // This function is defined as returning null when it can't parse the header
        }

        return null;
    }

    private static String parseContentDispositionWithFileName(String contentDisposition) {
        Matcher m = contentDispositionPattern.matcher(contentDisposition);
        if (m.find()) {
            String encodedFileName = m.group(ENCODED_FILE_NAME_GROUP);
            String encoding = m.group(ENCODING_GROUP);
            if (encodedFileName != null && encoding != null) {
                try {
                    return decodeHeaderField(encodedFileName, encoding);
                } catch (UnsupportedEncodingException e) {
                    // Do nothing
                }
            } else {
                // Return quoted string if available and replace escaped characters.
                String quotedFileName = m.group(QUOTED_FILE_NAME_GROUP);
                if (quotedFileName != null)
                    return quotedFileName.replaceAll("\\\\(.)", "$1");
                else
                    return m.group(UNQUOTED_FILE_NAME);
            }
        }
        return null;
    }

    private static String parseContentDispositionWithFileNameAsterisk(String contentDisposition) {
        Matcher alternative = fileNameAsteriskContentDispositionPattern.matcher(contentDisposition);

        if (alternative.find()) {
            String encoding = alternative.group(ALTERNATIVE_ENCODING_GROUP);
            String fileName = alternative.group(ALTERNATIVE_FILE_NAME_GROUP);
            if (encoding == null || fileName == null)
                return null;

            try {
                return decodeHeaderField(fileName, encoding);
            } catch (UnsupportedEncodingException e) {
                // Do nothing
            }
        }
        return null;
    }

    /**
     * Definition as per RFC 5987, section 3.2.1. (value-chars)
     */
    private static final Pattern encodedSymbolPattern =
            Pattern.compile("%[0-9a-f]{2}|[0-9a-z!#$&+-.^_`|~]", Pattern.CASE_INSENSITIVE);

    private static String decodeHeaderField(String field, String encoding)
            throws UnsupportedEncodingException {
        Matcher m = encodedSymbolPattern.matcher(field);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        while (m.find()) {
            String symbol = m.group();

            if (symbol.startsWith("%")) {
                stream.write(Integer.parseInt(symbol.substring(1), 16));
            } else {
                stream.write(symbol.charAt(0));
            }
        }

        return stream.toString(encoding);
    }

    /**
     * Compare the filename extension with the mime type and change it if necessary.
     */
    private static String changeExtension(String filename, String mimeType) {
        String extension = null;
        int dotIndex = filename.lastIndexOf(".");

        if (mimeType != null) {
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            // Compare the last segment of the extension against the mime type.
            // If there's a mismatch, discard the entire extension.
            String typeFromExt = mimeTypeMap.getMimeTypeFromExtension(StringUtils.substringAfterLast(filename, "."));
            if (typeFromExt != null && typeFromExt.equalsIgnoreCase(mimeType)) {
                extension = "." + mimeTypeMap.getExtensionFromMimeType(mimeType);
                // Check if the extension needs to be changed
                if (filename.toLowerCase(Locale.ROOT).endsWith(extension.toLowerCase())) {
                    return filename;
                }
            }
        }

        if (extension != null) {
            return filename.substring(0, dotIndex) + extension;
        } else {
            return filename;
        }
    }

    /**
     * Guess the extension for a file using the mime type.
     */
    private static String createExtension(String mimeType) {
        String extension = null;

        if (mimeType != null) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension != null) {
                extension = "." + extension;
            }
        }

        if (extension == null) {
            // checking startsWith to ignoring encoding value such as "text/html; charset=utf-8"
            if (mimeType != null && mimeType.toLowerCase(Locale.ROOT).startsWith("text/")) {
                if (mimeType.equalsIgnoreCase("text/html")) {
                    extension = ".html";
                } else {
                    extension = ".txt";
                }
            } else {
                // If there's no mime type assume binary data
                extension = ".bin";
            }
        }

        return extension;
    }
}
