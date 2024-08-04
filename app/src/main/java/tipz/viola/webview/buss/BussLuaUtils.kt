// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.buss

import android.util.Log
import java.util.Scanner
import java.util.regex.Matcher
import java.util.regex.Pattern


object BussLuaUtils {
    const val LOG_TAG = "BussLuaUtils"

    private var inCustomFunc = false
    private var inFetch = false
    private var fetchData: FetchData? = null
    private var forceContinue = false

    // Regex
    val supportedHtmlTags =
        "\"(div|h1|h2|h3|h4|h5|h6|p|a|ul|ol|hr|img|input|select|textarea|button)\"".toRegex()

    private fun isKeyword(line: String, keyword: String) = line.matches("^$keyword( .*)?".toRegex())
    private fun substituteKeyword(line: String, keyword: String, sub: String): String =
        line.replace("^$keyword( .*)?".toRegex(), "$sub\$1")

    // TODO: Merge
    private fun operatorRegex(operator: String) = "(.*? )$operator( .*?)".toRegex()
    private fun operatorMiscRegex(operator: String) = "(.*?)$operator(.*?)".toRegex()

    /* Modes:
     * 0 - Substitute name of function
     * 1 - Substitute name of function for assessing properties
     * 2 - Substitute name of function for writing properties
     * 3 - Substitute name of function for events
     * 4 - Substitute arguments with name of function
     */
    private fun substituteFunc(line: String, func: String,
                               new: String, mode: Int): String =
        when (mode) {
            0 -> line.replace("( .*)?$func(\\(.*?\\).*?)".toRegex(), "\$1$new\$2")
            1 -> line.replace("( .*)?$func\\(.*?\\)(.*?)".toRegex(), "\$1$new\$2")
            2 -> line.replace("( .*)?$func\\((.*)\\)".toRegex(), "\$1$new = \$2")
            3 -> line.replace("( .*)?$func\\((.*?)".toRegex(), "\$1$new = \$2")
            4 -> line.replace("( .*?$func\\().*?(\\).*?)".toRegex(), "\$1$new\$2")
            else -> line
        }

    private fun appendToFunc(line: String, func: String, str: String): String =
        line.replace("( .*)?($func\\(.*?\\))(.*?)".toRegex(), "\$1\$2$str\$3")

    private fun getFuncNames(line: String): List<String> {
        val matcher: Matcher = Pattern.compile("[^. ][A-Za-z0-9_]+\\(").matcher(line)
        val matches: MutableList<String> = mutableListOf()
        while (matcher.find()) {
            // TODO: Simplify this
            matches.add(matcher.group().substringBeforeLast('('))
            Log.i(LOG_TAG, matcher.group().substringBeforeLast('('))
        }
        return matches
    }
    private fun getArgsOfFunc(line: String, func: String): List<String> {
        val pattern: Pattern = Pattern.compile(".*?$func\\((.*)\\).*?", Pattern.DOTALL)
        val matcher: Matcher = pattern.matcher(line)
        if (matcher.matches()) {
            val matched = matcher.group(1) ?: return listOf()
            return matched.split(',').map { it.trim() }
        }
        return listOf() // Default
    }
    private fun getDataFromVar(line: String, varName: String): String {
        val testRegex = listOf(".*$varName = ?\"(.*?)\".*?", ".*$varName = ?(\\{.*?\\}).*?",
            ".*$varName = ?\\(.*?\\),?.*?")
        testRegex.forEach {
            val pattern: Pattern = Pattern.compile(it, Pattern.DOTALL)
            val matcher: Matcher = pattern.matcher(line)
            if (matcher.matches()) {
                return matcher.group(1) ?: return ""
            }
        }
        return "" // Default
    }

    object FetchData {
        var name = ""
        var url = ""
        var method = ""
        var headers = ""
        var body = ""

        var fullScript = ""
        var inHeaders = false
    }

