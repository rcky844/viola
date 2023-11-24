/*
 * Copyright (C) 2021-2023 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tipz.viola.broha.api;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import tipz.viola.Application;
import tipz.viola.broha.database.Broha;
import tipz.viola.broha.database.BrohaDao;
import tipz.viola.settings.SettingsKeys;
import tipz.viola.settings.SettingsUtils;
import tipz.viola.utils.CommonUtils;

public class HistoryApi {
    private final static int LATEST_API = 2;

    /* Old pref keys for migration */
    private static final String history = "history";

    private static SharedPreferences historyPref(Context context) {
        return context.getSharedPreferences("history.cfg", Activity.MODE_PRIVATE);
    }

    public static BrohaDao historyBroha(Context context) {
        return ((Application) context.getApplicationContext()).historyBroha;
    }

    public static void doApiInitCheck(Context context) {
        SharedPreferences pref = ((Application) context.getApplicationContext()).pref;

        if (SettingsUtils.getPrefNum(pref, SettingsKeys.historyApi) > LATEST_API
                || SettingsUtils.getPrefNum(pref, SettingsKeys.historyApi) <= -1)
            throw new RuntimeException();

        String historyData;
        switch (SettingsUtils.getPrefNum(pref, SettingsKeys.historyApi)) {
            case 0:
                historyData = SettingsUtils.getPref(pref, history);
                if (!historyData.isEmpty())
                    SettingsUtils.setPref(historyPref(context), history, historyData);

                SettingsUtils.setPref(pref, history, CommonUtils.EMPTY_STRING);
            case 1:
                historyData = SettingsUtils.getPref(historyPref(context), history);
                String[] listData = SettingsUtils.getPref(historyPref(context), history).trim().split("\n");
                if (!historyData.isEmpty())
                    for (String listDatum : listData)
                        historyBroha(context).insertAll(
                                new Broha(listDatum));
                historyPref(context).edit().clear().apply();
        }
        SettingsUtils.setPrefNum(pref, SettingsKeys.historyApi, LATEST_API);
    }
}
