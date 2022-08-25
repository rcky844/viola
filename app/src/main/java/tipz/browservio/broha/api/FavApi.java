package tipz.browservio.broha.api;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import tipz.browservio.Application;
import tipz.browservio.broha.database.BrohaDao;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;

public class FavApi {
    private final static int LATEST_API = 1;

    /* Old pref keys for migration */
    private static String bookmarked(int count) {
        return "bookmarked_".concat(Integer.toString(count));
    }
    private static final String bookmarked_title = "_title";
    private static final String bookmarked_show = "_show";

    private static SharedPreferences bookmarks(Context context) {
        return context.getSharedPreferences("bookmarks.cfg", Activity.MODE_PRIVATE);
    }

    public static BrohaDao favBroha(Context context) {
        return ((Application) context.getApplicationContext()).favBroha;
    }

    public static void doApiInitCheck(Context context) {
        SharedPreferences pref = ((Application) context.getApplicationContext()).pref;

        if (SettingsUtils.getPrefNum(pref, SettingsKeys.favApi) > LATEST_API
                || SettingsUtils.getPrefNum(pref, SettingsKeys.favApi) <= -1)
            throw new RuntimeException();

        if (SettingsUtils.getPrefNum(pref, SettingsKeys.favApi) == 0) {
            int populate_count = 0;
            while (populate_count != -1) {
                String shouldShow = SettingsUtils.getPref(bookmarks(context), bookmarked(populate_count).concat(bookmarked_show));
                if (!shouldShow.equals("0")) {
                    if (shouldShow.isEmpty())
                        populate_count = -2;
                    else
                        FavUtils.appendData(context, null, SettingsUtils.getPref(bookmarks(context), bookmarked(populate_count).concat(bookmarked_title)), SettingsUtils.getPref(bookmarks(context), bookmarked(populate_count)), null);
                }
                populate_count++;
            }
            bookmarks(context).edit().clear().apply();
        }
        SettingsUtils.setPrefNum(pref, SettingsKeys.favApi, LATEST_API);
    }
}
