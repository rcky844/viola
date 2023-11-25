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
package tipz.viola.broha.api

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import tipz.viola.Application
import tipz.viola.broha.database.BrohaDao
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsUtils.getPref
import tipz.viola.settings.SettingsUtils.getPrefNum
import tipz.viola.settings.SettingsUtils.setPrefNum

object FavApi {
    private const val LATEST_API = 0

    @JvmStatic
    fun favBroha(context: Context): BrohaDao? {
        return (context.applicationContext as Application).favBroha
    }

    fun doApiInitCheck(context: Context) {
        val pref = (context.applicationContext as Application).pref
        if (getPrefNum(pref!!, SettingsKeys.favApi) > LATEST_API
            || getPrefNum(pref, SettingsKeys.favApi) <= -1
        ) throw RuntimeException()
        setPrefNum(pref, SettingsKeys.favApi, LATEST_API)
    }
}