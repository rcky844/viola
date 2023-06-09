/*
 * Copyright (C) 2022-2023 Tipz Team
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
package tipz.browservio;

import android.content.SharedPreferences;
import android.os.Build;

import com.google.android.material.color.DynamicColors;

import tipz.browservio.broha.api.FavApi;
import tipz.browservio.broha.api.HistoryApi;
import tipz.browservio.broha.database.BrohaClient;
import tipz.browservio.broha.database.BrohaDao;
import tipz.browservio.broha.database.icons.IconHashClient;
import tipz.browservio.settings.SettingsInit;

public class Application extends android.app.Application {
    public BrohaDao historyBroha;
    public BrohaDao favBroha;
    public IconHashClient iconHashClient;
    public SharedPreferences pref;

    @Override
    public void onCreate() {
        super.onCreate();

        pref = new SettingsInit(this).pref; /* Init settings check */
        HistoryApi.doApiInitCheck(this);
        FavApi.doApiInitCheck(this);

        historyBroha = new BrohaClient(this, "history").getDao();
        favBroha = new BrohaClient(this, "bookmarks").getDao();
        iconHashClient = new IconHashClient(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
