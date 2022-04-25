package tipz.browservio.fav;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import tipz.browservio.broha.BrohaClient;
import tipz.browservio.broha.BrohaDao;
import tipz.browservio.settings.SettingsUtils;

public class FavApi {
    private final Context context;
    private final static int LATEST_API = 1;

    /* Old pref keys for migration */
    private static final String favApi = "favApi";
    private static String bookmarked(int count) {
        return "bookmarked_".concat(Integer.toString(count));
    }
    private static final String bookmarked_title = "_title";
    private static final String bookmarked_show = "_show";

    private static SharedPreferences bookmarks(Context context) {
        return context.getSharedPreferences("bookmarks.cfg", Activity.MODE_PRIVATE);
    }

    public static BrohaDao favBroha(Context context) {
        return new BrohaClient(context, "bookmarks").getDao();
    }

    public FavApi(Context c) {
        context = c;
        verChecker();
        verAdapter();
    }

    private void verAdapter() {
        if (SettingsUtils.getPrefNum(browservio_saver(context), favApi) == 0) {
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
        SettingsUtils.setPrefNum(browservio_saver(context), favApi, LATEST_API);
    }

    private void verChecker() {
        if (SettingsUtils.getPrefNum(browservio_saver(context), favApi) > LATEST_API
                || SettingsUtils.getPrefNum(browservio_saver(context), favApi) <= -1)
            throw new RuntimeException();
    }
}
