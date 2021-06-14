package tipz.browservio.Utils;

import android.content.SharedPreferences;

public class BrowservioSaverUtils {
    public static void checkIfEmpty(SharedPreferences pref, String tag, String defaultValue, boolean mustSet) {
        if (getPref(pref, tag).equals("") || mustSet) {
            setPref(pref, tag, defaultValue);
        }
    }

    public static void setPrefStringBoolAccBool(SharedPreferences pref, String tag, boolean bool, boolean flip) {
        if (bool) {
            setPref(pref, tag, (flip) ? "0" : "1");
        } else {
            setPref(pref, tag, (flip) ? "1" : "0");
        }
    }

    public static void setPref(SharedPreferences pref, String tag, String value) {
        pref.edit().putString(tag, value).apply();
    }

    public static String getPref(SharedPreferences pref, String tag) {
        return pref.getString(tag, "");
    }
}
