package tipz.browservio.history;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.CommonUtils;

public class HistoryApi {
    private final Context context;
    private final static int LATEST_API = 1;
    public final static String current_history_pref = SettingsKeys.history;

    public static SharedPreferences historyPref(Context context) {
        return context.getSharedPreferences(SettingsKeys.history_cfg, Activity.MODE_PRIVATE);
    }

    public HistoryApi(Context c) {
        context = c;
        verChecker();
        verAdapter();
    }

    private void verAdapter() {
        if (SettingsUtils.getPrefNum(historyPref(context), SettingsKeys.historyApi) == 0) {
            SettingsUtils.setPrefNum(historyPref(context), SettingsKeys.historyApi, LATEST_API);

            if (!SettingsUtils.getPref(browservio_saver(context), SettingsKeys.history).isEmpty()) {
                SettingsUtils.setPref(historyPref(context), SettingsKeys.history, SettingsUtils.getPref(browservio_saver(context), SettingsKeys.history));
            }

            SettingsUtils.setPref(browservio_saver(context), SettingsKeys.history, CommonUtils.EMPTY_STRING);
        }
    }

    private void verChecker() {
        if (SettingsUtils.getPrefNum(historyPref(context), SettingsKeys.historyApi) > LATEST_API
                || SettingsUtils.getPrefNum(historyPref(context), SettingsKeys.historyApi) <= -1)
            throw new RuntimeException();
    }
}
