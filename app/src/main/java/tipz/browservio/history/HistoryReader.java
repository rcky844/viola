package tipz.browservio.history;

import static tipz.browservio.history.HistoryApi.historyPref;

import android.content.Context;

import tipz.browservio.sharedprefs.utils.BrowservioSaverUtils;
import tipz.browservio.utils.BrowservioBasicUtil;

public class HistoryReader {
    public static String history_data(Context context) {
        return BrowservioSaverUtils.getPref(historyPref(context), HistoryApi.current_history_pref);
    }

    /**
     * History Saviour
     * <p>
     * Module to save history into a SharedPref.
     */
    public static void appendData(Context context, String data) {
        BrowservioSaverUtils.setPref(historyPref(context),
                HistoryApi.current_history_pref,
                (history_data(context).concat(history_data(context).isEmpty() ?
                        BrowservioBasicUtil.EMPTY_STRING
                        : BrowservioBasicUtil.LINE_SEPARATOR()).concat(data)));
    }

    public static void clear(Context context) {
        BrowservioSaverUtils.setPref(historyPref(context), HistoryApi.current_history_pref, BrowservioBasicUtil.EMPTY_STRING);
    }

    public static void write(Context context, String data) {
        BrowservioSaverUtils.setPref(historyPref(context), HistoryApi.current_history_pref, data);
    }

    public static boolean isEmptyCheck(Context context) {
        return BrowservioSaverUtils.getPref(historyPref(context), HistoryApi.current_history_pref).trim().isEmpty();
    }
}
