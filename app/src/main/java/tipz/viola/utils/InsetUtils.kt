// Copyright (c) 2024 The LineageOS Project
// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object InsetUtils {
    fun applyWindowInsetsListener(rootView: View?) {
        ViewCompat.setOnApplyWindowInsetsListener(
            rootView!!
        ) { view: View, windowInsets: WindowInsetsCompat ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}