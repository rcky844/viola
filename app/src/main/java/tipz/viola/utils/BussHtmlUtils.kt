package tipz.viola.utils

import android.util.Log
import tipz.viola.download.MiniDownloadHelper
import java.util.Scanner

object BussHtmlUtils {
    const val LOG_TAG = "BussHtmlUtils"

    private fun getLinkRegex(formats: String): Regex =
        "<link.*href=\".*.(${formats})\".*>".toRegex()

    private fun appendProperty(line: String, associate: String?,
                               property: String, isBefore: Boolean): String {
        associate!! // TODO: Handle null variants
        val startIndex = line.indexOf("${associate}=\"")
        val firstBracketIndex = startIndex + associate.length + 2
        val lastBracketIndex = line.indexOf('"', firstBracketIndex + 1)

        val replaceIndex = if (isBefore) startIndex else lastBracketIndex
        val replacementString = if (isBefore) "${property.trim()} " else " ${property.trim()}"
        return line.replaceRange(replaceIndex, replaceIndex,replacementString)
    }

    private fun getProperty(line: String, property: String): String {
        val startIndex = line.indexOf("${property}=\"")
        val firstBracketIndex = startIndex + property.length + 2
        val lastBracketIndex = line.indexOf('"', firstBracketIndex + 1)

        return line.substring(firstBracketIndex, lastBracketIndex)
    }

    suspend fun parseHtml(realUrl: String, data: ByteArray?): String {
        if (data == null) return ""
        val inData = String(data).lineSequence()
            .map { it.trim() }
            .joinToString("\n")

        val scanner = Scanner(inData)
        val builder = StringBuilder()
        var inHtmlTag = false
        var currentHtmlTag = ""

        while (scanner.hasNextLine()) {
            var line = scanner.nextLine()
            if (line.isBlank()) continue

            val endBracketStartIndex = line.length - 2
            if (line.endsWith("/>")) endBracketStartIndex - 1

            // TODO: Figure out the format of this (b/1)
            val cleanedLine = line.replaceFirst("<", "")
                .replaceRange(endBracketStartIndex, line.length - 1, CommonUtils.EMPTY_STRING)

            // Detect start of tag, assume an element is always multi-lined
            if (line.startsWith('<')) {
                inHtmlTag = true

                currentHtmlTag = if (cleanedLine.contains(' '))
                    cleanedLine.substringBefore(' ')
                else cleanedLine.substringBefore('>') // TODO: b/1
                Log.d(LOG_TAG, "currentHtmlTag=${currentHtmlTag}")
            }

            // Process a tag
            if (currentHtmlTag == "link") {
                // Process CSS
                if (line.matches(getLinkRegex("css"))) {
                    var cssUrl = getProperty(line, "href")
                    if (!cssUrl.matches(UrlUtils.httpUrlRegex.toRegex()))
                        cssUrl = realUrl.substringBeforeLast('/') + "/" + cssUrl

                    val cssData = MiniDownloadHelper.startDownload(cssUrl)!!
                    builder.append("<style>${String(cssData)}</style>").append(System.lineSeparator())
                    continue
                }

                // For "rel"
                val rel: String? =
                    if (line.matches(getLinkRegex("css"))) "stylesheet" // TODO: Remove?
                    else if (line.matches(getLinkRegex("ico|gif|png|svg|jpg|jpeg"))) "icon"
                    else null
                if (rel != null) line = appendProperty(line, "href",
                        "rel=\"${rel}\"", true)

                builder.append(line).append(System.lineSeparator())
                continue
            }

            // FIXME: Re-enable for Lua scripts
            if (currentHtmlTag == "script") {
                continue
            }

            // Detect end of tag
            if (line.contains('>')) {
                inHtmlTag = false
            }
            builder.append(line).append(System.lineSeparator())
        }

        return builder.toString()
    }
}