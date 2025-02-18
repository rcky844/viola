// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity.components

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import tipz.viola.R
import tipz.viola.ext.setImmersiveMode

class FullscreenFloatingActionButton(context: Context, attrs: AttributeSet?) :
    FloatingActionButton(context, attrs) {
    lateinit var activity: AppCompatActivity
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
        animate.duration = resources.getInteger(R.integer.anim_fullscreen_fab_fade_out_speed) * (
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    Settings.Global.getFloat(
                        context.contentResolver,
                        Settings.Global.ANIMATOR_DURATION_SCALE,
                        1.0f
                    )
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
                    @Suppress("DEPRECATION")
                    Settings.System.getFloat(
                        context.contentResolver,
                        Settings.System.ANIMATOR_DURATION_SCALE,
                        1.0f
                    )
                } else {
                    1.0f // Default for even older Android versions
                }).toLong()
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
    }
}