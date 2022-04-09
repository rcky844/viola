package tipz.browservio.fav;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import tipz.browservio.broha.BrohaClient;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;

public class FavApi {
    private final Context context;
    private final static int LATEST_API = 1;

    /* Old pref keys for migration */
    private static final String favApi = "favApi";

    public static SharedPreferences bookmarks(Context context) {
        return context.getSharedPreferences(SettingsKeys.bookmarks, Activity.MODE_PRIVATE);
    }

    public static BrohaClient favBroha(Context context) {
        return new BrohaClient(context, "bookmarks");
    }

    /* TODO: set to public */
    private FavApi(Context c) {
        context = c;
        verChecker();
        verAdapter();
    }

    private void verAdapter() {
        if (SettingsUtils.getPrefNum(browservio_saver(context), favApi) == 0) {/* TODO: Write migrator */
        }
        SettingsUtils.setPrefNum(browservio_saver(context), favApi, LATEST_API);
    }

    private void verChecker() {
        if (SettingsUtils.getPrefNum(browservio_saver(context), favApi) > LATEST_API
                || SettingsUtils.getPrefNum(browservio_saver(context), favApi) <= -1)
            throw new RuntimeException();
    }
}
