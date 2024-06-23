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
package tipz.viola

import android.app.Application
import com.google.android.material.color.DynamicColors
import tipz.viola.broha.database.IconHashUtils
import tipz.viola.settings.SettingsSharedPreference

class Application : Application() {
    lateinit var settingsPreference: SettingsSharedPreference
    lateinit var iconHashClient: IconHashUtils
    override fun onCreate() {
        super.onCreate()
        settingsPreference = SettingsSharedPreference(this)
        iconHashClient = IconHashUtils(this)

        // Observe dynamic colors changes
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}