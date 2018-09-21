package tipz.browservio.utils;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {

    private static final String[] startsWithMatch = {
            "http", "https", "content", "ftp", "file",
            "about", "javascript", "blob", "data"};

    /**
     * URL Checker
     * <p>
     * Checks if URL is valid, if not, make it a search term.
     *
     * @param url         is supplied as the URL to check.
     * @param canBeSearch sets if it should be changed to a search term when the supplied URL isn't valid.
     * @param searchUrl   as the Url supplied for search.
     * @return result
     */
    public static String UrlChecker(String url, boolean canBeSearch, String searchUrl) {
        String trimmedUrl = url.trim();

        if (startsWithMatch(trimmedUrl))
            return trimmedUrl;

        if (!trimmedUrl.contains(" ") && (trimmedUrl.contains("/") || trimmedUrl.contains(".")))
            return "http://" + trimmedUrl;

        if (canBeSearch)
            return searchUrl + trimmedUrl;

        return trimmedUrl;
    }

    public static boolean startsWithMatch(String url) {
        for (String match : startsWithMatch) {
            if (url.startsWith(match.concat(":")))
                return true;
        }
        return false;
    }

    public static String composeSearchUrl(String inQuery, String template,
                                          String queryPlaceHolder) {
        return template.replace(queryPlaceHolder, inQuery);
    }

    /**
     * Guess the name of the file that should be downloaded.
     * <p>
     * This method is largely identical to {@link android.webkit.URLUtil#guessFileName}
     * which unfortunately does not implement RfC 5987.
     *
     * @param url                Url to the content
     * @param contentDisposition Content-Disposition HTTP header or {@code null}
     * @param mimeType           Mime-type of the content or {@code null}
     * @return file name including extension
     */
    public static String guessFileName(String url, @Nullable String contentDisposition, @Nullable String mimeType) {
        String filename = null;
        String extension = null;

        // Extract file name from content disposition header field
        if (contentDisposition != null) {
            filename = parseContentDisposition(contentDisposition);
            if (filename != null) {
                int index = filename.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = filename.substring(index);
                }
            }
        }

        // If all the other http-related approaches failed, use the plain uri
        if (filename == null) {
            String decodedUrl = Uri.decode(url);
            if (decodedUrl != null) {
                int queryIndex = decodedUrl.indexOf('?');
                // If there is a query string strip it, same as desktop browsers
                if (queryIndex > 0) {
                    decodedUrl = decodedUrl.substring(0, queryIndex);
                }
                if (!decodedUrl.endsWith("/")) {
                    int index = decodedUrl.lastIndexOf('/') + 1;
                    if (index > 0) {
                        filename = decodedUrl.substring(index);
                    }
                }
            }
        }

        // Finally, if couldn't get filename from URI, get a generic filename
        if (filename == null) {
            filename = "downloadfile";
        }

        // Split filename between base and extension
        // Add an extension if filename does not have one
        int dotIndex = filename.indexOf('.');
        if (dotIndex < 0) {
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                if (extension != null) {
                    extension = "." + extension;
                }
            }
            if (extension == null) {
                if (mimeType != null && mimeType.toLowerCase(Locale.ROOT).startsWith("text/")) {
                    if (mimeType.equalsIgnoreCase("text/html")) {
                        extension = ".html";
                    } else {
                        extension = ".txt";
                    }
                } else {
                    extension = ".bin";
                }
            }
        } else {
            if (mimeType != null) {
                // Compare the last segment of the extension against the mime type.
                // If there's a mismatch, discard the entire extension.
                int lastDotIndex = filename.lastIndexOf('.');
                String typeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        filename.substring(lastDotIndex + 1));
                if (typeFromExt == null || !typeFromExt.equalsIgnoreCase(mimeType)) {
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                    if (extension != null) {
                        extension = "." + extension;
                    }
                }
            }
            if (extension == null) {
                extension = filename.substring(dotIndex);
            }
            filename = filename.substring(0, dotIndex);
        }

        return filename + extension;
    }

    /**
     * Format as defined in RFC 2616 and RFC 5987
     * Both inline and attachment types are supported.
     */
    private static final Pattern CONTENT_DISPOSITION_PATTERN =
            Pattern.compile("(inline|attachment)\\s*;" +
                            "\\s*filename\\s*=\\s*(\"((?:\\\\.|[^\"\\\\])*)\"|[^;]*)\\s*" +
                            "(?:;\\s*filename\\*\\s*=\\s*(utf-8|iso-8859-1)'[^']*'(\\S*))?",
                    Pattern.CASE_INSENSITIVE);

    /**
     * Keys for the capture groups inside CONTENT_DISPOSITION_PATTERN
     */
    private static final int ENCODED_FILE_NAME_GROUP = 5;
    private static final int ENCODING_GROUP = 4;
    private static final int QUOTED_FILE_NAME_GROUP = 3;
    private static final int UNQUOTED_FILE_NAME = 2;

    @Nullable
    private static String parseContentDisposition(String contentDisposition) {
        try {
            Matcher m = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition);

            if (m.find()) {
                // If escaped string is found, decode it using the given encoding.
                String encodedFileName = m.group(ENCODED_FILE_NAME_GROUP);
                String encoding = m.group(ENCODING_GROUP);

                if (encodedFileName != null) {
                    return decodeHeaderField(encodedFileName, encoding);
                }

                // Return quoted string if available and replace escaped characters.
                String quotedFileName = m.group(QUOTED_FILE_NAME_GROUP);

                if (quotedFileName != null) {
                    return quotedFileName.replaceAll("\\\\(.)", "$1");
                }

                // Otherwise try to extract the unquoted file name
                return m.group(UNQUOTED_FILE_NAME);
            }
        } catch (IllegalStateException | UnsupportedEncodingException ex) {
            // This function is defined as returning null when it can't parse the header
        }

        return null;
    }

    /**
     * Definition as per RFC 5987, section 3.2.1. (value-chars)
     */
    private static final Pattern ENCODED_SYMBOL_PATTERN =
            Pattern.compile("%[0-9a-f]{2}|[0-9a-z!#$&+-.^_`|~]", Pattern.CASE_INSENSITIVE);

    private static String decodeHeaderField(String field, String encoding)
            throws UnsupportedEncodingException {
        Matcher m = ENCODED_SYMBOL_PATTERN.matcher(field);
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
}
