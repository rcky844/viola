/*
 * Copyright (C) 2021-2023 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tipz.viola.settings;

import android.content.SharedPreferences;

/* TODO: rewrite to only use browservio_saver */
public class SettingsUtils {
    public static boolean isFirstLaunch(SharedPreferences pref) {
        return !SettingsUtils.getPref(pref, SettingsKeys.isFirstLaunch).equals("0");
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
