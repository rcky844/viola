// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity.components

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import tipz.viola.R
import tipz.viola.ext.dpToPx
import tipz.viola.ext.getOnSurfaceColor
import tipz.viola.ext.getSelectableItemBackground
import tipz.viola.webview.activity.BrowserActivity

class FindInPageView(
    context: Context, attrs: AttributeSet?
): ConstraintLayout(context, attrs) {
    lateinit var activity: BrowserActivity
    private val actionBarHeight = context.resources.getDimension(R.dimen.actionbar_widget_height).toInt()

    val closeButton = AppCompatImageView(context)
    val searchQueryEditText = AppCompatEditText(context)
    val searchResultCountTextView = AppCompatTextView(context)
    val searchClearButton = AppCompatImageView(context)
    val previousTermButton = AppCompatImageView(context)
    val nextTermButton = AppCompatImageView(context)

    var searchPositionInfo = Pair(0, 0)
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value

            val hasResults = value.second > 0
            previousTermButton.isEnabled = hasResults && value.first > 0
            nextTermButton.isEnabled = hasResults && value.first + 1 < value.second
            searchResultCountTextView.text =
                "${if (hasResults) value.first + 1 else 0}/${value.second}"

            val hasInput = searchQueryEditText.text.toString().isNotEmpty()
            searchResultCountTextView.isVisible = hasInput
            searchClearButton.isVisible = hasInput
        }

    var onStartSearchCallback: ((query: String) -> Unit)? = null
    var onSearchPositionChangeCallback: ((next: Boolean) -> Unit)? = null
    var onClearSearchCallback: (() -> Unit)? = null

    init {
        ContextCompat.getDrawable(context, R.drawable.toolbar_expandable_background).let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                background = it
            } else {
                @Suppress("DEPRECATION") setBackgroundDrawable(it)
            }
        }
        visibility = View.GONE

        // Set-up close button
        createButtons(closeButton)
        closeButton.run {
            updateLayoutParams<LayoutParams> {
                topToTop = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
                startToStart = ConstraintSet.PARENT_ID
                leftMargin = context.dpToPx(8)
            }
            setImageResource(R.drawable.cross)
            setOnClickListener {
                expand(true)
                clearSearch()
            }
        }
        addView(closeButton)

        // Set-up next term button
        createButtons(nextTermButton)
        nextTermButton.run {
            updateLayoutParams<LayoutParams> {
                topToTop = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
                rightMargin = context.dpToPx(8)
            }
            setImageResource(R.drawable.arrow_up)
            rotation = 180f
            setOnClickListener {
                onSearchPositionChangeCallback?.invoke(true)
            }
        }
        addView(nextTermButton)

        // Set-up previous term button
        createButtons(previousTermButton)
        previousTermButton.run {
            updateLayoutParams<LayoutParams> {
                topToTop = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
                endToStart = nextTermButton.id
            }
            setImageResource(R.drawable.arrow_up)
            setOnClickListener {
                onSearchPositionChangeCallback?.invoke(false)
            }
        }
        addView(previousTermButton)

        // Set-up search edit text
        searchQueryEditText.apply {
            layoutParams = LayoutParams(0, actionBarHeight).apply {
                setPadding(
                    context.dpToPx(16), 0,
                    context.dpToPx(16), 0
                )
            }
            setBackgroundDrawable(
                AppCompatResources.getDrawable(context, R.drawable.round_corner_elevated)
            )
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            isSingleLine = true
            isHorizontalFadingEdgeEnabled = true

            imeOptions = (EditorInfo.IME_ACTION_SEARCH
                    shl EditorInfo.IME_FLAG_NO_EXTRACT_UI
                    shl EditorInfo.IME_FLAG_NO_FULLSCREEN)
            setSelectAllOnFocus(true)

            setOnEditorActionListener { _, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_SEARCH -> {
                        text?.toString()?.takeUnless { it.isEmpty() }?.also {
                            onStartSearchCallback?.invoke(it)
                        } ?: run {
                            clearSearch()
                        }
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }.updateLayoutParams<LayoutParams> {
            topToTop = ConstraintSet.PARENT_ID
            bottomToBottom = ConstraintSet.PARENT_ID
            startToEnd = closeButton.id
            endToStart = previousTermButton.id
        }
        addView(searchQueryEditText)

        createButtons(searchClearButton)
        searchClearButton.apply {
            updateLayoutParams<LayoutParams> {
                topToTop = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
                endToStart = previousTermButton.id
                rightMargin = context.dpToPx(8)
            }
            setImageResource(R.drawable.close)
            setOnClickListener {
                clearSearch()
            }
            isVisible = false
        }
        addView(searchClearButton)

        searchResultCountTextView.apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                topToTop = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
                endToStart = searchClearButton.id
            }
            isVisible = false
        }
        addView(searchResultCountTextView)
    }

    /* UI related */
    private fun createButtons(view: AppCompatImageView) = view.apply {
        id = ViewCompat.generateViewId()
        layoutParams = LayoutParams(actionBarHeight, actionBarHeight).apply {
            setPadding(context.dpToPx(12))
        }
        setColorFilter(context.getOnSurfaceColor())
        setBackgroundResource(context.getSelectableItemBackground())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            translationZ = context.dpToPx(2).toFloat()
        } else {
            bringToFront()
        }
    }

    fun expand(viewVisible: Boolean = isVisible) {
        val transitionSet = TransitionSet()
            .addTransition(
                Slide(when (activity.viewMode) {
                    0 -> Gravity.TOP
                    1 -> Gravity.BOTTOM
                    else -> Gravity.BOTTOM
                })
                .addTarget(this)
                .setDuration(resources.getInteger(R.integer.anim_toolbar_expand_slide_speed).toLong())
            )
            .addTransition(
                Fade()
                .addTarget(this)
                .setDuration(resources.getInteger(R.integer.anim_toolbar_expand_fade_speed).toLong())
            )
        TransitionManager.beginDelayedTransition(this, transitionSet)
        visibility = if (viewVisible) View.GONE else View.VISIBLE
    }

    /* Search features */
    private fun clearSearch() {
        searchQueryEditText.setText("")
        searchPositionInfo = EMPTY_SEARCH_RESULT
        onClearSearchCallback?.invoke()
    }

    companion object {
        private val EMPTY_SEARCH_RESULT = Pair(0, 0)
    }
}
