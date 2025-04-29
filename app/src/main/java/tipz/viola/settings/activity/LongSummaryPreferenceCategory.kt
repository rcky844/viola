// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.activity

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder

// Based on https://stackoverflow.com/a/60598784
class LongSummaryPreferenceCategory @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): PreferenceCategory(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val summary = holder.findViewById(android.R.id.summary) as? TextView
        summary?.let {
            // Enable multiple line support
            summary.isSingleLine = false
            summary.maxLines = Integer.MAX_VALUE
        }
    }
}
