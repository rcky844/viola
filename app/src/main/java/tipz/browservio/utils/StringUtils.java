package tipz.browservio.utils;

public class StringUtils {
    public static final int INDEX_NOT_FOUND = -1;

    public static String substringBefore(final String str, final int separator) {
        if (str.isEmpty()) {
            return str;
        }
        final int pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    public static String substringAfterLast(final String str, final String separator) {
        if (str.isEmpty()) {
            return str;
        }
        if (separator.isEmpty()) {
            return CommonUtils.EMPTY_STRING;
        }
        final int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND || pos == str.length() - separator.length()) {
            return CommonUtils.EMPTY_STRING;
        }
        return str.substring(pos + separator.length());
    }
}
