package tipz.browservio.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import tipz.browservio.utils.CommonUtils;

public class SettingsUtils {
    public static SharedPreferences browservio_saver(Context context) {
        return context.getSharedPreferences(SettingsKeys.browservio_saver, Activity.MODE_PRIVATE);
    }

    /**
     * Check if SharedPreferences is empty
     * <p>
     * Check if SharedPreferences is empty, and set it if it is.
     *
     * @param pref         SharedPreference to get the value from.
     * @param tag          tag array to get the value from.
     * @param defaultValue value array to set if empty.
     */
    public static void checkIfEmpty(SharedPreferences pref, String[] tag, Object[] defaultValue) {
        int listLength = tag.length;
        if (listLength != defaultValue.length)
            return;

        boolean mustSet = CommonUtils.isIntStrOne(SettingsUtils.getPref(pref, SettingsKeys.isFirstLaunch));

        for (int i = 0; i < listLength; i++) {
            if ((defaultValue[i] instanceof String ? getPref(pref, tag[i]).isEmpty() : getPrefNum(pref, tag[i]) == 0)) {
                if (defaultValue[i] instanceof String)
                    setPref(pref, tag[i], (String) defaultValue[i]);
                else if (mustSet)
                    setPrefNum(pref, tag[i], (Integer) defaultValue[i]);
            }
        }

        if (mustSet)
            setPref(pref, SettingsKeys.isFirstLaunch, "0");
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
