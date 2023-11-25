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

import android.content.Context
import tipz.viola.utils.CommonUtils

object HistoryUtils {
    fun clear(context: Context?) {
        HistoryApi.historyBroha(context!!)?.deleteAll()
    }

    fun deleteById(context: Context?, id: Int) {
        HistoryApi.historyBroha(context!!)?.deleteById(id)
    }

    fun isEmptyCheck(context: Context?): Boolean {
        return HistoryApi.historyBroha(context!!)?.isEmpty!!.size === 0
    }

    fun lastUrl(context: Context?): String {
        val lastUrl = HistoryApi.historyBroha(context!!)?.lastUrl()
        return lastUrl?.url ?: CommonUtils.EMPTY_STRING
    }
}