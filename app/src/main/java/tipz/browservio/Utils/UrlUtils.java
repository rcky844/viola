package tipz.browservio.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {

    private static final String BASIC_URL_REGEX = "^((https?|ftp)://)?(www\\.)?([\\w]+\\.)+[\u200C\u200B\\w]{2,63}/?.*$";
    private static final Pattern p = Pattern.compile(BASIC_URL_REGEX);

    public static String UrlChecker(String url, boolean canBeSearch) {
        Matcher m = p.matcher(url);
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://") || url.startsWith("file://")) {
            return url;
        }
        if (m.find()) {
            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("ftp://")) {
                /*
                if (url.startsWith("http://")) {
                    return url.replaceFirst("http://", "https://");
                }
                */
                return "https://" + url;
            }
        } else {
            if (canBeSearch) {
                return "{se}" + url;
            }
        }
        return null;
    }
}
