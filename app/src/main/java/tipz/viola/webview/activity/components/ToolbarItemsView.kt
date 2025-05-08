// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.ImageViewCompat
import tipz.viola.R
import tipz.viola.ext.getSelectableItemBackground
import tipz.viola.ext.isDarkMode

class ToolbarItemsView(
    context: Context, attrs: AttributeSet?
) : LinearLayoutCompat(context, attrs) {

    private var realImageTint: ColorStateList? = null
    private lateinit var realOnClickListener: OnClickListener
    private lateinit var realOnLongClickListener: OnLongClickListener

    private lateinit var imageView: AppCompatImageView

    private var isItemEnabled = true

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        imageView = findViewById(R.id.imageView)
        realImageTint = ImageViewCompat.getImageTintList(imageView)
    }

    fun setItemEnabled(enabled: Boolean) {
        isItemEnabled = enabled
        if (enabled) {
            setBackgroundResource(context.getSelectableItemBackground())
            ImageViewCompat.setImageTintList(imageView, realImageTint)
            super.setOnClickListener(realOnClickListener)
            super.setOnLongClickListener(realOnLongClickListener)
        } else {
            setBackgroundResource(0)
            ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(
                if (context.isDarkMode()) Color.DKGRAY else Color.LTGRAY))
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