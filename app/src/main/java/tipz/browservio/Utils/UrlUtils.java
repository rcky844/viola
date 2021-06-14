package tipz.browservio.Utils;

import android.content.SharedPreferences;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tipz.browservio.Utils.BrowservioSaverUtils.getPref;

public class UrlUtils {

    private static final String BASIC_URL_REGEX = "^((https?|ftp)://)?(www\\.)?([\\w]+\\.)+[\u200C\u200B\\w]{2,63}/?.*$";
    private static final Pattern p = Pattern.compile(BASIC_URL_REGEX);

    public static String UrlChecker(String url, boolean canBeSearch, SharedPreferences pref, String defaultSearchPerf) {
        Matcher m = p.matcher(url);
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://") || url.startsWith("file://")) {
            return url;
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
