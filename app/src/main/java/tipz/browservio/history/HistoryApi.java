package tipz.browservio.history;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.CommonUtils;

public class HistoryApi {
    private final static int LATEST_API = 1;
    public final static String current_history_pref = SettingsKeys.history;

    public static SharedPreferences historyPref(Context context) {
        return context.getSharedPreferences(SettingsKeys.history_cfg, Activity.MODE_PRIVATE);
    }

    public static void HistoryApi_VerAdapter(SharedPreferences oldPref, SharedPreferences pref) {
        if (SettingsUtils.getPrefNum(pref, SettingsKeys.historyApi) == 0) {
            SettingsUtils.setPrefNum(pref, SettingsKeys.historyApi, LATEST_API);

            if (!SettingsUtils.getPref(oldPref, SettingsKeys.history).isEmpty()) {
                SettingsUtils.setPref(pref, SettingsKeys.history, SettingsUtils.getPref(oldPref, SettingsKeys.history));
            }

            SettingsUtils.setPref(oldPref, SettingsKeys.history, CommonUtils.EMPTY_STRING);
            SettingsUtils.setPrefNum(oldPref, SettingsKeys.historyApi, LATEST_API);
        }
    }

    public static void HistoryApi_VerChecker(SharedPreferences pref) {
        if (SettingsUtils.getPrefNum(pref, SettingsKeys.historyApi) > LATEST_API
                || SettingsUtils.getPrefNum(pref, SettingsKeys.historyApi) <= -1)
            throw new RuntimeException();
    }
}
