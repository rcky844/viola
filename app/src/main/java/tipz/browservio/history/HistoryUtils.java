package tipz.browservio.history;

import android.content.Context;
import android.graphics.Bitmap;

import tipz.browservio.broha.Broha;
import tipz.browservio.broha.icons.IconHashClient;

public class HistoryUtils {
    /**
     * History Saviour
     * <p>
     * Module to save history into a SharedPref.
     */
    public static void appendData(Context context, String url) {
        HistoryApi.historyBroha(context).insertAll(new Broha(url));
    }

    public static void updateData(Context context, IconHashClient iconHashClient, String title, String url, Bitmap icon) {
        Broha lastBroha = HistoryApi.historyBroha(context).lastUrl();
        if (!isEmptyCheck(context)) {
            if (title != null)
                lastBroha.setTitle(title);
            if (url != null)
                lastBroha.setUrl(url);
            if (icon != null) {
                lastBroha.setIconHash(iconHashClient.save(context, icon));
                lastBroha.setTitle(title);
            }
            HistoryApi.historyBroha(context).updateBroha(lastBroha);
        }
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
