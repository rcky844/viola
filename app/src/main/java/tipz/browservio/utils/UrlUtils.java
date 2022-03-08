package tipz.browservio.utils;

import static tipz.browservio.utils.urls.BrowservioURLs.realErrUrl;

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

        if (startsWithMatch(url))
            return url;

        if (url.endsWith("/") || url.contains("."))
            return "http://" + url;

        if (canBeSearch)
            return searchUrl + url;

        return url;
    }

    public static boolean startsWithMatch(String url) {
        for (String match : startsWithMatch) {
            if (url.startsWith(match))
                return true;
        }
        return false;
    }

    public static String composeSearchUrl(String inQuery, String template,
                                          String queryPlaceHolder) {
        return template.replace(queryPlaceHolder, inQuery);
    }
}
