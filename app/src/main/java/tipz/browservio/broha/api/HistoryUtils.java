package tipz.browservio.broha.api;

import android.content.Context;

import tipz.browservio.broha.database.Broha;
import tipz.browservio.utils.CommonUtils;

public class HistoryUtils {
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
        Broha lastUrl = HistoryApi.historyBroha(context).lastUrl();
        return lastUrl == null ? CommonUtils.EMPTY_STRING : lastUrl.getUrl();
    }
}
