package tipz.browservio.broha.api;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import tipz.browservio.Application;
import tipz.browservio.broha.database.Broha;
import tipz.browservio.broha.database.BrohaDao;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.CommonUtils;

public class HistoryApi {
    private final Context context;
    private final static int LATEST_API = 2;

    /* Old pref keys for migration */
    private static final String history = "history";
    private static final String historyApi = "historyApi";

    private static SharedPreferences historyPref(Context context) {
        return context.getSharedPreferences("history.cfg", Activity.MODE_PRIVATE);
    }

    public static BrohaDao historyBroha(Context context) {
        return ((Application) context.getApplicationContext()).historyBroha;
    }

    public HistoryApi(Context c) {
        context = c;
        verChecker();
        verAdapter();
    }

    private void verAdapter() {
        String historyData;
        switch (SettingsUtils.getPrefNum(browservio_saver(context), historyApi)) {
            case 0:
                historyData = SettingsUtils.getPref(browservio_saver(context), history);
                if (!historyData.isEmpty())
                    SettingsUtils.setPref(historyPref(context), history, historyData);

                SettingsUtils.setPref(browservio_saver(context), history, CommonUtils.EMPTY_STRING);
            case 1:
                historyData = SettingsUtils.getPref(historyPref(context), history);
                String[] listData = SettingsUtils.getPref(historyPref(context), history).trim().split("\n");
                if (!historyData.isEmpty())
                    for (String listDatum : listData)
                        historyBroha(context).insertAll(
                                new Broha(listDatum));
                historyPref(context).edit().clear().apply();
        }
        SettingsUtils.setPrefNum(browservio_saver(context), historyApi, LATEST_API);
    }

    private void verChecker() {
        if (SettingsUtils.getPrefNum(browservio_saver(context), historyApi) > LATEST_API
                || SettingsUtils.getPrefNum(browservio_saver(context), historyApi) <= -1)
            throw new RuntimeException();
    }
}
