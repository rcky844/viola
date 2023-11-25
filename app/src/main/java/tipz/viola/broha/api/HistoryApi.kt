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
package tipz.viola.broha.api

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import tipz.viola.Application
import tipz.viola.broha.database.Broha
import tipz.viola.broha.database.BrohaDao
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsUtils.getPref
import tipz.viola.settings.SettingsUtils.getPrefNum
import tipz.viola.settings.SettingsUtils.setPref
import tipz.viola.settings.SettingsUtils.setPrefNum
import tipz.viola.utils.CommonUtils

object HistoryApi {
    private const val LATEST_API = 2

    /* Old pref keys for migration */
    private const val history = "history"
    private fun historyPref(context: Context): SharedPreferences {
        return context.getSharedPreferences("history.cfg", Activity.MODE_PRIVATE)
    }

    fun historyBroha(context: Context): BrohaDao? {
        return (context.applicationContext as Application).historyBroha
    }

    fun doApiInitCheck(context: Context) {
        val pref = (context.applicationContext as Application).pref
        if (getPrefNum(pref!!, SettingsKeys.historyApi) > LATEST_API
            || getPrefNum(pref, SettingsKeys.historyApi) <= -1
        ) throw RuntimeException()
        var historyData: String?
        when (getPrefNum(pref, SettingsKeys.historyApi)) {
            0 -> {
                historyData = getPref(pref, history)
                if (historyData!!.isNotEmpty()) setPref(historyPref(context), history, historyData)
                setPref(pref, history, CommonUtils.EMPTY_STRING)
                historyData = getPref(historyPref(context), history)
                val listData = getPref(historyPref(context), history)!!
                    .trim { it <= ' ' }.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if (historyData!!.isNotEmpty()) for (listDatum in listData) historyBroha(context)!!
                    .insertAll(
                        Broha(listDatum)
                    )
                historyPref(context).edit().clear().apply()
            }

            1 -> {
                historyData = getPref(historyPref(context), history)
                val listData = getPref(historyPref(context), history)!!
                    .trim { it <= ' ' }.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if (historyData!!.isNotEmpty()) for (listDatum in listData) historyBroha(context)!!
                    .insertAll(
                        Broha(listDatum)
                    )
                historyPref(context).edit().clear().apply()
            }
        }
        setPrefNum(pref, SettingsKeys.historyApi, LATEST_API)
    }
}