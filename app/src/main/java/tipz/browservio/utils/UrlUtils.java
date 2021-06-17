package tipz.browservio.utils;

import android.content.SharedPreferences;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tipz.browservio.utils.BrowservioSaverUtils.getPref;

public class UrlUtils {

    private static final String BASIC_URL_REGEX = "^((https?|ftp)://)?(www\\.)?([\\w]+\\.)+[\u200C\u200B\\w]{2,63}/?.*$";
    private static final Pattern p = Pattern.compile(BASIC_URL_REGEX);
    private static final String[] startsWithMatch = {
            "http:", "https:", "ftp:", "file:",
            "about:", "javascript:", "blob:", "data:"};

    /**
     * URL Checker
     *
     * Checks if URL is valid, if not, make it a search term.
     *
     * @param url is supplied as the URL to check.
     * @param canBeSearch sets if it should be changed to a search term when the supplied URL isn't valid.
     * @param pref as the SharedPreference to get the default search engine from.
     * @param defaultSearchPerf as the tag to get the default search engine from pref.
     * @return result
     */
    @Nullable
    public static String UrlChecker(String url, boolean canBeSearch, SharedPreferences pref, String defaultSearchPerf) {
        Matcher m = p.matcher(url);
        for (String match : startsWithMatch) {
            if (url.startsWith(match)) {
                return url;
            }
        }
        if (m.find()) {
            return "https://" + url;
        } else {
            if (url.endsWith("/") || url.endsWith("\\")) {
                return "https://" + url;
            }
            if (canBeSearch) {
                return getPref(pref, defaultSearchPerf) + url;
            }
        }
        return null;
    }
}
