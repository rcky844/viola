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
package tipz.browservio.broha.api;

import android.content.Context;

import tipz.browservio.broha.database.Broha;
import tipz.browservio.utils.CommonUtils;

public class HistoryUtils {
    public static void clear(Context context) {
        HistoryApi.historyBroha(context).deleteAll();
    }

    public static void deleteById(Context context, int id) {
        HistoryApi.historyBroha(context).deleteById(id);
    }

    public static boolean isEmptyCheck(Context context) {
        return HistoryApi.historyBroha(context).isEmpty().size() == 0;
    }

    public static String lastUrl(Context context) {
        Broha lastUrl = HistoryApi.historyBroha(context).lastUrl();
        return lastUrl == null ? CommonUtils.EMPTY_STRING : lastUrl.getUrl();
    }
}
