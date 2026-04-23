// Copyright (c) 2023-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.download.MiniDownloadHelper
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import java.io.File
import java.io.FileOutputStream
import java.net.MalformedURLException
import java.util.Scanner
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

open class AdServersClient(
    private val context: Context, private val pref: SettingsSharedPreference
) {
    var adServers: HashSet<String> = hashSetOf()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val localHostUrls = arrayOf("0.0.0.0", "127.0.0.1", "localhost")
    private val adServersFile = File(context.filesDir.path + "/ad_servers_hosts.txt.gz")

    init {
        // API checks & Migration
        val apiVer = pref.getInt(SettingsKeys.adClientApi)
        if (apiVer > LATEST_API || apiVer <= -1) throw RuntimeException()
        when (apiVer) {
            0 -> {
                val legacyPath = File(context.filesDir.path + "ad_servers_hosts.txt")
                if (legacyPath.exists()) {
                    GZIPOutputStream(FileOutputStream(adServersFile)).use { gzip ->
                        gzip.write(legacyPath.bufferedReader().use { it.readText() }.toByteArray())
                    }
                    legacyPath.delete()
                }
            }
        }
        pref.setInt(SettingsKeys.adClientApi, LATEST_API)

        // Create the file
        if (!adServersFile.exists())
            adServersFile.createNewFile()

        // First launch check
        if (pref.getString(SettingsKeys.adServerUrls).isEmpty())
            pref.setString(SettingsKeys.adServerUrls, defaultAdServers)

        // Import servers
        if (pref.getIntBool(SettingsKeys.enableAdBlock))
            importAdServers()
    }

    fun importAdServers() {
        ioScope.launch {
            Log.d(LOG_TAG, "Starting ad servers import")
            adServers.clear()

            try {
                val servers = GZIPInputStream(
                    adServersFile.inputStream()).bufferedReader().use { it.readText() }
                adServers.addAll(servers.split("\n"))
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Encountered exception when importing!")
                e.printStackTrace()
            }

            Log.d(LOG_TAG, "Processed ${adServers.size} entries")
            Log.d(LOG_TAG, "Finished ad servers import")
        }
    }

    fun downloadAdServers(callback: () -> Unit) {
        ioScope.launch {
            Log.d(LOG_TAG, "Starting ad servers download")
            adServers.clear()

            var hostsRawFiles = ""
            pref.getString(SettingsKeys.adServerUrls).split("\n").forEach {
                hostsRawFiles += String(MiniDownloadHelper.startDownloadWithDialog(
                    context, it).response)
                hostsRawFiles += "\n"
            }

            val scanner = Scanner(hostsRawFiles)
            while (scanner.hasNextLine()) {
                var line = scanner.nextLine()
                if (localHostUrls.any { line.startsWith(it) && !line.endsWith(it) }) {
                    localHostUrls.forEach { line = line.replace(it, "") }
                    adServers.add(line.trim())
                }
            }
            scanner.close()

            GZIPOutputStream(FileOutputStream(adServersFile)).use { gzip ->
                gzip.write(adServers.joinToString(separator = "\n").toByteArray())
            }

            Log.d(LOG_TAG, "Processed ${adServers.size} entries")
            Log.d(LOG_TAG, "Finished ad servers download")
        }
        importAdServers()
        callback()
    }

    fun isAd(url: String): Boolean {
        try {
            return isAdHost(url.toUri().host)
        } catch (e: MalformedURLException) {
            Log.d(LOG_TAG, e.toString())
            return false
        }
    }

    // From MonsterTechnoGits/WebViewAdblock-Library
    private fun isAdHost(host: String?): Boolean {
        if (host.isNullOrEmpty()) {
            return false
        }
        val index = host.indexOf(".")
        return index >= 0 && (adServers.contains(host) ||
                index + 1 < host.length && isAdHost(host.substring(index + 1)))
    }

    companion object {
        private const val LOG_TAG = "AdServersClient"

        /* Some retroactive API versions assigned:
         * - API 0 -> Original implementation (up to 8.0.x)
         * - API 1 -> Path move, merged hosts lists + compression
         */
        private const val LATEST_API = 1

        /* Default built-in ad servers list */
        val defaultAdServers = """
            https://raw.githubusercontent.com/AdAway/adaway.github.io/master/hosts.txt
            https://cdn.jsdelivr.net/gh/jerryn70/GoodbyeAds@master/Hosts/GoodbyeAds.txt
            http://sbc.io/hosts/hosts
            https://hostfiles.frogeye.fr/firstparty-trackers-hosts.txt
        """.trimIndent()
    }
}
