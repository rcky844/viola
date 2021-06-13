package tipz.browservio.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {

    public static final String BASIC_URL_REGEX = "^((https?|ftp)://)?(www\\.)?([\\w]+\\.)+[\u200C\u200B\\w]{2,63}/?.*$";
    public static Pattern p = Pattern.compile(BASIC_URL_REGEX);
    public static Matcher m;

    public static String UrlUtils(String url, boolean canBeSearch) {
        m = p.matcher(url);
        if (m.find()) {
            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("ftp://")) {
                /*
                if (url.startsWith("http://")) {
                    return url.replaceFirst("http://", "https://");
                }
                */
                return "https://" + url;
            } else {
                return url;
            }
        } else {
            if (canBeSearch) {
                return "{se}" + url;
            }
        }
        return null;
    }
}
