/*
 * Copyright (c) 2022-2024 Tipz Team
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
import tipz.viola.broha.database.BrohaClient
import tipz.viola.settings.SettingsKeys

class FavClient(context: Context) : BrohaClient(context, "bookmarks") {
    init {
        val settingsPreference = (context.applicationContext as Application).settingsPreference
        val favApiVer = settingsPreference.getInt(SettingsKeys.favApi)
        if (favApiVer > LATEST_API || favApiVer <= -1) throw RuntimeException()
        settingsPreference.setInt(SettingsKeys.favApi, LATEST_API)
    }

    companion object {
        private const val LATEST_API = 0
    }
}