package tipz.browservio.history;

import static tipz.browservio.history.HistoryApi.historyPref;

import android.content.Context;

import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.CommonUtils;

public class HistoryReader {
    public static String history_data(Context context) {
        return SettingsUtils.getPref(historyPref(context), HistoryApi.current_history_pref);
    }

    /**
     * History Saviour
     * <p>
     * Module to save history into a SharedPref.
     */
    public static void appendData(Context context, String data) {
        SettingsUtils.setPref(historyPref(context),
                HistoryApi.current_history_pref,
                (history_data(context).concat(history_data(context).isEmpty() ?
                        CommonUtils.EMPTY_STRING
                        : CommonUtils.LINE_SEPARATOR()).concat(data)));
    }

    public static void clear(Context context) {
        SettingsUtils.setPref(historyPref(context), HistoryApi.current_history_pref, CommonUtils.EMPTY_STRING);
    }

    public static void write(Context context, String data) {
        SettingsUtils.setPref(historyPref(context), HistoryApi.current_history_pref, data);
    }

    public static boolean isEmptyCheck(Context context) {
        return SettingsUtils.getPref(historyPref(context), HistoryApi.current_history_pref).trim().isEmpty();
    }
}
