// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.pages

import tipz.viola.BuildConfig

object ExportedUrls {
    // Internal assets URLs
    const val actualStartUrl = "file:///android_asset/local-ntp/index.html"
    const val actualLicenseUrl = "file:///android_asset/LICENSE.txt"

    // Project URLs
    const val sourceUrl = "https://codeberg.org/TipzTeam/viola"
    const val websiteUrl = "https://rcky844.github.io/viola/"
    val changelogUrl = "${websiteUrl}builds/build" +
                (BuildConfig.VERSION_BUILD_ID ?: BuildConfig.VERSION_BUILD_GIT_REVISION) +
            (if (BuildConfig.VERSION_BUILD_REVISION == 0) "" else "r${BuildConfig.VERSION_BUILD_REVISION}")
    const val feedbackUrl = "$sourceUrl-/issues"
    const val updateJSONUrl = "${websiteUrl}updates.json"

    // Browser handling
    const val aboutBlankUrl = "about:blank"
    const val viewSourcePrefix = "view-source:"
    const val violaPrefix = "viola://"
    const val chromePrefix = "chrome://"
}