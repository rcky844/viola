// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity.components

import android.content.Context
import android.graphics.Typeface
import android.text.InputType
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import tipz.viola.R
import tipz.viola.ext.dpToPx
import tipz.viola.webview.activity.BrowserActivity


class LocalNtpPageView(
    context: Context, attrs: AttributeSet?
) : ConstraintLayout(context, attrs) {
    private lateinit var addressBar: AddressBarView
    private lateinit var realSearchBar: MaterialAutoCompleteTextView
    var involvedView: MutableList<BrowserActivity.ViewVisibility> = mutableListOf()
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
        }
        addView(appBanner)

        // Create fake search bar
        fakeSearchBar = AppCompatAutoCompleteTextView(context).apply {
            id = R.id.fake_search_bar
            layoutParams = LayoutParams(0, 0)
            setPadding(
                context.dpToPx(28), 0,
                context.dpToPx(28), 0
            )
            setHint(R.string.address_bar_hint)
            setBackgroundResource(R.drawable.round_corner_elevated)
            isSingleLine = true
            ellipsize = TextUtils.TruncateAt.END
            inputType = InputType.TYPE_NULL
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    updateVisibility(visible = false, overrideCallback = true)
                    realSearchBar.requestFocus()
                    imm.showSoftInput(realSearchBar, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
        addView(fakeSearchBar)

        // Wire up layouts
        set.clone(this)
        set.apply {
            // App banner
            appBanner.id.let {
                centerHorizontally(it, ConstraintSet.PARENT_ID)
                setVerticalChainStyle(it, ConstraintSet.CHAIN_PACKED)
                connect(it, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                connect(it, ConstraintSet.BOTTOM, fakeSearchBar.id, ConstraintSet.TOP)
            }

            // Fake search bar
            fakeSearchBar.id.let {
                connect(it, ConstraintSet.TOP, appBanner.id, ConstraintSet.BOTTOM,
                    context.dpToPx(36))
                connect(it, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                centerHorizontally(it, ConstraintSet.PARENT_ID)
                constrainHeight(it, context.dpToPx(52))
                constrainMaxWidth(it, context.dpToPx(480))
                setMargin(it, ConstraintSet.LEFT, context.dpToPx(28))
                setMargin(it, ConstraintSet.RIGHT, context.dpToPx(28))
            }
        }
        set.applyTo(this)

        // Allow page to show up again on clicked
        isFocusable = false
        setOnClickListener {
            if (realSearchBar.isFocused && realSearchBar.text.isEmpty()) {
                updateVisibility(visible = true, overrideCallback = true)
                imm.hideSoftInputFromWindow(realSearchBar.windowToken, 0)
                realSearchBar.clearFocus()
            }
        }
    }

    fun updateVisibility(visible: Boolean, overrideCallback: Boolean = false) {
        (if (visible) GONE else VISIBLE).let { vis ->
            if (involvedView.isEmpty()) addressBar.visibility = vis
            else involvedView.forEach {
                if (overrideCallback || it.isEnabledCallback()) it.view.visibility = vis
            }
        }
        fakeSearchBar.visibility = if (visible) VISIBLE else GONE
    }

    fun setRealSearchBar(addressBar: AddressBarView) {
        this.addressBar = addressBar
        this.realSearchBar = addressBar.textView
    }
}
