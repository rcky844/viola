// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity.components

import android.content.Context
import android.view.WindowManager
import androidx.annotation.GravityInt
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/*
 * AlertDialog that is anchored at the specified direction
 * TODO: Fixup animation to match the direction
 */
class PopupMaterialAlertDialogBuilder(
    context: Context, @GravityInt private val direction: Int
) : MaterialAlertDialogBuilder(context) {
    override fun create(): AlertDialog {
        val dialog = super.create()
        dialog.setOnShowListener {
            val wlp = dialog.window?.attributes ?: return@setOnShowListener
            wlp.windowAnimations = 0
            wlp.gravity = direction
            if (context.resources.configuration.smallestScreenWidthDp < 600)
                wlp.width = WindowManager.LayoutParams.MATCH_PARENT
            wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            dialog.window?.attributes = wlp
        }
        return dialog
    }
}