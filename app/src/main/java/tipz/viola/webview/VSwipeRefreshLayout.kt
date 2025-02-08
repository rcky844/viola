// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import tipz.viola.Application
import tipz.viola.settings.SettingsKeys
import tipz.viola.webview.VJavaScriptInterface.Companion.INTERFACE_NAME

class VSwipeRefreshLayout(
    context: Context, attrs: AttributeSet?
) : SwipeRefreshLayout(context, attrs) {
    private val LOG_TAG = "VSwipeRefreshLayout"

    private lateinit var webview: VWebView
    private var layoutEnabled = true

    fun initialize(webview: VWebView) {
        this.webview = webview
        setOnRefreshListener { webview.reload() }
        doSettingsCheck()
    }

    private fun settingEnabled() = (context.applicationContext as Application)
        .settingsPreference.getIntBool(SettingsKeys.enableSwipeRefresh)

    private fun doSettingsCheck() {
        if (layoutEnabled)
            isEnabled = settingEnabled()
    }

    @Override
    override fun setRefreshing(refreshing: Boolean) {
        super.setRefreshing(refreshing)
        webview.evaluateJavascript("""
            function _viola_styleHelper(propVal, checkVal, expectVal) {
                propVal != "" && Boolean(propVal == checkVal & expectVal);
            }
            $INTERFACE_NAME.setOverscrollEnabled(
                _viola_styleHelper(document.body.overflowY, 'hidden', true) ||
                _viola_styleHelper(document.body.overscrollBehaviorY, 'auto', false)
            );
        """.trimIndent())
    }

    fun setOverscrollEnabled(overscroll: Boolean) {
        if (!overscroll) Log.d(LOG_TAG, "Webpage does not want to overscroll.")
        layoutEnabled = overscroll
        if (settingEnabled()) isEnabled = overscroll
    }
}