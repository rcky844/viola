// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity.components

import android.content.Context
import android.graphics.Typeface
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import tipz.viola.R
import tipz.viola.ext.dpToPx


class LocalNtpPageView(
    context: Context, attrs: AttributeSet?
) : ConstraintLayout(context, attrs) {
    private lateinit var addressBar: AddressBarView
    private lateinit var realSearchBar: MaterialAutoCompleteTextView
    private lateinit var involvedView: View
    var fakeSearchBar: AppCompatAutoCompleteTextView
    private val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    private var set = ConstraintSet()

    init {
        // Create app banner
        val appBanner = AppCompatTextView(context).apply {
            id = R.id.app_banner
            gravity = Gravity.CENTER
            setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.app_logo, 0, 0, 0)
            setText(R.string.app_name_display)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0,
                context.dpToPx(36) // This is hacky...
            )
        }
        addView(appBanner)

        // Create fake search bar
        fakeSearchBar = AppCompatAutoCompleteTextView(context).apply {
            id = R.id.fake_search_bar
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setPadding( // This is hacky...
                context.dpToPx(28), 0,
                context.dpToPx(28 + 72), 0
            )
            setHint(R.string.address_bar_hint)
            setBackgroundResource(R.drawable.round_corner_elevated)
            isSingleLine = true
            inputType = InputType.TYPE_NULL
        }
        addView(fakeSearchBar)
        fakeSearchBar.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                updateVisibility(false)
                realSearchBar.requestFocus()
                imm.showSoftInput(realSearchBar, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        // Set-up Flow
        val pageFlow = Flow(context).apply {
            id = R.id.page_flow
            referencedIds = intArrayOf(appBanner.id, fakeSearchBar.id)
            setOrientation(LinearLayout.VERTICAL)
        }
        addView(pageFlow)

        // Wire up layouts
        set.clone(this)
        set.centerHorizontally(pageFlow.id, 0)
        set.centerVertically(pageFlow.id, 0)
        set.constrainHeight(fakeSearchBar.id, context.dpToPx(52))
        set.applyTo(this)

        // Allow page to show up again on clicked
        setOnClickListener {
            if (realSearchBar.isFocused) {
                updateVisibility(true)
                imm.hideSoftInputFromWindow(realSearchBar.windowToken, 0)
                realSearchBar.clearFocus()
            }
        }
    }

    fun updateVisibility(visible: Boolean) {
        if (this::involvedView.isInitialized)
            involvedView.visibility = if (visible) GONE else VISIBLE
        else addressBar.visibility = if (visible) GONE else VISIBLE
        fakeSearchBar.visibility = if (visible) VISIBLE else GONE
    }

    fun setRealSearchBar(addressBar: AddressBarView) {
        this.addressBar = addressBar
        this.realSearchBar = addressBar.textView
    }

    fun setInvolvedView(involvedView: View) {
        this.involvedView = involvedView
    }
}
