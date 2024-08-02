// Copyright (c) 2023-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.download.MiniDownloadHelper
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Scanner

open class AdServersHandler(context: Context, settingsPreference: SettingsSharedPreference) {
    private var mContext: Context
    private var mSettingsPreference: SettingsSharedPreference
    var adServers: String? = null

    private val adServersFilePath = "ad_servers_hosts.txt"
    private val localHostUrls = arrayOf("0.0.0.0", "127.0.0.1", "localhost")

    init {
        mContext = context
        mSettingsPreference = settingsPreference
    }

    fun importAdServers() {
        try {
            val inputStream: InputStream = mContext.openFileInput(adServersFilePath)
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var receiveString: String?
            val stringBuilder = java.lang.StringBuilder()
            while (bufferedReader.readLine().also { receiveString = it } != null) {
                stringBuilder.append("\n").append(receiveString)
            }
            inputStream.close()
            adServers = stringBuilder.toString()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            downloadAdServers()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun downloadAdServers() {
        CoroutineScope(Dispatchers.IO).launch {
            val scanner = Scanner(String(
                MiniDownloadHelper.startDownload(
                    (adServersList[mSettingsPreference.getInt(SettingsKeys.adServerId)]
                    ?: mSettingsPreference.getString(SettingsKeys.adServerUrl)).toString())!!
            ))
            val builder = StringBuilder()
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                if (localHostUrls.any { line.startsWith(it) })
                    builder.append(line).append(System.lineSeparator())
            }
            adServers = builder.toString()

            try {
                val outputStreamWriter =
                    OutputStreamWriter(
                        mContext.openFileOutput(
                            adServersFilePath,
                            Context.MODE_PRIVATE
                        )
                    )
                outputStreamWriter.write(adServers)
                outputStreamWriter.close()
            } catch (_: IOException) {
            }
        }
    }

    companion object {
        fun getCustomIndex(): Int {
            return adServersList.size - 1
        }

        val adServersList = arrayOf(
            "https://raw.githubusercontent.com/AdAway/adaway.github.io/master/hosts.txt",
            "https://cdn.jsdelivr.net/gh/jerryn70/GoodbyeAds@master/Hosts/GoodbyeAds.txt",
            "http://sbc.io/hosts/hosts",
            "https://hostfiles.frogeye.fr/firstparty-trackers-hosts.txt",
            null
        )
    }
}
