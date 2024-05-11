/*
 * Copyright (C) 2022-2023 Tipz Team
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
package tipz.viola.utils

import tipz.viola.BuildConfig

object InternalUrls {
    // "viola://" URI scheme
    const val violaPrefix = "viola://"
    const val violaLicenseSuffix = violaPrefix + "license"
    const val violaStartSuffix = violaPrefix + "start"

    // Project URLs
    const val sourceUrl = "https://gitlab.com/TipzTeam/viola/"
    const val changelogUrl = sourceUrl + "-/releases/" + BuildConfig.VERSION_NAME
    const val feedbackUrl = "$sourceUrl-/issues"

    // Internal assets
    const val licenseUrl = "file:///android_asset/LICENSE.txt"

    // Browser handling
    const val aboutBlankUrl = "about:blank"
    const val viewSourcePrefix = "view-source:"
}