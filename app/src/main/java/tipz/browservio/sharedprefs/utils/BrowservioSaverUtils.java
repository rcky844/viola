package tipz.browservio.sharedprefs.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import tipz.browservio.sharedprefs.AllPrefs;

public class BrowservioSaverUtils {
    public static SharedPreferences browservio_saver(Context context) {
        return context.getSharedPreferences(AllPrefs.browservio_saver, Activity.MODE_PRIVATE);
    }

    /**
     * Check if SharedPreferences is empty
     * <p>
     * Check if SharedPreferences is empty, and set it if it is.
     *
     * @param pref         as the SharedPreference to get the value from.
     * @param tag          as the tag to get the value from.
     * @param defaultValue as the value to set if empty.
     * @param mustSet      set it regardless.
     */
    public static void checkIfEmpty(SharedPreferences pref, String tag, String defaultValue, boolean mustSet) {
        if (getPref(pref, tag).isEmpty() || mustSet)
            setPref(pref, tag, defaultValue);
    }

    /**
     * Check if SharedPreferences is empty
     * <p>
     * Check if SharedPreferences is empty, and set it if it is.
     *
     * @param pref         as the SharedPreference to get the value from.
     * @param tag          as the tag to get the value from.
     * @param defaultValue as the value to set if empty.
     * @param mustSet      set it regardless.
     */
    public static void checkIfEmpty(SharedPreferences pref, String tag, int defaultValue, boolean mustSet) {
        if (getPref(pref, tag).isEmpty() || mustSet)
            setPrefNum(pref, tag, defaultValue);
    }

    /**
     * Set the tag as string bool according to boolean
     * <p>
     * Sets the string bool according to the boolean.
     *
     * @param pref as the SharedPreference to get the value from.
     * @param tag  as the tag to get the value from.
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
     * Set the tag as integer bool according to boolean
     * <p>
     * Sets the integer bool according to the boolean.
     *
     * @param pref as the SharedPreference to get the value from.
     * @param tag  as the tag to get the value from.
     * @param bool as the bool to check.
     * @param flip if the value needs to be flipped.
     */
    public static void setPrefIntBoolAccBool(SharedPreferences pref, String tag, boolean bool, boolean flip) {
        if (bool)
            setPrefNum(pref, tag, (flip) ? 0 : 1);
        else
            setPrefNum(pref, tag, (flip) ? 1 : 0);
    }

    /**
     * Set a preference to a string
     *
     * @param pref  as the SharedPreference to get the value from.
     * @param tag   as the tag to get the value from.
     * @param value as the value to set
     */
    public static void setPref(SharedPreferences pref, String tag, String value) {
        pref.edit().putString(tag, value).apply();
    }

    /**
     * Set a preference to a number
     *
     * @param pref  as the SharedPreference to get the value from.
     * @param tag   as the tag to get the value from.
     * @param value as the value to set
     */
    public static void setPrefNum(SharedPreferences pref, String tag, int value) {
        pref.edit().putInt(tag, value).apply();
    }

    /**
     * Get a string preference
     *
     * @param pref as the SharedPreference to get the value from.
     * @param tag  as the tag to get the value from.
     * @return the perf asked for.
     */
    public static String getPref(SharedPreferences pref, String tag) {
        return pref.getString(tag, "");
    }

    /**
     * Get a number preference
     *
     * @param pref as the SharedPreference to get the value from.
     * @param tag  as the tag to get the value from.
     * @return the perf asked for.
     */
    public static int getPrefNum(SharedPreferences pref, String tag) {
        return pref.getInt(tag, 0);
    }
}
