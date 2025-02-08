// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import tipz.viola.ext.dpToPx

class PropertyDisplayView : LinearLayoutCompat {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    var property: ArrayList<Array<Any>> = arrayListOf(arrayOf(Any(), Any()))
        get() = field
        set(value) {
            field = value
            updateView()
        }

    private fun updateView() {
        property.forEach {
            val isCategory = it.size <= 1
            val title = TextView(context).apply {
                setPadding(0, context.dpToPx(8), 0, 0)
                setTypeface(null, Typeface.BOLD)

                if (isCategory) {
                    isAllCaps = true
                }
            }
            val value = TextView(context).apply {
                setPadding(0, 0, 0, context.dpToPx(8))
            }

            if (it[0] is CharSequence) title.text = it[0] as CharSequence
            else if (it[0] is Int) title.setText(it[0] as Int)
            else return@forEach

            var hasValue = true
            if (isCategory) {
                hasValue = false
            } else {
                if (it[1] is CharSequence) {
                    val text = it[1] as CharSequence
                    if (text.isBlank()) return@forEach // Value is CharSequence but empty, skip
                    else value.text = it[1] as CharSequence
                }
                else if (it[1] is Int) value.setText(it[1] as Int)
                else hasValue = false
            }

            addView(title)
            if (hasValue) addView(value)
        }
    }

    init {
        orientation = VERTICAL
    }
}