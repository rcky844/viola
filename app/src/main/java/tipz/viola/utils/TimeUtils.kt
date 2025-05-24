// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeUtils {
    /**
     * Converts given epoch to formatted String.
     * The epoch provided must be in milliseconds, defaults to current time.
     * Style of formatted string should follow that from <code>DateTimeFormatter</code>,
     * defaults to <code>E MMM dd HH:mm:ss z yyyy</code>. Timezone will default to
     * system value if not specified.
     *
     * @param  epochMillis epoch to convert in milliseconds
     * @param  formatStyle style of output string
     * @param  timezone    timezone for formating date
     * @return             string of formatted date time
     * @see                Instant
     * @see                ZoneId
     * @see                DateTimeFormatter
     */
    fun formatEpochMillis(epochMillis: Long = Instant.now().toEpochMilli(),
                          formatStyle: String = "E MMM dd HH:mm:ss z yyyy",
                          timezone: ZoneId = ZoneId.systemDefault()): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(timezone).format(DateTimeFormatter.ofPattern(formatStyle))
    }
}