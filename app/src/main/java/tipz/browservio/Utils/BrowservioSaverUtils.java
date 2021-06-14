package tipz.browservio.Utils;

import android.content.SharedPreferences;

public class BrowservioSaverUtils {
    public static void checkIfEmpty(SharedPreferences pref, String tag, String defaultValue, boolean mustSet) {
        if (getPref(pref, tag).equals("") || mustSet) {
            setPref(pref, tag, defaultValue);
        }
    }

    public static void setPref(SharedPreferences pref, String tag, String value) {
        pref.edit().putString(tag, value).apply();
    }

    public static String getPref(SharedPreferences pref, String tag) {
        return pref.getString(tag, "");
    }
}
