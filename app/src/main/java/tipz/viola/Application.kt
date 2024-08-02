// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.google.android.material.color.DynamicColors
import tipz.viola.download.DownloadClient
import tipz.viola.settings.SettingsSharedPreference

class Application : Application() {
    lateinit var settingsPreference: SettingsSharedPreference
    lateinit var downloadClient: DownloadClient

    override fun onCreate() {
        super.onCreate()
        settingsPreference = SettingsSharedPreference(this)
        downloadClient = DownloadClient(this)

        // Observe dynamic colors changes
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }
}
