// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity.components

import android.content.Context
import android.os.Build
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.net.toUri
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import tipz.viola.R
import tipz.viola.databinding.DialogHitTestTitleBinding
import tipz.viola.ext.copyClipboard
import tipz.viola.ext.dpToPx
import tipz.viola.ext.getOnSurfaceColor
import tipz.viola.ext.getSelectableItemBackground
import tipz.viola.ext.setMaterialDialogViewPadding
import tipz.viola.search.SuggestionAdapter
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.webview.VWebView
import tipz.viola.webview.VWebViewActivity
import tipz.viola.widget.PropertyDisplayView
import java.text.DateFormat

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

            setOnClickListener {
                val cert = webViewActivity.webview.certificate
                val binding: DialogHitTestTitleBinding =
                    DialogHitTestTitleBinding.inflate(LayoutInflater.from(context)).apply {
                        title.apply {
                            text = webViewActivity.webview.title
                            setOnLongClickListener {
                                context.copyClipboard(webViewActivity.webview.title)
                                true
                            }
                        }
                        url.text = webViewActivity.webview.url.toUri().host
                        this.icon.apply {
                            webViewActivity.webview.faviconExt.takeUnless { it == null }?.let {
                                setImageBitmap(it)
                            } ?: setImageResource(R.drawable.default_favicon)
                        }
                    }
                val titleView = binding.root

                // SSL information
                val messageView = if (cert != null) {
                    val issuedTo = cert.issuedTo
                    val issuedBy = cert.issuedBy

                    val scrollView = NestedScrollView(context)
                    scrollView.addView(PropertyDisplayView(context).apply {
                        property = arrayListOf(
                            arrayOf(R.string.ssl_info_dialog_issued_to),
                            arrayOf(R.string.ssl_info_dialog_common_name, issuedTo.cName),
                            arrayOf(R.string.ssl_info_dialog_organization, issuedTo.oName),
                            arrayOf(R.string.ssl_info_dialog_organization_unit, issuedTo.uName),
                            arrayOf(R.string.ssl_info_dialog_issued_by),
                            arrayOf(R.string.ssl_info_dialog_common_name, issuedBy.cName),
                            arrayOf(R.string.ssl_info_dialog_organization, issuedBy.oName),
                            arrayOf(R.string.ssl_info_dialog_organization_unit, issuedBy.uName),
                            arrayOf(R.string.ssl_info_dialog_validity_period),
                            arrayOf(R.string.ssl_info_dialog_issued_on,
                                DateFormat.getDateTimeInstance().format(cert.validNotBeforeDate)),
                            arrayOf(R.string.ssl_info_dialog_expires_on,
                                DateFormat.getDateTimeInstance().format(cert.validNotAfterDate)),
                        )
                    })

                    scrollView // Return
                } else if (webViewActivity.webview.sslState == VWebView.SslState.SEARCH) {
                    TextView(context).apply {
                        setText(R.string.address_bar_hint)
                    }
                } else {
                    TextView(context).apply {
                        setText(R.string.ssl_info_dialog_content_nocert)
                    }
                }
                messageView.setMaterialDialogViewPadding()

                PopupMaterialAlertDialogBuilder(context, Gravity.TOP)
                    .setCustomTitle(titleView)
                    .setView(messageView)
                    .create().show()
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

    fun updateSslStateIcon() {
        // Handle individual SSL states
        when (webViewActivity.webview.sslState) {
            VWebView.SslState.NONE, VWebView.SslState.ERROR ->
                sslLock.setImageResource(R.drawable.warning)
            VWebView.SslState.SECURE -> sslLock.setImageResource(R.drawable.lock)
            VWebView.SslState.SEARCH -> sslLock.setImageResource(R.drawable.search)
            else -> {
                Log.w(LOG_TAG, "setSslCertificateState(): " +
                        "Unsupported SslState ${webViewActivity.webview.sslState}")
            }
        }
    }

    interface OnAddressBarStateChangeListener {
        fun onStateChanged(newState: AddressBarState)
    }

    companion object {
        const val LOG_TAG = "AddressBarView"
    }
}