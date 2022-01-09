package tipz.browservio.history;

import android.content.SharedPreferences;

public class HistoryInit {
    public HistoryInit(SharedPreferences oldPref, SharedPreferences pref) {
        HistoryApi.HistoryApi_VerChecker(pref);
        HistoryApi.HistoryApi_VerAdapter(oldPref, pref);
    }
}
