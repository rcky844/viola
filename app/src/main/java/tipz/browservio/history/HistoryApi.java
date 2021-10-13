package tipz.browservio.history;

import android.content.SharedPreferences;

import java.io.IOException;

import tipz.browservio.sharedprefs.AllPrefs;
import tipz.browservio.sharedprefs.utils.BrowservioSaverUtils;
import tipz.browservio.utils.BrowservioBasicUtil;

public class HistoryApi {
    private final static int LATEST_API = 1;
    public final static String current_history_pref = AllPrefs.history;
    
    public static void HistoryApi_VerAdapter(SharedPreferences oldPref, SharedPreferences pref) throws IOException {
        if (BrowservioSaverUtils.getPrefNum(pref, AllPrefs.historyApi) == 0) {
            BrowservioSaverUtils.setPrefNum(pref, AllPrefs.historyApi, LATEST_API);

            if (!BrowservioSaverUtils.getPref(oldPref, AllPrefs.history).isEmpty()) {
                BrowservioSaverUtils.setPref(pref, AllPrefs.history, BrowservioSaverUtils.getPref(oldPref, AllPrefs.history));
            }

            BrowservioSaverUtils.setPref(oldPref, AllPrefs.history, BrowservioBasicUtil.EMPTY_STRING);
            BrowservioSaverUtils.setPrefNum(oldPref, AllPrefs.historyApi, LATEST_API);
        }
    }

    public static void HistoryApi_VerChecker(SharedPreferences pref) {
        if (BrowservioSaverUtils.getPrefNum(pref, AllPrefs.historyApi) > LATEST_API
                || BrowservioSaverUtils.getPrefNum(pref, AllPrefs.historyApi) <= -1)
            throw new RuntimeException();
    }
}
