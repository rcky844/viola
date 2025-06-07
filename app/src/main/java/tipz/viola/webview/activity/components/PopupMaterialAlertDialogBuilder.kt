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
        dialog.window?.attributes.takeUnless { it == null }?.let {
            it.gravity = direction
            if (context.resources.configuration.smallestScreenWidthDp < 600)
                it.width = WindowManager.LayoutParams.MATCH_PARENT
            it.flags = it.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            dialog.window?.attributes = it
        }
        return dialog
    }
}