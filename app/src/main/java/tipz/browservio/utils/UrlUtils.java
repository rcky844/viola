package tipz.browservio.utils;

import android.webkit.URLUtil;

public class UrlUtils {

    private static final String[] startsWithMatch = {
            "http://", "https://", "ftp://", "file://",
            "about:", "javascript:", "blob:", "data:"};

    /**
     * URL Checker
     *
     * Checks if URL is valid, if not, make it a search term.
     *
     * @param url is supplied as the URL to check.
     * @param canBeSearch sets if it should be changed to a search term when the supplied URL isn't valid.
     * @param searchUrl as the Url supplied for search.
     * @return result
     */
    public static String UrlChecker(String url, boolean canBeSearch, String searchUrl) {
        for (String match : startsWithMatch) {
            if (url.startsWith(match)) {
                return url;
            }
        }
        if (URLUtil.isValidUrl(url)) {
            return url;
        } else {
            if (url.endsWith("/") || url.endsWith("\\")) {
                return "https://" + url;
            }
            if (canBeSearch) {
                return searchUrl + url;
            }
        }
        return url;
    }
}
