// Copyright (c) 2024-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.view.View
import tipz.viola.R

class FadeOrchestrator(private val context: Context) {
    private var faded = false
    private var firstFade = true
    private var onClickListener: View.OnClickListener? = null

    private val fadeViewList: ArrayList<View> = arrayListOf()

    var dynamicDisable = false
        set(value) {
            field = value
            resetAnim()
        }
    var requireInitialClickToFade = false

    fun resetAnim() {
        fadeViewList.forEach {
            it.alpha = 1f
            it.animate().cancel()
        }
        faded = false
        fadeOut()
    }

    private fun fadeOut() {
        if ((requireInitialClickToFade && firstFade) || dynamicDisable) return
        fadeViewList.forEach {
            val animate = it.animate()
            animate.alpha(0.25f)
            animate.duration = context.resources.getInteger(
                R.integer.anim_fullscreen_fab_fade_out_speed).toLong()
            animate.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                    faded = true
                }
            })
            animate.startDelay = context.resources.getInteger(
                R.integer.anim_fullscreen_fab_fade_out_delay).toLong()
            animate.start()
        }
    }

    fun register(view: View) {
        fadeViewList.add(view)

        // Set on click actions
        view.setOnClickListener {
            // Animations
            firstFade = false
            if (faded) resetAnim()
            else onClickListener?.onClick(view)
        }
        view.setOnLongClickListener {
            resetAnim()
            firstFade = false
            true
        }

        // Begin timer
        resetAnim()
    }

    fun setOnVisibleClickListener(onClickListener: View.OnClickListener) {
        this.onClickListener = onClickListener
    }
}