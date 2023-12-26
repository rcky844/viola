package tipz.viola.webview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.utils.DownloadUtils
import java.util.Scanner

class AdServersHandler(settingsPreference: SettingsSharedPreference) {
    private var mSettingsPreference : SettingsSharedPreference
    var adServers: String? = null

    // TODO: Add support for dialog selection and merged lists
    private val adServersList = arrayOf("https://raw.githubusercontent.com/AdAway/adaway.github.io/master/hosts.txt",
        "https://www.github.developerdan.com/hosts/lists/ads-and-tracking-extended.txt",
        null)

    init {
        mSettingsPreference = settingsPreference
    }

    // TODO: Don't always download at launch
    fun downloadAdServers() {
        CoroutineScope(Dispatchers.IO).launch {
            val scanner = Scanner(String(DownloadUtils.startFileDownload(adServersList[mSettingsPreference.getInt(SettingsKeys.adServerId)])))
            val builder = StringBuilder()
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                if (line.startsWith("127.0.0.1 ")) builder.append(line)
                    .append(System.lineSeparator())
            }
            adServers = builder.toString()
        }
    }
}
