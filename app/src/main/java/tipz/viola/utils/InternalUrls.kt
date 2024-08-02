// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.utils

import tipz.viola.BuildConfig

object InternalUrls {
    // "viola://" URI scheme
    const val violaPrefix = "viola://"
    const val violaLicenseUrl = violaPrefix + "license"
    const val violaStartUrl = violaPrefix + "start"

    // Project URLs
    const val sourceUrl = "https://gitlab.com/TipzTeam/viola/"
    const val websiteUrl = "https://rcky844.github.io/viola/"
    val changelogUrl = "${websiteUrl}changelogs/build" +
                (BuildConfig.VERSION_BUILD_ID ?: BuildConfig.VERSION_BUILD_GIT_REVISION) + ".html"
    const val feedbackUrl = "$sourceUrl-/issues"
    const val updateJSONUrl = "${websiteUrl}updates.json"

    // Internal assets
    const val licenseUrl = "file:///android_asset/LICENSE.txt"
    const val localNtpUrl = "file:///android_asset/local-ntp/index.html"

    // Browser handling
    const val aboutBlankUrl = "about:blank"
    const val viewSourcePrefix = "view-source:"
    const val chromePrefix = "chrome://"
}