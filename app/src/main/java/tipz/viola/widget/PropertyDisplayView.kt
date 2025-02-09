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
        set(value) {
            field = value
            updateView()
        }

    private fun updateView() {
        property.forEachIndexed { i, it ->
            val isCategory = it.size <= 1
            val title = TextView(context).apply {
                setTypeface(null, Typeface.BOLD)

                var paddingTop = 8
                if (isCategory) {
                    if (i != 0) paddingTop = 16
                    isAllCaps = true
                }
                setPadding(0, context.dpToPx(paddingTop), 0, 0)
            }
            val value = TextView(context).apply {
                setPadding(0, 0, 0, context.dpToPx(8))
            }

            if (it[0] is CharSequence) title.text = it[0] as CharSequence
            else if (it[0] is Int) title.setText(it[0] as Int)
            else return@forEachIndexed

            var hasValue = true
            if (isCategory) {
                hasValue = false
            } else {
                if (it[1] is CharSequence) {
                    val text = it[1] as CharSequence
                    if (text.isBlank()) return@forEachIndexed // Value is CharSequence but empty, skip
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