// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.search

import tipz.viola.utils.CommonUtils

class EngineObject {
    var name: String = CommonUtils.EMPTY_STRING // We assume each engine have unique identifiers
    var homePage: String? = null
    var search: String? = null
    var suggestion: String? = null
}