    suspend fun parseLua(data: ByteArray?): String {
        if (data == null) return ""
        val inData = String(data).lineSequence()
            .map { it.trim() }
            .joinToString("\n")

        val scanner = Scanner(inData + "\n")
        val builder = StringBuilder()

        while (scanner.hasNextLine()) {
            var line = scanner.nextLine()
            if (line.isBlank()) continue

            // Comments
            if (isKeyword(line, "--")) {
                line = substituteKeyword(line, "--", "//")
                builder.append(line).append(System.lineSeparator())
                continue
            }

            // Variables
            // TODO: const support
            if (isKeyword(line, "local")) {
                line = substituteKeyword(line, "local", "var")
                if (line.contains("var function +.*\\(".toRegex())) {
                    line = line.replace("(var )function +(.*)(\\(.*?\\)).*?".toRegex(),
                        "\$1\$2 = function\$3")
                }
                Log.d(LOG_TAG, "End substituteKeyword(), line=$line")
            }

            // Blocks
            // Adds brackets to blocks by default
            if (isKeyword(line, "if") || isKeyword(line, "elseif")) {
                line = substituteKeyword(line, "if", "if (")
                line = substituteKeyword(line, "elseif", "} else if (")
            } else if (isKeyword(line, "else")) {
                line = substituteKeyword(line, "else", "} else {")
            } else if (isKeyword(line, "end")) {
                line = substituteKeyword(line, "end", "}")
            } else if (isKeyword(line, "for")) {
                line = substituteKeyword(line, "for", "for (let [")
                if (line.contains(" in ")) {
                    line = line.replace("(for.* )in( .*)".toRegex(), "\$1] in\$2")
                }
            }
            if (line.endsWith("end)")) { // FIXME
                line = "})"
            }

            if (line.endsWith("then")) {
                line = line.replace("(.*)?then".toRegex(), "\$1) {")
            } else if (line.endsWith("do")) {
                line = line.replace("(.*)?do".toRegex(), "\$1) {")
            }

            // Operators
            if (line.matches(operatorRegex("and"))) {
                line = line.replace(operatorRegex("and"), "\$1&&\$2")
            } else if (line.matches(operatorRegex("or"))) {
                line = line.replace(operatorRegex("or"), "\$1||\$2")
            } else if (line.matches(operatorRegex("not"))) {
                line = line.replace(operatorRegex("not"), "\$1!\$2")
            } else if (line.matches(operatorMiscRegex("\\.\\."))) {
                line = line.replace(operatorMiscRegex("\\.\\."), "\$1+\$2")
            } else if (line.matches(operatorMiscRegex("#"))) {
                line = line.replace(operatorMiscRegex("#"), "\$2.length") // FIXME
            }

            // Functions
            if (line.startsWith("function") || line.contains("function\\(.*?\\)".toRegex())) {
                inCustomFunc = true
                line += " {"
            }
            if (inCustomFunc && (line.endsWith("end"))) {
                inCustomFunc = false
                line = "}"
            }

            getFuncNames(line).forEachIndexed { index, element ->
                Log.d(LOG_TAG, "For getFuncNames(), index=$index, element=$element")
                when (element) {
                    "get" -> { // get(x, y)
                        val args = getArgsOfFunc(line, element).toMutableList()
                        Log.d(LOG_TAG, "$args")

                        val replacementFunc =
                            if (args[0].contains(supportedHtmlTags))
                                "document.querySelector"
                            else "document.getElementsByClassName"
                        line = substituteFunc(line, element, args[0], 4)
                        line = substituteFunc(line, element, replacementFunc, 0)

                        if (args.size != 2) args.add("true") // Default
                        if (args[1] == "true")
                            line = appendToFunc(line, replacementFunc, "[0]")
                    }

                    "get_content" -> // get_content()
                        line = substituteFunc(line, element, "textContent", 1)

                    "get_href" -> // get_href()
                        line = substituteFunc(line, element, "href", 1)

                    "get_source" -> // get_source()
                        line = substituteFunc(line, element, "src", 1)

                    "get_opacity" -> // get_opacity()
                        line = substituteFunc(line, element, "style.opacity", 1)

                    "set_content" -> // set_content(x)
                        line = substituteFunc(line, element, "textContent", 2)

                    "set_href" -> // set_href(x)
                        line = substituteFunc(line, element, "href", 2)

                    "set_source" -> // set_source(x)
                        line = substituteFunc(line, element, "src", 2)

                    "set_opacity" -> // set_opacity(x)
                        line = substituteFunc(line, element, "style.opacity", 2)

                    "on_click" -> { // on_click(function)
                        if (inCustomFunc) {
                            line = substituteFunc(line, element, "onclick", 3)
                        }
                    }

                    "on_input" -> { // on_input(function)
                        if (inCustomFunc) {
                            line = substituteFunc(line, element, "oninput", 3)
                        }
                    }

                    "on_submit" -> { // on_submit(function)
                        if (inCustomFunc) {
                            line = substituteFunc(line, element, "onsubmit", 3)
                        }
                    }

                    "print" -> // print(x)
                        line = substituteFunc(line, element, "console.log", 0)

                    "fetch" -> { // fetch(x), x is array type
                        // FIXME: Implement
                        if (line.contains("fetch ?\\(\\{".toRegex())) {
                            inFetch = true
                            forceContinue = true
                            fetchData = FetchData
                        }
                        if (isKeyword(line, "var")) {
                            val matcher: Matcher = Pattern.compile("var ([A-Za-z0-9_]+) ?=")
                                .matcher(line)
                            if (matcher.find()) {
                                fetchData!!.name = matcher.group(1)!!
                                fetchData!!.fullScript = "const ${fetchData!!.name} = "
                            }
                            Log.i(LOG_TAG, matcher.group())
                        }
                    }

                    // Lua specific
                    "pairs" ->
                        line = substituteFunc(line, element, "Object.entries", 0)

                    "string.format" -> {

                    }
                }
                Log.d(LOG_TAG, "End getFuncNames() processing, " +
                        "index=$index, element=$element, line=$line")
            }

            // For element
            if (inFetch) {
                if (line.contains("\\} ?\\)$".toRegex())) {
                    forceContinue = false
                    inFetch = false
                    fetchData!!.fullScript += "});"
                    builder.append(fetchData!!.fullScript).append(System.lineSeparator())
                    continue
                } else {
                    if (line.startsWith("url")) {
                        fetchData!!.url = getDataFromVar(line, "url")
                        fetchData!!.fullScript += "fetch(\"${fetchData!!.url}\", {\n"
                    }
                    if (line.startsWith("method")) {
                        fetchData!!.method = getDataFromVar(line, "method")
                        fetchData!!.fullScript += "method: \"${fetchData!!.method}\",\n"
                        Log.d(LOG_TAG, fetchData!!.method)
                    }
                    if (line.startsWith("headers")) {
                        fetchData!!.headers = getDataFromVar(line, "headers")
                        if (fetchData!!.headers.endsWith("{")
                            && !fetchData!!.headers.endsWith("}")) {
                            fetchData!!.inHeaders = true
                        }
                        fetchData!!.headers = fetchData!!.headers
                            .replace("[", "").replace("]", "")
                            .replace(" =", ":")
                        fetchData!!.fullScript += "headers: ${fetchData!!.headers},\n"
                    }
                    if (line.startsWith("body")) { // TODO: Test
                        fetchData!!.body = getDataFromVar(line, "body")
                        fetchData!!.fullScript += "body: JSON.stringify(${fetchData!!.headers}),\n"
                        Log.d(LOG_TAG, fetchData!!.body)
                    }
                    Log.d(LOG_TAG, "inFetch=$inFetch")
                }
            }

            if (forceContinue) continue

            if (line.matches("^.*?[^. ][A-Za-z0-9_]+\\(.*$".toRegex())
                && !line.endsWith("end)") && !line.endsWith(";")
                && !line.endsWith("(") && !line.endsWith(" )")
                && !line.endsWith(',') && !line.endsWith("{")) line += ";"
            builder.append(line).append(System.lineSeparator())
        }

        return builder.toString()
    }
}