// Copyright (c) 2023-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.download.MiniDownloadHelper
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import java.io.File
import java.io.FileOutputStream
import java.util.Scanner

open class AdServersClient(context: Context, private val pref: SettingsSharedPreference) {
    var adServers: String? = null

    private val LOG_TAG = "AdServersClient"

    private val localHostUrls = arrayOf("0.0.0.0", "127.0.0.1", "localhost")
    private val adServersFile = File(context.filesDir.path + "ad_servers_hosts.txt")

    private val adServersList = arrayOf(
        "https://raw.githubusercontent.com/AdAway/adaway.github.io/master/hosts.txt",
        "https://cdn.jsdelivr.net/gh/jerryn70/GoodbyeAds@master/Hosts/GoodbyeAds.txt",
        "http://sbc.io/hosts/hosts",
        "https://hostfiles.frogeye.fr/firstparty-trackers-hosts.txt",
        null
    )

    init {
        // Create the file
        if (!adServersFile.exists())
            adServersFile.createNewFile()

        // Import servers
        if (pref.getIntBool(SettingsKeys.enableAdBlock))
            importAdServers()
    }

    fun importAdServers() {
        Log.d(LOG_TAG, "Starting ad servers import")
        val reader = adServersFile.bufferedReader()
        var receiveString: String?
        val stringBuilder = java.lang.StringBuilder()
        while (reader.readLine().also { receiveString = it } != null) {
            stringBuilder.append("\n").append(receiveString)
        }
        adServers = stringBuilder.toString()
        Log.d(LOG_TAG, "Finished ad servers import")
    }

    fun downloadAdServers() =
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(LOG_TAG, "Starting ad servers download")
            val scanner = Scanner(String(
                MiniDownloadHelper.startDownload(
                    (adServersList[pref.getInt(SettingsKeys.adServerId)]
                    ?: pref.getString(SettingsKeys.adServerUrl)).toString())!!
            ))
            val builder = StringBuilder()
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                if (localHostUrls.any { line.startsWith(it) })
                    builder.append(line).append(System.lineSeparator())
            }
            adServers = builder.toString()

            val fos = FileOutputStream(adServersFile)
            fos.write(adServers!!.toByteArray())
            fos.close()
            Log.d(LOG_TAG, "Finished ad servers download")
        }
}
