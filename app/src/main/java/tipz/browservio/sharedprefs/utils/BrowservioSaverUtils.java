package tipz.browservio.sharedprefs.utils;

import android.content.SharedPreferences;

public class BrowservioSaverUtils {
    /**
     * Check if SharedPreferences is empty
     *
     * Check if SharedPreferences is empty, and set it if it is.
     *
     * @param pref as the SharedPreference to get the value from.
     * @param tag as the tag to get the value from.
     * @param defaultValue as the value to set if empty.
     * @param mustSet set it regardless.
     */
    public static void checkIfEmpty(SharedPreferences pref, String tag, String defaultValue, boolean mustSet) {
        if (getPref(pref, tag).isEmpty() || mustSet)
            setPref(pref, tag, defaultValue);
    }

    /**
     * Set the tag as string bool according to boolean
     *
     * Sets the string bool according to the boolean.
     *
     * @param pref as the SharedPreference to get the value from.
     * @param tag as the tag to get the value from.
     * @param bool as the bool to check.
     * @param flip if the value needs to be flipped.
     */
    public static void setPrefStringBoolAccBool(SharedPreferences pref, String tag, boolean bool, boolean flip) {
        if (bool)
            setPref(pref, tag, (flip) ? "0" : "1");
        else
            setPref(pref, tag, (flip) ? "1" : "0");
    }

    /**
     * Set a preference to a string
     *
     * @param pref as the SharedPreference to get the value from.
     * @param tag as the tag to get the value from.
     * @param value as the value to set
     */
    public static void setPref(SharedPreferences pref, String tag, String value) {
        pref.edit().putString(tag, value).apply();
    }

    /**
     * Set a preference to a number
     *
     * @param pref as the SharedPreference to get the value from.
     * @param tag as the tag to get the value from.
     * @param value as the value to set
     */
    public static void setPrefNum(SharedPreferences pref, String tag, int value) {
        pref.edit().putInt(tag, value).apply();
    }

    /**
     * Get a string preference
     *
     * @param pref as the SharedPreference to get the value from.
     * @param tag as the tag to get the value from.
     * @return the perf asked for.
     */
    public static String getPref(SharedPreferences pref, String tag) {
        return pref.getString(tag, "");
    }

    /**
     * Get a number preference
     *
     * @param pref as the SharedPreference to get the value from.
     * @param tag as the tag to get the value from.
     * @return the perf asked for.
     */
    public static int getPrefNum(SharedPreferences pref, String tag) {
        return pref.getInt(tag, 0);
    }
}
