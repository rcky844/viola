// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.buss

import android.util.Log
import tipz.viola.download.MiniDownloadHelper
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.UrlUtils
import java.util.Scanner
import java.util.regex.Matcher
import java.util.regex.Pattern

object BussHtmlUtils {
    const val LOG_TAG = "BussHtmlUtils"

    // These are some defaults found in "napture/src/b9/css.rs" of the same variable name
    const val DEFAULT_CSS = "body { gap: 10; background-color: transparent; direction: column; align-items: fill; } h1 { font-size: 24pt; } h2 { font-size: 22pt; } h3 { font-size: 20pt; } h4 { font-size: 18pt; } h5 { font-size: 16pt; } h6 { font-size: 14pt; } a { border: none; color: #67B7D1; text-decoration: underline; } input { padding: 5px; border-color: #616161; border-width: 1px; border-style: solid; border-radius: 12px; } textarea { padding: 5px; border-color: #616161; border-width: 1px; border-style: solid; border-radius: 12px; width: 400px; height: 100px; }"

    private fun getLinkRegex(formats: String): Regex =
        "<link.*href=\".*.(${formats})\".*>".toRegex()

    private fun appendProperty(line: String, associate: String?,
                               property: String, isBefore: Boolean): String =
        line.replace("(.*?)($associate=\".+?\")(.*?)".toRegex(),
            if (isBefore) "\$1 $property \$2\$3" else "\$1\$2 $property \$3")

    private fun getProperty(line: String, property: String): String {
        val pattern: Pattern = Pattern.compile(
            "(?-s).*?(?s)$property=\"(.+?)\"(?-s).*?(?s)", Pattern.DOTALL)
        val matcher: Matcher = pattern.matcher(line)
        if (matcher.matches()) return matcher.group(1) ?: ""
        return "" // Default
    }

    suspend fun parseHtml(realUrl: String, data: ByteArray?): String {
        if (data == null) return ""
        val inData = String(data).lineSequence()
            .map { it.trim() }
            .joinToString("\n")

        val scanner = Scanner(inData)
        val builder = StringBuilder()
        var inHtmlTag = false

        // For <head>
        var defaultCssAdded = false

        // For any
        var inScriptTag = false

        // Updatable
        var currentHtmlTag = ""

        while (scanner.hasNextLine()) {
            var line = scanner.nextLine()
            if (line.isBlank()) continue

            val endBracketStartIndex = line.length - 2
            if (line.endsWith("/>")) endBracketStartIndex - 1

            // TODO: Figure out the format of this (b/1)
            var cleanedLine = line
            if (cleanedLine.contains('<'))
                cleanedLine = line.replaceFirst("<", "")
            if (cleanedLine.contains('>'))
                cleanedLine = cleanedLine.replaceRange(endBracketStartIndex,
                    line.length - 1, CommonUtils.EMPTY_STRING
                )
            Log.d(LOG_TAG, "cleanedLine=${cleanedLine}")

            // Detect start of tag, assume an element is always multi-lined
            if (line.startsWith('<')) {
                inHtmlTag = true

                currentHtmlTag = if (cleanedLine.contains(' '))
                    cleanedLine.substringBefore(' ')
                else cleanedLine.substringBefore('>') // TODO: b/1
                Log.d(LOG_TAG, "currentHtmlTag=${currentHtmlTag}")
            }

            // Process some outer tags
            if (currentHtmlTag == "head") {
                builder.append(line).append(System.lineSeparator())
                if (!defaultCssAdded) {
                    builder.append("<style>$DEFAULT_CSS</style>").append(System.lineSeparator())
                    defaultCssAdded = true
                }
                continue
            } else if (currentHtmlTag == "/head") {
                builder.append(line).append(System.lineSeparator())
                continue
            }

            if (currentHtmlTag == "link") {
                // Process CSS
                if (line.matches(getLinkRegex("css"))) {
                    var cssUrl = getProperty(line, "href")
                    if (cssUrl.isNotBlank()) {
                        if (!cssUrl.matches(UrlUtils.httpUrlRegex.toRegex()))
                            cssUrl = realUrl.substringBeforeLast('/') + "/" + cssUrl

                        val cssData = MiniDownloadHelper.startDownload(cssUrl)!!
                        builder.append("<style>${String(cssData)}</style>").append(System.lineSeparator())
                        continue
                    }
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

            if (currentHtmlTag == "script") {
                inScriptTag = true
                var scriptUrl = getProperty(line, "src")
                if (scriptUrl.isNotBlank()) {
                    if (!scriptUrl.matches(UrlUtils.httpUrlRegex.toRegex()))
                        scriptUrl = realUrl.substringBeforeLast('/') + "/" + scriptUrl

                    val scriptData = MiniDownloadHelper.startDownload(scriptUrl)!!
                    val finalData = if (scriptUrl.endsWith(".lua"))
                        BussLuaUtils.parseLua(scriptData)
                    else String(scriptData)

                    builder.append("<script>${finalData}</script>").append(System.lineSeparator())
                    continue
                }
            } else if (currentHtmlTag == "/script") { // Embedded scripts end tag
                inScriptTag = false
            } else if (inScriptTag && cleanedLine == line) { // Embedded scripts content
                continue // TODO: parse embedded scripts
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