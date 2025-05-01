// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.ImageViewCompat
import tipz.viola.R

class ToolbarItemsView(
    context: Context, attrs: AttributeSet?
) : LinearLayoutCompat(context, attrs) {

    private lateinit var realOnClickListener: OnClickListener
    private lateinit var realOnLongClickListener: OnLongClickListener

    private var isItemEnabled = true

    fun setItemEnabled(enabled: Boolean) {
        val imageView : AppCompatImageView = findViewById(R.id.imageView)

        isItemEnabled = enabled
        if (enabled) {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                typedValue,
                true
            )
            setBackgroundResource(typedValue.resourceId)
            ImageViewCompat.setImageTintList(imageView, null)
            super.setOnClickListener(realOnClickListener)
            super.setOnLongClickListener(realOnLongClickListener)
        } else {
            setBackgroundResource(0)
            ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(Color.LTGRAY))
            super.setOnClickListener { }
            super.setOnLongClickListener { true }
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        if (isItemEnabled) super.setOnClickListener(l)
        realOnClickListener = l!!
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        if (isItemEnabled) super.setOnLongClickListener(l)
        realOnLongClickListener = l!!
    }
}