package tipz.browservio.history;

import static tipz.browservio.history.HistoryApi.historyPref;
import static tipz.browservio.settings.SettingsUtils.browservio_saver;

import android.content.Context;

public class HistoryInit {
    public HistoryInit(Context c) {
        HistoryApi.HistoryApi_VerChecker(historyPref(c));
        HistoryApi.HistoryApi_VerAdapter(browservio_saver(c), historyPref(c));
    }
}
