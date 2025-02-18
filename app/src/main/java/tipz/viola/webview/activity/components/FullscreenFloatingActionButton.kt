// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import tipz.viola.R
import tipz.viola.ext.dpToPx
import tipz.viola.ext.setImmersiveMode
import tipz.viola.settings.SettingsKeys
import tipz.viola.webview.activity.BrowserActivity

class FullscreenFloatingActionButton(context: Context, attrs: AttributeSet?) :
    FloatingActionButton(context, attrs) {
    lateinit var activity: BrowserActivity
    var hiddenViews: MutableList<View> = mutableListOf()
    private var faded = false
    var isFullscreen = false

    private fun resetAnim() {
        this.alpha = 1f
        faded = false
    }

    private fun fadeOut() {
        val animate = this.animate()
        animate.alpha(0f)
        animate.duration = resources.getInteger(R.integer.anim_fullscreen_fab_fade_out_speed).toLong()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            animate.withEndAction {
                faded = true
            }
        }
        animate.startDelay = resources.getInteger(R.integer.anim_fullscreen_fab_fade_out_delay).toLong()
        animate.start()
    }

    init {
        // Basic setup
        setImageResource(R.drawable.fullscreen_close)
        setOnClickListener {
            // Animations
            if (faded) {
                resetAnim()
                fadeOut()
            } else {
                isFullscreen = false

                // Handle views
                hiddenViews.forEach {
                    it.visibility = VISIBLE
                }
                this.visibility = GONE

                // Immersive Mode
                activity.setImmersiveMode(false)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Suppress("DEPRECATION")
    override fun show() {
        isFullscreen = true

        // Handle views
        hiddenViews.forEach {
            it.visibility = GONE
        }
        this.visibility = VISIBLE

        // Immersive Mode
        activity.setImmersiveMode(true)

        // Animations
        resetAnim()
        fadeOut()

        // Warning dialog
        if (activity.settingsPreference.getIntBool(SettingsKeys.showFullscreenWarningDialog)) {
            val checkBox = CheckBox(context).apply {
                setText(R.string.do_not_show_again)
                isChecked = true
            }

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_fullscreen_title)
                .setMessage(R.string.dialog_fullscreen_message)
                .setView(checkBox, context.dpToPx(20), context.dpToPx(16),
                    context.dpToPx(20), context.dpToPx(12))
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    if (checkBox.isChecked)
                        activity.settingsPreference.setIntBool(
                            SettingsKeys.showFullscreenWarningDialog, false)
                }
                .create().show()
        }
    }
}