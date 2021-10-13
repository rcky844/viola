package tipz.browservio.history;

import android.content.SharedPreferences;

import tipz.browservio.sharedprefs.utils.BrowservioSaverUtils;
import tipz.browservio.utils.BrowservioBasicUtil;

public class HistoryReader {
    public static String history_data(SharedPreferences pref) {
        return BrowservioSaverUtils.getPref(pref, HistoryApi.current_history_pref);
    }

    /**
     * History Saviour
     *
     * Module to save history into a SharedPref.
     */
    public static void appendData(SharedPreferences pref, String data) {
        BrowservioSaverUtils.setPref(pref,
                HistoryApi.current_history_pref,
                (history_data(pref).concat(history_data(pref).isEmpty() ?
                                BrowservioBasicUtil.EMPTY_STRING
                                : BrowservioBasicUtil.LINE_SEPARATOR()).concat(data)));
    }

    public static void clear(SharedPreferences pref) {
        BrowservioSaverUtils.setPref(pref, HistoryApi.current_history_pref, BrowservioBasicUtil.EMPTY_STRING);
    }

    public static void write(SharedPreferences pref, String data) {
        BrowservioSaverUtils.setPref(pref, HistoryApi.current_history_pref, data);
    }

    public static boolean isEmptyCheck(SharedPreferences pref) {
        return BrowservioSaverUtils.getPref(pref, HistoryApi.current_history_pref).trim().isEmpty();
    }
}
