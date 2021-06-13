package tipz.browservio.Utils;

import android.content.SharedPreferences;

public class BrowservioSaverUtils {
    public static void checkIfEmpty(SharedPreferences perf, String tag, String defaultValue, boolean mustSet) {
        if (perf.getString(tag, "").equals("") || mustSet) {
            setPref(perf, tag, defaultValue);
        }
    }

    public static void setPref(SharedPreferences perf, String tag, String value) {
        perf.edit().putString(tag, value).apply();
    }

    public static String getPref(SharedPreferences perf, String tag) {
        return perf.getString(tag, "");
    }
}
