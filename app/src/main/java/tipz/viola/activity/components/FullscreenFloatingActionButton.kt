// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.activity.components

import android.content.Context
import android.provider.Settings
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import tipz.viola.R
import tipz.viola.utils.CommonUtils

class FullscreenFloatingActionButton(context: Context, attrs: AttributeSet?) :
    FloatingActionButton(context, attrs) {
    lateinit var activity: AppCompatActivity
    var hiddenViews: MutableList<View> = mutableListOf()
    private var faded = false

    private fun resetAnim() {
        this.alpha = 1f
        faded = false
    }

    private fun fadeOut() {
        val animate = this.animate()
        animate.alpha(0f)
        animate.duration = (resources.getInteger(R.integer.anim_fullscreen_fab_fade_out_speed) * Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )).toLong()
        animate.startDelay = resources.getInteger(R.integer.anim_fullscreen_fab_fade_out_delay).toLong()
        animate.withEndAction {
            faded = true
        }
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
                // Handle views
                hiddenViews.forEach {
                    it.visibility = VISIBLE
                }
                this.visibility = GONE

                // Immersive Mode
                CommonUtils.setImmersiveMode(activity, false)
            }
        }
    }

    override fun show() {
        // Handle views
        hiddenViews.forEach {
            it.visibility = GONE
        }
        this.visibility = VISIBLE

        // Immersive Mode
        CommonUtils.setImmersiveMode(activity, true)

        // Animations
        resetAnim()
        fadeOut()
    }
}