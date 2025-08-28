// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity.components

import android.content.Context
import android.os.Build
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import tipz.viola.R
import tipz.viola.ext.dpToPx
import tipz.viola.ext.getOnSurfaceColor
import tipz.viola.ext.getSelectableItemBackground
import tipz.viola.search.SuggestionAdapter
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.webview.VWebViewActivity

class AddressBarView(
    context: Context, attrs: AttributeSet?
): ConstraintLayout(context, attrs) {
    private val settingsPreference = SettingsSharedPreference.instance
    private val webViewActivity = context as VWebViewActivity
    private val suggestionAdapter = SuggestionAdapter(webViewActivity)
    private val imm: InputMethodManager =
        context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

    val sslLock = AppCompatImageView(context)
    val textView = MaterialAutoCompleteTextView(context)

    enum class AddressBarState {
        FOCUSED, CLOSED
    }

    var state = AddressBarState.CLOSED
        set(value) {
            field = value
            onStateChangeListener?.onStateChanged(value)
        }
    private var onStateChangeListener: OnAddressBarStateChangeListener? = null

    init {
        val actionBarHeight = context.resources.getDimension(R.dimen.actionbar_widget_height).toInt()

        // Set-up text view
        textView.apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, actionBarHeight).apply {
                //setMargins(
                //    context.dpToPx(4), 0,
                //    context.dpToPx(4), 0
                //)
                setPadding(
                    context.dpToPx(52), 0,
                    context.dpToPx(16), 0
                )
            }
            setBackgroundDrawable(
                AppCompatResources.getDrawable(context, R.drawable.round_corner_elevated)
            )
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            threshold = 1
            isSingleLine = true
            dropDownWidth = LayoutParams.MATCH_PARENT
            isHorizontalFadingEdgeEnabled = true
            setHint(R.string.address_bar_hint)

            imeOptions = (EditorInfo.IME_ACTION_GO
                    shl EditorInfo.IME_FLAG_NO_EXTRACT_UI
                    shl EditorInfo.IME_FLAG_NO_FULLSCREEN)
            inputType = InputType.TYPE_TEXT_VARIATION_URI
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
            }
            setSelectAllOnFocus(true)
            setAdapter(suggestionAdapter)

            setOnTouchListener(
                SwipeController(if (settingsPreference.getIntBool(SettingsKeys.reverseAddressBar))
                    SwipeController.DIRECTION_SWIPE_UP else SwipeController.DIRECTION_SWIPE_DOWN) {
                    sslLock.performClick()
                })

            setOnFocusChangeListener { _, hasFocus ->
                setAddressBarState(if (hasFocus) AddressBarState.FOCUSED else AddressBarState.CLOSED)
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_GO, KeyEvent.ACTION_DOWN -> {
                        webViewActivity.webview.loadUrl(textView.text.toString())
                        setAddressBarState(AddressBarState.CLOSED)
                        return@setOnEditorActionListener true
                    }
                }
                false
            }

            setOnItemClickListener { _, v, _, _ ->
                webViewActivity.webview.loadUrl(
                    v.findViewById<AppCompatTextView>(android.R.id.text1).text.toString())
                setAddressBarState(AddressBarState.CLOSED)
            }
        }.updateLayoutParams<LayoutParams> {
            topToTop = ConstraintSet.PARENT_ID
            bottomToBottom = ConstraintSet.PARENT_ID
            startToStart = ConstraintSet.PARENT_ID
            endToEnd = ConstraintSet.PARENT_ID
            leftMargin = context.dpToPx(4)
            rightMargin = context.dpToPx(4)
        }
        addView(textView)

        // Set-up SSL lock
        sslLock.apply {
            layoutParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                LayoutParams(actionBarHeight, actionBarHeight).apply {
                    setPadding(context.dpToPx(12))
                }
            } else {
                val paddedActionBarHeight = actionBarHeight - context.dpToPx(12)
                LayoutParams(paddedActionBarHeight, paddedActionBarHeight)
            }

            setImageResource(R.drawable.search) // Defaults
            setColorFilter(context.getOnSurfaceColor())
            setBackgroundResource(context.getSelectableItemBackground())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                translationZ = context.dpToPx(2).toFloat()
            } else {
                bringToFront()
            }
        }.updateLayoutParams<LayoutParams> {
            topToTop = ConstraintSet.PARENT_ID
            bottomToBottom = ConstraintSet.PARENT_ID
            startToStart = ConstraintSet.PARENT_ID
            leftMargin =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) context.dpToPx(8)
                else context.dpToPx(8 + 8)
        }
        addView(sslLock)
    }

    fun setOnStateChangeListener(listener: OnAddressBarStateChangeListener) {
        onStateChangeListener = listener
    }

    // TODO: Investigate why Url bar remains focused in some cases
    fun setAddressBarState(newState: AddressBarState) {
        if (state == newState)
            return

        Log.d(LOG_TAG, "New address bar state: $newState")
        when (newState) {
            AddressBarState.FOCUSED -> {
                // Enable suggestions
                suggestionAdapter.enableFiltering = true
            }

            AddressBarState.CLOSED -> {
                // Reset address bar on closing
                webViewActivity.webview.url.takeIf { it != textView.text.toString() }
                    .let { textView.setText(it) }

                // Disable suggestions
                suggestionAdapter.enableFiltering = false

                // Close keyboard
                imm.hideSoftInputFromWindow(webViewActivity.currentFocus?.windowToken, 0)
                textView.clearFocus()
                webViewActivity.webview.requestFocus()
            }
        }
        state = newState
    }

    interface OnAddressBarStateChangeListener {
        fun onStateChanged(newState: AddressBarState)
    }

    companion object {
        const val LOG_TAG = "AddressBarView"
    }
}