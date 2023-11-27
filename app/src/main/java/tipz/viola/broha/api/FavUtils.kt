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
import android.graphics.Bitmap
import tipz.viola.broha.api.FavApi.favBroha
import tipz.viola.broha.database.Broha
import tipz.viola.broha.database.icons.IconHashClient

object FavUtils {
    /**
     * Favourites Saviour
     *
     *
     * Module to save favourites into a db.
     */
    suspend fun appendData(
        context: Context?,
        iconHashClient: IconHashClient?,
        title: String?,
        url: String?,
        icon: Bitmap?
    ) {
        if (url.isNullOrEmpty()) return
        if (iconHashClient != null && icon != null)favBroha(context!!)!!
            .insertAll(Broha(iconHashClient.save(icon), title, url)) else favBroha(context!!)!!
            .insertAll(Broha(title, url))
    }

    suspend fun clear(context: Context?) {
        favBroha(context!!)!!.deleteAll()
    }

    suspend fun deleteById(context: Context?, id: Int) {
        favBroha(context!!)!!.deleteById(id)
    }

    suspend fun isEmptyCheck(context: Context?): Boolean {
        return favBroha(context!!)!!.isEmpty().isEmpty()
    }
}