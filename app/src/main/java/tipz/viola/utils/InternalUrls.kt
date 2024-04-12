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

    const val sourceUrl = "https://gitlab.com/TipzTeam/viola/"
    const val realChangelogUrl = sourceUrl + "-/releases/" + BuildConfig.VERSION_NAME
    const val feedbackUrl = "$sourceUrl-/issues"
    const val realLicenseUrl = "file:///android_asset/LICENSE.txt"
    const val prefix = "viola://"
    const val licenseUrl = prefix + "license"
    const val startUrl = prefix + "start"
    const val aboutBlankUrl = "about:blank"
}