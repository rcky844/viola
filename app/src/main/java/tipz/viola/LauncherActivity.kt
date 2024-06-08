/*
 * Copyright (c) 2020-2024 Tipz Team
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

import android.content.Intent
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Bundle
import android.webkit.CookieManager
import androidx.appcompat.app.AppCompatActivity
import cat.ereza.customactivityoncrash.config.CaocConfig
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.UrlUtils
import tipz.viola.webviewui.BrowserActivity
import tipz.viola.webviewui.CustomTabsActivity
import kotlin.system.exitProcess

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        if (!webViewEnabled()) {
            CommonUtils.showMessage(this, resources.getString(R.string.no_webview))
            exitProcess(0)
        }
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM)
            .enabled(true)
            .showErrorDetails(true)
            .showRestartButton(true)
            .logErrorOnRestart(true)
            .trackActivities(true)
            .minTimeBetweenCrashesMs(2000)
            .restartActivity(LauncherActivity::class.java)
            .errorActivity(null)
            .apply()

        // Getting information from intents, either from
        // sharing menu or default browser launch.
        val intent = intent
        val action = intent.action
        val type = intent.type
        val scheme = intent.scheme

        // Parse supplied url
        var uri: Uri? = null
        if (Intent.ACTION_SEND == action || NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) { /* NFC sharing */
            if (type != null) {
                if ("text/plain" == type) {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    uri = if (sharedText.isNullOrBlank()) Uri.EMPTY else Uri.parse(UrlUtils.patchUrlForCVEMitigation(sharedText))
                }
            }
        } else if (Intent.ACTION_VIEW == action) { /* From default browser */
            for (match in UrlUtils.TypeSchemeMatch) {
                if (match == type || match == scheme) {
                    uri = getIntent().data
                }
            }
        }

        val shortcutType = intent.getIntExtra(EXTRA_SHORTCUT_TYPE, -1)
        val launchIntent = Intent(
            this,
            if (shortcutType != -1)
                when (shortcutType) {
                    0 -> BrowserActivity::class.java
                    1,2 -> CustomTabsActivity::class.java
                    else -> LauncherActivity::class.java // FIXME: Better option?
                }
            // Decide whether to use Custom Tabs
            // ===
            // Conditions:
            // - Enabled in Settings
            // - Launcher category
            // - Url is not empty
            else if (intent.hasCategory("android.intent.category.LAUNCHER") || Uri.EMPTY.equals(uri)
                || (applicationContext as Application).settingsPreference!!.getInt(SettingsKeys.useCustomTabs) == 0)
                BrowserActivity::class.java else CustomTabsActivity::class.java
        )
        launchIntent.data = uri
        if (shortcutType == 2) launchIntent.putExtra(CustomTabsActivity.EXTRA_LAUNCH_AS_WEBAPP, true)
        startActivity(launchIntent)

        // Finally, kill this activity
        finish()
    }

    private fun webViewEnabled(): Boolean {
        return try {
            CookieManager.getInstance()
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        const val EXTRA_SHORTCUT_TYPE = "shortcutType"
    }
}