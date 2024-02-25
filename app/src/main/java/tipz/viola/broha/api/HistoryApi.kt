/*
 * Copyright (c) 2021-2024 Tipz Team
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
package tipz.viola.broha.api

import android.content.Context
import tipz.viola.Application
import tipz.viola.broha.database.BrohaDao
import tipz.viola.settings.SettingsKeys

object HistoryApi {
    private const val LATEST_API = 0

    fun historyBroha(context: Context): BrohaDao? {
        return (context.applicationContext as Application).historyBroha
    }

    fun doApiInitCheck(context: Context) {
        val settingsPreference = (context.applicationContext as Application).settingsPreference!!
        val historyApiVer = settingsPreference.getInt(SettingsKeys.historyApi)
        if (historyApiVer > LATEST_API || historyApiVer <= -1) throw RuntimeException()
        settingsPreference.setInt(SettingsKeys.historyApi, LATEST_API)
    }
}