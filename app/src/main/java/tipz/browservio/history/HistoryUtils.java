package tipz.browservio.history;

import android.content.Context;

import tipz.browservio.broha.Broha;

public class HistoryUtils {
    /**
     * History Saviour
     * <p>
     * Module to save history into a SharedPref.
     */
    public static void appendData(Context context, String data) {
        HistoryApi.historyBroha(context).insertAll(new Broha(data));
    }

    public static void clear(Context context) {
        HistoryApi.historyBroha(context).deleteAll();
    }

    public static void deleteById(Context context, int id) {
        HistoryApi.historyBroha(context).deleteById(id);
    }

    public static boolean isEmptyCheck(Context context) {
        return HistoryApi.historyBroha(context).isEmpty().size() == 0;
    }

    public static String lastUrl(Context context) {
        return HistoryApi.historyBroha(context).lastUrl().getUrl();
    }
}
