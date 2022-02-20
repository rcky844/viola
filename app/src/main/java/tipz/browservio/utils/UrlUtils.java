package tipz.browservio.utils;

import static tipz.browservio.urls.BrowservioURLs.realErrUrl;

public class UrlUtils {

    private static final String[] startsWithMatch = {
            "http:", "https:", "ftp:", "file:",
            "about:", "javascript:", "blob:", "data:"};

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
        if (url.contains("/?"))
            return realErrUrl;
        for (String match : startsWithMatch) {
            if (url.startsWith(match)) {
                return url;
            }
        }
        if (url.endsWith("/") || url.endsWith("\\") || url.contains("."))
            return "http://" + url;
        if (canBeSearch)
            return searchUrl + url;
        return url;
    }

    public static String composeSearchUrl(String inQuery, String template,
                                          String queryPlaceHolder) {
        return template.replace(queryPlaceHolder, inQuery);
    }
}
